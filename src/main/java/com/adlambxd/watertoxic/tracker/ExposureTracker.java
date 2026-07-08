package com.adlambxd.watertoxic.tracker;

import com.adlambxd.watertoxic.WaterToxicPlugin;
import com.adlambxd.watertoxic.config.ConfigManager;
import com.adlambxd.watertoxic.model.PlayerWaterData;
import com.adlambxd.watertoxic.punishment.PunishmentExecutor;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ExposureTracker {

    private final WaterToxicPlugin plugin;
    private final ConfigManager config;
    private final PunishmentExecutor executor;
    private final Map<UUID, PlayerWaterData> trackingData = new ConcurrentHashMap<>();

    public ExposureTracker(WaterToxicPlugin plugin, ConfigManager config, PunishmentExecutor executor) {
        this.plugin = plugin;
        this.config = config;
        this.executor = executor;
    }

    public void onPlayerEnterWater(Player player) {
        UUID id = player.getUniqueId();
        PlayerWaterData data = trackingData.computeIfAbsent(id, k -> new PlayerWaterData(id));
        data.setInWater(true);

        if (config.isVerboseLog()) {
            plugin.getLogger().info(player.getName() + " entered water (accumulated: " + data.getAccumulatedSeconds() + "s)");
        }

        ensureSchedulerRunning(player, data);
    }

    public void onPlayerLeaveWater(Player player) {
        PlayerWaterData data = trackingData.get(player.getUniqueId());
        if (data != null) {
            data.setInWater(false);
            if (config.isVerboseLog()) {
                plugin.getLogger().info(player.getName() + " left water (accumulated: " + data.getAccumulatedSeconds() + "s)");
            }
        }
    }

    public PlayerWaterData getData(UUID playerId) {
        return trackingData.get(playerId);
    }

    public void cleanup(UUID playerId) {
        PlayerWaterData data = trackingData.remove(playerId);
        if (data != null) {
            data.cancelTask();
        }
    }

    public void cleanupAll() {
        for (PlayerWaterData data : trackingData.values()) {
            data.cancelTask();
        }
        trackingData.clear();
    }

    public void reset(UUID playerId) {
        cleanup(playerId);
    }

    private void ensureSchedulerRunning(Player player, PlayerWaterData data) {
        if (data.getScheduledTask() != null && !data.getScheduledTask().isCancelled()) {
            return;
        }

        long interval = config.getCheckInterval();
        data.setScheduledTask(player.getScheduler().runAtFixedRate(
            plugin,
            task -> processTick(player, data),
            null,
            1L,
            interval
        ));
    }

    private void processTick(Player player, PlayerWaterData data) {
        if (!player.isOnline()) {
            cleanup(player.getUniqueId());
            return;
        }

        if (data.isInWater()) {
            if (!player.isInWater()) {
                data.setInWater(false);
                return;
            }
            accumulate(player, data);
        } else {
            decay(player, data);
        }
    }

    private void accumulate(Player player, PlayerWaterData data) {
        int interval = config.getCheckInterval();
        if (config.isRespectWaterBreathing() && player.hasPotionEffect(org.bukkit.potion.PotionEffectType.WATER_BREATHING)) {
            interval = Math.max(1, interval / 2);
        }
        data.setAccumulatedTicks(data.getAccumulatedTicks() + interval);
        checkStages(player, data);
    }

    private void decay(Player player, PlayerWaterData data) {
        int interval = config.getCheckInterval();
        int decay = (int) (interval * config.getDecayMultiplier());
        data.setAccumulatedTicks(Math.max(0, data.getAccumulatedTicks() - decay));

        if (data.getAccumulatedTicks() <= 0) {
            data.setCurrentStage(0);
            data.cancelTask();
            trackingData.remove(player.getUniqueId());
        }
    }

    private void checkStages(Player player, PlayerWaterData data) {
        int totalSeconds = data.getAccumulatedTicks() / 20;
        int newStage = 0;

        for (int stage : config.getStageNumbers()) {
            if (totalSeconds >= config.getStageTriggerSeconds(stage)) {
                newStage = Math.max(newStage, stage);
            }
        }

        if (newStage > data.getCurrentStage()) {
            data.setCurrentStage(newStage);
            if (config.isVerboseLog()) {
                plugin.getLogger().info(player.getName() + " reached stage " + newStage + " ("
                    + data.getAccumulatedSeconds() + "s in water)");
            }
            executor.executeStage(newStage, player);
        }
    }
}
