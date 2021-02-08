package com.xatkit.example.utils;

public class Video {

    private String videoId;
    private String videoTitle;
    private String channelId;
    private String channelTitle;
    private String publishTime;
    private String videoURL;
    private String channelURL;
    private String thumbnailURL;



    Video(String vId, String vT, String cId, String cT, String pt, String th) {
        videoId = vId;
        videoTitle = vT;
        channelId = cId;
        channelTitle = cT;
        publishTime = pt;
        videoURL = "https://www.youtube.com/watch?v=" + videoId;
        channelURL = "https://www.youtube.com/channel/" + channelId;
        thumbnailURL = th;
    }

    public String getVideoId() {
        return videoId;
    }

    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }

    public String getVideoTitle() {
        return videoTitle;
    }

    public void setVideoTitle(String videoTitle) {
        this.videoTitle = videoTitle;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public String getChannelTitle() {
        return channelTitle;
    }

    public void setChannelTitle(String channelTitle) {
        this.channelTitle = channelTitle;
    }

    public String getPublishTime() {
        return publishTime;
    }

    public void setPublishTime(String publishTime) {
        this.publishTime = publishTime;
    }

    public String getVideoURL() {
        return videoURL;
    }

    public void setVideoURL(String videoURL) {
        this.videoURL = videoURL;
    }

    public String getChannelURL() {
        return channelURL;
    }

    public void setChannelURL(String channelURL) {
        this.channelURL = channelURL;
    }

    public String getThumbnailURL() {
        return thumbnailURL;
    }

    public void setThumbnailURL(String thumbnailURL) {
        this.thumbnailURL = thumbnailURL;
    }

}
