package io.sniperjohnny.github.custom_SMP_Plugin.pvp_logic.Commands;

import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Revive_Command implements TabExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        String playerName = args[0];
        BanList banList = Bukkit.getBanList(BanList.Type.PROFILE);
        if (!(banList.isBanned(playerName))) {
            sender.sendMessage(ChatColor.RED + "Player is not dead.");
            return true;
        }

        // Check if the player is actually banned
        if (banList.isBanned(playerName)) {
            // Remove them from the ban list
            banList.pardon(playerName);
            sender.sendMessage("Player has been revived.");
        }

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
                StringUtil.copyPartialMatches(args[1], players, completions);
                return completions;
            }
            return Collections.emptyList();
        }
    }

