package com.xatkit.example;

import com.google.gson.JsonElement;
import com.xatkit.core.XatkitBot;
import com.xatkit.plugins.github.platform.GithubPlatform;
import com.xatkit.plugins.github.platform.io.GithubWebhookEventProvider;
import com.xatkit.plugins.slack.platform.SlackPlatform;
import lombok.val;
import org.apache.commons.configuration2.BaseConfiguration;
import org.apache.commons.configuration2.Configuration;

import java.text.MessageFormat;

import static com.xatkit.dsl.DSL.eventIs;
import static com.xatkit.dsl.DSL.fallbackState;
import static com.xatkit.dsl.DSL.model;
import static com.xatkit.dsl.DSL.state;
import static com.xatkit.example.StarPayloadUtils.getRepositoryName;
import static com.xatkit.example.StarPayloadUtils.getRepositoryUrl;
import static com.xatkit.example.StarPayloadUtils.getSenderLogin;
import static com.xatkit.example.StarPayloadUtils.getSenderUrl;
import static com.xatkit.example.StarPayloadUtils.getStargazersCount;

public class GithubStargazerBot {

    /*
     * Your bot is a plain Java application: you need to define a main method to make the created jar executable.
     */
    public static void main(String[] args) {

        /*
         * Instantiate the platforms we will use in the bot definition.
         */
        SlackPlatform slackPlatform = new SlackPlatform();
        GithubPlatform githubPlatform = new GithubPlatform();
        /*
         * Similarly, instantiate the intent/event providers we want to use.
         */
        GithubWebhookEventProvider githubProvider = new GithubWebhookEventProvider(githubPlatform);

        /*
         * Create the states we want to use in our bot.
         */
        val init = state("Init");
        val handleNewStar = state("HandleNewStar");
        val handleDeletedStar = state("HandleDeletedStar");

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
                .when(eventIs(GithubWebhookEventProvider.StarCreated)).moveTo(handleNewStar)
                .when(eventIs(GithubWebhookEventProvider.StarDeleted)).moveTo(handleDeletedStar);


        handleNewStar
                .body(context -> {
                    JsonElement githubPayload = ((JsonElement) context.getEventInstance().getValue("json"));
                    String repositoryUrl = getRepositoryUrl(githubPayload);
                    String repositoryName = getRepositoryName(githubPayload);
                    String senderUrl = getSenderUrl(githubPayload);
                    String senderLogin = getSenderLogin(githubPayload);
                    int count = getStargazersCount(githubPayload);
                    slackPlatform.postMessage(context, MessageFormat.format(":tada: New star on <{0} | {1}> by <{2} |" +
                                    " {3}> :confetti_ball:\nCurrent stargazer count: {4} :clap::champagne:",
                            repositoryUrl, repositoryName, senderUrl, senderLogin, count),
                            (String) context.getConfiguration().get("slack.channel"),
                            (String) context.getConfiguration().get("slack.team"));
                })
                .next()
                .moveTo(init);

        handleDeletedStar
                .body(context -> {
                    JsonElement githubPayload =  ((JsonElement) context.getEventInstance().getValue("json"));
                    String repositoryUrl = getRepositoryUrl(githubPayload);
                    String repositoryName = getRepositoryName(githubPayload);
                    String senderUrl = getSenderUrl(githubPayload);
                    String senderLogin = getSenderLogin(githubPayload);
                    int count = getStargazersCount(githubPayload);
                    slackPlatform.postMessage(context,
                            MessageFormat.format(":sob: <{0} | {1}> unstarred <{2} | {3}> :face_with_head_bandage: " +
                                            "let''s forget about it and build awesome features! " +
                                            ":kissing_heart:\nCurrent stargazer count: {4} :stars:", senderUrl,
                                    senderLogin, repositoryUrl, repositoryName, count),
                            (String) context.getConfiguration().get("slack.channel"),
                            (String) context.getConfiguration().get("slack.team"));
                })
                .next()
                .moveTo(init);


        /*
         * The state that is executed if the engine doesn't find any navigable transition in a state and the state
         * doesn't contain a fallback.
         */
        val defaultFallback = fallbackState();

        /*
         * Creates the bot model that will be executed by the Xatkit engine.
         * <p>
         * A bot model contains:
         * - A list of intents/events (or libraries) used by the bot. This allows to register the events/intents to
         * the NLP
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
                .usePlatform(slackPlatform)
                .usePlatform(githubPlatform)
                .listenTo(githubProvider)
                .initState(init)
                .defaultFallbackState(defaultFallback);

        Configuration botConfiguration = new BaseConfiguration();
        /*
         * Slack platform configuration.
         */
        botConfiguration.addProperty("xatkit.slack.token", "<Your Slack Token>");
        botConfiguration.addProperty("slack.channel", "<Your Slack Channel Name/ID>");
        botConfiguration.addProperty("slack.team", "<Your Slack Team ID>");

        /*
         * Github platform configuration.
         */
        botConfiguration.addProperty("xatkit.github.oauth.token", "<Your Github OAuth Token>");

        XatkitBot xatkitBot = new XatkitBot(botModel, botConfiguration);
        xatkitBot.run();
        /*
         * The bot is now started, you can check http://localhost:5000/admin to test it.
         * The logs of the bot are stored in the logs folder at the root of this project.
         */
    }
}
