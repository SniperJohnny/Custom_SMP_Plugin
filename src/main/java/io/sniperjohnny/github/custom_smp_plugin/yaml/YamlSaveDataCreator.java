package io.sniperjohnny.github.custom_smp_plugin.yaml;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
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

public class YamlSaveDataCreator {
    private final JavaPlugin plugin;
    private final File dataFolder;
    private final Function<String, File> fileResolver;
    private final Consumer<Exception> errorHandler;
    private final BiConsumer<String, String> saveLogger;
    private final Map<String, Function<Object, Object>> serializers;
    private final Map<String, Function<Object, Object>> deserializers;
    private final Map<String, Predicate<Object>> validators;
    private final List<String> protectedKeys;
    private final boolean asyncSafe;
    private final boolean createIfMissing;

    private YamlSaveDataCreator(Builder builder) {
        this.plugin = builder.plugin;
        this.dataFolder = builder.dataFolder;
        this.fileResolver = builder.fileResolver;
        this.errorHandler = builder.errorHandler;
        this.saveLogger = builder.saveLogger;
        this.serializers = new HashMap<>(builder.serializers);
        this.deserializers = new HashMap<>(builder.deserializers);
        this.validators = new HashMap<>(builder.validators);
        this.protectedKeys = new ArrayList<>(builder.protectedKeys);
        this.asyncSafe = builder.asyncSafe;
        this.createIfMissing = builder.createIfMissing;
    }

    public void ensureDataFolderExists() {
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
    }

    public CompletableFuture<Void> ensureDataFolderExistsAsync() {
        return CompletableFuture.runAsync(() -> ensureDataFolderExists(), getExecutor());
    }

    public void saveData(String fileName, String key, Object value) {
        if (protectedKeys.contains(key)) {
            handleError(new IllegalStateException("Cannot modify protected key: " + key));
            return;
        }
        
        if (validators.containsKey(key) && !validators.get(key).test(value)) {
            handleError(new IllegalArgumentException("Validation failed for key: " + key + " with value: " + value));
            return;
        }
        
        File dataFile = fileResolver.apply(fileName);
        saveInternal(dataFile, key, value, null);
    }

    public void saveData(String fileName, String key, Object value, Consumer<Exception> customErrorHandler) {
        if (protectedKeys.contains(key)) {
            if (customErrorHandler != null) customErrorHandler.accept(new IllegalStateException("Cannot modify protected key: " + key));
            return;
        }
        
        if (validators.containsKey(key) && !validators.get(key).test(value)) {
            if (customErrorHandler != null) customErrorHandler.accept(new IllegalArgumentException("Validation failed for key: " + key));
            return;
        }
        
        File dataFile = fileResolver.apply(fileName);
        saveInternal(dataFile, key, value, customErrorHandler);
    }

    public CompletableFuture<Void> saveDataAsync(String fileName, String key, Object value) {
        return CompletableFuture.runAsync(() -> saveData(fileName, key, value), getExecutor());
    }

    public void saveDataBulk(String fileName, Map<String, Object> dataMap) {
        File dataFile = fileResolver.apply(fileName);
        saveBulkInternal(dataFile, dataMap, null);
    }

    public void saveDataBulk(String fileName, Map<String, Object> dataMap, Consumer<Exception> customErrorHandler) {
        File dataFile = fileResolver.apply(fileName);
        saveBulkInternal(dataFile, dataMap, customErrorHandler);
    }

    public CompletableFuture<Void> saveDataBulkAsync(String fileName, Map<String, Object> dataMap) {
        return CompletableFuture.runAsync(() -> saveDataBulk(fileName, dataMap), getExecutor());
    }

    private void saveInternal(File dataFile, String key, Object value, Consumer<Exception> customErrorHandler) {
        // Ensure file exists if createIfMissing
        if (createIfMissing && !dataFile.exists()) {
            try {
                dataFile.getParentFile().mkdirs();
                dataFile.createNewFile();
            } catch (IOException e) {
                handleError(e, "Could not create data file: " + dataFile.getName(), customErrorHandler);
                return;
            }
        }
        
        YamlConfiguration config = YamlConfiguration.loadConfiguration(dataFile);
        
        // Apply serializer if exists
        Object serializedValue = value;
        if (serializers.containsKey(key)) {
            serializedValue = serializers.get(key).apply(value);
        }
        
        config.set(key, serializedValue);
        
        try {
            config.save(dataFile);
            if (saveLogger != null) {
                saveLogger.accept(dataFile.getName().replace(".yml", ""), key);
            }
        } catch (IOException e) {
            handleError(e, "Could not save data for file " + dataFile.getName() + " (key: " + key + ")", customErrorHandler);
        }
    }

    private void saveBulkInternal(File dataFile, Map<String, Object> dataMap, Consumer<Exception> customErrorHandler) {
        // Ensure file exists if createIfMissing
        if (createIfMissing && !dataFile.exists()) {
            try {
                dataFile.getParentFile().mkdirs();
                dataFile.createNewFile();
            } catch (IOException e) {
                handleError(e, "Could not create data file", customErrorHandler);
                return;
            }
        }
        
        YamlConfiguration config = YamlConfiguration.loadConfiguration(dataFile);
        
        for (Map.Entry<String, Object> entry : dataMap.entrySet()) {
            String key = entry.getKey();
            if (protectedKeys.contains(key)) {
                handleError(new IllegalStateException("Skipping protected key: " + key));
                continue;
            }
            
            if (validators.containsKey(key) && !validators.get(key).test(entry.getValue())) {
                handleError(new IllegalArgumentException("Validation failed for key: " + key + " with value: " + entry.getValue()));
                continue;
            }
            
            Object value = entry.getValue();
            if (serializers.containsKey(key)) {
                value = serializers.get(key).apply(value);
            }
            config.set(key, value);
        }
        
        try {
            config.save(dataFile);
            if (saveLogger != null) {
                String fileName = dataFile.getName().replace(".yml", "");
                dataMap.forEach((key, value) -> saveLogger.accept(fileName, key));
            }
        } catch (IOException e) {
            handleError(e, "Could not save bulk data for file " + dataFile.getName(), customErrorHandler);
        }
    }

    public Object getData(String fileName, String key) {
        File dataFile = fileResolver.apply(fileName);
        if (!dataFile.exists()) {
            return null;
        }
        YamlConfiguration config = YamlConfiguration.loadConfiguration(dataFile);
        Object value = config.get(key);
        
        // Apply deserializer if exists
        if (value != null && deserializers.containsKey(key)) {
            value = deserializers.get(key).apply(value);
        }
        return value;
    }

    public <T> T getData(String fileName, String key, T defaultValue) {
        Object value = getData(fileName, key);
        if (value == null) {
            return defaultValue;
        }
        try {
            return (T) value;
        } catch (ClassCastException e) {
            return defaultValue;
        }
    }

    public CompletableFuture<Object> getDataAsync(String fileName, String key) {
        return CompletableFuture.supplyAsync(() -> getData(fileName, key), getExecutor());
    }

    public YamlConfiguration loadConfig(String fileName) {
        File dataFile = fileResolver.apply(fileName);
        return YamlConfiguration.loadConfiguration(dataFile);
    }

    public CompletableFuture<YamlConfiguration> loadConfigAsync(String fileName) {
        return CompletableFuture.supplyAsync(() -> loadConfig(fileName), getExecutor());
    }

    public void deleteData(String fileName) {
        File dataFile = fileResolver.apply(fileName);
        if (dataFile.exists() && !dataFile.delete()) {
            plugin.getLogger().warning("Could not delete data file: " + fileName);
        }
    }

    public CompletableFuture<Void> deleteDataAsync(String fileName) {
        return CompletableFuture.runAsync(() -> deleteData(fileName), getExecutor());
    }

    public boolean dataExists(String fileName) {
        return fileResolver.apply(fileName).exists();
    }

    public File getDataFile(String fileName) {
        return fileResolver.apply(fileName);
    }

    public JavaPlugin getPlugin() {
        return plugin;
    }

    public File getDataFolder() {
        return dataFolder;
    }

    public boolean isAsyncSafe() {
        return asyncSafe;
    }

    public boolean isProtectedKey(String key) {
        return protectedKeys.contains(key);
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

    private java.util.concurrent.Executor getExecutor() {
        return runnable -> Bukkit.getScheduler().runTaskAsynchronously(plugin, runnable);
    }

    private void handleError(Exception e) {
        handleError(e, null);
    }

    private void handleError(Exception e, String customMessage) {
        if (errorHandler != null) {
            errorHandler.accept(e);
        } else {
            String message = customMessage != null ? customMessage : "Error in YamlSaveDataCreator: " + e.getMessage();
            plugin.getLogger().severe(message);
            e.printStackTrace();
        }
    }

    private void handleError(Exception e, String message, Consumer<Exception> customHandler) {
        if (customHandler != null) {
            customHandler.accept(e);
        } else if (errorHandler != null) {
            errorHandler.accept(e);
        } else {
            String msg = message != null ? message : "Error in YamlSaveDataCreator: " + e.getMessage();
            plugin.getLogger().severe(msg);
            e.printStackTrace();
        }
    }

    public static class Builder {
        private JavaPlugin plugin;
        private File dataFolder;
        private Function<String, File> fileResolver;
        private Consumer<Exception> errorHandler;
        private BiConsumer<String, String> saveLogger;
        private final Map<String, Function<Object, Object>> serializers = new HashMap<>();
        private final Map<String, Function<Object, Object>> deserializers = new HashMap<>();
        private final Map<String, Predicate<Object>> validators = new HashMap<>();
        private final List<String> protectedKeys = new ArrayList<>();
        private boolean asyncSafe = true;
        private boolean createIfMissing = true;

        public Builder(JavaPlugin plugin) {
            this.plugin = plugin;
            this.dataFolder = plugin.getDataFolder();
            this.fileResolver = fileName -> new File(dataFolder, fileName + ".yml");
        }

        public Builder dataFolder(File folder) {
            this.dataFolder = folder;
            this.fileResolver = fileName -> new File(folder, fileName + ".yml");
            return this;
        }

        public Builder dataFolder(String folderName) {
            this.dataFolder = new File(plugin.getDataFolder(), folderName);
            this.fileResolver = fileName -> new File(dataFolder, fileName + ".yml");
            return this;
        }

        public Builder fileResolver(Function<String, File> resolver) {
            this.fileResolver = resolver;
            return this;
        }

        public Builder errorHandler(Consumer<Exception> handler) {
            this.errorHandler = handler;
            return this;
        }

        public Builder saveLogger(BiConsumer<String, String> logger) {
            this.saveLogger = logger;
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

        public Builder validator(String key, Predicate<Object> validator) {
            this.validators.put(key, validator);
            return this;
        }

        public Builder validator(String key, Class<?> type, Predicate<Object> validator) {
            this.validators.put(key, value -> type.isInstance(value) && validator.test(value));
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

        public Builder asyncSafe(boolean asyncSafe) {
            this.asyncSafe = asyncSafe;
            return this;
        }

        public Builder createIfMissing(boolean createIfMissing) {
            this.createIfMissing = createIfMissing;
            return this;
        }

        public YamlSaveDataCreator build() {
            return new YamlSaveDataCreator(this);
        }
    }
}