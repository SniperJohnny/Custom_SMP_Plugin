package io.sniperjohnny.github.custom_smp_plugin.pvp_logic.listeners;

import io.sniperjohnny.github.custom_smp_plugin.Custom_SMP_Plugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Kill_Listener implements Listener {
    public int potionEffectTime = 600;
    private final Custom_SMP_Plugin plugin;

    public Kill_Listener(Custom_SMP_Plugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {
        final Player deadPlayer = e.getPlayer();

        // Safe way to get the killer in modern Paper versions
        Player killer = deadPlayer.getKiller();
        if (killer == null) return; // If there is no player killer, skip the logic

        if (plugin.getConfig().getBoolean("pvp.kill_bonus")) {
            Location deathLocation = deadPlayer.getLocation();

            // Apply bonuses to the killer
            killer.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, potionEffectTime, 1));
            killer.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, potionEffectTime, 1));
            killer.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, potionEffectTime, 1));
            killer.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, potionEffectTime, 1));

            killer.getWorld().playSound(killer.getLocation(), Sound.MUSIC_DISC_PIGSTEP, 5f, 1f);
            deadPlayer.getWorld().strikeLightningEffect(deathLocation);
            deadPlayer.getWorld().playSound(deathLocation, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 10f, 0.9f);

            // Handle Lives System

    }
        Object currentLivesObj = plugin.getPlayerData(deadPlayer.getUniqueId(), "lives");
        int currentRemainingLives = (currentLivesObj != null) ? (int) currentLivesObj : plugin.getConfig().getInt("pvp.default_lives");
        int newLives = currentRemainingLives - 1;
        deadPlayer.sendMessage("You have " + newLives + " remaining." + ChatColor.RED);

        plugin.savePlayerData(deadPlayer.getUniqueId(), "lives", newLives);

        // Build Weapon Component Safely
        ItemStack weapon = killer.getInventory().getItemInMainHand();
        Component weaponComponent;

        if (weapon.getType().isAir()) {
            weaponComponent = Component.text("their bare hands", NamedTextColor.GOLD);
        } else if (weapon.hasItemMeta() && weapon.getItemMeta().hasDisplayName()) {
            weaponComponent = weapon.getItemMeta().displayName(); // Grabs actual modern Component
        } else {
            // If it doesn't have a custom name, use its translation key (e.g. "Diamond Sword")
            weaponComponent = Component.translatable(weapon.getType().translationKey(), NamedTextColor.GOLD);
        }

        // Add the item hover event safely
        if (!weapon.getType().isAir()) {
            weaponComponent = weaponComponent.hoverEvent(weapon.asHoverEvent());
        }

        // Disable standard vanilla death message so your custom one takes over
        e.deathMessage(null);

        if (newLives <= 0) {
            // Ban and Kick Logic
            if (deadPlayer.getAddress() != null) {
                String ipAddress = deadPlayer.getAddress().getAddress().getHostAddress();
                String reasonLegacy = "§4§n§lYou ran out of Lives and are therefore banned, wait for getting revived by a teammate or an Admin";

                // Paper modern Ban API usage
                Bukkit.getBanList(BanList.Type.NAME).addBan(deadPlayer.getName(), reasonLegacy, null, "Console_smpPlugin");
            }

            Component kickMessage = LegacyComponentSerializer.legacyAmpersand().deserialize("&4&n&lYou ran out of Lives and are therefore banned, wait for getting revived by a teammate or an Admin");
            deadPlayer.kick(kickMessage); // Modern API alternative to kickPlayer()

            // Global Ban Death Message
            Component banDeathMessage = Component.text(deadPlayer.getName(), NamedTextColor.DARK_RED)
                    .append(Component.text(" has been slain by ", NamedTextColor.GRAY))
                    .append(Component.text(killer.getName(), NamedTextColor.DARK_RED))
                    .append(Component.text(" using ", NamedTextColor.GRAY))
                    .append(weaponComponent)
                    .append(Component.text(". He has lost all of his Lives and is therefore banned!", NamedTextColor.GRAY));

            Bukkit.getServer().sendMessage(banDeathMessage);
        } else {
            // Normal Death Message
            Component regularDeathMessage = Component.text(deadPlayer.getName(), NamedTextColor.DARK_RED)
                    .append(Component.text(" has been slain by ", NamedTextColor.GRAY))
                    .append(Component.text(killer.getName(), NamedTextColor.DARK_RED))
                    .append(Component.text(" using ", NamedTextColor.GRAY))
                    .append(weaponComponent);

            Bukkit.getServer().sendMessage(regularDeathMessage);
        }

        // Update Killer Stats
        Object currentKillsObj = plugin.getPlayerData(killer.getUniqueId(), "kills");
        int currentKills = (currentKillsObj != null) ? (int) currentKillsObj : 0;
        plugin.savePlayerData(killer.getUniqueId(), "kills", currentKills + 1);
    }
}