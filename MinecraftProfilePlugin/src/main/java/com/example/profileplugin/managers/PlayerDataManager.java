package com.example.profileplugin.managers;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerDataManager {

    private final JavaPlugin plugin;
    private final File dataFolder;
    private final Map<UUID, YamlConfiguration> playerData;

    public PlayerDataManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.dataFolder = new File(plugin.getDataFolder(), "playerdata");
        this.playerData = new HashMap<>();

        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
    }

    public void loadPlayerData(Player player) {
        UUID uuid = player.getUniqueId();
        File playerFile = new File(dataFolder, uuid.toString() + ".yml");

        if (!playerFile.exists()) {
            try {
                playerFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Failed to create player data file for " + player.getName());
                return;
            }
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(playerFile);
        playerData.put(uuid, config);
    }

    public void savePlayerData(Player player) {
        UUID uuid = player.getUniqueId();
        YamlConfiguration config = playerData.get(uuid);

        if (config != null) {
            try {
                config.save(new File(dataFolder, uuid.toString() + ".yml"));
            } catch (IOException e) {
                plugin.getLogger().severe("Failed to save player data for " + player.getName());
            }
        }
    }

    public void unloadPlayerData(Player player) {
        UUID uuid = player.getUniqueId();
        savePlayerData(player);
        playerData.remove(uuid);
    }

    public boolean getVisibility(Player player) {
        YamlConfiguration config = playerData.get(player.getUniqueId());
        return config != null ? config.getBoolean("visibility", true) : true;
    }

    public void setVisibility(Player player, boolean visible) {
        YamlConfiguration config = playerData.get(player.getUniqueId());
        if (config != null) {
            config.set("visibility", visible);
        }
    }

    public boolean getFlyMode(Player player) {
        YamlConfiguration config = playerData.get(player.getUniqueId());
        return config != null ? config.getBoolean("fly-mode", false) : false;
    }

    public void setFlyMode(Player player, boolean flying) {
        YamlConfiguration config = playerData.get(player.getUniqueId());
        if (config != null) {
            config.set("fly-mode", flying);
        }
    }

    public boolean isCaptchaCompleted(Player player) {
        YamlConfiguration config = playerData.get(player.getUniqueId());
        return config != null ? config.getBoolean("captcha-completed", false) : false;
    }

    public void setCaptchaCompleted(Player player, boolean completed) {
        YamlConfiguration config = playerData.get(player.getUniqueId());
        if (config != null) {
            config.set("captcha-completed", completed);
        }
    }

    public long getLastLogin(Player player) {
        YamlConfiguration config = playerData.get(player.getUniqueId());
        return config != null ? config.getLong("last-login", 0) : 0;
    }

    public void setLastLogin(Player player, long time) {
        YamlConfiguration config = playerData.get(player.getUniqueId());
        if (config != null) {
            config.set("last-login", time);
        }
    }

    public void saveAllData() {
        for (UUID uuid : playerData.keySet()) {
            Player player = plugin.getServer().getPlayer(uuid);
            if (player != null) {
                savePlayerData(player);
            }
        }
    }
}
