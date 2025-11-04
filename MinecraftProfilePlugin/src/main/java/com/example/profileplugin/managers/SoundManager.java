package com.example.profileplugin.managers;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class SoundManager {

    private final JavaPlugin plugin;
    private final ConfigManager config;

    public SoundManager(JavaPlugin plugin, ConfigManager config) {
        this.plugin = plugin;
        this.config = config;
    }

    public void playSound(Player player, String soundKey) {
        if (!config.getBoolean("features.sounds")) return;

        try {
            Sound sound = Sound.valueOf(config.getString("sounds." + soundKey));
            player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid sound: " + config.getString("sounds." + soundKey));
        }
    }

    public void playMenuOpen(Player player) {
        playSound(player, "menu-open");
    }

    public void playMenuClick(Player player) {
        playSound(player, "menu-click");
    }

    public void playSuccess(Player player) {
        playSound(player, "success");
    }

    public void playError(Player player) {
        playSound(player, "error");
    }

    public void playWelcome(Player player) {
        playSound(player, "welcome");
    }

    public void reloadConfig() {
        // Sound configurations are reloaded dynamically in playSound method
    }
}
