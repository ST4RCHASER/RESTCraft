package me.starchaser.restcraft;

import com.google.gson.Gson;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

import java.util.Arrays;
import java.util.List;

public class configReader {
    private final Plugin plugin;
    private String ip = "localhost";
    private int port = 7132;
    private String hash = "NONE";
    private String secret = "PLEASE_CHANGE_THIS_SECRET";
    private String resType = "JSON";
    private boolean whitelistEnabled = false;
    private List<String> whitelist = Arrays.asList(new String[]{"127.0.0.1"});
    private List<String> disableCommands = Arrays.asList(new String[]{"op"});
    private boolean[] sysSwitch = new boolean[]{true,true,true,true};
    private FileConfiguration config;
    configReader(Plugin plugin) {
        this.plugin = plugin;
        config = plugin.getConfig();
        this.reloadConfig();
    }
    public void reloadConfig() {
        this.config.addDefault("server.ip_bind", this.ip);
        this.config.addDefault("server.port", this.port);
        this.config.addDefault("server.password.hash", this.hash);
        this.config.addDefault("server.password.secret", this.secret);
        this.config.addDefault("server.response_type", this.resType);
        this.config.addDefault("server.ip_whitelist.enabled", this.whitelistEnabled);
        this.config.addDefault("server.ip_whitelist.list", this.whitelist);
        this.config.addDefault("disable_commands", this.disableCommands);
        this.config.addDefault("switch.as_player", this.sysSwitch[0]);
        this.config.addDefault("switch.as_op", this.sysSwitch[1]);
        this.config.addDefault("switch.as_console", this.sysSwitch[2]);
        this.config.addDefault("switch.is_online", this.sysSwitch[3]);
        this.config.options().copyDefaults(true);
        this.plugin.saveConfig();
        if(this.config.getString("server.ip_bind") != null) this.ip = this.config.getString("server.ip_bind");
        if(this.config.getInt("server.port") != 0) this.port = this.config.getInt("server.port");
        if(this.config.getString("server.password.hash") != null) this.hash = this.config.getString("server.password.hash");
        if(this.config.getString("server.password.secret") != null) this.secret = this.config.getString("server.password.secret");
        if(this.config.getString("server.response_type") != null) this.resType = this.config.getString("server.response_type");
        this.whitelistEnabled = this.config.getBoolean("server.ip_whitelist.enabled");
        this.whitelist = this.config.getStringList("server.ip_whitelist.list");
        this.disableCommands = this.config.getStringList("disable_commands");
        this.sysSwitch[0] = this.config.getBoolean("switch.as_player");
        this.sysSwitch[1] = this.config.getBoolean("switch.as_op");
        this.sysSwitch[2] = this.config.getBoolean("switch.as_console");
        this.sysSwitch[3] = this.config.getBoolean("switch.is_online");
        this.plugin.getLogger().info("Config loaded: " + this.getHash());
    }
    public boolean[] getSysSwitch() {
        return this.sysSwitch;
    }
    public int getPort() {
        return this.port;
    }
    public String getHash() {
        return this.hash;
    }
    public String getIp() {
        return this.ip;
    }
    public String getResType() {
        return this.resType;
    }
    public String getSecret() {
        return this.secret;
    }
    public List<String> getWhitelist() {
        return this.whitelist;
    }
    public boolean isAsPlayerEnabled() {
        return this.sysSwitch[0];
    }
    public boolean isAsOpEnabled() {
        return this.sysSwitch[1];
    }
    public boolean isAsConsoleEnabled() {
        return this.sysSwitch[2];
    }
    public boolean isOnlineCheckEnabled() {
        return this.sysSwitch[3];
    }
    public List<String> getDisableCommands() {
        return disableCommands;
    }

    public boolean isWhitelistEnabled() {
        return whitelistEnabled;
    }
}
