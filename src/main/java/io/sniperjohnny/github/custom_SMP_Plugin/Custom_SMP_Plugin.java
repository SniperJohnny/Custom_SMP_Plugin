package io.sniperjohnny.github.custom_SMP_Plugin;

import io.sniperjohnny.github.custom_SMP_Plugin.cuality_of_life.commands.Fly_Command;
import io.sniperjohnny.github.custom_SMP_Plugin.cuality_of_life.commands.Smite_Command;
import io.sniperjohnny.github.custom_SMP_Plugin.cuality_of_life.commands.Unban_Command;
import io.sniperjohnny.github.custom_SMP_Plugin.cuality_of_life.listeners.Join_listener;
import io.sniperjohnny.github.custom_SMP_Plugin.pvp_logic.Commands.Revive_Beacon_recipe_shower_command;
import io.sniperjohnny.github.custom_SMP_Plugin.pvp_logic.Commands.Revive_Command;
import io.sniperjohnny.github.custom_SMP_Plugin.pvp_logic.listeners.InventoryListener_pvp;
import io.sniperjohnny.github.custom_SMP_Plugin.pvp_logic.listeners.Kill_Listener;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public final class Custom_SMP_Plugin extends JavaPlugin implements Listener {
    private static Custom_SMP_Plugin instance = null;
    public void savePlayerData(UUID playerUUID, String key, Object value) {
        // Locate the player's file
        File playerFile = new File(getDataFolder() + File.separator + "players", playerUUID + ".yml");

        // Load the YAML configuration from that file
        YamlConfiguration config = YamlConfiguration.loadConfiguration(playerFile);

        // Set the data (e.g., "kills", 10)
        config.set(key, value);

        // Save the changes back to the disk
        try {
            config.save(playerFile);
        } catch (IOException e) {
            getLogger().severe("Could not save data for player " + playerUUID);
            e.printStackTrace();
        }
    }

    public Object getPlayerData(UUID playerUUID, String key) {
        File playerFile = new File(getDataFolder() + File.separator + "players", playerUUID + ".yml");
        YamlConfiguration config = YamlConfiguration.loadConfiguration(playerFile);

        // Returns the value, or null if it doesn't exist
        return config.get(key);
    }
    @EventHandler
    public void playeryamlfilecreator(PlayerJoinEvent e) {
        // Create the "players" folder if it doesn't exist
        File folder = new File(getDataFolder() + File.separator + "players");
        if (!folder.exists()) {
            folder.mkdirs(); // mkdirs() is safer as it creates parent folders too
        }

        // Create the player's specific YAML file
        File playerFile = new File(folder, e.getPlayer().getUniqueId() + ".yml");
        if (!playerFile.exists()) {
            try {
                playerFile.createNewFile();
            } catch (IOException ex) {
                getLogger().severe("Could not create configuration file for " + e.getPlayer().getName());
                ex.printStackTrace();
            }
        }
        YamlConfiguration config = YamlConfiguration.loadConfiguration(playerFile);
        Object defaultAmountOfLives = getConfig().getInt("pvp.default_lives");
        savePlayerData(e.getPlayer().getUniqueId(), "lives", defaultAmountOfLives);
        savePlayerData(e.getPlayer().getUniqueId(), "kills", 0);
        try {
            config.save(playerFile);
        } catch (IOException exept) {
            getLogger().severe("Could not save data for player.");
            exept.printStackTrace();
        }
    }

    @Override
    public void onEnable() {
        instance = this;

        // Save default config
        saveDefaultConfig();

        // Register Events
        getServer().getPluginManager().registerEvents(this, this); // Registers THIS class for the event
        getServer().getPluginManager().registerEvents(new Join_listener(this), this);
        getServer().getPluginManager().registerEvents(new Kill_Listener(this), this);
        getServer().getPluginManager().registerEvents(new InventoryListener_pvp(), this);

        // Register Commands
        getCommand("fly").setExecutor(new Fly_Command());
        getCommand("smite").setExecutor(new Smite_Command());
        getCommand("unban").setExecutor(new Unban_Command());
        getCommand("revive").setExecutor(new Revive_Command());
        getCommand("revivebeaconrecipe").setExecutor(new Revive_Beacon_recipe_shower_command());

        getLogger().info("Custom_SMP_Plugin_started");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public static Custom_SMP_Plugin get_Instance() {
        return instance;
    }
}