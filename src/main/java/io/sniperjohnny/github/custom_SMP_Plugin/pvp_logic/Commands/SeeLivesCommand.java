package io.sniperjohnny.github.custom_smp_plugin.pvp_logic.commands;

import io.sniperjohnny.github.custom_smp_plugin.Custom_SMP_Plugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class SeeLivesCommand implements CommandExecutor {
    Custom_SMP_Plugin plugin;
    public SeeLivesCommand(Custom_SMP_Plugin instance) {
        plugin = instance;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] args) {
        if(!(sender instanceof final Player p)){
            sender.sendMessage("Only Players can execute this command!");
            return true;
        }

        Object currentLivesObj = plugin.getPlayerData(p.getUniqueId(), "lives");
        p.sendMessage(currentLivesObj.toString() + " lives remaining!");
        return true;
    }
}
