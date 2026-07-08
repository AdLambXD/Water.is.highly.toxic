package com.adlambxd.watertoxic.config;

import com.adlambxd.watertoxic.WaterToxicPlugin;
import org.bukkit.GameMode;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.*;

public class ConfigManager {

    private final WaterToxicPlugin plugin;

    private int checkInterval;
    private double decayMultiplier;
    private List<String> enabledWorlds;
    private List<String> disabledWorlds;
    private boolean respectGamemode;
    private boolean respectVehicle;
    private boolean respectWaterBreathing;
    private boolean verboseLog;

    private final Map<Integer, StageConfig> stages = new LinkedHashMap<>();

    public ConfigManager(WaterToxicPlugin plugin) {
        this.plugin = plugin;
    }

    public void load() {
        plugin.reloadConfig();
        FileConfiguration config = plugin.getConfig();

        checkInterval = config.getInt("check-interval", 10);
        decayMultiplier = config.getDouble("decay-multiplier", 1.0);
        enabledWorlds = config.getStringList("enabled-worlds");
        disabledWorlds = config.getStringList("disabled-worlds");
        respectGamemode = config.getBoolean("respect-gamemode", true);
        respectVehicle = config.getBoolean("respect-vehicle", true);
        respectWaterBreathing = config.getBoolean("respect-water-breathing", false);
        verboseLog = config.getBoolean("verbose-log", false);

        stages.clear();
        ConfigurationSection stagesSection = config.getConfigurationSection("stages");
        if (stagesSection != null) {
            for (String key : stagesSection.getKeys(false)) {
                int stageNum;
                try {
                    stageNum = Integer.parseInt(key);
                } catch (NumberFormatException e) {
                    plugin.getLogger().warning("Invalid stage key: " + key);
                    continue;
                }

                ConfigurationSection stageSection = stagesSection.getConfigurationSection(key);
                if (stageSection == null) continue;

                int triggerSeconds = stageSection.getInt("trigger-seconds", 5);
                List<Map<?, ?>> effects = (List<Map<?, ?>>) stageSection.getList("effects");

                StageConfig stageConfig = new StageConfig(stageNum, triggerSeconds, effects != null ? effects : List.of());
                stages.put(stageNum, stageConfig);
            }
        }
    }

    public boolean shouldTrackPlayer(Player player) {
        if (player.hasPermission("watertoxic.bypass")) return false;
        if (respectGamemode) {
            GameMode gm = player.getGameMode();
            if (gm == GameMode.CREATIVE || gm == GameMode.SPECTATOR) return false;
        }
        if (respectVehicle && player.isInsideVehicle()) return false;
        return isWorldEnabled(player.getWorld().getName());
    }

    public boolean isWorldEnabled(String worldName) {
        if (!disabledWorlds.isEmpty() && disabledWorlds.contains(worldName)) return false;
        if (!enabledWorlds.isEmpty() && !enabledWorlds.contains(worldName)) return false;
        return true;
    }

    public int getCheckInterval() {
        return checkInterval;
    }

    public double getDecayMultiplier() {
        return decayMultiplier;
    }

    public boolean isRespectGamemode() {
        return respectGamemode;
    }

    public boolean isRespectVehicle() {
        return respectVehicle;
    }

    public boolean isRespectWaterBreathing() {
        return respectWaterBreathing;
    }

    public boolean isVerboseLog() {
        return verboseLog;
    }

    public Set<Integer> getStageNumbers() {
        return stages.keySet();
    }

    public int getStageTriggerSeconds(int stage) {
        StageConfig sc = stages.get(stage);
        return sc != null ? sc.triggerSeconds : Integer.MAX_VALUE;
    }

    public List<Map<?, ?>> getStageEffects(int stage) {
        StageConfig sc = stages.get(stage);
        return sc != null ? sc.effects : List.of();
    }

    private record StageConfig(int number, int triggerSeconds, List<Map<?, ?>> effects) {
    }
}
