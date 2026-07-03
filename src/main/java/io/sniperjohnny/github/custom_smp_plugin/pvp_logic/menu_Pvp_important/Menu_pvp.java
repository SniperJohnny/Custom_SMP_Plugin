package io.sniperjohnny.github.custom_smp_plugin.pvp_logic.menu_Pvp_important;

import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.function.Consumer;

public interface Menu_pvp extends InventoryHolder {
    void click(Player p, int slot);

    void setItem(int slot, ItemStack item);
    void setItem(int slot, ItemStack item, Consumer<Player> action);

    void onSetItems();

    default void open(Player p) {
        onSetItems();
        p.openInventory(getInventory());
    }


}

