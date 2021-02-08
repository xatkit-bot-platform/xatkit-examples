package com.xatkit.example;

import com.google.api.services.youtube.YouTube;
import com.xatkit.core.XatkitBot;
import com.xatkit.example.utils.Video;
import com.xatkit.execution.StateContext;
import com.xatkit.plugins.react.platform.ReactPlatform;
import com.xatkit.plugins.react.platform.io.ReactEventProvider;
import com.xatkit.plugins.react.platform.io.ReactIntentProvider;
import lombok.val;
import org.apache.commons.configuration2.BaseConfiguration;
import org.apache.commons.configuration2.Configuration;

import java.io.IOException;
import java.security.GeneralSecurityException;

import static com.xatkit.dsl.DSL.any;
import static com.xatkit.dsl.DSL.integer;
import static com.xatkit.dsl.DSL.eventIs;
import static com.xatkit.dsl.DSL.fallbackState;
import static com.xatkit.dsl.DSL.intent;
import static com.xatkit.dsl.DSL.intentIs;
import static com.xatkit.dsl.DSL.model;
import static com.xatkit.dsl.DSL.state;
import static com.xatkit.example.YoutubeAPI.getSearchListResponse;
import static com.xatkit.example.YoutubeAPI.getService;
import static com.xatkit.example.utils.ResponseParser.getData;

/**
 * This is an example bot which can search Youtube videos using a keyword provided by the user, designed with Xatkit.
 * <p>
 * You can check our <a href="https://github.com/xatkit-bot-platform/xatkit/wiki">wiki</a>
 * to learn more about bot creation, supported platforms, and advanced usage.
 */
public class YoutubeBot {

    private static void replyYoutubeBot(StateContext context, ReactPlatform reactPlatform, String response, String keyword) {
        Video[] data = getData(response);
        String msg = "I searched \"" + keyword + "\" and I found these videos for you...\n";
        reactPlatform.reply(context, msg);

        for (Video video : data) {
            reactPlatform.replyLinkSnippet(context, video.getVideoTitle(), video.getVideoURL(), video.getThumbnailURL());
        }
    }

    public static void main(String[] args)
            throws GeneralSecurityException, IOException {

        val search = intent("Search")
                .trainingSentence("Search KEYWORD")
                .trainingSentence("Search NUM videos of KEYWORD")
                .trainingSentence("Search NUM videos about KEYWORD")
                .trainingSentence("Give me KEYWORD")
                .trainingSentence("Give me NUM videos of KEYWORD")
                .trainingSentence("Give me NUM videos about KEYWORD")
                .trainingSentence("Search KEYWORD and give me NUM videos")
                .parameter("keyword").fromFragment("KEYWORD").entity(any())
                .parameter("num").fromFragment("NUM").entity(integer());

        YouTube youtubeService = getService();
        ReactPlatform reactPlatform = new ReactPlatform();
        ReactEventProvider reactEventProvider = new ReactEventProvider(reactPlatform);
        ReactIntentProvider reactIntentProvider = new ReactIntentProvider(reactPlatform);

        val init = state("Init");
        val awaitingInput = state("AwaitingInput");
        val handleWelcome = state("HandleWelcome");
        val handleSearch = state("HandleSearch");

        init
                .next()
                .when(eventIs(ReactEventProvider.ClientReady)).moveTo(handleWelcome);

        awaitingInput
                .next()
                .when(intentIs(search)).moveTo(handleSearch);

        handleWelcome
                .body(context -> reactPlatform.reply(context,
                        "Hi, I am here to search Youtube videos for you! "+
                                "You can ask me to search videos about whatever you want. "+
                                "Feel free to specify the number of videos you want me to search."))
                .next()
                .moveTo(awaitingInput);

        handleSearch
                .body(context -> {
                    String response = null;
                    String keyword = (String) context.getIntent().getValue("keyword");
                    String num_string = (String) context.getIntent().getValue("num");
                    long num;
                    if (num_string.equals("")) num = 3L;
                    else num = Long.parseLong(num_string);
                    try {
                        response = getSearchListResponse(youtubeService, keyword, num).toString();
                    } catch (GeneralSecurityException | IOException e) {
                        e.printStackTrace();
                    }
                    replyYoutubeBot(context, reactPlatform, response, keyword);
                })
                .next()
                    .moveTo(awaitingInput);

        val defaultFallback = fallbackState()
                .body(context -> reactPlatform.reply(context, "Sorry, I didn't get it"));

        val botModel = model()
                .usePlatform(reactPlatform)
                .listenTo(reactEventProvider)
                .listenTo(reactIntentProvider)
                .initState(init)
                .defaultFallbackState(defaultFallback);

        Configuration botConfiguration = new BaseConfiguration();

        botConfiguration.setProperty("xatkit.dialogflow.projectId", "YOUR PROJECT ID");
        botConfiguration.setProperty("xatkit.dialogflow.credentials.path", "PATH TO YOUR DIALOGFLOW CREDENTIALS");
        botConfiguration.setProperty("xatkit.dialogflow.language", "en-Us");
        botConfiguration.setProperty("xatkit.dialogflow.clean_on_startup", true);

        XatkitBot xatkitBot = new XatkitBot(botModel, botConfiguration);
        xatkitBot.run();
        /*
         * The bot is now started, you can check http://localhost:5000/admin to test it.
         * The logs of the bot are stored in the logs folder at the root of this project.
         */
    }
}
