package com.oxh.login;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

public class Main extends JavaPlugin {
    private static Main instance;
    private CaptchaManager captchaManager;
    private GUIManager guiManager;
    private PlayerData playerData;
    private MusicManager musicManager;
    private SecurityManager securityManager;
    private WorldEditSchematicPaster schematicPaster;
    private SQLiteStorage sqliteStorage;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        try {
            this.playerData = new PlayerData(this);

            this.captchaManager = new CaptchaManager(this);
            this.musicManager = new MusicManager(this);
            this.securityManager = new SecurityManager(this);
            if (cfg().getBoolean("storage.use-sqlite", true)) this.sqliteStorage = new SQLiteStorage(this);
            this.schematicPaster = new WorldEditSchematicPaster(this);
            this.guiManager = new GUIManager(this, captchaManager, playerData);

            // Pegar schematic si aplica
            schematicPaster.pasteIfConfigured();

            // Registrar listeners y comandos
            getServer().getPluginManager().registerEvents(new JoinListener(this, captchaManager, guiManager, playerData), this);
            getServer().getPluginManager().registerEvents(new InventoryClickListener(guiManager), this);

            getCommand("captcha").setExecutor(new CaptchaCommand(captchaManager, guiManager, playerData, securityManager, musicManager));
            getCommand("login").setExecutor(new LoginCommand(playerData, guiManager, musicManager));
            getCommand("premiumconfirmar").setExecutor(new PremiumConfirmCommand(playerData, guiManager));
            getCommand("register").setExecutor(new RegisterCommand(playerData, musicManager));

            getLogger().info("sajuilogin habilitado.");
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Fallo al inicializar sajuilogin", e);
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        if (playerData != null) playerData.saveAll();
        if (musicManager != null) musicManager.stopAll();
        getLogger().info("sajuilogin deshabilitado.");
    }

    public static Main getInstance() { return instance; }
    public FileConfiguration cfg() { return getConfig(); }
    public SecurityManager getSecurityManager(){ return securityManager; }
    public MusicManager getMusicManager(){ return musicManager; }
}
