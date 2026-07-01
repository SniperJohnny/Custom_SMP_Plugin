package io.sniperjohnny.github.custom_SMP_Plugin.pvp_logic.Commands;

import io.sniperjohnny.github.custom_SMP_Plugin.Custom_SMP_Plugin;
import io.sniperjohnny.github.custom_SMP_Plugin.pvp_logic.menu_Pvp_important.SimpleMenu_pvp;
import org.bukkit.*;
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
    Custom_SMP_Plugin plugin =
            Custom_SMP_Plugin.get_Instance();
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        String playerName = args[0];
        BanList banList = Bukkit.getBanList(BanList.Type.IP);
        if (!(banList.isBanned(playerName))) {
            sender.sendMessage(ChatColor.RED + "Player is not dead.");
            return true;
        }

        // Check if the player is actually banned
        if (banList.isBanned(playerName)) {
            // Remove them from the ban list
            banList.pardon(playerName);
            sender.sendMessage("Player has been revived.");
            OfflinePlayer unbannedPlayer = Bukkit.getOfflinePlayer(playerName);
            plugin.savePlayerData(unbannedPlayer.getUniqueId(), "lives", 3);
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

