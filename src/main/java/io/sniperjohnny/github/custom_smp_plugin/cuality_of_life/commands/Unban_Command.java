package io.sniperjohnny.github.custom_smp_plugin.cuality_of_life.commands;

import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Unban_Command implements TabExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        String playerName = args[0];
        BanList banList = Bukkit.getBanList(BanList.Type.IP);
        if (!(banList.isBanned(playerName))) {
            sender.sendMessage(ChatColor.RED + "Player not found.");
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


        if (args.length == 1) {
            List<String> bannedNames = new ArrayList<>();

            // 1. Loop through the ban entries (Type.NAME holds player bans)
            for (BanEntry<?> entry : Bukkit.getBanList(BanList.Type.NAME).getEntries()) {
                String targetName = entry.getTarget();

                // 2. If you need the actual Player/OfflinePlayer object for logic:
                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(targetName);

                // 3. Add the name to the completion list
                if (offlinePlayer.getName() != null) {
                    bannedNames.add(offlinePlayer.getName());
                }
            }

            StringUtil.copyPartialMatches(args[0], bannedNames, completions);
            Collections.sort(completions);
            return completions;
        }
        return Collections.emptyList();
    }
}
