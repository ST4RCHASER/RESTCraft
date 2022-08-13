package me.starchaser.restcraft;

import me.starchaser.restcraft.models.Request;
import me.starchaser.restcraft.models.Response;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import com.google.gson.Gson;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import static spark.Spark.halt;

public class httpHandleResponse {
    final private spark.Request request;
    final private spark.Response response;
    private final Gson gson = new Gson();

    httpHandleResponse(spark.Request request, spark.Response response) {
        this.request = request;
        this.response = response;
    }

    public String generateResponse(int code, String resp, String keyword, String type) {
        Response response = new Response(code, resp, (keyword != null ? resp.contains(keyword) : false));
        switch (type.toLowerCase()) {
            case "json":
                return gson.toJson(response);
            case "boolean":
                return response.keyword_found ? "true" : "false";
            case "plantext":
            default:
                return resp;
        }
    }

    private String isAuthenticated(configReader config,Server instance, spark.Request request) {
        Request requestData = gson.fromJson(request.body(), Request.class);
        String secretQuery = request.queryParams("secret");
        String secretBody = requestData == null ? null : requestData.getSecret();
        String secretHeader = request.headers("Authorization") != null && request.headers("Authorization").split(" ").length > 1 ? request.headers("Authorization").split(" ")[1] : null;
        String clientIP = request.ip();
        if (config.isWhitelistEnabled()) {
            if (!config.getWhitelist().contains(clientIP)) {
                return "This ip is not in whitelist";
            }
        }
        if (secretQuery == null && secretBody == null && secretHeader == null) return "No secret provided";
        String secret = secretQuery != null ? secretQuery : secretBody != null ? secretBody : secretHeader;
        try {
            if (config.getHash().toUpperCase().equals("NONE")) {
                if (secret.equals(config.getSecret())) return "ok";
                return "Unauthorized";
            }
            String correctSecret = config.getSecret();
            byte[] correctByte = correctSecret.getBytes(StandardCharsets.UTF_8);
            MessageDigest correct = MessageDigest.getInstance(config.getHash().toUpperCase());
            byte[] correctDigest = correct.digest(correctByte);
            String correctString = new String(correctDigest, StandardCharsets.UTF_8);
            String currectHexString = toHexString(correctDigest);
            instance.getLogger().info("Secret: " + secret + " Correct: " + currectHexString);
            if (secret.equals(currectHexString)) return "ok";
        } catch (Exception e) {
            instance.getLogger().severe(e.toString());
            return "Error while hashing secret";
        }
        return "Unauthorized";
    }
    public String toHexString(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(0xFF & bytes[i]);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }

        return hexString.toString();
    }
    public Object execute(Plugin plugin, configReader config) {
        final String isAuth = this.isAuthenticated(config, plugin.getServer(), this.request);
        if (isAuth.equals("ok")) {
            response.header("Access-Control-Allow-Origin", "*");
            response.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            response.header("Access-Control-Allow-Headers", "Content-Type, Authorization, X-Requested-With");
            response.header("Access-Control-Allow-Credentials", "true");
        } else {
            return halt(401, new httpHandleResponse(request, response).generateResponse(403, isAuth, null, config.getResType()));
        }
        Server instance = plugin.getServer();
        instance.getLogger().info("Request " + request.pathInfo() + " from " + request.ip());
        instance.getLogger().info("Body: " + request.body());
        Request requestData = gson.fromJson(request.body(), Request.class);
        String respType = config.getResType().toLowerCase();
        if (request.requestMethod().equals("POST") || request.requestMethod().equals("GET")) {
            if (request.pathInfo().equalsIgnoreCase("/")) {
                return generateResponse(200, "Welcome to RESTCraft please read document for how to use", null, respType);
            }
            if (request.pathInfo().equalsIgnoreCase("/ping")) {
                return generateResponse(200, "pong", null, respType);
            }
            if (request.pathInfo().equalsIgnoreCase("/version")) {
                return generateResponse(200, plugin.getDescription().getVersion(), null, respType);
            }
            if (request.pathInfo().equalsIgnoreCase("/isOnline")) {
                if (!config.isOnlineCheckEnabled())
                    return generateResponse(403, "online check is disabled", null, respType);
                String target = requestData.getTargetPlayer();
                Player player = instance.getPlayerExact(target);
                if (player == null)
                    return generateResponse(404, "Player name " + target + " is not found", null, respType);
                return generateResponse(200, "Player name " + target + " found", null, respType);
            }
            String target = requestData.getTargetPlayer();
            String command = requestData.getCommand();
            if (request.pathInfo().equalsIgnoreCase("/runAsPlayer")) {
                if (!config.isAsPlayerEnabled())
                    return generateResponse(403, "run as player is disabled", null, respType);
                if (target == null) return generateResponse(400, "No target provided", null, respType);
                if (command == null) return generateResponse(400, "No command provided", null, respType);
                Player player = instance.getPlayerExact(target);
                if (player == null)
                    return generateResponse(404, "Player name " + target + " is not found", null, respType);
                if (command.split(" ").length > 0 && config.getDisableCommands().contains(command.split(" ")[0]))
                    return generateResponse(403, "Command " + command.split(" ")[0] + " is disabled", null, respType);
                try {
                    final MessageInterceptingCommandRunner cmdRunner = new MessageInterceptingCommandRunner(player);
                    return instance.getScheduler().callSyncMethod(plugin, () -> {
                        instance.dispatchCommand(cmdRunner, command);
                        return generateResponse(200, cmdRunner.getMessageLogStripColor(), null, respType);
                    }).get();
                } catch (Exception ex) {
                    try {
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                player.performCommand(command);
                            }
                        }.runTask(plugin);
                        return generateResponse(200, "Command run successfully, but can't get response", null, respType);
                    } catch (Exception ex2) {
                        try {
                            new BukkitRunnable() {
                                @Override
                                public void run() {
                                    instance.dispatchCommand(player, command);
                                }
                            }.runTask(plugin);
                            return generateResponse(200, "Command run successfully, but can't get response", null, respType);
                        } catch (Exception ex3) {
                            ex.printStackTrace();
                            ex2.printStackTrace();
                            ex3.printStackTrace();
                            return generateResponse(500, "Error while running as player command: " + ex.toString(), null, respType);
                        }
                    }
                }
            }
            if (request.pathInfo().equalsIgnoreCase("/runAsOp")) {
                if (!config.isAsOpEnabled()) return generateResponse(403, "run as op is disabled", null, respType);
                if (target == null) return generateResponse(400, "No target provided", null, respType);
                if (command == null) return generateResponse(400, "No command provided", null, respType);
                Player player = instance.getPlayerExact(target);
                if (player == null)
                    return generateResponse(404, "Player name " + target + " is not found", null, respType);
                if (command.split(" ").length > 0 && config.getDisableCommands().contains(command.split(" ")[0]))
                    return generateResponse(403, "Command " + command.split(" ")[0] + " is disabled", null, respType);
                boolean isAlreadyOp = player.isOp();
                player.setOp(true);
                try {
                    final MessageInterceptingCommandRunner cmdRunner = new MessageInterceptingCommandRunner(player);
                    return instance.getScheduler().callSyncMethod(plugin, () -> {
                        instance.dispatchCommand(cmdRunner, command);
                        player.setOp(isAlreadyOp);
                        return generateResponse(200, cmdRunner.getMessageLogStripColor(), null, respType);
                    }).get();
                } catch (Exception ex) {
                    try {
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                player.performCommand(command);
                                player.setOp(isAlreadyOp);
                            }
                        }.runTask(plugin);
                        return generateResponse(200, "Command run successfully, but can't get response", null, respType);
                    } catch (Exception ex2) {
                        try {
                            new BukkitRunnable() {
                                @Override
                                public void run() {
                                    instance.dispatchCommand(player, command);
                                    player.setOp(isAlreadyOp);
                                }
                            }.runTask(plugin);
                            return generateResponse(200, "Command run successfully, but can't get response", null, respType);
                        } catch (Exception ex3) {
                            player.setOp(isAlreadyOp);
                            ex.printStackTrace();
                            ex2.printStackTrace();
                            ex3.printStackTrace();
                            return generateResponse(500, "Error while running as op command: " + ex.toString(), null, respType);
                        }
                    }
                }
            }
            if (request.pathInfo().equalsIgnoreCase("/runAsConsole")) {
                if (!config.isAsConsoleEnabled())
                    return generateResponse(403, "run as console is disabled", null, respType);
                if (command == null) return generateResponse(400, "No command provided", null, respType);
                if (command.split(" ").length > 0 && config.getDisableCommands().contains(command.split(" ")[0]))
                    return generateResponse(403, "Command " + command.split(" ")[0] + " is disabled", null, respType);
                try {
                    final MessageInterceptingConsoleCommandRunner cmdRunner = new MessageInterceptingConsoleCommandRunner(instance.getConsoleSender());
                    return instance.getScheduler().callSyncMethod(plugin, () -> {
                        instance.dispatchCommand(cmdRunner, command);
                        return generateResponse(200, cmdRunner.getMessageLogStripColor(), null, respType);
                    }).get();
                } catch (Exception e) {
                    e.printStackTrace();
                    return generateResponse(500, "Error while running as console command: " + e.toString(), null, respType);
                }
            }
        }
        return halt(404, generateResponse(404, "NOT FOUND", request.body(), respType));
    }
}
