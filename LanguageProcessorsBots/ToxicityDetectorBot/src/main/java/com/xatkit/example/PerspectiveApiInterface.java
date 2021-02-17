package com.xatkit.example;

import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import lombok.NonNull;
import org.apache.xpath.operations.Bool;
import org.json.JSONObject;
import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class PerspectiveApiInterface {

    private String apiKey;
    private String commentText;
    private String commentType = "PLAIN_TEXT";
    private ArrayList<AttributeType> requestedAttributes;
    private ArrayList<String> languages;
    private Boolean doNotStore;
    private String clientToken;
    private String sessionId;

    private JSONObject request;

    PerspectiveApiInterface(@NonNull String apiKey, @NonNull String commentText,
                            @NonNull ArrayList<AttributeType> requestedAttributes,
                            ArrayList<String> languages, Boolean doNotStore, String clientToken, String sessionId) {

        this.apiKey = apiKey;
        this.commentText = commentText;
        this.requestedAttributes = (ArrayList<AttributeType>) requestedAttributes.clone();
        this.languages = languages != null ? (ArrayList<String>) languages.clone() : null;
        this.doNotStore = doNotStore != null ? doNotStore : false;
        this.clientToken = clientToken;
        this.sessionId = sessionId;
    }

    public void setCommentText(String commentText) {
        this.commentText = commentText;
    }


    public ArrayList<AttributeType> addRequestedAttributes(ArrayList<AttributeType> attributes) {
        for (AttributeType a : attributes) {
            if (!this.requestedAttributes.contains(a)) {
                this.requestedAttributes.add(a);
            }

        }
        return this.requestedAttributes;
    }

    public ArrayList<AttributeType> removeRequestedAttributes(ArrayList<AttributeType> attributes) {
            this.requestedAttributes.removeAll(attributes);
            return this.requestedAttributes;
    }

    public HashMap<String, Double> analyzeRequest() throws UnirestException {

        JSONObject request = this.createJSONObjectRequest();
        JSONObject response = Unirest.post("https://commentanalyzer.googleapis"
                + ".com/v1alpha1/comments:analyze?key=" + apiKey)
                .header("Content-Type", "application/json")
                .body(request)
                .asJson().getBody().getObject();
        System.out.println(response);
        JSONObject attributes = response.getJSONObject("attributeScores");
        HashMap<String, Double> scores = new HashMap<>();
        for (String k : attributes.keySet()) {
            Double score = attributes.getJSONObject(k).getJSONObject("summaryScore").getDouble("value");
            scores.put(k, score);
        }
        return scores;
    }

    private JSONObject createJSONObjectRequest() {

        JSONObject request = new JSONObject();
        JSONObject comment = new JSONObject();
        comment.put("text", this.commentText);
        comment.put("type", this.commentType);
        JSONObject requestedAttributes = new JSONObject();
        for (AttributeType a : this.requestedAttributes) {
            requestedAttributes.put(a.toString(), (Map<?, ?>) null);
        }
        request.put("comment", comment);
        request.put("requestedAttributes", requestedAttributes);
        request.put("doNotStore", this.doNotStore);
        request.put("clientToken", this.clientToken);
        request.put("sessionId", this.sessionId);
        return request;
    }


    public void printApi() {

        System.out.println(apiKey);
        System.out.println(commentText);
        System.out.println(commentType);
        System.out.println(requestedAttributes);
        System.out.println(languages);
        System.out.println(doNotStore);
        System.out.println(clientToken);
        System.out.println(sessionId);

    }

}
