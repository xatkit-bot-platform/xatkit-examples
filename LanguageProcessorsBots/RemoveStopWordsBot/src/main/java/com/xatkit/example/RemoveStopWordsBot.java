package com.xatkit.example;

import com.xatkit.core.XatkitBot;
import com.xatkit.core.recognition.IntentRecognitionProviderFactoryConfiguration;
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

/**
 * This is an example bot which uses the RemoveEnglishStopWordsPostProcessor, designed with Xatkit.
 * <p>
 * You can check our <a href="https://github.com/xatkit-bot-platform/xatkit/wiki">wiki</a>
 * to learn more about bot creation, supported platforms, and advanced usage.
 */
public class RemoveStopWordsBot {

   public static void main(String[] args) {

        val list = intent("List")
                .trainingSentence("LIST")
                .parameter("list").fromFragment("LIST").entity(any());

        ReactPlatform reactPlatform = new ReactPlatform();
        ReactEventProvider reactEventProvider = new ReactEventProvider(reactPlatform);
        ReactIntentProvider reactIntentProvider = new ReactIntentProvider(reactPlatform);

        val init = state("Init");
        val awaitingInput = state("AwaitingInput");
        val handleWelcome = state("HandleWelcome");
        val handleList = state("HandleList");

        init
                .next()
                .when(eventIs(ReactEventProvider.ClientReady)).moveTo(handleWelcome);

        handleWelcome
                .body(context -> reactPlatform.reply(context,
                        "Hi, I am your favourite bot :)"))
                .next()
                .moveTo(awaitingInput);

        awaitingInput
                .next()
                .when(intentIs(list)).moveTo(handleList);

        handleList
                .body(context -> {
                    String list_str = (String) context.getIntent().getValue("list");
                    String[] items = list_str.split(",");
                    for (String item : items) {
                        reactPlatform.reply(context, item);
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

        botConfiguration.setProperty(RECOGNITION_POSTPROCESSORS_KEY,"RemoveEnglishStopWords");
        botConfiguration.setProperty("xatkit.dialogflow.projectId", "YOUR PROJECT ID");
        botConfiguration.setProperty("xatkit.dialogflow.credentials.path", "YOUR CREDENTIALS PATH");
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
