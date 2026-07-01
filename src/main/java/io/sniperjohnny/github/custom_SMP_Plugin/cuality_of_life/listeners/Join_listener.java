package io.sniperjohnny.github.custom_SMP_Plugin.cuality_of_life.listeners;

import io.sniperjohnny.github.custom_SMP_Plugin.Custom_SMP_Plugin;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.io.File;

public class Join_listener implements Listener {
    public int Effect_time_on_join = 600;
    private final Custom_SMP_Plugin plugin;
    public Join_listener(Custom_SMP_Plugin plugin) {
        this.plugin = plugin;
    }



    @EventHandler
    public void onPlayerJoin_message_swapper(PlayerJoinEvent e){
        String join_message = this.plugin.getConfig().getString("join.message");
        join_message = join_message.replace("&player&", e.getPlayer().getDisplayName());
        if (e.getPlayer().isOp()) {
            e.getPlayer().sendMessage(ChatColor.RED + "Welcome " + e.getPlayer().getDisplayName() + ", thanks for joining and downloading this plugin!");
        }
        if (join_message != null) {

            if (join_message.equalsIgnoreCase("0")) {
                e.setJoinMessage("");
            } else if (join_message.equalsIgnoreCase("")) {
                e.setJoinMessage(e.getPlayer().getDisplayName() + " has joined the game" + ChatColor.YELLOW);
            }
            else {
                e.setJoinMessage(ChatColor.YELLOW + join_message);
            }
        }
    }

    @EventHandler
    public void what_should_be_done_on_join(PlayerJoinEvent e){
        Player p = e.getPlayer();
        Boolean should_be_feeded = plugin.getConfig().getBoolean("join.feed");
        Boolean should_be_tped_to_spawn = plugin.getConfig().getBoolean("join.tpspawn");
        Boolean should_be_set_to_full_health = plugin.getConfig().getBoolean("join.setfullhealth");
        Boolean should_be_given_speed_for_30_seconds = plugin.getConfig().getBoolean("join.give.speed.for.30.seconds");
        if (should_be_feeded) {
            p.setFoodLevel(800);
        }
        if (should_be_set_to_full_health) {
            p.setHealth(20);
        }
        if (should_be_given_speed_for_30_seconds){
            p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Effect_time_on_join, 1));
        }

    }

}
