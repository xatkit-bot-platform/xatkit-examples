package com.xatkit.example;

import com.mashape.unirest.http.exceptions.UnirestException;
import com.xatkit.core.XatkitBot;
import com.xatkit.plugins.react.platform.ReactPlatform;
import com.xatkit.plugins.react.platform.io.ReactEventProvider;
import com.xatkit.plugins.react.platform.io.ReactIntentProvider;
import lombok.val;
import org.apache.commons.configuration2.BaseConfiguration;
import org.apache.commons.configuration2.Configuration;

import static com.xatkit.dsl.DSL.any;
import static com.xatkit.dsl.DSL.eventIs;
import static com.xatkit.dsl.DSL.fallbackState;
import static com.xatkit.dsl.DSL.intent;
import static com.xatkit.dsl.DSL.intentIs;
import static com.xatkit.dsl.DSL.model;
import static com.xatkit.dsl.DSL.state;
import static com.xatkit.core.recognition.IntentRecognitionProviderFactoryConfiguration.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReference;

/**
 * This is an example bot which uses IsEnglishYesNoQuestion and EnglishSentiment postprocessors, designed with Xatkit.
 * <p>
 * You can check our <a href="https://github.com/xatkit-bot-platform/xatkit/wiki">wiki</a>
 * to learn more about bot creation, supported platforms, and advanced usage.
 */
public class ToxicityDetectorBot {


    public static void main(String[] args) throws UnirestException {

        String API_KEY = "YOUR PERSPECTIVEAPI KEY";
        ArrayList<AttributeType> attribs = new ArrayList<>();
        attribs.add(AttributeType.TOXICITY);
        attribs.add(AttributeType.INSULT);
        PerspectiveApiInterface perspectiveapi = new PerspectiveApiInterface(API_KEY, "helloworld", attribs,
                null, null, null, null);
        AtomicReference<HashMap<String, Double>> commentScores = new AtomicReference<>(new HashMap<>());
        val question = intent("comment")
                .trainingSentence("COMMENT")
                .parameter("comment").fromFragment("COMMENT").entity(any());

        ReactPlatform reactPlatform = new ReactPlatform();
        ReactEventProvider reactEventProvider = new ReactEventProvider(reactPlatform);
        ReactIntentProvider reactIntentProvider = new ReactIntentProvider(reactPlatform);

        val init = state("Init");
        val awaitingInput = state("AwaitingInput");
        val handleWelcome = state("HandleWelcome");
        val handleComment = state("HandleComment");

        init
                .next()
                .when(eventIs(ReactEventProvider.ClientReady)).moveTo(handleWelcome);

        handleWelcome
                .body(context -> reactPlatform.reply(context,
                        "Hi, I am your favourite bot :) I will score your comments toxicity."))
                .next()
                .moveTo(awaitingInput);

        awaitingInput
                .next()
                .when(intentIs(question)).moveTo(handleComment);

        handleComment
                .body(context -> {
                    perspectiveapi.setCommentText(context.getIntent().getMatchedInput());
                    try {
                        commentScores.set(perspectiveapi.analyzeRequest());
                    } catch (UnirestException e) {
                        e.printStackTrace();
                    }
                    for (String k : commentScores.get().keySet()) {
                        String msg = k + ": " + commentScores.get().get(k);
                        reactPlatform.reply(context, msg);
                    }
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
        botConfiguration.setProperty("xatkit.dialogflow.credentials.path",
                "PATH TO YOUR CREDENTIALS");
        botConfiguration.setProperty("xatkit.dialogflow.language", "en-US");
        botConfiguration.setProperty("xatkit.dialogflow.clean_on_startup", true);
        botConfiguration.setProperty(ENABLE_RECOGNITION_ANALYTICS, false);

        XatkitBot xatkitBot = new XatkitBot(botModel, botConfiguration);
        xatkitBot.run();
        /*
         * The bot is now started, you can check http://localhost:5000/admin to test it.
         * The logs of the bot are stored in the logs folder at the root of this project.
         */
    }
}
