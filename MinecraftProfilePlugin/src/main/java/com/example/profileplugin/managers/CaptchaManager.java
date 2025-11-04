package com.example.profileplugin.managers;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class CaptchaManager {

    private final JavaPlugin plugin;
    private final ConfigManager config;
    private final SoundManager soundManager;
    private final Map<Player, Material> activeCaptchas;
    private final Map<Player, BukkitRunnable> captchaTimeouts;
    private final Random random;

    public CaptchaManager(JavaPlugin plugin, ConfigManager config, SoundManager soundManager) {
        this.plugin = plugin;
        this.config = config;
        this.soundManager = soundManager;
        this.activeCaptchas = new HashMap<>();
        this.captchaTimeouts = new HashMap<>();
        this.random = new Random();
    }

    public void startCaptcha(Player player) {
        if (!config.getBoolean("features.captcha-enabled")) {
            return;
        }

        Material correctMaterial = generateCaptchaMaterial();
        activeCaptchas.put(player, correctMaterial);

        // Send message
        player.sendMessage(ChatColor.translateAlternateColorCodes('&',
            config.getString("messages.captcha-required")));

        // Open GUI
        openCaptchaGUI(player, correctMaterial);

        // Set timeout
        BukkitRunnable timeout = new BukkitRunnable() {
            @Override
            public void run() {
                if (activeCaptchas.containsKey(player)) {
                    timeoutCaptcha(player);
                }
            }
        };
        timeout.runTaskLater(plugin, config.getInt("captcha.timeout") * 20L);
        captchaTimeouts.put(player, timeout);

        soundManager.playMenuOpen(player);
    }

    private void openCaptchaGUI(Player player, Material correctMaterial) {
        Inventory inventory = Bukkit.createInventory(null, 9, // A row of 9 blocks
            ChatColor.translateAlternateColorCodes('&', config.getString("captcha.title")));

        List<Material> possibleMaterials = new ArrayList<>(Arrays.asList(
            Material.STONE,
            Material.DIRT,
            Material.COBBLESTONE,
            Material.OAK_LOG,
            Material.BIRCH_LOG,
            Material.SPRUCE_LOG,
            Material.ACACIA_LOG,
            Material.DARK_OAK_LOG,
            Material.JUNGLE_LOG
        ));
        possibleMaterials.remove(correctMaterial); // Ensure correct material is not in distractors

        List<ItemStack> captchaBlocks = new ArrayList<>();
        for (int i = 0; i < 8; i++) { // 8 incorrect blocks
            Material randomMaterial = possibleMaterials.get(random.nextInt(possibleMaterials.size()));
            ItemStack item = new ItemStack(randomMaterial);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(" ");
            item.setItemMeta(meta);
            captchaBlocks.add(item);
        }

        // Add the correct block
        ItemStack correctItem = new ItemStack(correctMaterial);
        ItemMeta correctMeta = correctItem.getItemMeta();
        correctMeta.setDisplayName(ChatColor.GREEN + "Click me!");
        correctItem.setItemMeta(correctMeta);
        captchaBlocks.add(correctItem);

        Collections.shuffle(captchaBlocks);

        for (int i = 0; i < captchaBlocks.size(); i++) {
            inventory.setItem(i, captchaBlocks.get(i));
        }

        player.openInventory(inventory);
    }



    public boolean handleCaptchaClick(Player player, int slot, ItemStack clickedItem) {
        if (!activeCaptchas.containsKey(player)) return false;

        Material correctMaterial = activeCaptchas.get(player);

        if (clickedItem != null && clickedItem.getType() == correctMaterial) {
            completeCaptcha(player);
            return true;
        } else {
            soundManager.playError(player);
            player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                config.getString("messages.captcha-incorrect")));
        }
        return false;
    }

    private void completeCaptcha(Player player) {
        activeCaptchas.remove(player);

        // Cancel timeout
        if (captchaTimeouts.containsKey(player)) {
            captchaTimeouts.get(player).cancel();
            captchaTimeouts.remove(player);
        }

        player.closeInventory();
        player.sendMessage(ChatColor.translateAlternateColorCodes('&',
            config.getString("messages.captcha-correct")));

        soundManager.playSuccess(player);
    }

    private void timeoutCaptcha(Player player) {
        activeCaptchas.remove(player);
        captchaTimeouts.remove(player);

        player.closeInventory();
        player.kickPlayer(ChatColor.translateAlternateColorCodes('&',
            config.getString("messages.captcha-timeout")));
    }

    private Material generateCaptchaMaterial() {
        List<Material> possibleMaterials = Arrays.asList(
            Material.STONE,
            Material.DIRT,
            Material.COBBLESTONE,
            Material.OAK_LOG,
            Material.BIRCH_LOG,
            Material.SPRUCE_LOG,
            Material.ACACIA_LOG,
            Material.DARK_OAK_LOG,
            Material.JUNGLE_LOG
        );
        return possibleMaterials.get(random.nextInt(possibleMaterials.size()));
    }

    public void cleanup(Player player) {
        activeCaptchas.remove(player);
        if (captchaTimeouts.containsKey(player)) {
            captchaTimeouts.get(player).cancel();
            captchaTimeouts.remove(player);
        }
    }

    public void reloadConfig() {
        // Captcha configurations are handled dynamically
    }
}
