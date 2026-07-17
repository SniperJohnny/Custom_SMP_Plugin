package io.sniperjohnny.github.custom_smp_plugin.pvp_logic.commands;

import io.sniperjohnny.github.custom_smp_plugin.Custom_SMP_Plugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class See_Lives_Command implements TabExecutor {
    Custom_SMP_Plugin plugin;
    public See_Lives_Command(Custom_SMP_Plugin instance) {
        plugin = instance;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] args) {
        if(!(sender instanceof final Player p)){
            sender.sendMessage("Only Players can execute this command!");
            return true;
        }
        if(!(p.hasPermission("pvp.seeotherplayerslives"))) {
            if(!(p.isOp())) {
                Object currentLivesObj = plugin.getPlayerData(p.getUniqueId(), "lives");
                p.sendMessage(currentLivesObj.toString() + " lives remaining!");
                return true;
            }
        }
        String playerName = args[0];
        Player target = Bukkit.getPlayerExact(playerName);
        if (target == null) {
            p.sendMessage(ChatColor.RED + "Player not found.");
            return true;
        }
        Object currentLivesObjothers = plugin.getPlayerData(target.getUniqueId(), "lives");
        p.sendMessage(args[0].toString() + "has" + currentLivesObjothers.toString() + "lives Remaining");
        return true;
    }
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        List<String> completions = new ArrayList<>();


        if(args.length == 1) {

            List<String> players = new ArrayList<>();
            for (Player p : Bukkit.getOnlinePlayers()) {
                players.add(p.getName());
            }
            StringUtil.copyPartialMatches(args[0], players, completions);
            return completions;
        }
        return Collections.emptyList();
    }
}
