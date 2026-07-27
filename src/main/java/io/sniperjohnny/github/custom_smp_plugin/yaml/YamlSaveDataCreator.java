package io.sniperjohnny.github.custom_smp_plugin.yaml;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public class YamlSaveDataCreator {
    private final JavaPlugin plugin;
    private final File playersFolder;
    private final Function<UUID, File> fileResolver;
    private final Consumer<Exception> errorHandler;
    private final BiConsumer<UUID, String> saveLogger;

    private YamlSaveDataCreator(Builder builder) {
        this.plugin = builder.plugin;
        this.playersFolder = builder.playersFolder;
        this.fileResolver = builder.fileResolver;
        this.errorHandler = builder.errorHandler;
        this.saveLogger = builder.saveLogger;
    }

    public void savePlayerData(UUID playerUUID, String key, Object value) {
        File playerFile = fileResolver.apply(playerUUID);
        
        YamlConfiguration config = YamlConfiguration.loadConfiguration(playerFile);
        config.set(key, value);
        
        try {
            config.save(playerFile);
            if (saveLogger != null) {
                saveLogger.accept(playerUUID, key);
            }
        } catch (IOException e) {
            if (errorHandler != null) {
                errorHandler.accept(e);
            } else {
                plugin.getLogger().severe("Could not save data for player " + playerUUID + " (key: " + key + ")");
                e.printStackTrace();
            }
        }
    }

    public void savePlayerData(Player player, String key, Object value) {
        savePlayerData(player.getUniqueId(), key, value);
    }

    public void savePlayerData(UUID playerUUID, String key, Object value, Consumer<Exception> customErrorHandler) {
        File playerFile = fileResolver.apply(playerUUID);
        
        YamlConfiguration config = YamlConfiguration.loadConfiguration(playerFile);
        config.set(key, value);
        
        try {
            config.save(playerFile);
            if (saveLogger != null) {
                saveLogger.accept(playerUUID, key);
            }
        } catch (IOException e) {
            if (customErrorHandler != null) {
                customErrorHandler.accept(e);
            } else if (errorHandler != null) {
                errorHandler.accept(e);
            } else {
                plugin.getLogger().severe("Could not save data for player " + playerUUID + " (key: " + key + ")");
                e.printStackTrace();
            }
        }
    }

    public void savePlayerDataBulk(UUID playerUUID, java.util.Map<String, Object> dataMap) {
        File playerFile = fileResolver.apply(playerUUID);
        
        YamlConfiguration config = YamlConfiguration.loadConfiguration(playerFile);
        for (java.util.Map.Entry<String, Object> entry : dataMap.entrySet()) {
            config.set(entry.getKey(), entry.getValue());
        }
        
        try {
            config.save(playerFile);
            if (saveLogger != null) {
                dataMap.forEach((key, value) -> saveLogger.accept(playerUUID, key));
            }
        } catch (IOException e) {
            if (errorHandler != null) {
                errorHandler.accept(e);
            } else {
                plugin.getLogger().severe("Could not save bulk data for player " + playerUUID);
                e.printStackTrace();
            }
        }
    }

    public void savePlayerDataBulk(Player player, java.util.Map<String, Object> dataMap) {
        savePlayerDataBulk(player.getUniqueId(), dataMap);
    }

    public Object getPlayerData(UUID playerUUID, String key) {
        File playerFile = fileResolver.apply(playerUUID);
        YamlConfiguration config = YamlConfiguration.loadConfiguration(playerFile);
        return config.get(key);
    }

    public Object getPlayerData(Player player, String key) {
        return getPlayerData(player.getUniqueId(), key);
    }

    public <T> T getPlayerData(UUID playerUUID, String key, T defaultValue) {
        Object value = getPlayerData(playerUUID, key);
        if (value == null) {
            return defaultValue;
        }
        try {
            return (T) value;
        } catch (ClassCastException e) {
            return defaultValue;
        }
    }

    public <T> T getPlayerData(Player player, String key, T defaultValue) {
        return getPlayerData(player.getUniqueId(), key, defaultValue);
    }

    public YamlConfiguration loadPlayerConfig(UUID playerUUID) {
        File playerFile = fileResolver.apply(playerUUID);
        return YamlConfiguration.loadConfiguration(playerFile);
    }

    public YamlConfiguration loadPlayerConfig(Player player) {
        return loadPlayerConfig(player.getUniqueId());
    }

    public void deletePlayerData(UUID playerUUID) {
        File playerFile = fileResolver.apply(playerUUID);
        if (playerFile.exists() && !playerFile.delete()) {
            plugin.getLogger().warning("Could not delete player data file for " + playerUUID);
        }
    }

    public void deletePlayerData(Player player) {
        deletePlayerData(player.getUniqueId());
    }

    public boolean playerDataExists(UUID playerUUID) {
        return fileResolver.apply(playerUUID).exists();
    }

    public boolean playerDataExists(Player player) {
        return playerDataExists(player.getUniqueId());
    }

    public File getPlayerFile(UUID playerUUID) {
        return fileResolver.apply(playerUUID);
    }

    public File getPlayerFile(Player player) {
        return getPlayerFile(player.getUniqueId());
    }

    public JavaPlugin getPlugin() {
        return plugin;
    }

    public File getPlayersFolder() {
        return playersFolder;
    }

    public static class Builder {
        private JavaPlugin plugin;
        private File playersFolder;
        private Function<UUID, File> fileResolver;
        private Consumer<Exception> errorHandler;
        private BiConsumer<UUID, String> saveLogger;

        public Builder(JavaPlugin plugin) {
            this.plugin = plugin;
            this.playersFolder = new File(plugin.getDataFolder(), "players");
            this.fileResolver = uuid -> new File(playersFolder, uuid + ".yml");
        }

        public Builder playersFolder(File folder) {
            this.playersFolder = folder;
            this.fileResolver = uuid -> new File(folder, uuid + ".yml");
            return this;
        }

        public Builder playersFolder(String folderName) {
            this.playersFolder = new File(plugin.getDataFolder(), folderName);
            this.fileResolver = uuid -> new File(playersFolder, uuid + ".yml");
            return this;
        }

        public Builder fileResolver(Function<UUID, File> resolver) {
            this.fileResolver = resolver;
            return this;
        }

        public Builder errorHandler(Consumer<Exception> handler) {
            this.errorHandler = handler;
            return this;
        }

        public Builder saveLogger(BiConsumer<UUID, String> logger) {
            this.saveLogger = logger;
            return this;
        }

        public YamlSaveDataCreator build() {
            return new YamlSaveDataCreator(this);
        }
    }
}