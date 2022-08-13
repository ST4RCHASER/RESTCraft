package me.starchaser.restcraft.models;

public class Request {
    private String command;
    private String secret;
    private String keyword;
    private String target_player;
    Request(String command, String secret, String keyword, String targetPlayer) {
        this.command = command;
        this.secret = secret;
        this.keyword = keyword;
        this.target_player = targetPlayer;
    }

    public String getCommand() {
        return command;
    }
    public String getSecret() {
        return secret;
    }
    public String getTargetPlayer() {
        return target_player;
    }

    public String getKeyword() {
        return keyword;
    }
}
