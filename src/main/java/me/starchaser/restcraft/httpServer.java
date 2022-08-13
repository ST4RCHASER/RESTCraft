package me.starchaser.restcraft;

import me.starchaser.restcraft.models.Request;

import org.bukkit.Server;
import com.google.gson.Gson;
import org.bukkit.plugin.Plugin;


import static spark.Spark.*;

import java.nio.charset.StandardCharsets;
import java.security.*;

public class httpServer {
    private final Server instance;
    private Plugin plugin;
    private final configReader config;
    private final Gson gson = new Gson();

    httpServer(Server instance, Plugin plugin, configReader config) {
        this.instance = instance;
        this.plugin = plugin;
        this.config = config;
    }

    public void startServer() {
        ipAddress(config.getIp());
        port(this.config.getPort());
        internalServerError("HAVE CRITICAL ERROR PLEASE CHECK CONSOLE");
        notFound("NOT FOUND");
        setupRoutes();
        this.instance.getLogger().info("RESTCraft is now running on " + this.config.getIp() + ":" + this.config.getPort());
    }

    public void setupRoutes() {
        get("*", (request, response) -> {
            return new httpHandleResponse(request, response).execute(this.plugin, this.config);
        });
        post("*", (request, response) -> {
            return new httpHandleResponse(request, response).execute(this.plugin, this.config);
        });
    }

    public void shutdown() {
        stop();
    }
}
