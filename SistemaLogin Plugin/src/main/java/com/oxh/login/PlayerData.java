package com.oxh.login;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class PlayerData {
    private final Main plugin;
    private final File file;
    private final YamlConfiguration cfg;
    private SQLiteStorage sqlite;
    private final boolean useSQLite;

    public PlayerData(Main plugin){
        this.plugin = plugin;
        this.useSQLite = plugin.cfg().getBoolean("storage.use-sqlite", true);
        if (useSQLite){
            sqlite = new SQLiteStorage(plugin);
            file = null; cfg = null;
        } else {
            file = new File(plugin.getDataFolder(), "players.yml");
            if (!file.exists()){
                try { file.getParentFile().mkdirs(); file.createNewFile(); } catch (IOException e){ e.printStackTrace(); }
            }
            cfg = YamlConfiguration.loadConfiguration(file);
        }
    }

    public boolean isRegistered(UUID uuid){
        if (useSQLite) return sqlite.userExists(uuid);
        return cfg.contains("players."+uuid.toString()+".pass");
    }

    public void registerHashed(UUID uuid, String hashedPass){
        if (useSQLite) sqlite.saveUser(uuid, hashedPass, false);
        else { cfg.set("players."+uuid.toString()+".pass", hashedPass); save(); }
    }

    public boolean checkPasswordHashed(UUID uuid, String plainPassword){
        if (!isRegistered(uuid)) return false;
        String stored;
        if (useSQLite) stored = sqlite.getHash(uuid);
        else stored = cfg.getString("players."+uuid.toString()+".pass");
        if (stored==null) return false;
        try { return org.mindrot.jbcrypt.BCrypt.checkpw(plainPassword, stored); } catch (Exception ex) { return false; }
    }

    public void setPremium(UUID uuid, boolean v){
        if (useSQLite) sqlite.saveUser(uuid, getHashOrEmpty(uuid), v);
        else { cfg.set("players."+uuid.toString()+".premium", v); save(); }
    }

    public boolean isPremium(UUID uuid){ if (useSQLite) return sqlite.isPremium(uuid); return cfg.getBoolean("players."+uuid.toString()+".premium", false); }

    public String getHashOrEmpty(UUID uuid){ String h = (useSQLite? sqlite.getHash(uuid) : cfg.getString("players."+uuid.toString()+".pass")); return h==null?"":h; }

    public void save(){ if (!useSQLite) { try { cfg.save(file); } catch (IOException e) { e.printStackTrace(); } } }

    public void saveAll(){ save(); }

    public void onSuccessfulLogin(UUID uuid, Player p){
        String spawnWorld = plugin.cfg().getString("postlogin.world", p.getWorld().getName());
        World w = plugin.getServer().getWorld(spawnWorld);
        if (w==null) w = p.getWorld();
        double x = plugin.cfg().getDouble("postlogin.x", p.getLocation().getX());
        double y = plugin.cfg().getDouble("postlogin.y", p.getLocation().getY());
        double z = plugin.cfg().getDouble("postlogin.z", p.getLocation().getZ());
        p.teleport(new Location(w,x,y,z));
        plugin.getSecurityManager().resetAttempts(uuid);
    }
}
