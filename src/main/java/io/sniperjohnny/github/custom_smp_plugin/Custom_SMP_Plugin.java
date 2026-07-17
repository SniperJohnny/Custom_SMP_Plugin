package io.sniperjohnny.github.custom_smp_plugin;

import io.sniperjohnny.github.custom_smp_plugin.cuality_of_life.commands.Enchant_Command;
import io.sniperjohnny.github.custom_smp_plugin.cuality_of_life.commands.Gamemode_Command;
import io.sniperjohnny.github.custom_smp_plugin.cuality_of_life.commands.Fly_Command;
import io.sniperjohnny.github.custom_smp_plugin.cuality_of_life.commands.Smite_Command;
import io.sniperjohnny.github.custom_smp_plugin.cuality_of_life.commands.Unban_Command;
import io.sniperjohnny.github.custom_smp_plugin.cuality_of_life.listeners.Join_listener;
import io.sniperjohnny.github.custom_smp_plugin.cuality_of_life.listeners.Leave_Listener;
import io.sniperjohnny.github.custom_smp_plugin.pvp_logic.commands.Revive_beacon_recipe_command;
import io.sniperjohnny.github.custom_smp_plugin.pvp_logic.commands.Revive_Command;
import io.sniperjohnny.github.custom_smp_plugin.pvp_logic.commands.SeeLivesCommand;
import io.sniperjohnny.github.custom_smp_plugin.pvp_logic.listeners.InventoryListener_pvp;
import io.sniperjohnny.github.custom_smp_plugin.pvp_logic.listeners.Kill_Listener;
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
        File folder = new File(getDataFolder() + File.separator + "players");
        if (!folder.exists()) {
            folder.mkdirs();
        }

        File playerFile = new File(folder, e.getPlayer().getUniqueId() + ".yml");

        // Check if it's a completely new player profile
        if (!playerFile.exists()) {
            try {
                playerFile.createNewFile();

                // ONLY set default stats here, inside the fresh creation block!
                int defaultAmountOfLives = getConfig().getInt("pvp.default_lives", 3);
                savePlayerData(e.getPlayer().getUniqueId(), "lives", defaultAmountOfLives);
                savePlayerData(e.getPlayer().getUniqueId(), "kills", 0);

            } catch (IOException ex) {
                getLogger().severe("Could not create configuration file for " + e.getPlayer().getName());
                ex.printStackTrace();
            }
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
        getServer().getPluginManager().registerEvents(new Leave_Listener(this), this);

        // Register Commands
        getCommand("fly").setExecutor(new Fly_Command());
        getCommand("smite").setExecutor(new Smite_Command());
        getCommand("revive").setExecutor(new Revive_Command());
        getCommand("revivebeaconrecipe").setExecutor(new Revive_beacon_recipe_command());
        getCommand("lives").setExecutor(new SeeLivesCommand(this));
        if(this.getConfig().getBoolean("qol.revamp_normal_commands")) {
            getCommand("gm").setExecutor(new Gamemode_Command());
            getCommand("unban").setExecutor(new Unban_Command());
            getCommand("enchant").setExecutor(new Enchant_Command());
        }

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