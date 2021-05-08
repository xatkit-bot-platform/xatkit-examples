package com.xatkit.example;

import com.xatkit.core.XatkitBot;
import com.xatkit.core.recognition.processor.InternetSlangPreProcessor;
import com.xatkit.plugins.react.platform.ReactPlatform;
import com.xatkit.plugins.react.platform.io.ReactEventProvider;
import com.xatkit.plugins.react.platform.io.ReactIntentProvider;
import lombok.val;
import org.apache.commons.configuration2.BaseConfiguration;
import org.apache.commons.configuration2.Configuration;


import static com.xatkit.core.recognition.IntentRecognitionProviderFactoryConfiguration.ENABLE_RECOGNITION_ANALYTICS;
import static com.xatkit.core.recognition.IntentRecognitionProviderFactoryConfiguration.RECOGNITION_PREPROCESSORS_KEY;
import static com.xatkit.dsl.DSL.any;
import static com.xatkit.dsl.DSL.eventIs;
import static com.xatkit.dsl.DSL.fallbackState;
import static com.xatkit.dsl.DSL.intent;
import static com.xatkit.dsl.DSL.intentIs;
import static com.xatkit.dsl.DSL.model;
import static com.xatkit.dsl.DSL.state;


/**
 * This is an example bot which uses InternetSlangPreProcessor, designed with Xatkit.
 * <p>
 * You can check our <a href="https://github.com/xatkit-bot-platform/xatkit/wiki">wiki</a>
 * to learn more about bot creation, supported platforms, and advanced usage.
 */
public class SlangTranslatorBot {


    public static void main(String[] args) {

        val sentence = intent("sentence")
                .trainingSentence("TEXT")
                .parameter("text").fromFragment("TEXT").entity(any());

        ReactPlatform reactPlatform = new ReactPlatform();
        ReactEventProvider reactEventProvider = new ReactEventProvider(reactPlatform);
        ReactIntentProvider reactIntentProvider = new ReactIntentProvider(reactPlatform);

        val init = state("Init");
        val awaitingInput = state("AwaitingInput");
        val handleWelcome = state("HandleWelcome");
        val handleSentence = state("HandleSentence");

        init
                .next()
                .when(eventIs(ReactEventProvider.ClientReady)).moveTo(handleWelcome);

        handleWelcome
                .body(context -> reactPlatform.reply(context,
                        "Hi, I am a slang translator bot! Feel free to write something using internet expressions like"
                                + " \"idk\" or \"omg\" and I'll answer you translating them to standard expressions"))
                .next()
                .moveTo(awaitingInput);

        awaitingInput
                .next()
                .when(intentIs(sentence)).moveTo(handleSentence);

        handleSentence
                .body(context -> {
                    String processedInput = context.getIntent().getMatchedInput();
                    reactPlatform.reply(context, processedInput);
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
        botConfiguration.setProperty(RECOGNITION_PREPROCESSORS_KEY, "InternetSlangPreProcessor");
        // Comment the next line if you want to use the slang dictionary integrated in Xatkit
        botConfiguration.setProperty(InternetSlangPreProcessor.SLANG_DICTIONARY_SOURCE, "<Absolute path to your file>");
        botConfiguration.setProperty("xatkit.dialogflow.projectId", "<Your project ID>");
        botConfiguration.setProperty("xatkit.dialogflow.credentials.path","<Path to your credentials file>");
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
