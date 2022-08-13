package me.starchaser.restcraft.models;

public class Response {
    public final int code;
    public final boolean success;
    public final boolean keyword_found;
    public final String data;
    public Response(int code, String data, boolean keywordFound) {
        this.code = code;
        this.success = code == 200;
        this.data = data;
        this.keyword_found = keywordFound;
    }
}
