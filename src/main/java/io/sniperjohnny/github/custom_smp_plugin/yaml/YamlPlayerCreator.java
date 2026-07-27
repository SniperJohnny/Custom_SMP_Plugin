package io.sniperjohnny.github.custom_smp_plugin.yaml;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.function.Function;

public class YamlPlayerCreator {
    private final JavaPlugin plugin;
    private final File playersFolder;
    private final Function<Player, Integer> defaultLivesProvider;
    private final int defaultKills;

    public YamlPlayerCreator(JavaPlugin plugin, String playersFolderName, int defaultLives, int defaultKills) {
        this.plugin = plugin;
        this.playersFolder = new File(plugin.getDataFolder(), playersFolderName);
        this.defaultLivesProvider = player -> defaultLives;
        this.defaultKills = defaultKills;
    }

    public YamlPlayerCreator(JavaPlugin plugin, String playersFolderName, Function<Player, Integer> defaultLivesProvider, int defaultKills) {
        this.plugin = plugin;
        this.playersFolder = new File(plugin.getDataFolder(), playersFolderName);
        this.defaultLivesProvider = defaultLivesProvider;
        this.defaultKills = defaultKills;
    }

    public void ensurePlayersFolderExists() {
        if (!playersFolder.exists()) {
            playersFolder.mkdirs();
        }
    }

    public void createPlayerFileIfNotExists(Player player) {
        ensurePlayersFolderExists();
        
        File playerFile = new File(playersFolder, player.getUniqueId() + ".yml");
        
        if (!playerFile.exists()) {
            try {
                playerFile.createNewFile();
                initializePlayerData(player, playerFile);
            } catch (Exception e) {
                plugin.getLogger().severe("Could not create configuration file for " + player.getName());
                e.printStackTrace();
            }
        }
    }

    private void initializePlayerData(Player player, File playerFile) {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(playerFile);
        
        int lives = defaultLivesProvider.apply(player);
        config.set("lives", lives);
        config.set("kills", defaultKills);
        
        try {
            config.save(playerFile);
            plugin.getLogger().info("Created new player data file for " + player.getName() + " with " + lives + " lives and " + defaultKills + " kills");
        } catch (Exception e) {
            plugin.getLogger().severe("Could not save initial data for player " + player.getName());
            e.printStackTrace();
        }
    }

    public File getPlayerFile(UUID playerUUID) {
        return new File(playersFolder, playerUUID + ".yml");
    }

    public File getPlayerFile(Player player) {
        return getPlayerFile(player.getUniqueId());
    }

    public boolean playerFileExists(UUID playerUUID) {
        return getPlayerFile(playerUUID).exists();
    }

    public boolean playerFileExists(Player player) {
        return playerFileExists(player.getUniqueId());
    }

    public YamlConfiguration loadPlayerConfig(UUID playerUUID) {
        File playerFile = getPlayerFile(playerUUID);
        return YamlConfiguration.loadConfiguration(playerFile);
    }

    public YamlConfiguration loadPlayerConfig(Player player) {
        return loadPlayerConfig(player.getUniqueId());
    }

    public JavaPlugin getPlugin() {
        return plugin;
    }

    public File getPlayersFolder() {
        return playersFolder;
    }

    public static class Builder {
        private JavaPlugin plugin;
        private String playersFolderName = "players";
        private Function<Player, Integer> defaultLivesProvider = player -> 3;
        private int defaultKills = 0;

        public Builder(JavaPlugin plugin) {
            this.plugin = plugin;
        }

        public Builder playersFolderName(String folderName) {
            this.playersFolderName = folderName;
            return this;
        }

        public Builder defaultLives(int defaultLives) {
            this.defaultLivesProvider = player -> defaultLives;
            return this;
        }

        public Builder defaultLivesProvider(Function<Player, Integer> provider) {
            this.defaultLivesProvider = provider;
            return this;
        }

        public Builder defaultKills(int defaultKills) {
            this.defaultKills = defaultKills;
            return this;
        }

        public YamlPlayerCreator build() {
            return new YamlPlayerCreator(plugin, playersFolderName, defaultLivesProvider, defaultKills);
        }
    }
}