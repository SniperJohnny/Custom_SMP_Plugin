package io.sniperjohnny.github.custom_smp_plugin.pvp_logic.commands;

import org.bukkit.BanEntry;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Set_Lives_Command implements TabExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] args) {
        return false;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] args) {
        List<String> completions = new ArrayList<>();


        if (args.length == 1) {
            List<String> players_which_have_lives = new ArrayList<>();

            for (OfflinePlayer p : Bukkit.getOfflinePlayers()) {
                players_which_have_lives.add(p.getName());
            }


            StringUtil.copyPartialMatches(args[0], players_which_have_lives, completions);
            Collections.sort(completions);
            return completions;
        }
        return Collections.emptyList();
    }
}
