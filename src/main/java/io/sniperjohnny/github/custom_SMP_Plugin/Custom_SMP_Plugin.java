package io.sniperjohnny.github.custom_SMP_Plugin;

import io.sniperjohnny.github.custom_SMP_Plugin.cuality_of_life.commands.Fly_Command;
import io.sniperjohnny.github.custom_SMP_Plugin.cuality_of_life.listeners.Join_listener;
import org.bukkit.plugin.java.JavaPlugin;

public final class Custom_SMP_Plugin extends JavaPlugin {

    @Override
    public void onEnable() {
        // Plugin startup logic


    saveDefaultConfig();

    getServer().getPluginManager().registerEvents(new Join_listener(this), this);
    getCommand("fly").setExecutor(new Fly_Command());
    getLogger().info("Custom_SMP_Plugin_started");

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
