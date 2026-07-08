package com.adlambxd.watertoxic.listener;

import com.adlambxd.watertoxic.WaterToxicPlugin;
import com.adlambxd.watertoxic.config.ConfigManager;
import com.adlambxd.watertoxic.model.PlayerWaterData;
import com.adlambxd.watertoxic.tracker.ExposureTracker;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class WaterListener implements Listener {

    private final WaterToxicPlugin plugin;
    private final ConfigManager config;
    private final ExposureTracker tracker;

    public WaterListener(WaterToxicPlugin plugin, ConfigManager config, ExposureTracker tracker) {
        this.plugin = plugin;
        this.config = config;
        this.tracker = tracker;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!event.hasChangedBlock()) return;

        Player player = event.getPlayer();
        if (!config.shouldTrackPlayer(player)) return;

        boolean isInWaterNow = player.isInWater();
        PlayerWaterData data = tracker.getData(player.getUniqueId());
        boolean wasInWater = data != null && data.isInWater();

        if (isInWaterNow && !wasInWater) {
            tracker.onPlayerEnterWater(player);
        } else if (!isInWaterNow && wasInWater) {
            tracker.onPlayerLeaveWater(player);
        }
    }
}
