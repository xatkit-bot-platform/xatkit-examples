package com.xatkit.example;

import com.xatkit.core.XatkitBot;
import com.xatkit.core.recognition.dialogflow.DialogFlowConfiguration;
import com.xatkit.core.recognition.processor.LanguageDetectionPostProcessor;
import com.xatkit.core.recognition.processor.LanguageDetectionScore;
import com.xatkit.plugins.react.platform.ReactPlatform;
import com.xatkit.plugins.react.platform.io.ReactEventProvider;
import com.xatkit.plugins.react.platform.io.ReactIntentProvider;
import lombok.val;
import org.apache.commons.configuration2.BaseConfiguration;
import org.apache.commons.configuration2.Configuration;

import java.util.List;

import static com.xatkit.core.recognition.IntentRecognitionProviderFactoryConfiguration.ENABLE_RECOGNITION_ANALYTICS;
import static com.xatkit.core.recognition.IntentRecognitionProviderFactoryConfiguration.RECOGNITION_POSTPROCESSORS_KEY;
import static com.xatkit.dsl.DSL.any;
import static com.xatkit.dsl.DSL.eventIs;
import static com.xatkit.dsl.DSL.fallbackState;
import static com.xatkit.dsl.DSL.intent;
import static com.xatkit.dsl.DSL.intentIs;
import static com.xatkit.dsl.DSL.model;
import static com.xatkit.dsl.DSL.state;
import static java.util.Objects.nonNull;


/**
 * This is an example bot which uses LanguagePredictionPostProcessor, designed with Xatkit.
 * <p>
 * You can check our <a href="https://github.com/xatkit-bot-platform/xatkit/wiki">wiki</a>
 * to learn more about bot creation, supported platforms, and advanced usage.
 */
public class LanguageDetectorBot {


    public static void main(String[] args) {

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
                        "Hi, I am your favourite bot :) I can guess the language you talk. Just say something!"))
                .next()
                .moveTo(awaitingInput);

        awaitingInput
                .next()
                .when(intentIs(question)).moveTo(handleComment);

        handleComment
                .body(context -> {
                    LanguageDetectionScore score1 = (LanguageDetectionScore)
                            context.getIntent().getNlpData().get("nlp.opennlp.langdetect.lastInput");
                    if (nonNull(score1)) {
                        List<String> languages = score1.getLanguageNames();
                        reactPlatform.reply(context,
                                "I think this message language is '" + languages.get(0) + "'");
                    }
                    LanguageDetectionScore score2 =
                            (LanguageDetectionScore) context.getSession().get("nlp.opennlp.langdetect.lastNInputs");
                    if (nonNull(score2)) {
                        List<String> languages = score2.getLanguageNames();
                        reactPlatform.reply(context,
                                "And based on the entire conversation, I think you are writing in '"
                                        + languages.get(0) + "' language");
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
        botConfiguration.setProperty(RECOGNITION_POSTPROCESSORS_KEY, "LanguageDetectionPostProcessor");
        botConfiguration.setProperty(LanguageDetectionPostProcessor.MAX_NUM_USER_MESSAGES, 10);
        botConfiguration.setProperty(LanguageDetectionPostProcessor.MAX_NUM_LANGUAGES_IN_SCORE, 3);
        botConfiguration.setProperty(LanguageDetectionPostProcessor.OPENNLP_MODEL_PATH_PARAMETER_KEY, "<Path to the "
                + "OpenNLP model");
        botConfiguration.setProperty(DialogFlowConfiguration.PROJECT_ID_KEY, "<DialogFlow Project ID>");
        botConfiguration.setProperty(DialogFlowConfiguration.GOOGLE_CREDENTIALS_PATH_KEY, "<DialogFlow Credentials");
        botConfiguration.setProperty(ENABLE_RECOGNITION_ANALYTICS, false);

        XatkitBot xatkitBot = new XatkitBot(botModel, botConfiguration);
        xatkitBot.run();
        /*
         * The bot is now started, you can check http://localhost:5000/admin to test it.
         * The logs of the bot are stored in the logs folder at the root of this project.
         */
    }

}
