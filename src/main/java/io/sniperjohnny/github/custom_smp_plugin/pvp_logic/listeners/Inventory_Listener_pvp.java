package io.sniperjohnny.github.custom_smp_plugin.pvp_logic.listeners;

import io.sniperjohnny.github.custom_smp_plugin.pvp_logic.menu_Pvp_important.Menu_pvp;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;


public class Inventory_Listener_pvp implements Listener {
    @EventHandler
    public void onClick(InventoryClickEvent e) {
        final Inventory clickedinv = e.getClickedInventory();

        if(clickedinv == null) {
            return;
        }
        if(!(clickedinv.getHolder() instanceof Menu_pvp menu)) {
            return;
        }
        e.setCancelled(true);
        menu.click((Player) e.getWhoClicked(), e.getSlot());
    }
}
