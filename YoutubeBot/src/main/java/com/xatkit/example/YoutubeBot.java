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
 * This is an example greetings bot designed with Xatkit.
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

    /*
     * Your bot is a plain Java application: you need to define a main method to make the created jar executable.
     */
    public static void main(String[] args)
            throws GeneralSecurityException, IOException {

        YouTube youtubeService = getService();

        /*
         * Define the intents our bot will react to.
         */
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

        /*
         * Instantiate the platform we will use in the bot definition.
         */
        ReactPlatform reactPlatform = new ReactPlatform();
        /*
         * Similarly, instantiate the intent/event providers we want to use.
         */
        ReactEventProvider reactEventProvider = new ReactEventProvider(reactPlatform);
        ReactIntentProvider reactIntentProvider = new ReactIntentProvider(reactPlatform);

        /*
         * Create the states we want to use in our bot.
         */
        val init = state("Init");
        val awaitingInput = state("AwaitingInput");
        val handleWelcome = state("HandleWelcome");
        val handleSearch = state("HandleSearch");

        /*
         * Specify the content of the bot states (i.e. the behavior of the bot).
         * <p>
         * Each state contains:
         * <ul>
         * <li>An optional body executed when entering the state. This body is provided as a lambda expression
         * with a context parameter representing the current state of the bot.</li>
         * <li>A mandatory list of next() transitions that are evaluated when a new event is received. This list
         * must contain at least one transition. Transitions can be guarded with a when(...) clause, or
         * automatically navigated using a direct moveTo(state) clause.</li>
         * <li>An optional fallback executed when there is no navigable transition matching the received event. As
         * for the body the state fallback is provided as a lambda expression with a context parameter representing
         * the current state of the bot. If there is no fallback defined for a state the bot's default fallback state
         * is executed instead.
         * </li>
         * </ul>
         */
        init
                .next()
                    /*
                     * We check that the received event matches the ClientReady event defined in the
                     * ReactEventProvider. The list of events defined in a provider is available in the provider's
                     * wiki page.
                     */
                    .when(eventIs(ReactEventProvider.ClientReady)).moveTo(handleWelcome);

        awaitingInput
                .next()
                    /*
                     * The Xatkit DSL offers dedicated predicates (intentIs(IntentDefinition) and eventIs
                     * (EventDefinition) to check received intents/events.
                     * <p>
                     * You can also check a condition over the underlying bot state using the following syntax:
                     * <pre>
                     * {@code
                     * .when(context -> [condition manipulating the context]).moveTo(state);
                     * }
                     * </pre>
                     */
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

        /*
         * The state that is executed if the engine doesn't find any navigable transition in a state and the state
         * doesn't contain a fallback.
         * <p>
         * The default fallback state is typically used to answer generic error messages, while state fallback can
         * benefit from contextual information to answer more precisely.
         * <p>
         * Note that every Xatkit bot needs a default fallback state.
         */
        val defaultFallback = fallbackState()
                .body(context -> reactPlatform.reply(context, "Sorry, I didn't get it"));

        /*
         * Creates the bot model that will be executed by the Xatkit engine.
         * <p>
         * A bot model contains:
         * - A list of platforms used by the bot. Xatkit will take care of starting and initializing the platforms
         * when starting the bot.
         * - A list of providers the bot should listen to for events/intents. As for the platforms Xatkit will take
         * care of initializing the provider when starting the bot.
         * - The entry point of the bot (a.k.a init state)
         * - The default fallback state: the state that is executed if the engine doesn't find any navigable
         * transition in a state and the state doesn't contain a fallback.
         */
        val botModel = model()
                .usePlatform(reactPlatform)
                .listenTo(reactEventProvider)
                .listenTo(reactIntentProvider)
                .initState(init)
                .defaultFallbackState(defaultFallback);

        Configuration botConfiguration = new BaseConfiguration();

        /*
         * Add configuration properties (e.g. authentication tokens, platform tuning, intent provider to use).
         * Check the corresponding platform's wiki page for further information on optional/mandatory parameters and
         * their values.
         */
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
