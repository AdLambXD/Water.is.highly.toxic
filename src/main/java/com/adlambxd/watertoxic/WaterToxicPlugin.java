package com.adlambxd.watertoxic;

import com.adlambxd.watertoxic.command.WaterToxicCommand;
import com.adlambxd.watertoxic.config.ConfigManager;
import com.adlambxd.watertoxic.listener.PlayerStateListener;
import com.adlambxd.watertoxic.listener.WaterListener;
import com.adlambxd.watertoxic.placeholder.WaterExpansion;
import com.adlambxd.watertoxic.punishment.PunishmentExecutor;
import com.adlambxd.watertoxic.tracker.ExposureTracker;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class WaterToxicPlugin extends JavaPlugin {

    private ConfigManager configManager;
    private ExposureTracker exposureTracker;
    private PunishmentExecutor punishmentExecutor;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        configManager = new ConfigManager(this);
        configManager.load();

        punishmentExecutor = new PunishmentExecutor(this, configManager);
        exposureTracker = new ExposureTracker(this, configManager, punishmentExecutor);

        registerListeners();
        registerCommands();
        registerPlaceholderExpansion();

        getLogger().info("WaterIsHighlyToxic enabled - Water is toxic!");
    }

    @Override
    public void onDisable() {
        if (exposureTracker != null) {
            exposureTracker.cleanupAll();
        }
        getLogger().info("WaterIsHighlyToxic disabled");
    }

    private void registerListeners() {
        WaterListener waterListener = new WaterListener(this, configManager, exposureTracker);
        PlayerStateListener stateListener = new PlayerStateListener(configManager, exposureTracker);
        Bukkit.getPluginManager().registerEvents(waterListener, this);
        Bukkit.getPluginManager().registerEvents(stateListener, this);
    }

    private void registerCommands() {
        WaterToxicCommand executor = new WaterToxicCommand(this, configManager, exposureTracker);
        getCommand("watertoxic").setExecutor(executor);
        getCommand("watertoxic").setTabCompleter(executor);
    }

    private void registerPlaceholderExpansion() {
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new WaterExpansion(this, exposureTracker).register();
            getLogger().info("PlaceholderAPI expansion registered");
        }
    }

    public void reloadPlugin() {
        configManager.load();
        getLogger().info("Configuration reloaded");
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public ExposureTracker getExposureTracker() {
        return exposureTracker;
    }
}
