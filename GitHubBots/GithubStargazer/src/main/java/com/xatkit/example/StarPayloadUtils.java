package com.xatkit.example;

import com.google.gson.JsonElement;

/*
 * Utility methods to manipulate Star payloads.
 */
public class StarPayloadUtils {

    public static String getRepositoryUrl(JsonElement payload) {
        return payload.getAsJsonObject().getAsJsonObject("repository").get("html_url").getAsString();
    }

    public static String getRepositoryName(JsonElement payload) {
        return payload.getAsJsonObject().getAsJsonObject("repository").get("name").getAsString();
    }

    public static String getSenderUrl(JsonElement payload) {
        return payload.getAsJsonObject().getAsJsonObject("sender").get("html_url").getAsString();
    }

    public static String getSenderLogin(JsonElement payload) {
        return payload.getAsJsonObject().getAsJsonObject("sender").get("login").getAsString();
    }

    public static int getStargazersCount(JsonElement payload) {
        return payload.getAsJsonObject().getAsJsonObject("repository").get("stargazers_count").getAsInt();
    }

}
