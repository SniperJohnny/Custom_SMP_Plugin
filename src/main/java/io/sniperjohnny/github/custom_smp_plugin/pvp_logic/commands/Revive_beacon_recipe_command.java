package io.sniperjohnny.github.custom_smp_plugin.pvp_logic.commands;

import io.sniperjohnny.github.custom_smp_plugin.pvp_logic.gui_pvp.Revive_beacon_recipe_shower;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

// Der Klassenname MUSS exakt so heißen wie die .java Datei!
public class Revive_beacon_recipe_command implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!(sender instanceof final Player p)) {
            sender.sendMessage(ChatColor.DARK_RED + "this command is only for Players");
            return true;
        }

        new Revive_beacon_recipe_shower().open(p);
        return true;
    }
}