package io.sniperjohnny.github.custom_smp_plugin.yaml;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Consumer;
import java.util.function.BiConsumer;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.function.Predicate;

public class YamlPlayerCreator {
    private final JavaPlugin plugin;
    private final File playersFolder;
    private final Function<Player, Integer> defaultLivesProvider;
    private final int defaultKills;
    private final Map<String, Object> defaultValues;
    private final Map<String, Function<Object, Object>> serializers;
    private final Map<String, Function<Object, Object>> deserializers;
    private final Map<String, Predicate<Object>> validators;
    private final Consumer<String> errorLogger;
    private final BiConsumer<UUID, String> saveLogger;
    private final boolean asyncSafe;
    private final boolean createIfMissing;
    private final List<String> protectedKeys;

    private YamlPlayerCreator(Builder builder) {
        this.plugin = builder.plugin;
        this.playersFolder = builder.playersFolder;
        this.defaultLivesProvider = builder.defaultLivesProvider;
        this.defaultKills = builder.defaultKills;
        this.defaultValues = new HashMap<>(builder.defaultValues);
        this.serializers = new HashMap<>(builder.serializers);
        this.deserializers = new HashMap<>(builder.deserializers);
        this.validators = new HashMap<>(builder.validators);
        this.errorLogger = builder.errorLogger;
        this.saveLogger = builder.saveLogger;
        this.asyncSafe = builder.asyncSafe;
        this.createIfMissing = builder.createIfMissing;
        this.protectedKeys = new ArrayList<>(builder.protectedKeys);
        
        // Ensure default lives/kills are in defaultValues if not already present
        if (!this.defaultValues.containsKey("lives")) {
            this.defaultValues.put("lives", 3);
        }
        if (!this.defaultValues.containsKey("kills")) {
            this.defaultValues.put("kills", 0);
        }
    }

    public void ensurePlayersFolderExists() {
        if (!playersFolder.exists()) {
            playersFolder.mkdirs();
        }
    }

    public CompletableFuture<Void> ensurePlayersFolderExistsAsync() {
        return CompletableFuture.runAsync(() -> ensurePlayersFolderExists(), getExecutor());
    }

    public void createPlayerFileIfNotExists(Player player) {
        ensurePlayersFolderExists();
        
        File playerFile = new File(playersFolder, player.getUniqueId() + ".yml");
        
        if (!playerFile.exists()) {
            try {
                playerFile.createNewFile();
                initializePlayerData(player, playerFile);
            } catch (Exception e) {
                logError("Could not create configuration file for " + player.getName());
                e.printStackTrace();
            }
        }
    }

    public CompletableFuture<Void> createPlayerFileIfNotExistsAsync(Player player) {
        return CompletableFuture.runAsync(() -> createPlayerFileIfNotExists(player), getExecutor());
    }

    private void initializePlayerData(Player player, File playerFile) {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(playerFile);
        
        // Apply default values
        for (Map.Entry<String, Object> entry : defaultValues.entrySet()) {
            String key = entry.getKey();
            if (!protectedKeys.contains(key) || !config.contains(key)) {
                Object value = entry.getValue();
                if (value instanceof Function) {
                    value = ((Function<Player, Object>) value).apply(player);
                }
                config.set(key, value);
            }
        }
        
        // Ensure lives and kills are set
        int lives = defaultLivesProvider.apply(player);
        config.set("lives", lives);
        config.set("kills", defaultKills);
        
        try {
            config.save(playerFile);
            if (saveLogger != null) {
                saveLogger.accept(player.getUniqueId(), "initialization");
            }
            plugin.getLogger().info("Created new player data file for " + player.getName() + " with " + lives + " lives and " + defaultKills + " kills");
        } catch (Exception e) {
            logError("Could not save initial data for player " + player.getName());
            e.printStackTrace();
        }
    }

    public void savePlayerData(UUID playerUUID, String key, Object value) {
        if (protectedKeys.contains(key)) {
            logError("Cannot modify protected key: " + key);
            return;
        }
        
        if (validators.containsKey(key) && !validators.get(key).test(value)) {
            logError("Validation failed for key: " + key + " with value: " + value);
            return;
        }
        
        File playerFile = getPlayerFile(playerUUID);
        saveInternal(playerFile, key, value);
    }

    public void savePlayerData(Player player, String key, Object value) {
        savePlayerData(player.getUniqueId(), key, value);
    }

    public void savePlayerData(UUID playerUUID, String key, Object value, Consumer<Exception> customErrorHandler) {
        if (protectedKeys.contains(key)) {
            if (customErrorHandler != null) customErrorHandler.accept(new IllegalStateException("Cannot modify protected key: " + key));
            return;
        }
        
        if (validators.containsKey(key) && !validators.get(key).test(value)) {
            if (customErrorHandler != null) customErrorHandler.accept(new IllegalArgumentException("Validation failed for key: " + key));
            return;
        }
        
        File playerFile = getPlayerFile(playerUUID);
        saveInternal(playerFile, key, value, customErrorHandler);
    }

    public CompletableFuture<Void> savePlayerDataAsync(UUID playerUUID, String key, Object value) {
        return CompletableFuture.runAsync(() -> savePlayerData(playerUUID, key, value), getExecutor());
    }

    public CompletableFuture<Void> savePlayerDataAsync(Player player, String key, Object value) {
        return savePlayerDataAsync(player.getUniqueId(), key, value);
    }

    public void savePlayerDataBulk(UUID playerUUID, Map<String, Object> dataMap) {
        File playerFile = getPlayerFile(playerUUID);
        saveBulkInternal(playerFile, dataMap, null);
    }

    public void savePlayerDataBulk(Player player, Map<String, Object> dataMap) {
        savePlayerDataBulk(player.getUniqueId(), dataMap);
    }

    public void savePlayerDataBulk(UUID playerUUID, Map<String, Object> dataMap, Consumer<Exception> customErrorHandler) {
        File playerFile = getPlayerFile(playerUUID);
        saveBulkInternal(playerFile, dataMap, customErrorHandler);
    }

    public CompletableFuture<Void> savePlayerDataBulkAsync(UUID playerUUID, Map<String, Object> dataMap) {
        return CompletableFuture.runAsync(() -> savePlayerDataBulk(playerUUID, dataMap), getExecutor());
    }

    public CompletableFuture<Void> savePlayerDataBulkAsync(Player player, Map<String, Object> dataMap) {
        return savePlayerDataBulkAsync(player.getUniqueId(), dataMap);
    }

    private void saveInternal(File playerFile, String key, Object value) {
        saveInternal(playerFile, key, value, null);
    }

    private void saveInternal(File playerFile, String key, Object value, Consumer<Exception> customErrorHandler) {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(playerFile);
        
        // Apply serializer if exists
        Object serializedValue = value;
        if (serializers.containsKey(key)) {
            serializedValue = serializers.get(key).apply(value);
        }
        
        config.set(key, serializedValue);
        
        try {
            config.save(playerFile);
            if (saveLogger != null) {
                saveLogger.accept(getUUIDFromFile(playerFile), key);
            }
        } catch (IOException e) {
            handleError(e, "Could not save data for player " + getUUIDFromFile(playerFile) + " (key: " + key + ")", customErrorHandler);
        }
    }

    private void saveBulkInternal(File playerFile, Map<String, Object> dataMap, Consumer<Exception> customErrorHandler) {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(playerFile);
        
        for (Map.Entry<String, Object> entry : dataMap.entrySet()) {
            String key = entry.getKey();
            if (protectedKeys.contains(key)) {
                logError("Skipping protected key: " + key);
                continue;
            }
            
            if (validators.containsKey(key) && !validators.get(key).test(entry.getValue())) {
                logError("Validation failed for key: " + key + " with value: " + entry.getValue());
                continue;
            }
            
            Object value = entry.getValue();
            if (serializers.containsKey(key)) {
                value = serializers.get(key).apply(value);
            }
            config.set(key, value);
        }
        
        try {
            config.save(playerFile);
            if (saveLogger != null) {
                UUID uuid = getUUIDFromFile(playerFile);
                dataMap.forEach((key, value) -> saveLogger.accept(uuid, key));
            }
        } catch (IOException e) {
            handleError(e, "Could not save bulk data for player " + getUUIDFromFile(playerFile), customErrorHandler);
        }
    }

    public Object getPlayerData(UUID playerUUID, String key) {
        File playerFile = getPlayerFile(playerUUID);
        YamlConfiguration config = YamlConfiguration.loadConfiguration(playerFile);
        Object value = config.get(key);
        
        // Apply deserializer if exists
        if (value != null && deserializers.containsKey(key)) {
            value = deserializers.get(key).apply(value);
        }
        return value;
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

    public CompletableFuture<Object> getPlayerDataAsync(UUID playerUUID, String key) {
        return CompletableFuture.supplyAsync(() -> getPlayerData(playerUUID, key), getExecutor());
    }

    public CompletableFuture<Object> getPlayerDataAsync(Player player, String key) {
        return getPlayerDataAsync(player.getUniqueId(), key);
    }

    public YamlConfiguration loadPlayerConfig(UUID playerUUID) {
        File playerFile = getPlayerFile(playerUUID);
        return YamlConfiguration.loadConfiguration(playerFile);
    }

    public YamlConfiguration loadPlayerConfig(Player player) {
        return loadPlayerConfig(player.getUniqueId());
    }

    public CompletableFuture<YamlConfiguration> loadPlayerConfigAsync(UUID playerUUID) {
        return CompletableFuture.supplyAsync(() -> loadPlayerConfig(playerUUID), getExecutor());
    }

    public CompletableFuture<YamlConfiguration> loadPlayerConfigAsync(Player player) {
        return loadPlayerConfigAsync(player.getUniqueId());
    }

    public void deletePlayerData(UUID playerUUID) {
        File playerFile = getPlayerFile(playerUUID);
        if (playerFile.exists() && !playerFile.delete()) {
            plugin.getLogger().warning("Could not delete player data file for " + playerUUID);
        }
    }

    public void deletePlayerData(Player player) {
        deletePlayerData(player.getUniqueId());
    }

    public CompletableFuture<Void> deletePlayerDataAsync(UUID playerUUID) {
        return CompletableFuture.runAsync(() -> deletePlayerData(playerUUID), getExecutor());
    }

    public boolean playerDataExists(UUID playerUUID) {
        return getPlayerFile(playerUUID).exists();
    }

    public boolean playerDataExists(Player player) {
        return playerDataExists(player.getUniqueId());
    }

    public File getPlayerFile(UUID playerUUID) {
        return new File(playersFolder, playerUUID + ".yml");
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

    public Map<String, Object> getDefaultValues() {
        return new HashMap<>(defaultValues);
    }

    public boolean isAsyncSafe() {
        return asyncSafe;
    }

    public boolean isProtectedKey(String key) {
        return protectedKeys.contains(key);
    }

    public void addDefaultValue(String key, Object value) {
        defaultValues.put(key, value);
    }

    public void addSerializer(String key, Function<Object, Object> serializer) {
        serializers.put(key, serializer);
    }

    public void addDeserializer(String key, Function<Object, Object> deserializer) {
        deserializers.put(key, deserializer);
    }

    public void addValidator(String key, Predicate<Object> validator) {
        validators.put(key, validator);
    }

    public void addProtectedKey(String key) {
        protectedKeys.add(key);
    }

    private UUID getUUIDFromFile(File file) {
        String fileName = file.getName();
        if (fileName.endsWith(".yml")) {
            fileName = fileName.substring(0, fileName.length() - 4);
        }
        try {
            return UUID.fromString(fileName);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private java.util.concurrent.Executor getExecutor() {
        return runnable -> Bukkit.getScheduler().runTaskAsynchronously(plugin, runnable);
    }

    private void logError(String message) {
        if (errorLogger != null) {
            errorLogger.accept(message);
        } else {
            plugin.getLogger().severe(message);
        }
    }

    private void handleError(Exception e, String message, Consumer<Exception> customHandler) {
        if (customHandler != null) {
            customHandler.accept(e);
        } else if (errorLogger != null) {
            errorLogger.accept(message);
            e.printStackTrace();
        } else {
            plugin.getLogger().severe(message);
            e.printStackTrace();
        }
    }

    public static class Builder {
        private JavaPlugin plugin;
        private File playersFolder;
        private Function<Player, Integer> defaultLivesProvider = player -> 3;
        private int defaultKills = 0;
        private final Map<String, Object> defaultValues = new HashMap<>();
        private final Map<String, Function<Object, Object>> serializers = new HashMap<>();
        private final Map<String, Function<Object, Object>> deserializers = new HashMap<>();
        private final Map<String, Predicate<Object>> validators = new HashMap<>();
        private Consumer<String> errorLogger;
        private BiConsumer<UUID, String> saveLogger;
        private boolean asyncSafe = true;
        private boolean createIfMissing = true;
        private final List<String> protectedKeys = new ArrayList<>();

        public Builder(JavaPlugin plugin) {
            this.plugin = plugin;
            this.playersFolder = new File(plugin.getDataFolder(), "players");
            
            // Default protected keys
            protectedKeys.add("uuid");
            protectedKeys.add("name");
            protectedKeys.add("firstJoin");
            protectedKeys.add("lastJoin");
        }

        public Builder playersFolder(File folder) {
            this.playersFolder = folder;
            return this;
        }

        public Builder playersFolder(String folderName) {
            this.playersFolder = new File(plugin.getDataFolder(), folderName);
            return this;
        }

        public Builder defaultLives(int defaultLives) {
            this.defaultLivesProvider = player -> defaultLives;
            this.defaultValues.put("lives", defaultLives);
            return this;
        }

        public Builder defaultLivesProvider(Function<Player, Integer> provider) {
            this.defaultLivesProvider = provider;
            return this;
        }

        public Builder defaultKills(int defaultKills) {
            this.defaultKills = defaultKills;
            this.defaultValues.put("kills", defaultKills);
            return this;
        }

        public Builder defaultValue(String key, Object value) {
            this.defaultValues.put(key, value);
            return this;
        }

        public Builder defaultValue(String key, Function<Player, Object> valueProvider) {
            this.defaultValues.put(key, valueProvider);
            return this;
        }

        public Builder defaultValues(Map<String, Object> values) {
            this.defaultValues.putAll(values);
            return this;
        }

        public Builder serializer(String key, Function<Object, Object> serializer) {
            this.serializers.put(key, serializer);
            return this;
        }

        public Builder deserializer(String key, Function<Object, Object> deserializer) {
            this.deserializers.put(key, deserializer);
            return this;
        }

        public Builder serializer(String key, Class<?> fromType, Class<?> toType, Function<Object, Object> serializer) {
            this.serializers.put(key, serializer);
            return this;
        }

        public Builder validator(String key, Predicate<Object> validator) {
            this.validators.put(key, validator);
            return this;
        }

        public Builder validator(String key, Class<?> type, Predicate<Object> validator) {
            this.validators.put(key, value -> type.isInstance(value) && validator.test(value));
            return this;
        }

        public Builder errorLogger(Consumer<String> logger) {
            this.errorLogger = logger;
            return this;
        }

        public Builder saveLogger(BiConsumer<UUID, String> logger) {
            this.saveLogger = logger;
            return this;
        }

        public Builder asyncSafe(boolean asyncSafe) {
            this.asyncSafe = asyncSafe;
            return this;
        }

        public Builder createIfMissing(boolean createIfMissing) {
            this.createIfMissing = createIfMissing;
            return this;
        }

        public Builder protectedKey(String key) {
            this.protectedKeys.add(key);
            return this;
        }

        public Builder protectedKeys(List<String> keys) {
            this.protectedKeys.addAll(keys);
            return this;
        }

        public YamlPlayerCreator build() {
            return new YamlPlayerCreator(this);
        }
    }
}