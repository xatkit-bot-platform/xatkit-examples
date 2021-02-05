package com.xatkit.example.utils;

import org.json.JSONArray;
import org.json.JSONObject;

public class ResponseParser {

    public static Video[] getData(String response) {

        JSONObject obj = new JSONObject(response);
        JSONArray items = obj.getJSONArray("items");
        Video[] videos = new Video[items.length()];
        for (int i = 0; i < items.length(); ++i) {
            videos[i] = getVideoData(items.getJSONObject(i));
        }
        return videos;
    }

    private static Video getVideoData(JSONObject item) {
        String videoId = item.getJSONObject("id").get("videoId").toString();
        String videoTitle = item.getJSONObject("snippet").get("title").toString();
        String publishTime = item.getJSONObject("snippet").get("publishTime").toString();
        String channelTitle = item.getJSONObject("snippet").get("channelTitle").toString();
        String channelId = item.getJSONObject("snippet").get("channelId").toString();
        String thumbnail = item.getJSONObject("snippet").getJSONObject("thumbnails").getJSONObject("high").get("url").toString();

        return new Video(videoId, videoTitle, channelId, channelTitle, publishTime, thumbnail);
    }

}