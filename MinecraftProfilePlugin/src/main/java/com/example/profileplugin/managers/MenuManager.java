package com.example.profileplugin.managers;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;
import net.luckperms.api.LuckPerms;
import net.milkbowl.vault.economy.Economy;

import java.util.Arrays;
import java.util.List;

public class MenuManager {

    private final JavaPlugin plugin;
    private final ConfigManager config;
    private final SoundManager soundManager;
    private final PlayerDataManager playerDataManager;
    private LuckPerms luckPerms;
    private Economy economy;

    public MenuManager(JavaPlugin plugin, ConfigManager config, SoundManager soundManager,
                      PlayerDataManager playerDataManager) {
        this.plugin = plugin;
        this.config = config;
        this.soundManager = soundManager;
        this.playerDataManager = playerDataManager;
    }

    public void setLuckPerms(LuckPerms luckPerms) {
        this.luckPerms = luckPerms;
    }

    public void setEconomy(Economy economy) {
        this.economy = economy;
    }

    public void openProfileMenu(Player player) {
        Inventory inventory = Bukkit.createInventory(null, config.getInt("menu.size"),
            ChatColor.translateAlternateColorCodes('&', config.getString("menu.title")));

        // Add border and decorations
        addMenuDecorations(inventory);

        // Player head
        ItemStack playerHead = createPlayerHead(player);
        inventory.setItem(config.getInt("menu.player-head-slot"), playerHead);

        // Rank head
        ItemStack rankHead = createRankHead(player);
        inventory.setItem(config.getInt("menu.rank-head-slot"), rankHead);

        // Money head
        ItemStack moneyHead = createMoneyHead(player);
        inventory.setItem(config.getInt("menu.money-head-slot"), moneyHead);

        // Settings head
        ItemStack settingsHead = createSettingsHead();
        inventory.setItem(config.getInt("menu.settings-head-slot"), settingsHead);

        player.openInventory(inventory);
        soundManager.playMenuOpen(player);
    }

    public void openSettingsMenu(Player player) {
        Inventory inventory = Bukkit.createInventory(null, config.getInt("settings.size"),
            ChatColor.translateAlternateColorCodes('&', config.getString("settings.title")));

        // Add border and decorations
        addSettingsDecorations(inventory);

        // Visibility toggle
        ItemStack visibilityItem = createVisibilityToggle(player);
        inventory.setItem(config.getInt("settings.visibility-slot"), visibilityItem);

        // Fly toggle
        ItemStack flyItem = createFlyToggle(player);
        inventory.setItem(config.getInt("settings.fly-slot"), flyItem);

        // Back button
        ItemStack backItem = createBackButton();
        inventory.setItem(config.getInt("settings.back-slot"), backItem);

        player.openInventory(inventory);
        soundManager.playMenuOpen(player);
    }

    private void addMenuDecorations(Inventory inventory) {
        Material borderMaterial = Material.valueOf(config.getString("menu.border-material"));
        Material decorationMaterial = Material.valueOf(config.getString("menu.decoration-material"));

        // Border
        int[] borderSlots = {0, 1, 2, 3, 5, 6, 7, 8, 9, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26};
        for (int slot : borderSlots) {
            ItemStack border = new ItemStack(borderMaterial);
            ItemMeta meta = border.getItemMeta();
            meta.setDisplayName(" ");
            border.setItemMeta(meta);
            inventory.setItem(slot, border);
        }

        // Decorations
        int[] decorationSlots = {10, 12, 14, 16};
        for (int slot : decorationSlots) {
            ItemStack decoration = new ItemStack(decorationMaterial);
            ItemMeta meta = decoration.getItemMeta();
            meta.setDisplayName(ChatColor.AQUA + "✦");
            decoration.setItemMeta(meta);
            inventory.setItem(slot, decoration);
        }
    }

    private void addSettingsDecorations(Inventory inventory) {
        Material borderMaterial = Material.valueOf(config.getString("menu.border-material"));

        // Border for settings menu
        for (int i = 0; i < inventory.getSize(); i++) {
            if (inventory.getItem(i) == null) {
                ItemStack border = new ItemStack(borderMaterial);
                ItemMeta meta = border.getItemMeta();
                meta.setDisplayName(" ");
                border.setItemMeta(meta);
                inventory.setItem(i, border);
            }
        }
    }

    private ItemStack createPlayerHead(Player player) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        meta.setOwningPlayer(player);
        meta.setDisplayName(ChatColor.GOLD + "§l" + player.getName());
        meta.setLore(Arrays.asList(
            ChatColor.GRAY + "Your player profile",
            ChatColor.YELLOW + "UUID: " + player.getUniqueId().toString().substring(0, 8) + "...",
            "",
            ChatColor.GREEN + "Click to view detailed stats!"
        ));
        head.setItemMeta(meta);
        return head;
    }

    private ItemStack createRankHead(Player player) {
        Material material = Material.valueOf(config.getString("menu.rank-head-material"));
        ItemStack head = new ItemStack(material);
        ItemMeta meta = head.getItemMeta();
        meta.setDisplayName(ChatColor.BLUE + "§lRANK & PERMISSIONS");

        List<String> lore = Arrays.asList(
            ChatColor.GRAY + "Current Rank:",
            ChatColor.AQUA + "§l" + getPlayerRank(player),
            "",
            ChatColor.YELLOW + "Permission Groups:",
            ChatColor.GRAY + "• Access to server features",
            ChatColor.GRAY + "• Special commands",
            ChatColor.GRAY + "• Exclusive areas"
        );
        meta.setLore(lore);
        head.setItemMeta(meta);
        return head;
    }

    private ItemStack createMoneyHead(Player player) {
        Material material = Material.valueOf(config.getString("menu.money-head-material"));
        ItemStack head = new ItemStack(material);
        ItemMeta meta = head.getItemMeta();
        meta.setDisplayName(ChatColor.GREEN + "§lECONOMY & BALANCE");

        double balance = getPlayerBalance(player);
        List<String> lore = Arrays.asList(
            ChatColor.GRAY + "Current Balance:",
            ChatColor.GOLD + "§l$" + String.format("%.2f", balance),
            "",
            ChatColor.YELLOW + "Ways to earn money:",
            ChatColor.GRAY + "• Complete quests",
            ChatColor.GRAY + "• Mine resources",
            ChatColor.GRAY + "• Trade with players"
        );
        meta.setLore(lore);
        head.setItemMeta(meta);
        return head;
    }

    private ItemStack createSettingsHead() {
        Material material = Material.valueOf(config.getString("menu.settings-head-material"));
        ItemStack head = new ItemStack(material);
        ItemMeta meta = head.getItemMeta();
        meta.setDisplayName(ChatColor.RED + "§lSETTINGS & PREFERENCES");
        meta.setLore(Arrays.asList(
            ChatColor.GRAY + "Customize your experience",
            "",
            ChatColor.YELLOW + "Available options:",
            ChatColor.GRAY + "• Player visibility",
            ChatColor.GRAY + "• Flight mode",
            ChatColor.GRAY + "• Sound preferences",
            "",
            ChatColor.GREEN + "Click to open settings!"
        ));
        head.setItemMeta(meta);
        return head;
    }

    private ItemStack createVisibilityToggle(Player player) {
        Material material = Material.valueOf(config.getString("settings.visibility-material"));
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        boolean visible = playerDataManager.getVisibility(player);
        meta.setDisplayName(ChatColor.AQUA + "§lPLAYER VISIBILITY");

        List<String> lore = Arrays.asList(
            ChatColor.GRAY + "Control who can see you",
            "",
            ChatColor.YELLOW + "Current status:",
            (visible ? ChatColor.GREEN + "§lVISIBLE" : ChatColor.RED + "§lHIDDEN"),
            "",
            ChatColor.GRAY + "Click to toggle!"
        );
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createFlyToggle(Player player) {
        Material material = Material.valueOf(config.getString("settings.fly-material"));
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        boolean flying = playerDataManager.getFlyMode(player);
        meta.setDisplayName(ChatColor.LIGHT_PURPLE + "§lFLIGHT MODE");

        List<String> lore = Arrays.asList(
            ChatColor.GRAY + "Toggle creative flight",
            "",
            ChatColor.YELLOW + "Current status:",
            (flying ? ChatColor.GREEN + "§lENABLED" : ChatColor.RED + "§lDISABLED"),
            "",
            ChatColor.GRAY + "Requires permission!",
            ChatColor.GRAY + "Click to toggle!"
        );
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createBackButton() {
        Material material = Material.valueOf(config.getString("settings.back-material"));
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.GRAY + "§l← BACK TO PROFILE");
        meta.setLore(Arrays.asList(
            ChatColor.GRAY + "Return to main menu",
            "",
            ChatColor.YELLOW + "Click to go back!"
        ));
        item.setItemMeta(meta);
        return item;
    }

    private String getPlayerRank(Player player) {
        if (luckPerms != null) {
            return luckPerms.getUserManager().getUser(player.getUniqueId()).getPrimaryGroup();
        }
        return "Default";
    }

    private double getPlayerBalance(Player player) {
        if (economy != null) {
            return economy.getBalance(player);
        }
        return 0.0;
    }

    public void reloadConfig() {
        // Menu configurations are handled dynamically
    }
}
