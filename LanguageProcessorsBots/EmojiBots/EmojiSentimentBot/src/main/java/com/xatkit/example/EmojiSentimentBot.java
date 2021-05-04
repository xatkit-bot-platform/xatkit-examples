package com.xatkit.example;

import com.xatkit.core.XatkitBot;

import com.xatkit.core.recognition.processor.EmojiData;
import com.xatkit.core.recognition.processor.EmojiPostProcessor;
import com.xatkit.plugins.react.platform.ReactPlatform;
import com.xatkit.plugins.react.platform.io.ReactEventProvider;
import com.xatkit.plugins.react.platform.io.ReactIntentProvider;
import lombok.val;
import org.apache.commons.configuration2.BaseConfiguration;
import org.apache.commons.configuration2.Configuration;

import static com.xatkit.core.recognition.IntentRecognitionProviderFactoryConfiguration.ENABLE_RECOGNITION_ANALYTICS;
import static com.xatkit.core.recognition.IntentRecognitionProviderFactoryConfiguration.RECOGNITION_POSTPROCESSORS_KEY;

import java.util.Set;

import static com.xatkit.dsl.DSL.any;
import static com.xatkit.dsl.DSL.eventIs;
import static com.xatkit.dsl.DSL.fallbackState;
import static com.xatkit.dsl.DSL.intent;
import static com.xatkit.dsl.DSL.intentIs;
import static com.xatkit.dsl.DSL.model;
import static com.xatkit.dsl.DSL.state;


/**
 * This is an example bot which uses EmojiPostProcessor, designed with Xatkit.
 * <p>
 * You can check our <a href="https://github.com/xatkit-bot-platform/xatkit/wiki">wiki</a>
 * to learn more about bot creation, supported platforms, and advanced usage.
 */
public class EmojiSentimentBot {


    public static void main(String[] args) {

        val Emojis = intent("Emojis")
                .trainingSentence("EMOJIS")
                .parameter("emojis").fromFragment("EMOJIS").entity(any());

        ReactPlatform reactPlatform = new ReactPlatform();
        ReactEventProvider reactEventProvider = new ReactEventProvider(reactPlatform);
        ReactIntentProvider reactIntentProvider = new ReactIntentProvider(reactPlatform);

        val init = state("Init");
        val handleEmojis = state("HandleEmojis");
        val handleWelcome = state("HandleWelcome");

        init
                .next()
                .when(eventIs(ReactEventProvider.ClientReady)).moveTo(handleWelcome);

        handleWelcome
                .body(context -> {
                    reactPlatform.reply(context, "Hi \uD83D\uDC4B I am a sentiment analyzer bot");
                    reactPlatform.reply(context, "Feel free to write some emojis and I'll guess your mood!");
                })
                .next()
                .moveTo(handleEmojis);

        handleEmojis
                .body(context -> {
                    Set<EmojiData> emojis = (Set<EmojiData>) context.getIntent().getNlpData()
                            .get("nlp.emoji.emojiDataSet");
                    String message = "Sorry, couldn't read any emoji...";
                    if (!emojis.isEmpty()) {

                        double sentiment = 0;
                        for (EmojiData e : emojis) {
                            double positiveSentiment = e.getPositiveSentiment() * e.getOccurrences();
                            sentiment += positiveSentiment;
                            double negativeSentiment = e.getNegativeSentiment() * e.getOccurrences();
                            sentiment -= negativeSentiment;
                        }
                        /*
                         * sentiment is now in [-1,1]. -1 represents negative sentiment, while 1 represents positive
                         * sentiment.
                         * Note that this metric is probably too simple: it is used in this example to showcase what
                         * can be done with the sentiment analysis processor for emojis, but it should be adapted to
                         * your specific use case.
                         */
                        reactPlatform.reply(context, String.valueOf(sentiment));
                        if (Math.abs(sentiment) < 0.1) {
                            message = "I think your mood is quite neutral... \uD83D\uDE10";
                        } else if (sentiment > 0) {
                            message = "You seem to be quite happy \uD83D\uDE09";
                        } else {
                            message = "You look sad...\uD83D\uDE15";
                        }
                    }
                    reactPlatform.reply(context, message);
                })
                .next()
                .when(intentIs(Emojis)).moveTo(handleEmojis);


        val defaultFallback = fallbackState()
                .body(context -> reactPlatform.reply(context, "Sorry, I didn't get it"));

        val botModel = model()
                .usePlatform(reactPlatform)
                .listenTo(reactEventProvider)
                .listenTo(reactIntentProvider)
                .initState(init)
                .defaultFallbackState(defaultFallback);

        Configuration botConfiguration = new BaseConfiguration();
        botConfiguration.setProperty(RECOGNITION_POSTPROCESSORS_KEY,"EmojiPostProcessor");
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
