package me.starchaser.restcraft.models;

public class Request {
    private String command;
    private String secret;
    private String keyword;
    private String target_player;
    public Request() {

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

    public void setCommand(String command) {
        this.command = command;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public void setTargetPlayer(String targetPlayer) {
        this.target_player = targetPlayer;
    }
}
