package com.adlambxd.watertoxic.command;

import com.adlambxd.watertoxic.WaterToxicPlugin;
import com.adlambxd.watertoxic.config.ConfigManager;
import com.adlambxd.watertoxic.model.PlayerWaterData;
import com.adlambxd.watertoxic.tracker.ExposureTracker;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class WaterToxicCommand implements CommandExecutor, TabCompleter {

    private final WaterToxicPlugin plugin;
    private final ConfigManager config;
    private final ExposureTracker tracker;

    public WaterToxicCommand(WaterToxicPlugin plugin, ConfigManager config, ExposureTracker tracker) {
        this.plugin = plugin;
        this.config = config;
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
                    sender.sendMessage(ChatColor.RED + "You don't have permission.");
                    return true;
                }
                plugin.reloadPlugin();
                sender.sendMessage(ChatColor.GREEN + "WaterIsHighlyToxic configuration reloaded.");
            }

            case "status" -> {
                if (!sender.hasPermission("watertoxic.admin")) {
                    sender.sendMessage(ChatColor.RED + "You don't have permission.");
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
                sender.sendMessage(ChatColor.RED + "Player not found: " + args[1]);
                return;
            }
        } else if (sender instanceof Player) {
            target = (Player) sender;
        } else {
            sender.sendMessage(ChatColor.RED + "Usage: /watertoxic status <player>");
            return;
        }

        PlayerWaterData data = tracker.getData(target.getUniqueId());
        if (data == null || data.getAccumulatedTicks() <= 0) {
            sender.sendMessage(ChatColor.YELLOW + target.getName() + " has no water toxicity data.");
            return;
        }

        sender.sendMessage(ChatColor.GOLD + "=== " + target.getName() + "'s Water Toxicity ===");
        sender.sendMessage(ChatColor.YELLOW + "In Water: " + (data.isInWater() ? ChatColor.RED + "Yes" : ChatColor.GREEN + "No"));
        sender.sendMessage(ChatColor.YELLOW + "Accumulated: " + ChatColor.WHITE + String.format("%.1f", data.getAccumulatedSeconds()) + "s");
        sender.sendMessage(ChatColor.YELLOW + "Stage: " + ChatColor.WHITE + (data.getCurrentStage() > 0 ? data.getCurrentStage() : "None"));
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "=== WaterIsHighlyToxic ===");
        sender.sendMessage(ChatColor.YELLOW + "/watertoxic reload " + ChatColor.GRAY + "- Reload configuration");
        sender.sendMessage(ChatColor.YELLOW + "/watertoxic status [player] " + ChatColor.GRAY + "- View toxicity status");
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
