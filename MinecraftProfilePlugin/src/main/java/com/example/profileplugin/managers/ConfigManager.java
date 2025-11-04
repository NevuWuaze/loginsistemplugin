package com.example.profileplugin.managers;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class ConfigManager {

    private final JavaPlugin plugin;
    private final Map<String, Object> defaultValues;

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.defaultValues = new HashMap<>();
        initializeDefaults();
    }

    private void initializeDefaults() {
        // Profile head settings
        defaultValues.put("profile-head-slot", 8);
        defaultValues.put("profile-head-material", "PLAYER_HEAD");

        // Menu settings
        defaultValues.put("menu.title", "§8§lProfile Menu");
        defaultValues.put("menu.size", 27);
        defaultValues.put("menu.player-head-slot", 4);
        defaultValues.put("menu.rank-head-slot", 11);
        defaultValues.put("menu.money-head-slot", 13);
        defaultValues.put("menu.settings-head-slot", 15);
        defaultValues.put("menu.rank-head-material", "DIAMOND");
        defaultValues.put("menu.money-head-material", "GOLD_INGOT");
        defaultValues.put("menu.settings-head-material", "REDSTONE");
        defaultValues.put("menu.border-material", "GRAY_STAINED_GLASS_PANE");
        defaultValues.put("menu.decoration-material", "LIGHT_BLUE_STAINED_GLASS_PANE");

        // Settings menu
        defaultValues.put("settings.title", "§8§lSettings");
        defaultValues.put("settings.size", 27);
        defaultValues.put("settings.visibility-slot", 11);
        defaultValues.put("settings.fly-slot", 13);
        defaultValues.put("settings.back-slot", 18);
        defaultValues.put("settings.visibility-material", "ENDER_PEARL");
        defaultValues.put("settings.fly-material", "FEATHER");
        defaultValues.put("settings.back-material", "ARROW");

        // Captcha settings
        defaultValues.put("captcha.title", "§8§lVerify Identity");
        defaultValues.put("captcha.size", 27);
        defaultValues.put("captcha.length", 6);
        defaultValues.put("captcha.timeout", 300); // seconds

        // Messages
        defaultValues.put("messages.welcome", "§a§lWelcome to the server, §e%player%§a§l!");
        defaultValues.put("messages.captcha-required", "§ePlease solve the captcha to continue playing.");
        defaultValues.put("messages.captcha-correct", "§aCaptcha solved! Welcome!");
        defaultValues.put("messages.captcha-incorrect", "§cIncorrect captcha. Please try again.");
        defaultValues.put("messages.captcha-timeout", "§cCaptcha timeout. Please rejoin.");
        defaultValues.put("messages.visibility-enabled", "§aPlayer visibility §eenabled§a.");
        defaultValues.put("messages.visibility-disabled", "§cPlayer visibility §edisabled§c.");
        defaultValues.put("messages.fly-enabled", "§aFly mode §eenabled§a.");
        defaultValues.put("messages.fly-disabled", "§cFly mode §edisabled§c.");
        defaultValues.put("messages.no-permission", "§cYou don't have permission to use this feature.");

        // Sounds
        defaultValues.put("sounds.menu-open", "UI_BUTTON_CLICK");
        defaultValues.put("sounds.menu-click", "UI_BUTTON_CLICK");
        defaultValues.put("sounds.success", "ENTITY_EXPERIENCE_ORB_PICKUP");
        defaultValues.put("sounds.error", "ENTITY_VILLAGER_NO");
        defaultValues.put("sounds.welcome", "ENTITY_PLAYER_LEVELUP");

        // Permissions
        defaultValues.put("permissions.use-profile", "profileplugin.use");
        defaultValues.put("permissions.admin", "profileplugin.admin");

        // Features
        defaultValues.put("features.captcha-enabled", true);
        defaultValues.put("features.welcome-message", true);
        defaultValues.put("features.sounds", true);
        defaultValues.put("features.persistence", true);
    }

    public void loadConfig() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        FileConfiguration config = plugin.getConfig();

        // Validate critical config values
        validateConfig();
    }

    private void validateConfig() {
        FileConfiguration config = plugin.getConfig();

        // Validate slots are within inventory bounds
        if (config.getInt("profile-head-slot", 8) < 0 || config.getInt("profile-head-slot", 8) > 8) {
            plugin.getLogger().warning("Invalid profile-head-slot. Using default value 8.");
            config.set("profile-head-slot", 8);
        }

        // Validate materials exist
        try {
            Material.valueOf(config.getString("menu.rank-head-material", "DIAMOND"));
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid menu.rank-head-material. Using default DIAMOND.");
            config.set("menu.rank-head-material", "DIAMOND");
        }

        // Similar validation for other materials...
    }

    public Object get(String path) {
        return plugin.getConfig().get(path, defaultValues.get(path));
    }

    public String getString(String path) {
        return plugin.getConfig().getString(path, (String) defaultValues.get(path));
    }

    public int getInt(String path) {
        return plugin.getConfig().getInt(path, (Integer) defaultValues.get(path));
    }

    public boolean getBoolean(String path) {
        return plugin.getConfig().getBoolean(path, (Boolean) defaultValues.get(path));
    }

    public void reload() {
        plugin.reloadConfig();
        validateConfig();
    }
}
