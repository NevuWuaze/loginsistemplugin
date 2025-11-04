package com.oxh.login;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class JoinListener implements Listener {
    private final Main plugin;
    private final CaptchaManager captchaManager;
    private final GUIManager guiManager;
    private final PlayerData playerData;

    public JoinListener(Main plugin, CaptchaManager cm, GUIManager gm, PlayerData pd){ this.plugin = plugin; this.captchaManager = cm; this.guiManager = gm; this.playerData = pd; }

    @EventHandler
    public void onJoin(PlayerJoinEvent e){
        Player p = e.getPlayer();
        String worldName = plugin.cfg().getString("login.world", "world");
        World w = Bukkit.getWorld(worldName);
        if (w==null) w = p.getWorld();
        double x = plugin.cfg().getDouble("login.x", 0.5);
        double y = plugin.cfg().getDouble("login.y", 65);
        double z = plugin.cfg().getDouble("login.z", 0.5);
        Location loc = new Location(w, x,y,z);
        p.teleport(loc);

        if (!playerData.isRegistered(p.getUniqueId())){
            String op = captchaManager.generarOperacion(p);
            p.sendTitle("§cCAPTCHA", "§eResuelve: §6"+op, 10, 20*10, 10);
            p.sendMessage(ChatColor.YELLOW + "Responde: /captcha <resultado>");
            plugin.getMusicManager().startBackgroundFor(p);
        } else {
            guiManager.openLoginPremiumMenu(p);
            plugin.getMusicManager().startBackgroundFor(p);
        }
    }
}
