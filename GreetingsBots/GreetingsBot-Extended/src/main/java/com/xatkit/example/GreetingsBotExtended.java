package com.xatkit.example;

import com.xatkit.core.XatkitBot;
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

public class GreetingsBotExtended {

    /*
     * Your bot is a plain Java application: you need to define a main method to make the created jar executable.
     */
    public static void main(String[] args) {

        /*
         * Define the intents our bot will react to.
         */
        val greetings = intent("Greetings")
                .trainingSentence("Hi")
                .trainingSentence("Hello")
                .trainingSentence("Good morning")
                .trainingSentence("Good afternoon");

        val howAreYou = intent("HowAreYou")
                .trainingSentence("How are you?")
                .trainingSentence("What's up?")
                .trainingSentence("How do you feel?");

        val fine = intent("Fine")
                .trainingSentence("I am fine")
                .trainingSentence("Fine")
                .trainingSentence("fine");

        val sad = intent("Sad")
                .trainingSentence("Not so good")
                .trainingSentence("I am sad")
                .trainingSentence("Sad");

        /*
         * Instantiate the platform we will use in the bot definition.
         */
        /*
         * Similarly, instantiate the intent/event providers we want to use.
         */
        ReactPlatform reactPlatform = new ReactPlatform();
        ReactEventProvider reactEventProvider = reactPlatform.getReactEventProvider();
        ReactIntentProvider reactIntentProvider = reactPlatform.getReactIntentProvider();

        /*
         * Create the states we want to use in our bot.
         */
        val init = state("Init");
        val awaitingInput = state("AwaitingInput");
        val handleWelcome = state("HandleWelcome");
        val handleWhatsUp = state("HandleWhatsUp");
        val handleFine = state("HandleFine");
        val handleSad = state("HandleSad");

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
                .when(eventIs(ReactEventProvider.ClientReady)).moveTo(awaitingInput);

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
                .when(intentIs(greetings)).moveTo(handleWelcome)
                .when(intentIs(howAreYou)).moveTo(handleWhatsUp);

        handleWelcome
                .body(context -> reactPlatform.reply(context, "Hi, nice to meet you!"))
                .next()
                /*
                 * A transition that is automatically navigated: in this case once we have answered the user we
                 * want to go back in a state where we wait for the next intent.
                 */
                .moveTo(awaitingInput);

        handleWhatsUp
                .body(context -> reactPlatform.reply(context, "I am fine and you?"))
                .next()
                    .when(intentIs(fine)).moveTo(handleFine)
                    .when(intentIs(sad)).moveTo(handleSad);

        handleFine
                .body(context -> reactPlatform.reply(context, "Great!"))
                .next()
                    .moveTo(awaitingInput);

        handleSad
                .body(context -> reactPlatform.reply(context, "Oh, let me know if I can do something for you :/"))
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
         * - A list of platforms used by the bot. Xatkit will take care of starting and initializing the platforms
         * when starting the bot.
         * - A list of providers the bot should listen to for events/intents. As for the platforms Xatkit will take
         * care of initializing the provider when starting the bot.
         * - The entry point of the bot (a.k.a init state). Full list of intents and states are calculated based on this entry point
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

        XatkitBot xatkitBot = new XatkitBot(botModel, botConfiguration);
        xatkitBot.run();
        /*
         * The bot is now started, you can check http://localhost:5000/admin to test it.
         * The logs of the bot are stored in the logs folder at the root of this project.
         */
    }
}
