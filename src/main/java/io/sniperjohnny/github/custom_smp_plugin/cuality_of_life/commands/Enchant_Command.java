package io.sniperjohnny.github.custom_smp_plugin.cuality_of_life.commands;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.KeyPattern;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Enchant_Command implements TabExecutor {
    @NotNull
    private static Enchantment customgetEnchantment(@NotNull @KeyPattern.Value String key) {
        return RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT).getOrThrow(Key.key(Key.MINECRAFT_NAMESPACE, key));
    }
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if(!(sender instanceof final Player p)) {
            sender.sendMessage("You need to be an Player to use this Command.");
            return true;
        }

        if (p.getInventory().getItemInMainHand() == null || p.getInventory().getItemInMainHand().getType() == Material.AIR) {
            p.sendMessage(ChatColor.DARK_RED + "You need to hold an item in your Main hand to use this Command!");
            return true;
        }
        ItemStack currentEnchatedItem = p.getInventory().getItemInMainHand();
        ItemMeta currentItemMeta = currentEnchatedItem.getItemMeta();
        if(args[1] == null) {
            currentItemMeta.addEnchant(customgetEnchantment(args[0].toString()), 1, false);
            currentEnchatedItem.setItemMeta(currentItemMeta);
            p.setItemInHand(currentEnchatedItem);
            return true;
        }
        int enchantmentlevel;

        try {
            enchantmentlevel = Integer.parseInt(args[1]);


            currentItemMeta.addEnchant(customgetEnchantment(args[0]), enchantmentlevel, true);
            currentEnchatedItem.setItemMeta(currentItemMeta);
            p.setItemInHand(currentEnchatedItem);
            return true;
        } catch (NumberFormatException e) {
            p.sendMessage("You need to enter an Number here not something else");
            return true;
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender,
                                                @NotNull Command command,
                                                @NotNull String label,
                                                @NotNull String[] args) {

        if (args.length == 1) {
            List<String> completions = new ArrayList<>();

            var registry = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT);

            for (var enchantment : registry) {
                completions.add(enchantment.key().value());
            }

            return org.bukkit.util.StringUtil.copyPartialMatches(args[0], completions, new ArrayList<>());
        }

        return Collections.emptyList();
    }
}
