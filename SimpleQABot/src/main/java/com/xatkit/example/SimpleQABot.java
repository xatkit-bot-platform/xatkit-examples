package com.xatkit.example;

import com.xatkit.core.XatkitBot;
import com.xatkit.library.core.CoreLibrary;
import com.xatkit.plugins.react.platform.ReactPlatform;
import com.xatkit.plugins.react.platform.io.ReactEventProvider;
import com.xatkit.plugins.react.platform.io.ReactIntentProvider;
import lombok.val;
import org.apache.commons.configuration2.BaseConfiguration;
import org.apache.commons.configuration2.Configuration;

import static com.xatkit.dsl.DSL.eventIs;
import static com.xatkit.dsl.DSL.fallbackState;
import static com.xatkit.dsl.DSL.intent;
import static com.xatkit.dsl.DSL.intentIs;
import static com.xatkit.dsl.DSL.model;
import static com.xatkit.dsl.DSL.state;

public class SimpleQABot {

    /*
     * Your bot is a plain Java application: you need to define a main method to make the created jar executable.
     */
    public static void main(String[] args) {

        /*
         * Define the intents our bot will react to.
         */
        val xatkitInfo = intent("XatkitInfo")
                .trainingSentence("What is Xatkit?");

        val wantBot = intent("WantBot")
                .trainingSentence("I want a bot");


        /*
         * Instantiate the platforms we will use in the bot definition.
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
        val greetUser = state("GreetUser");
        val awaitingInput = state("AwaitingInput");
        val handleGreetings = state("HandleGreetings");
        val giveInfo = state("GiveInfo");
        val askBotSize = state("AskBotSize");
        val answerSmallBot = state("AnswerSmallBot");
        val answerBigBot = state("AnswerBigBot");

        /*
         * Specify the content of the bot states (i.e. the behavior of the bot).
         */
        init
                .next()
                /*
                 * We check that the received event matches the ClientReady event defined in the
                 * ReactEventProvider. The list of events defined in a provider is available in the provider's
                 * wiki page.
                 */
                .when(eventIs(ReactEventProvider.ClientReady)).moveTo(greetUser);

        greetUser
                .body(context -> reactPlatform.reply(context, "Hi, I can help you, ask me a question!"))
                .next()
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
                .when(intentIs(CoreLibrary.Greetings)).moveTo(handleGreetings)
                .when(intentIs(xatkitInfo)).moveTo(giveInfo)
                .when(intentIs(wantBot)).moveTo(askBotSize);


        handleGreetings
                .body(context -> reactPlatform.reply(context, "Hi, what can I do for you?"))
                .next()
                    .moveTo(awaitingInput);

        giveInfo
                .body(context -> reactPlatform.reply(context, "It's an amazing platform!"))
                .next()
                    .moveTo(awaitingInput);

        askBotSize
                .body(context -> reactPlatform.reply(context, "Sure, how many intents do you want?"))
                .next()
                    .when(intentIs(CoreLibrary.NumberValue).and(context -> {
                        int numberOfIntents = Integer.parseInt((String) context.getIntent().getValue("value"));
                        return numberOfIntents <= 10;
                    })).moveTo(answerSmallBot)
                    .when(intentIs(CoreLibrary.NumberValue).and(context -> {
                        int numberOfIntents = Integer.parseInt((String) context.getIntent().getValue("value"));
                        return numberOfIntents > 10;
                    })).moveTo(answerBigBot)
                .fallback(context -> {
                    int retries = (Integer) context.getSession().getOrDefault("botsize.fallback", 0);
                    retries++;
                    context.getSession().put("botsize.fallback", retries);
                    if(retries < 3) {
                        reactPlatform.reply(context, "Sorry I didn't get it, could you give me a number?");
                    } else {
                        reactPlatform.reply(context, "Maybe you don't know what's a number ...");
                        reactPlatform.reply(context, "Anyway, I am done with you, come back when you'll have a number" +
                                " to give");
                    }

                });

        answerSmallBot
                .body(context -> reactPlatform.reply(context, "It's 10$!"))
                .next()
                    .moveTo(awaitingInput);

        answerBigBot
                .body(context -> reactPlatform.reply(context, "It's 100000$!"))
                .next()
                    .moveTo(awaitingInput);

        /*
         * The state that is executed if the engine doesn't find any navigable transition in a state and the state
         * doesn't contain a fallback.
         */
        val defaultFallback = fallbackState()
                .body(context -> reactPlatform.reply(context, "Sorry, I didn't, get it"));

        /*
         * Creates the bot model that will be executed by the Xatkit engine.
         * <p>
         * A bot model contains:
         * - A list of intents/events (or libraries) used by the bot. This allows to register the events/intents to the NLP
         * service.
         * - A list of platforms used by the bot. Xatkit will take care of starting and initializing the platforms
         * when starting the bot.
         * - A list of providers the bot should listen to for events/intents. As for the platforms Xatkit will take
         * care of initializing the provider when starting the bot.
         * - The list of states the compose the bot (this list can contain the init/fallback state, but it is optional)
         * - The entry point of the bot (a.k.a init state)
         * - The default fallback state: the state that is executed if the engine doesn't find any navigable
         * transition in a state and the state doesn't contain a fallback.
         */
        val botModel = model()
                .useIntent(CoreLibrary.Greetings)
                .useIntent(xatkitInfo)
                .useIntent(wantBot)
                .useIntent(CoreLibrary.NumberValue)
                .usePlatform(reactPlatform)
                .listenTo(reactEventProvider)
                .listenTo(reactIntentProvider)
                .useState(greetUser)
                .useState(awaitingInput)
                .useState(handleGreetings)
                .useState(giveInfo)
                .useState(askBotSize)
                .useState(answerSmallBot)
                .useState(answerBigBot)
                .initState(init)
                .defaultFallbackState(defaultFallback);

        Configuration botConfiguration = new BaseConfiguration();
        /*
         * Add configuration properties (e.g. authentication tokens, platform tuning, intent provider to use).
         * Check the corresponding platform's wiki page for further information on optional/mandatory parameters and
         * their values.
         */

        XatkitBot xatkitBot = new XatkitBot(botModel, botConfiguration);
        xatkitBot.run();
        /*
         * The bot is now started, you can check http://localhost:5000/admin to test it.
         * The logs of the bot are stored in the logs folder at the root of this project.
         */
    }
}
