package com.xatkit.example;

import com.xatkit.core.XatkitBot;
import com.xatkit.core.recognition.processor.toxicity.detoxify.DetoxifyConfiguration;
import com.xatkit.core.recognition.processor.toxicity.detoxify.DetoxifyScore;
import com.xatkit.core.recognition.processor.toxicity.perspectiveapi.PerspectiveApiConfiguration;
import com.xatkit.core.recognition.processor.toxicity.perspectiveapi.PerspectiveApiScore;
import com.xatkit.plugins.react.platform.ReactPlatform;
import com.xatkit.plugins.react.platform.io.ReactEventProvider;
import com.xatkit.plugins.react.platform.io.ReactIntentProvider;
import lombok.val;
import org.apache.commons.configuration2.BaseConfiguration;
import org.apache.commons.configuration2.Configuration;

import static com.xatkit.core.recognition.processor.ToxicityPostProcessorConfiguration.USE_DETOXIFY;
import static com.xatkit.core.recognition.processor.ToxicityPostProcessorConfiguration.USE_PERSPECTIVE_API;
import static com.xatkit.dsl.DSL.any;
import static com.xatkit.dsl.DSL.eventIs;
import static com.xatkit.dsl.DSL.fallbackState;
import static com.xatkit.dsl.DSL.intent;
import static com.xatkit.dsl.DSL.intentIs;
import static com.xatkit.dsl.DSL.model;
import static com.xatkit.dsl.DSL.state;
import static com.xatkit.core.recognition.IntentRecognitionProviderFactoryConfiguration.*;
import static java.util.Objects.nonNull;


/**
 * This is an example bot which uses ToxicityPostProcessor, designed with Xatkit.
 * <p>
 * You can check our <a href="https://github.com/xatkit-bot-platform/xatkit/wiki">wiki</a>
 * to learn more about bot creation, supported platforms, and advanced usage.
 */
public class ToxicityDetectorBot {


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
                        "Hi, I am your favourite bot :) I will score your comments toxicity."))
                .next()
                .moveTo(awaitingInput);

        awaitingInput
                .next()
                .when(intentIs(question)).moveTo(handleComment);

        handleComment
                .body(context -> {
                    PerspectiveApiScore score1 =
                            (PerspectiveApiScore)context.getIntent().getNlpData().get("nlp.perspectiveapi");
                    if(nonNull(score1)) {
                        /*
                         * Ignore this part if Perspective API is not enabled for the bot.
                         */
                    Double toxicity1 = score1.getToxicityScore();
                        if (!toxicity1.equals(PerspectiveApiScore.DEFAULT_SCORE)) {
                            reactPlatform.reply(context,
                                    "[PerspectiveAPI] Your comment was " + Math.round(toxicity1 * 100) + "% toxic");
                        } else {
                            reactPlatform.reply(context, "[PerspectiveAPI] Sorry, I can't compute your comment toxicity");
                        }
                    }
                    DetoxifyScore score2 =
                            (DetoxifyScore) context.getIntent().getNlpData().get("nlp.detoxify");
                    if(nonNull(score2)) {
                        /*
                         * Ignore this part if Detoxify is not enabled for the bot.
                         */
                        Double toxicity2 = score2.getToxicityScore();
                        if (!toxicity2.equals(DetoxifyScore.DEFAULT_SCORE)) {
                            reactPlatform.reply(context,
                                    "[Detoxify] Your comment was " + Math.round(toxicity2 * 100) + "% toxic");
                        } else {
                            reactPlatform.reply(context, "[Detoxify] Sorry, I can't compute your comment toxicity");
                        }
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
        botConfiguration.setProperty(RECOGNITION_POSTPROCESSORS_KEY,"ToxicityPostProcessor");
        botConfiguration.setProperty(USE_PERSPECTIVE_API, true);
        botConfiguration.setProperty(PerspectiveApiConfiguration.API_KEY, "YOUR-PERSPECTIVEAPI-KEY");
        botConfiguration.setProperty(PerspectiveApiConfiguration.LANGUAGE, "en");
        botConfiguration.setProperty(USE_DETOXIFY, true);
        botConfiguration.setProperty(DetoxifyConfiguration.DETOXIFY_SERVER_URL, "YOUR-SERVER-URL");
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
