package com.adlambxd.watertoxic.placeholder;

import com.adlambxd.watertoxic.WaterToxicPlugin;
import com.adlambxd.watertoxic.model.PlayerWaterData;
import com.adlambxd.watertoxic.tracker.ExposureTracker;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class WaterExpansion extends PlaceholderExpansion {

    private final WaterToxicPlugin plugin;
    private final ExposureTracker tracker;

    public WaterExpansion(WaterToxicPlugin plugin, ExposureTracker tracker) {
        this.plugin = plugin;
        this.tracker = tracker;
    }

    @Override
    @NotNull
    public String getIdentifier() {
        return "watertoxic";
    }

    @Override
    @NotNull
    public String getAuthor() {
        return String.join(", ", plugin.getPluginMeta().getAuthors());
    }

    @Override
    @NotNull
    public String getVersion() {
        return plugin.getPluginMeta().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String params) {
        if (player == null) return "";

        PlayerWaterData data = tracker.getData(player.getUniqueId());

        return switch (params.toLowerCase()) {
            case "time" -> data != null ? String.format("%.1f", data.getAccumulatedSeconds()) : "0.0";
            case "stage" -> data != null ? String.valueOf(data.getCurrentStage()) : "0";
            case "inwater" -> data != null && data.isInWater() ? "yes" : "no";
            case "tick" -> data != null ? String.valueOf(data.getAccumulatedTicks()) : "0";
            default -> null;
        };
    }
}
