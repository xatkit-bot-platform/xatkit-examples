package com.xatkit.example;

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

import java.util.Random;

/**
 * This is an example bot which uses IsEnglishYesNoQuestion and EnglishSentiment postprocessors, designed with Xatkit.
 * <p>
 * You can check our <a href="https://github.com/xatkit-bot-platform/xatkit/wiki">wiki</a>
 * to learn more about bot creation, supported platforms, and advanced usage.
 */
public class YesNoAndSentimentBot {

    public static void main(String[] args) {

        val question = intent("Question")
                .trainingSentence("QUESTION?")
                .parameter("question").fromFragment("QUESTION").entity(any());

        val affirmation = intent("Affirmation")
                .trainingSentence("AFFIRMATION")
                .parameter("affirmation").fromFragment("AFFIRMATION").entity(any());

        ReactPlatform reactPlatform = new ReactPlatform();
        ReactEventProvider reactEventProvider = new ReactEventProvider(reactPlatform);
        ReactIntentProvider reactIntentProvider = new ReactIntentProvider(reactPlatform);

        val init = state("Init");
        val awaitingInput = state("AwaitingInput");
        val handleWelcome = state("HandleWelcome");
        val handleQuestion = state("HandleQuestion");
        val handleAffirmation = state("HandleAffirmation");

        init
                .next()
                .when(eventIs(ReactEventProvider.ClientReady)).moveTo(handleWelcome);

        handleWelcome
                .body(context -> reactPlatform.reply(context,
                        "Hi, I am your favourite bot :) I can answer yes/no questions and be your psychologist!"))
                .next()
                .moveTo(awaitingInput);

        awaitingInput
                .next()
                .when(intentIs(question)).moveTo(handleQuestion)
                .when(intentIs(affirmation)).moveTo(handleAffirmation);

        handleQuestion
                .body(context -> {
                    Boolean isYesNo = (Boolean) context.getIntent().getNlpData().get("nlp.stanford.isYesNo");
                    if (isYesNo) {
                        Random rd = new Random();
                        String answer;
                        if (rd.nextBoolean()) {
                            answer = "Yes.";
                        }
                        else {
                            answer = "No.";
                        }
                        reactPlatform.reply(context, answer);
                    }
                    else {
                        reactPlatform.reply(context, "Sorry, I only answer yes/no questions.");
                    }
                })
                .next()
                .moveTo(awaitingInput);

        handleAffirmation
                .body(context -> {
                    String sentiment = (String) context.getIntent().getNlpData().get("nlp.stanford.sentiment");
                    reactPlatform.reply(context, "You have a " + sentiment.toLowerCase() + " attitude.");
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

        botConfiguration.addProperty(RECOGNITION_POSTPROCESSORS_KEY,
                "IsEnglishYesNoQuestion, EnglishSentiment");
        botConfiguration.setProperty("xatkit.dialogflow.projectId", "YOUR PROJECT ID");
        botConfiguration.setProperty("xatkit.dialogflow.credentials.path", "YOUR CREDENTIALS FILE PATH");
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
