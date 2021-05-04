package com.xatkit.example;

import com.xatkit.core.XatkitBot;

import com.xatkit.core.recognition.processor.EmojiToTextPreProcessor;
import com.xatkit.plugins.react.platform.ReactPlatform;
import com.xatkit.plugins.react.platform.io.ReactEventProvider;
import com.xatkit.plugins.react.platform.io.ReactIntentProvider;
import lombok.val;
import org.apache.commons.configuration2.BaseConfiguration;
import org.apache.commons.configuration2.Configuration;

import static com.xatkit.core.recognition.IntentRecognitionProviderFactoryConfiguration.ENABLE_RECOGNITION_ANALYTICS;

import java.util.HashMap;
import java.util.Map;

import static com.xatkit.core.recognition.IntentRecognitionProviderFactoryConfiguration.RECOGNITION_PREPROCESSORS_KEY;
import static com.xatkit.dsl.DSL.any;
import static com.xatkit.dsl.DSL.countryCode;
import static com.xatkit.dsl.DSL.eventIs;
import static com.xatkit.dsl.DSL.fallbackState;
import static com.xatkit.dsl.DSL.intent;
import static com.xatkit.dsl.DSL.intentIs;
import static com.xatkit.dsl.DSL.model;
import static com.xatkit.dsl.DSL.state;


/**
 * This is an example bot which uses EmojiToTextPreProcessor, designed with Xatkit.
 * <p>
 * You can check our <a href="https://github.com/xatkit-bot-platform/xatkit/wiki">wiki</a>
 * to learn more about bot creation, supported platforms, and advanced usage.
 */
public class EmojiToTextBot {


    public static void main(String[] args) {

        val IAmFrom = intent("IAmFrom")
                .trainingSentence("I am from COUNTRY")
                .trainingSentence("From COUNTRY")
                .trainingSentence("COUNTRY")
                .parameter("country").fromFragment("COUNTRY").entity(countryCode());

        val FavouriteAnimal = intent("FavouriteAimal")
                .trainingSentence("My favorite animal is ANIMAL")
                .trainingSentence("It is ANIMAL")
                .trainingSentence("It is the ANIMAL")
                .trainingSentence("The ANIMAL")
                .trainingSentence("ANIMAL")
                .parameter("animal").fromFragment("ANIMAL").entity(any());

        ReactPlatform reactPlatform = new ReactPlatform();
        ReactEventProvider reactEventProvider = new ReactEventProvider(reactPlatform);
        ReactIntentProvider reactIntentProvider = new ReactIntentProvider(reactPlatform);

        val init = state("Init");
        val handleWelcome = state("HandleWelcome");
        val handleIAmFrom = state("HandleIAmFrom");
        val handleAnimal = state("HandleAnimal");


        init
                .next()
                .when(eventIs(ReactEventProvider.ClientReady)).moveTo(handleWelcome);

        handleWelcome
                .body(context -> reactPlatform.reply(context,
                        "Hi! Where are you from?"))
                .next()
                .when(intentIs(IAmFrom)).moveTo(handleIAmFrom);

        handleIAmFrom
                .body(context -> {
                    Map<Object, Object> countryMap = (Map<Object, Object>) context.getIntent().getValue("country");
                    String country = (String) countryMap.get("name");
                    reactPlatform.reply(context, country + "? That's cool!");
                    reactPlatform.reply(context,  "What is your favorite animal?");

                })
                .next()
                .when(intentIs(FavouriteAnimal)).moveTo(handleAnimal);

        handleAnimal
                .body(context -> {
                    String animal = (String) context.getIntent().getValue("animal");
                    reactPlatform.reply(context, animal + "? Good choice \uD83D\uDE0F");
                    reactPlatform.reply(context,  "Thank you for your collaboration \uD83D\uDE0A");
                })
                .next()
                .moveTo(handleWelcome);

        val defaultFallback = fallbackState()
                .body(context -> reactPlatform.reply(context, "Sorry, I didn't get it"));

        val botModel = model()
                .usePlatform(reactPlatform)
                .listenTo(reactEventProvider)
                .listenTo(reactIntentProvider)
                .initState(init)
                .defaultFallbackState(defaultFallback);

        Configuration botConfiguration = new BaseConfiguration();
        botConfiguration.setProperty(RECOGNITION_PREPROCESSORS_KEY, "EmojiToTextPreProcessor");
        botConfiguration.setProperty(EmojiToTextPreProcessor.REMOVE_EMOJIS, false);
        botConfiguration.setProperty("xatkit.dialogflow.projectId", "YOUR-PROJECT-ID");
        botConfiguration.setProperty("xatkit.dialogflow.credentials.path", "YOUR-CREDENTIALS-PATH");
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
