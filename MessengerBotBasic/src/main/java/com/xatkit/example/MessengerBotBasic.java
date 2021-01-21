package com.xatkit.example;

import com.xatkit.core.XatkitBot;
import com.xatkit.core.platform.action.RuntimeArtifactAction;
import com.xatkit.dsl.model.ExecutionModelProvider;
import com.xatkit.dsl.state.BodyStep;
import com.xatkit.plugins.messenger.platform.MessengerPlatform;
import com.xatkit.plugins.messenger.platform.MessengerUtils;
import com.xatkit.plugins.messenger.platform.entity.Attachment;
import com.xatkit.plugins.messenger.platform.entity.Attachment.AttachmentType;
import com.xatkit.plugins.messenger.platform.entity.MediaElement;
import com.xatkit.plugins.messenger.platform.entity.response.MessengerResponse;
import com.xatkit.plugins.messenger.platform.entity.templates.ButtonTemplate;
import com.xatkit.plugins.messenger.platform.entity.templates.GenericTemplate;
import com.xatkit.plugins.messenger.platform.entity.Message;
import com.xatkit.plugins.messenger.platform.entity.templates.MediaTemplate;
import com.xatkit.plugins.messenger.platform.entity.payloads.GenericTemplatePayload;
import com.xatkit.plugins.messenger.platform.io.MessengerIntentProvider;
import fr.inria.atlanmod.commons.log.Log;
import lombok.val;
import org.apache.commons.configuration2.BaseConfiguration;

import java.io.IOException;
import java.util.*;

import static com.xatkit.dsl.DSL.*;

public class MessengerBotBasic {
    public static void main(String[] args) {


        /*
            As of December 16 2020, some features on will not work in Europe.
            https://developers.facebook.com/docs/messenger-platform/europe-updates

            We have noted those features below where they are used.
            Some additional Webhooks that don't work are Deliveries and Reads.
            These don't work anywhere.

            The same goes for the SenderActions with the features typing_on and typing_off.
         */


        // Initialization

        val platform = new MessengerPlatform();
        val intentProvider = new MessengerIntentProvider(platform);
        Log.info("Created the Messenger Platform");

        /*
            Images in Facebook Messenger are reusable.
            It is a good idea to keep files you want to send organized.
            We have created a FileManager class for that purpose.

            It creates a com.xatkit.plugins.messenger.platform.entity.File
            using the filepath and the attachment type that will be used
            in the message.

            The five attachment types are:
            * audio
            * video
            * image
            * template
            * file

            You can read more about attachment types here:
            https://developers.facebook.com/docs/messenger-platform/reference/attachment-upload-api#attachment

            As of December 16 2020, Audio, Video, and File attachment ARE NOT SUPPORTED
            on Webview (they are still supported on Android and iOS) in Europe. Images still work.
         */
        val files = FileManager.get();
        Log.info("Created the File Manager");
        try {
            files.loadFileToName(AttachmentType.image, "pic.jpg", "picture");
        } catch (IOException e) {
            Log.error("Loading Files failed with the following message: {0}", e.getMessage());
        }
        Log.info("Finished loading files.");


        // States

        val defaultFallback = fallbackState();
        val init = state("Init");
        val waiting = state("Waiting");
        val handleWelcome = state("HandleWelcome");
        val handleWelcome2 = state("HandleWelcome2");
        val handleHowAreYou = state("HandleWelcome");
        val handleLink = state("HandleLink");
        val handleFile = state("HandleFile");
        val handleGenericTemplate = state("HandleTemplate");
        val handleButtonTemplate = state("ButtonTemplate");
        val handleMediaTemplate = state("MediaTemplate");
        val handlePostBackTemplate = state("PostBackTemplate");
        val handleReaction = state("Reaction");
        val handleUnReaction = state("UnReaction");


        // Intents

        val greetings = intent("Greetings")
                .trainingSentence("Hi")
                .trainingSentence("Hi!")
                .trainingSentence("Hello")
                .trainingSentence("Hello!")
                .trainingSentence("Good morning")
                .trainingSentence("Good morning!")
                .trainingSentence("Good afternoon")
                .trainingSentence("Good afternoon!")
                .trainingSentence("Greetings");

        val link = intent("Link")
                .trainingSentence("Link");

        val file = intent("File")
                .trainingSentence("File");

        val genericTemplate = intent("GenericTemplate")
                .trainingSentence("GenericTemplate");

        val buttonTemplate = intent("ButtonTemplate")
                .trainingSentence("ButtonTemplate");

        val mediaTemplate = intent("MediaTemplate")
                .trainingSentence("MediaTemplate");

        val postBackTemplate = intent("PostBackTemplate")
                .trainingSentence("PostBackTemplate")
                .trainingSentence("Help");

        val howAreYou = intent("HowAreYou")
                .trainingSentence("How are you?")
                .trainingSentence("How are you doing?");


        // State Descriptions

        List<BodyStep> hubStates = new LinkedList<>();
        hubStates.add(init);
        hubStates.add(waiting);
        for (BodyStep knot : hubStates) {
            knot.
                    next()
                    .when(intentIs(greetings)).moveTo(handleWelcome)
                    .when(intentIs(howAreYou)).moveTo(handleHowAreYou)
                    .when(intentIs(link)).moveTo(handleLink)
                    .when(intentIs(file)).moveTo(handleFile)
                    .when(intentIs(genericTemplate)).moveTo(handleGenericTemplate)
                    .when(intentIs(buttonTemplate)).moveTo(handleButtonTemplate)
                    .when(intentIs(mediaTemplate)).moveTo(handleMediaTemplate)
                    .when(intentIs(postBackTemplate)).moveTo(handlePostBackTemplate)
                    .when(eventIs(MessengerIntentProvider.MessageReact)).moveTo(handleReaction)
                    .when(eventIs(MessengerIntentProvider.MessageUnreact)).moveTo(handleUnReaction);
        }

        defaultFallback
                .body(context -> platform.reply(context, "Sorry, I didn't understand that."));

        handleWelcome
                .body(context -> platform.reply(context, "Hi, nice to meet you!"))
                .next()
                .moveTo(waiting);

        handleWelcome2
                .body(context -> platform.reply(context, "Hi again!"))
                .next()
                .moveTo(waiting);

        handleHowAreYou
                .body(context -> {
                    MessengerResponse reply = platform.reply(context, "I'm fine.");

                    //Will use session based map to remember what text came along with each MessageID. This map will be used when unreacting from messages.
                    //Only states which put elements to this map can be unreacted from, saving space in maps
                    Map<String, String> messageIDtoMessageText = (Map<String, String>) context.getSession().computeIfAbsent("MessageIDtoMessageText", x -> new HashMap<String, String>());
                    messageIDtoMessageText.put(reply.getMessageId(), "I'm fine.");
                })
                .next()
                .moveTo(waiting);

        handleLink
                .body(context -> platform.reply(context, new Message(AttachmentType.image, "https://xatkit.com/wp-content/uploads/2019/09/small-logo.png", true)))
                .next()
                .moveTo(waiting);

        /*
            To send files, use the sendFile(@NonNull StateContext context, @NonNull File file) method.
            com.xatkit.plugins.messenger.platform.entity.File is a class that keeps track of everything
            involved in sending a file.

            You can also use the sendFile(@NonNull StateContext context, @NonNull String attachmentId, @NonNull Attachment.AttachmentType attachmentType) method
            when manually keeping track of attachment_id-s.
        */
        handleFile
                .body(context -> platform.sendFile(context, files.getFile("picture")))
                .next()
                .moveTo(waiting);

        /*
            Our connector supports 3 types of template.

            We have created new primitives to help you in creating these templates.
            You can send as a message by adding the template to a new attachment as a payload.
            Use the .getPayload() method to finalize your template.

            GenericTemplate can use images, buttons and text.
            In Facebook's own words, "will send a horizontally scrollable carousel of items".
            To group elements together to the same tab, let their elementID be equal.

            MediaTemplate can use media elements already uploaded to Facebook via an url.

            ButtonTemplate will create a menu of buttons. These are either URL buttons or PostBack buttons.
            How PostBacks work, will be described further below.

            As of December 16 2020, GenericTemplates, MediaTemplates, and ButtonTemplates ARE NOT SUPPORTED
            on Webview (they are still supported on Android and iOS) in Europe.

            This also means that URL Button and Postback Button ARE NOT SUPPORTED
            on Webview (they are still supported on Android and iOS) in Europe.
        */
        GenericTemplate template1 = new GenericTemplate()
                .setImageAspectRatio(GenericTemplatePayload.ImageAspectRatio.horizontal)
                .constructUrlButtonToElement(1, "Check out Xatkit", "https://xatkit.com/", null)
                .constructPostbackButtonToElement(1, "Postback B", "I clicked the button")
                .setDefaultElementButton(1, "https://xatkit.com/", null)
                .constructElement(1, "Xatkit", "EASIEST WAY TO GET YOUR OWN SMART CHATBOT", "https://pbs.twimg.com/profile_banners/1153789919954948096/1569877884/1500x500")
                .constructUrlButtonToElement(2, "Github", "https://github.com/xatkit-bot-platform/xatkit", null)
                .constructUrlButtonToElement(2, "Why a chatbot?", "https://youtu.be/P8ieU0dleUE", null)
                .setDefaultElementButton(2, "https://www.youtube.com/watch?v=qe__Zh7TGc0", null)
                .constructElement(2, "About", null, "https://xatkit.com/wp-content/uploads/2020/04/whychatbot.png");

        MediaTemplate template3 = new MediaTemplate()
                .constructUrlButton("URL #1", "https://github.com/xatkit-bot-platform/xatkit", null)
                .constructElementUsingUrl(MediaElement.MediaType.video, "https://www.facebook.com/118526656660/videos/546202052685070/?__cft__[0]=AZX3iMFMQTYIUOh2qYWkAdCQaTEY3cXCIbjhpZXweSfJ7TF3G-MIuVsIypxmQPljs4gz_O4b3qZd-leVPpiQvBZQ7ZcqMrZVgWW-IiYmTnSukWhtXfIm_Zszb6jjok1qhUEuCiUZKW92mbgcZ0PmvIUmCKjqTxhJlha_QbaHUYs5yc6wxhJveFBgnPa_m8bsoPo");

        ButtonTemplate template2 = new ButtonTemplate()
                .setText("This is a very good button template with over 0 buttons")
                .constructPostbackButton("A postback", "Hi")
                .constructUrlButton("URL #1", "https://github.com/xatkit-bot-platform/xatkit", null)
                .constructUrlButton("URL #2", "https://www.youtube.com/watch?v=qe__Zh7TGc0", null);

        ButtonTemplate template4 = new ButtonTemplate()
                .setText("Check out our functionality")
                .constructPostbackButton("Send me a file", "File")
                .constructPostbackButton("I want to see a generic template", "GenericTemplate")
                .constructPostbackButton("Greet me again", "Hi");

        handleGenericTemplate
                .body(context -> platform.reply(context, new Message(new Attachment(template1.getPayload()))))
                .next()
                .moveTo(waiting);

        handleButtonTemplate
                .body(context -> platform.reply(context, new Message(new Attachment(template2.getPayload()))))
                .next()
                .moveTo(waiting);

        handleMediaTemplate
                .body(context -> platform.reply(context, new Message(new Attachment(template3.getPayload()))))
                .next()
                .moveTo(waiting);

        handlePostBackTemplate
                .body(context -> platform.reply(context, new Message(new Attachment(template4.getPayload()))))
                .next()
                .moveTo(waiting);

        /*
             By default both message_reactions and messaging_postbacks webhooks events are handled as Xatkit Events.
             The predicate for testing whether an event was called is: eventIs(EventDefinition event).
             EventDefinitions can be accessed through public static fields in the MessengerIntentProvider class.

             Optionally and for the sake of convenience you can handle these webhooks events as normal intents instead.
             To do so, set the MessengerUtils.INTENT_FROM_POSTBACK property true for PostBacks,
             or set the MessengerUtils.INTENT_FROM_REACTION property true for Reactions.

             By default, the connector doesn't handle Reactions, to turn that on,
             set the MessengerUtils.HANDLE_REACTIONS_KEY property true.

             A PostBack consists of the title of the button pressed and the payload associated with it.
             By default the PostBack intent is generated from its payload.
             Set the MessengerUtils.USE_TITLE_TEXT property true to generate it from the title instead.

             A Reaction consists of the displayed emoji and the text that Facebook associates with it.
             By default the Reaction intent is generated from the emoji.
             Set the MessengerUtils.USE_REACTION_TEXT property true to generate it from the associated text instead.

             Be careful, a Reaction Event also generated when the user removes a reaction and neither the text nor emoji
             is sent back by the connector.

             As of December 16 2020, reactions ARE NOT SUPPORTED.
         */
        handleReaction
                .body(context -> {
                    //Will use session based map to remember which message has which emoji on it. As there can only be 1 emoji, as the bot itself won't emoji, then no need for a list of Strings
                    Map<String, String> IDtoReact = (Map<String, String>) context.getSession().computeIfAbsent("MessageIDtoReactionInformation", x -> new HashMap<String, String>());
                    String emoji = (String) context.getEventInstance().getPlatformData().get(MessengerUtils.EMOJI_KEY);
                    if (emoji != null) {
                        IDtoReact.put((String) context.getEventInstance().getPlatformData().get(MessengerUtils.MESSAGE_ID_KEY), emoji);
                        platform.reply(context, new Message(emoji));
                    }

                })
                .next()
                .moveTo(waiting);

        handleUnReaction
                .body(context -> {
                    //Retrieve the saved maps
                    Map<String, String> IDtoReact = (Map<String, String>) context.getSession().computeIfAbsent("MessageIDtoReactionInformation", x -> new HashMap<String, String>());
                    Map<String, String> IDtoMessage = (Map<String, String>) context.getSession().computeIfAbsent("MessageIDtoMessageText", x -> new HashMap<String, String>());
                    String messageID = (String) context.getEventInstance().getPlatformData().get(MessengerUtils.MESSAGE_ID_KEY);

                    // and check if the unreacted messageID has a entry in the maps
                    if (IDtoMessage.get(messageID) != null && IDtoReact.get(messageID) != null) {
                        platform.reply(context, "You have unreacted the emoji " + IDtoReact.get(messageID) + " from the message \"" + IDtoMessage.get(messageID) + "\"");
                        IDtoReact.remove(messageID);
                    }

                })
                .next()
                .moveTo(waiting);


        // Bot Initialization

        ExecutionModelProvider botModel = model()
                .useEvent(MessengerIntentProvider.MessageReact)
                .useEvent(greetings)
                .useEvent(howAreYou)
                .usePlatform(platform)
                .listenTo(intentProvider)
                .useState(handleWelcome)
                .useState(handleWelcome2)
                .useState(handleHowAreYou)
                .useState(handleLink)
                .useState(handleFile)
                .useState(handleGenericTemplate)
                .useState(handleButtonTemplate)
                .useState(handleMediaTemplate)
                .useState(handleReaction)
                .useState(handleUnReaction)
                .initState(init)
                .defaultFallbackState(defaultFallback);

        val botConfiguration = new BaseConfiguration();
        PropertiesManager propertiesManager = null;
        try {
            propertiesManager = PropertiesManager.get();
        } catch (IOException exception) {
            Log.debug("Couldn't load the properties file, forced to close.");
            System.exit(1);
        }

        /*
            Facebook Messenger API uses the following keys to communicate and authenticate its communications:
            VERIFY_TOKEN_KEY - Developer provided token for Facebook to verify itself by.
            ACCESS_TOKEN_KEY - A Facebook generated access token for the bot to verify itself by.
            APP_SECRET_KEY - A secret used to log in with the bot.

            To see a guide on how to get these keys, visit our installation guide.
            https://github.com/xatkit-bot-platform/xatkit-messenger-bot-examples/wiki/Setting-up-Facebook-Messenger-platform-and-bot
            We have created a PropertiesManager class that loads these in from a "bot-private.properties" file.
         */

        val verifyTokenKey = propertiesManager.getValue("VERIFY_TOKEN_KEY");
        val accessTokenKey = propertiesManager.getValue("ACCESS_TOKEN_KEY");
        val appSecretKey = propertiesManager.getValue("APP_SECRET_KEY");


        botConfiguration.addProperty(MessengerUtils.VERIFY_TOKEN_KEY, verifyTokenKey);
        botConfiguration.addProperty(MessengerUtils.ACCESS_TOKEN_KEY, accessTokenKey);
        botConfiguration.setProperty(MessengerUtils.APP_SECRET_KEY, appSecretKey);

        botConfiguration.setProperty(MessengerUtils.HANDLE_REACTIONS_KEY, true);
        botConfiguration.setProperty(MessengerUtils.INTENT_FROM_POSTBACK, true);

        //The bot has a 2 second delay before responding and has the typing animation for that time
        botConfiguration.setProperty(RuntimeArtifactAction.MESSAGE_DELAY_KEY, 2000);
        //The bot marks  the user's messages as read before responding
        botConfiguration.setProperty(MessengerUtils.AUTO_MARK_SEEN_KEY, true);

        XatkitBot xatkitBot = new XatkitBot(botModel, botConfiguration);
        xatkitBot.run();
    }
}
