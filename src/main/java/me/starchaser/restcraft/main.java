package me.starchaser.restcraft;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public final class main extends JavaPlugin {
    private Server instance;
    private Plugin plugin;
    private configReader config;
    private httpServer http;

    @Override
    public void onEnable() {
        this.instance = this.getServer();
        this.plugin = this;
        this.config = new configReader(this);
        this.http = new httpServer(this.instance, this, this.config);
        http.startServer();
    }

    @Override
    public void onDisable() {
        http.shutdown();
    }
}
