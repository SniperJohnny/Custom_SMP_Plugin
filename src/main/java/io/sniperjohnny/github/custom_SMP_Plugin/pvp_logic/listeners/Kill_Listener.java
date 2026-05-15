package io.sniperjohnny.github.custom_SMP_Plugin.pvp_logic.listeners;

import io.sniperjohnny.github.custom_SMP_Plugin.Custom_SMP_Plugin;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.SpawnCategory;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Kill_Listener implements Listener {
    public int PotionEffectTime = 600;
    private final Custom_SMP_Plugin plugin;
    public Kill_Listener(Custom_SMP_Plugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void Playerdied(PlayerDeathEvent e) {
        Location PlayerDeathLocation = e.getPlayer().getLocation();
        Entity entity =e.getDamageSource().getCausingEntity();
        if (entity instanceof final Player source) {
            if (plugin.getConfig().getBoolean("pvp.kill_bonus")) {
                source.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, PotionEffectTime, 1));
                source.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, PotionEffectTime, 1));
                source.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, PotionEffectTime, 1));
                source.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, PotionEffectTime, 1));

            }

            e.getPlayer().getWorld().strikeLightning(PlayerDeathLocation);
        }
    }
}
