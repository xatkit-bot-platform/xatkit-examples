package com.xatkit.example;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;

import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchListResponse;

import java.io.*;
import java.security.GeneralSecurityException;

public class YoutubeAPI {

    private static final String DEVELOPER_KEY = "YOUR DEVELOPER KEY";
    private static final String APPLICATION_NAME = "YOUR APP NAME";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    /**
     * Build and return an authorized API client service.
     *
     * @return an authorized API client service
     * @throws GeneralSecurityException, IOException
     */
    public static YouTube getService() throws GeneralSecurityException, IOException {
        final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        return new YouTube.Builder(httpTransport, JSON_FACTORY, null)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    /**
     * Call function to create API service object. Define and
     * execute API request.
     *
     * @return API response.
     * @throws GeneralSecurityException, IOException, GoogleJsonResponseException
     */
    public static SearchListResponse getSearchListResponse(YouTube youtubeService, String keyword, Long num)
            throws GeneralSecurityException, IOException, GoogleJsonResponseException {
        YouTube.Search.List request = youtubeService.search()
                .list("snippet");
        SearchListResponse response = request.setKey(DEVELOPER_KEY)
                .setMaxResults(num)
                .setQ(keyword)
                .setType("video")
                .execute();
        return response;
    }
}