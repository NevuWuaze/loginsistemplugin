package com.example.profileplugin;

import com.example.profileplugin.managers.*;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.ChatColor;
import org.bukkit.plugin.RegisteredServiceProvider;

public class ProfilePlugin extends JavaPlugin implements Listener {

    private ConfigManager configManager;
    private PlayerDataManager playerDataManager;
    private SoundManager soundManager;
    private CaptchaManager captchaManager;
    private MenuManager menuManager;

    private LuckPerms luckPerms;
    private Economy economy;

    @Override
    public void onEnable() {
        // Initialize managers
        configManager = new ConfigManager(this);
        configManager.loadConfig();

        playerDataManager = new PlayerDataManager(this);
        soundManager = new SoundManager(this, configManager);
        captchaManager = new CaptchaManager(this, configManager, soundManager);
        menuManager = new MenuManager(this, configManager, soundManager, playerDataManager);

        getServer().getPluginManager().registerEvents(this, this);

        // Hook into LuckPerms
        if (getServer().getPluginManager().getPlugin("LuckPerms") != null) {
            luckPerms = LuckPermsProvider.get();
            menuManager.setLuckPerms(luckPerms);
        }

        // Hook into Vault for economy
        if (getServer().getPluginManager().getPlugin("Vault") != null) {
            RegisteredServiceProvider<Economy> economyProvider =
                    getServer().getServicesManager().getRegistration(Economy.class);
            if (economyProvider != null) {
                economy = economyProvider.getProvider();
                menuManager.setEconomy(economy);
                getLogger().info("Successfully hooked into Vault economy!");
            } else {
                getLogger().warning("Vault is installed but no economy provider found. Economy features will be disabled.");
            }
        } else {
            getLogger().warning("Vault not found. Economy features will be disabled.");
        }

        getLogger().info("ProfilePlugin has been enabled!");
    }

    @Override
    public void onDisable() {
        // Save all player data
        playerDataManager.saveAllData();
        getLogger().info("ProfilePlugin has been disabled!");
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // Load player data
        playerDataManager.loadPlayerData(player);

        // Welcome message
        if (configManager.getBoolean("features.welcome-message")) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    configManager.getString("messages.welcome").replace("%player%", player.getName())));
            soundManager.playWelcome(player);
        }

        // Add profile head to inventory
        int slot = configManager.getInt("profile-head-slot");
        ItemStack head = createPlayerHead(player);
        player.getInventory().setItem(slot, head);

        // Start captcha if enabled
        captchaManager.startCaptcha(player);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        // Save player data
        playerDataManager.savePlayerData(player);

        // Cleanup captcha
        captchaManager.cleanup(player);
    }

    private ItemStack createPlayerHead(Player player) {
        ItemStack head = new ItemStack(Material.valueOf(configManager.getString("profile-head-material")));
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        if (meta != null) {
            meta.setOwningPlayer(player);
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&',
                    configManager.getString("menu.player-head-name").replace("%player%", player.getName())));
            head.setItemMeta(meta);
        }
        return head;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getCurrentItem() == null) return;

        String title = event.getView().getTitle();

        // Check for profile head click
        if (event.getCurrentItem().getType() == Material.PLAYER_HEAD &&
                event.getCurrentItem().getItemMeta() != null &&
                event.getCurrentItem().getItemMeta().getDisplayName().contains(player.getName())) {
            event.setCancelled(true);
            menuManager.openProfileMenu(player);
            return;
        }

        // Handle profile menu clicks
        if (title.equals(ChatColor.translateAlternateColorCodes('&', configManager.getString("menu.title")))) {
            event.setCancelled(true);
            handleProfileMenuClick(player, event.getSlot());
            return;
        }

        // Handle settings menu clicks
        if (title.equals(ChatColor.translateAlternateColorCodes('&', configManager.getString("settings.title")))) {
            event.setCancelled(true);
            handleSettingsMenuClick(player, event.getSlot());
            return;
        }

        // Handle captcha clicks
        if (title.equals(ChatColor.translateAlternateColorCodes('&', configManager.getString("captcha.title")))) {
            event.setCancelled(true);
            handleCaptchaClick(player, event.getSlot(), event.getCurrentItem());
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;

        String title = event.getView().getTitle();
        if (title.equals(ChatColor.translateAlternateColorCodes('&', configManager.getString("captcha.title")))) {
            captchaManager.cleanup(player);
        }
    }

    private void handleProfileMenuClick(Player player, int slot) {
        if (slot == configManager.getInt("menu.settings-head-slot")) {
            menuManager.openSettingsMenu(player);
        }
    }

    private void handleSettingsMenuClick(Player player, int slot) {
        if (slot == configManager.getInt("settings.visibility-slot")) {
            toggleVisibility(player);
        } else if (slot == configManager.getInt("settings.fly-slot")) {
            toggleFly(player);
        } else if (slot == configManager.getInt("settings.back-slot")) {
            menuManager.openProfileMenu(player);
        }
    }

    private void handleCaptchaClick(Player player, int slot, ItemStack item) {
        captchaManager.handleCaptchaClick(player, slot, item);
    }

    private void toggleVisibility(Player player) {
        boolean visible = playerDataManager.getVisibility(player);
        playerDataManager.setVisibility(player, !visible);

        for (Player other : Bukkit.getOnlinePlayers()) {
            if (!visible) {
                other.showPlayer(this, player);
            } else {
                other.hidePlayer(this, player);
            }
        }

        player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                !visible ? configManager.getString("messages.visibility-enabled") :
                        configManager.getString("messages.visibility-disabled")));
        soundManager.playSuccess(player);
        menuManager.openSettingsMenu(player);
    }

    private void toggleFly(Player player) {
        if (!player.hasPermission(configManager.getString("permissions.use-profile"))) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    configManager.getString("messages.no-permission")));
            soundManager.playError(player);
            return;
        }

        boolean flying = playerDataManager.getFlyMode(player);
        playerDataManager.setFlyMode(player, !flying);

        player.setAllowFlight(!flying);
        player.setFlying(!flying);

        player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                !flying ? configManager.getString("messages.fly-enabled") :
                        configManager.getString("messages.fly-disabled")));
        soundManager.playSuccess(player);
        menuManager.openSettingsMenu(player);
    }

    @EventHandler
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String command = event.getMessage().toLowerCase();

        if (command.equals("/profile")) {
            event.setCancelled(true);
            if (player.hasPermission(configManager.getString("permissions.use-profile"))) {
                menuManager.openProfileMenu(player);
            } else {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        configManager.getString("messages.no-permission")));
                soundManager.playError(player);
            }
        } else if (command.equals("/profilere")) {
            event.setCancelled(true);
            if (player.hasPermission(configManager.getString("permissions.reload"))) {
                reloadConfiguration();
                player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        configManager.getString("messages.reload-success")));
                soundManager.playSuccess(player);
            } else {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        configManager.getString("messages.no-permission")));
                soundManager.playError(player);
            }
        }
    }

    private void reloadConfiguration() {
        configManager.loadConfig();
        soundManager.reloadConfig();
        captchaManager.reloadConfig();
        menuManager.reloadConfig();

        getLogger().info("Configuration reloaded successfully!");
    }
}
