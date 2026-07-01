package io.sniperjohnny.github.custom_SMP_Plugin.pvp_logic.listeners;

import io.sniperjohnny.github.custom_SMP_Plugin.Custom_SMP_Plugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.SpawnCategory;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import javax.inject.Named;
import java.awt.*;

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
        final Player p = e.getPlayer();
        if (entity instanceof final Player source) {
            if (plugin.getConfig().getBoolean("pvp.kill_bonus")) {
                source.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, PotionEffectTime, 1));
                source.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, PotionEffectTime, 1));
                source.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, PotionEffectTime, 1));
                source.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, PotionEffectTime, 1));
                source.getWorld().playSound(source.getLocation(), Sound.MUSIC_DISC_PIGSTEP, 5f, 1f);
                e.getPlayer().getWorld().strikeLightningEffect(PlayerDeathLocation);
                e.getPlayer().getWorld().playSound(PlayerDeathLocation, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 10f, 0.9f);

                Object currentLivesObj = plugin.getPlayerData(e.getPlayer().getUniqueId(), "lives");
                int currentRemainingLives = (currentLivesObj!= null) ? (int) currentLivesObj :0;

                plugin.savePlayerData(e.getPlayer().getUniqueId(), "lives",currentRemainingLives - 1);

                ItemStack weapon = p.getInventory().getItemInMainHand();
                Component message = Component.text(p.getName(), NamedTextColor.DARK_RED)
                        .append(Component.text(" has been slayn by " + source.getDisplayName() + " using ", NamedTextColor.GRAY))
                        .append(
                                Component.text(weapon.getItemMeta().getDisplayName(), NamedTextColor.DARK_RED)
                                        .hoverEvent(weapon.asHoverEvent())
                        );

                Bukkit.getServer().sendMessage(message);


                if(currentRemainingLives - 1 <= 0) {
                    String ipAddress = e.getPlayer().getAddress().getAddress().getHostAddress();
                    String reason = "&4&n&lYou ran out of Lives and are therefore banned, wait for getting revived by a teammate or an Admin";

                    Bukkit.getBanList(BanList.Type.IP).addBan(
                            ipAddress,
                            reason,
                            null,
                            "&kConsole_smpPlugin"
                    );
                    Component kickmessage = LegacyComponentSerializer.legacyAmpersand().deserialize(reason);
                    e.getPlayer().kickPlayer(String.valueOf(kickmessage));
                    Component ban_deathmessage = Component.text(p.getName(), NamedTextColor.DARK_RED)
                            .append(Component.text(" has been slayn by " + source.getDisplayName() + " using ", NamedTextColor.GRAY))
                            .append(
                                    Component.text(weapon.getItemMeta().getDisplayName(), NamedTextColor.DARK_RED)
                                            .hoverEvent(weapon.asHoverEvent())
                            )
                            .append(Component.text(". he has lost all of his Lives and is therefore banned!", NamedTextColor.GRAY));

                    Bukkit.getServer().sendMessage(ban_deathmessage);
                }




                Object currentKillsObj = plugin.getPlayerData(source.getUniqueId(), "kills");
                int currentKills = (currentKillsObj != null) ? (int) currentKillsObj : 0;

                // 2. Add 1 and save it back
                plugin.savePlayerData(source.getUniqueId(), "kills", currentKills + 1);



            }

        }
    }
}
