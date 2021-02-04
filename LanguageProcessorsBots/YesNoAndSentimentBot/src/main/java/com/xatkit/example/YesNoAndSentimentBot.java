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
 * This is an example greetings bot designed with Xatkit.
 * <p>
 * You can check our <a href="https://github.com/xatkit-bot-platform/xatkit/wiki">wiki</a>
 * to learn more about bot creation, supported platforms, and advanced usage.
 */
public class YesNoAndSentimentBot {

    /*
     * Your bot is a plain Java application: you need to define a main method to make the created jar executable.
     */
    public static void main(String[] args) {

        /*
         * Define the intents our bot will react to.
         */
        val question = intent("Question")
                .trainingSentence("QUESTION?")
                .parameter("question").fromFragment("QUESTION").entity(any());

        val affirmation = intent("Affirmation")
                .trainingSentence("AFFIRMATION")
                .parameter("affirmation").fromFragment("AFFIRMATION").entity(any());
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
        val handleQuestion = state("HandleQuestion");
        val handleAffirmation = state("HandleAffirmation");

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

        handleWelcome
                .body(context -> reactPlatform.reply(context,
                        "Hi, I am your favourite bot :) I can answer yes/no questions and be your psychologist!"))
                .next()
                /*
                 * A transition that is automatically navigated: in this case once we have answered the user we
                 * want to go back in a state where we wait for the next intent.
                 */
                .moveTo(awaitingInput);

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
                .when(intentIs(question)).moveTo(handleQuestion)
                .when(intentIs(affirmation)).moveTo(handleAffirmation);


        handleQuestion
                .body(context -> {
                    //String keyword = (String) context.getIntent().getValue("keyword");
                    Boolean isYesNo = (Boolean) context.getIntent().getNlpData().get("nlp.stanford.isYesNo");
                    //String sentiment = (String) context.getIntent().getNlpData().get("nlp.stanford.sentiment");
                    //reactPlatform.reply(context, "Your keyword is: \"" + keyword + "\", and isYesNo = "
                    //        + isYesNo + ", and sentiment = " + sentiment);
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

        botConfiguration.addProperty(RECOGNITION_POSTPROCESSORS_KEY,
                "IsEnglishYesNoQuestion, EnglishSentiment");
        botConfiguration.setProperty("xatkit.dialogflow.projectId", "YOUR PROJECT ID");
        botConfiguration.setProperty("xatkit.dialogflow.credentials.path", "YOUR CREDENTIALS FILE PATH");
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
