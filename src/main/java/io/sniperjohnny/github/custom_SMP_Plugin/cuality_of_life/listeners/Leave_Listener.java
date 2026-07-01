package io.sniperjohnny.github.custom_SMP_Plugin.cuality_of_life.listeners;

import io.sniperjohnny.github.custom_SMP_Plugin.Custom_SMP_Plugin;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerQuitEvent;

import java.net.http.WebSocket;

public class Leave_Listener implements WebSocket.Listener {
    Custom_SMP_Plugin plugin = new Custom_SMP_Plugin();
    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent e) {
        String quitmessagebluprint = this.plugin.getConfig().getString("leave.message");
        String quit_message = quitmessagebluprint.replace("&player&", e.getPlayer().getDisplayName());
        if (e.getPlayer().isOp()) {
            e.getPlayer().sendMessage(ChatColor.RED + "Welcome " + e.getPlayer().getDisplayName() + ", thanks for joining and downloading this plugin!");
        }
        if (quit_message != null) {

            if (quit_message.equalsIgnoreCase("0")) {
                e.setQuitMessage("");
            } else if (quit_message.equalsIgnoreCase("")) {
                e.setQuitMessage(e.getPlayer().getDisplayName() + " has left the game."+ ChatColor.YELLOW);
            }
            else {
                e.setQuitMessage(ChatColor.YELLOW + quit_message);
            }
        }
    }


}
