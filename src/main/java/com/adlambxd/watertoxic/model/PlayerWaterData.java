package com.adlambxd.watertoxic.model;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;

import java.util.UUID;

public class PlayerWaterData {

    private final UUID playerId;
    private int accumulatedTicks;
    private boolean inWater;
    private int currentStage;
    private ScheduledTask scheduledTask;

    public PlayerWaterData(UUID playerId) {
        this.playerId = playerId;
        this.accumulatedTicks = 0;
        this.inWater = false;
        this.currentStage = 0;
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public int getAccumulatedTicks() {
        return accumulatedTicks;
    }

    public void setAccumulatedTicks(int accumulatedTicks) {
        this.accumulatedTicks = accumulatedTicks;
    }

    public boolean isInWater() {
        return inWater;
    }

    public void setInWater(boolean inWater) {
        this.inWater = inWater;
    }

    public int getCurrentStage() {
        return currentStage;
    }

    public void setCurrentStage(int currentStage) {
        this.currentStage = currentStage;
    }

    public ScheduledTask getScheduledTask() {
        return scheduledTask;
    }

    public void setScheduledTask(ScheduledTask scheduledTask) {
        this.scheduledTask = scheduledTask;
    }

    public double getAccumulatedSeconds() {
        return accumulatedTicks / 20.0;
    }

    public void cancelTask() {
        if (scheduledTask != null && !scheduledTask.isCancelled()) {
            scheduledTask.cancel();
        }
        scheduledTask = null;
    }
}
