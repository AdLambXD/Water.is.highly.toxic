package com.adlambxd.watertoxic.punishment;

import com.adlambxd.watertoxic.WaterToxicPlugin;
import com.adlambxd.watertoxic.config.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;
import java.util.Map;
import java.util.Random;

public class PunishmentExecutor {

    private final WaterToxicPlugin plugin;
    private final ConfigManager config;
    private final Random random = new Random();

    public PunishmentExecutor(WaterToxicPlugin plugin, ConfigManager config) {
        this.plugin = plugin;
        this.config = config;
    }

    public void executeStage(int stage, Player player) {
        List<Map<?, ?>> effects = config.getStageEffects(stage);
        if (effects.isEmpty()) return;

        for (Map<?, ?> effect : effects) {
            String type = ((String) effect.get("type")).toUpperCase();
            switch (type) {
                case "POTION" -> applyPotion(player, effect);
                case "DAMAGE" -> applyDamage(player, effect);
                case "LIGHTNING" -> applyLightning(player, effect);
                case "SUMMON" -> applySummon(player, effect);
                case "COMMAND" -> applyCommand(player, effect);
                case "MESSAGE" -> applyMessage(player, effect);
            }
        }
    }

    private void applyPotion(Player player, Map<?, ?> effect) {
        String effectName = (String) effect.get("effect");
        PotionEffectType type = Registry.EFFECT.get(NamespacedKey.minecraft(effectName.toLowerCase()));
        if (type == null) {
            plugin.getLogger().warning("Unknown potion effect: " + effectName);
            return;
        }
        int duration = ((Number) effect.get("duration")).intValue();
        int amplifier = ((Number) effect.get("amplifier")).intValue();
        player.addPotionEffect(new PotionEffect(type, duration, amplifier, false, true));
    }

    private void applyDamage(Player player, Map<?, ?> effect) {
        double amount = ((Number) effect.get("amount")).doubleValue();
        player.damage(amount);
        if (config.isVerboseLog()) {
            plugin.getLogger().info("Applied " + amount + " damage to " + player.getName());
        }
    }

    private void applyLightning(Player player, Map<?, ?> effect) {
        double chance = ((Number) effect.get("chance")).doubleValue();
        if (random.nextDouble() >= chance) return;

        Location loc = player.getLocation().clone();
        Bukkit.getRegionScheduler().run(plugin, loc, task ->
            player.getWorld().strikeLightningEffect(loc)
        );
    }

    private void applySummon(Player player, Map<?, ?> effect) {
        String entityName = (String) effect.get("entity");
        EntityType entityType;
        try {
            entityType = EntityType.valueOf(entityName.toUpperCase());
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Unknown entity type: " + entityName);
            return;
        }

        int amount = ((Number) effect.get("amount")).intValue();
        int radius = effect.containsKey("radius") ? ((Number) effect.get("radius")).intValue() : 3;

        Location loc = player.getLocation().clone();
        Bukkit.getRegionScheduler().run(plugin, loc, task -> {
            for (int i = 0; i < amount; i++) {
                Location spawnLoc = loc.clone().add(
                    random.nextDouble() * radius * 2 - radius,
                    0,
                    random.nextDouble() * radius * 2 - radius
                );
                loc.getWorld().spawnEntity(spawnLoc, entityType);
            }
        });
    }

    private void applyCommand(Player player, Map<?, ?> effect) {
        String command = ((String) effect.get("command")).replace("{player}", player.getName());
        Bukkit.getGlobalRegionScheduler().run(plugin, task ->
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command)
        );
    }

    private void applyMessage(Player player, Map<?, ?> effect) {
        String message = (String) effect.get("message");
        player.sendMessage(message.replace('&', '§'));
    }
}
