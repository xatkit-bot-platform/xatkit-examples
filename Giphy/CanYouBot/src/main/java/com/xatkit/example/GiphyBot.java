package com.xatkit.example;

import com.xatkit.core.XatkitBot;
import com.xatkit.core.recognition.dialogflow.DialogFlowConfiguration;
import com.xatkit.library.core.CoreLibrary;
import com.xatkit.plugins.giphy.platform.GiphyPlatform;
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
import static java.util.Objects.isNull;

public class GiphyBot {

    /*
     * Your bot is a plain Java application: you need to define a main method to make the created jar executable.
     */
    public static void main(String[] args) {

        /*
         * Define the intents our bot will react to.
         */
        val canYou = intent("CanYou")
                .trainingSentence("Can you dance?")
                .trainingSentence("Have you seen Scarface?")
                .trainingSentence("Do you know Jim Carrey?")
                .trainingSentence("Any thoughts about politics?")
                .trainingSentence("What do you think about testing?")
                .trainingSentence("dance?")

                .trainingSentence("Can you dance")
                .trainingSentence("Have you seen Scarface")
                .trainingSentence("Do you know Jim Carrey")
                .trainingSentence("Any thoughts about politics")
                .trainingSentence("What do you think about testing")
                .trainingSentence("dance")
                .parameter("request")
                .fromFragment("dance", "Scarface", "Jim Carrey", "politics", "testing")
                .entity(any());

        /*
         * Instantiate the platforms we will use in the bot definition.
         */
        ReactPlatform reactPlatform = new ReactPlatform();
        GiphyPlatform giphyPlatform = new GiphyPlatform();
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
        val initialGreetings = state("InitialGreetings");
        val handleGreetings = state("HandleGreetings");
        val handleCanYou = state("HandleCanYou");
        val handleHelp = state("HandleHelp");

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
                .when(eventIs(ReactEventProvider.ClientReady)).moveTo(initialGreetings);

        initialGreetings
                .body(context -> {
                    reactPlatform.reply(context, "Hi! What can I do for you?  \nYou can " +
                            "start with something like `Can you <whatever you want>?`  \nThis bot is inspired by [this " +
                            "article](https://uxdesign.cc/wanna-build-a-superbot-that-can-do-anything-heres-how-d8eeeeef1882)");
                })
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
                .when(intentIs(canYou)).moveTo(handleCanYou)
                .when(intentIs(CoreLibrary.Help)).moveTo(handleHelp)
                .when(eventIs(ReactEventProvider.ClientReady)).moveTo(awaitingInput);


        handleGreetings
                .body(context -> reactPlatform.reply(context, "Hi, I can do many things, challenge me!  \nYou can " +
                        "start with something like `Can you <whatever you want>?`  \nThis bot is inspired by [this " +
                        "article](https://uxdesign.cc/wanna-build-a-superbot-that-can-do-anything-heres-how-d8eeeeef1882)"))
                .next()
                    .moveTo(awaitingInput);

        handleCanYou
                .body(context -> {
                    String request = (String) context.getIntent().getValue("request");
                    if(isNull(request) || request.isEmpty()) {
                        reactPlatform.reply(context, "Sorry, I didn't get it, could you rephrase?");
                    } else {
                        String url = giphyPlatform.getGif(context, request);
                        reactPlatform.reply(context, "Sure!  \n![look](" + url + ")");
                        reactPlatform.reply(context, "Anything else?");
                    }
                })
                .next()
                    .moveTo(awaitingInput);

        handleHelp
                .body(context -> reactPlatform.reply(context, "Ask me if I can do something and I'll tell you!  \nYou" +
                        " can start with something like `Can you <whatever you want>?`"))
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
                .usePlatform(reactPlatform)
                .usePlatform(giphyPlatform)
                .listenTo(reactEventProvider)
                .listenTo(reactIntentProvider)
                .initState(init)
                .defaultFallbackState(defaultFallback);

        Configuration botConfiguration = new BaseConfiguration();
        /*
         * Add you Giphy token here to have access to the Giphy API (this is required by the Giphy platform).
         */
        botConfiguration.addProperty("xatkit.giphy.token", "<Your Giphy Token>");
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
