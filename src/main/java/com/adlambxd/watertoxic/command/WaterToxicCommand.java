package com.adlambxd.watertoxic.command;

import com.adlambxd.watertoxic.WaterToxicPlugin;
import com.adlambxd.watertoxic.model.PlayerWaterData;
import com.adlambxd.watertoxic.tracker.ExposureTracker;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class WaterToxicCommand implements CommandExecutor, TabCompleter {

    private final WaterToxicPlugin plugin;
    private final ExposureTracker tracker;

    public WaterToxicCommand(WaterToxicPlugin plugin, ExposureTracker tracker) {
        this.plugin = plugin;
        this.tracker = tracker;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload" -> {
                if (!sender.hasPermission("watertoxic.admin")) {
                    sender.sendMessage(Component.text("You don't have permission.", NamedTextColor.RED));
                    return true;
                }
                plugin.reloadPlugin();
                sender.sendMessage(Component.text("WaterIsHighlyToxic configuration reloaded.", NamedTextColor.GREEN));
            }

            case "status" -> {
                if (!sender.hasPermission("watertoxic.admin")) {
                    sender.sendMessage(Component.text("You don't have permission.", NamedTextColor.RED));
                    return true;
                }
                handleStatus(sender, args);
            }

            default -> sendHelp(sender);
        }
        return true;
    }

    private void handleStatus(CommandSender sender, String[] args) {
        Player target;

        if (args.length >= 2) {
            target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                sender.sendMessage(Component.text("Player not found: " + args[1], NamedTextColor.RED));
                return;
            }
        } else if (sender instanceof Player) {
            target = (Player) sender;
        } else {
            sender.sendMessage(Component.text("Usage: /watertoxic status <player>", NamedTextColor.RED));
            return;
        }

        PlayerWaterData data = tracker.getData(target.getUniqueId());
        if (data == null || data.getAccumulatedTicks() <= 0) {
            sender.sendMessage(Component.text(target.getName() + " has no water toxicity data.", NamedTextColor.YELLOW));
            return;
        }

        sender.sendMessage(Component.text("=== " + target.getName() + "'s Water Toxicity ===", NamedTextColor.GOLD));
        sender.sendMessage(Component.textOfChildren(
            Component.text("In Water: ", NamedTextColor.YELLOW),
            Component.text(data.isInWater() ? "Yes" : "No", data.isInWater() ? NamedTextColor.RED : NamedTextColor.GREEN)
        ));
        sender.sendMessage(Component.textOfChildren(
            Component.text("Accumulated: ", NamedTextColor.YELLOW),
            Component.text(String.format("%.1f", data.getAccumulatedSeconds()) + "s", NamedTextColor.WHITE)
        ));
        sender.sendMessage(Component.textOfChildren(
            Component.text("Stage: ", NamedTextColor.YELLOW),
            Component.text(data.getCurrentStage() > 0 ? String.valueOf(data.getCurrentStage()) : "None", NamedTextColor.WHITE)
        ));
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(Component.text("=== WaterIsHighlyToxic ===", NamedTextColor.GOLD));
        sender.sendMessage(Component.textOfChildren(
            Component.text("/watertoxic reload ", NamedTextColor.YELLOW),
            Component.text("- Reload configuration", NamedTextColor.GRAY)
        ));
        sender.sendMessage(Component.textOfChildren(
            Component.text("/watertoxic status [player] ", NamedTextColor.YELLOW),
            Component.text("- View toxicity status", NamedTextColor.GRAY)
        ));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            String input = args[0].toLowerCase();
            if ("reload".startsWith(input)) completions.add("reload");
            if ("status".startsWith(input)) completions.add("status");
        } else if (args.length == 2 && args[0].equalsIgnoreCase("status")) {
            String input = args[1].toLowerCase();
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.getName().toLowerCase().startsWith(input)) {
                    completions.add(player.getName());
                }
            }
        }

        return completions;
    }
}
