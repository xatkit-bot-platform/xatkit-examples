# SlackTrelloBot Example
An example bot using the [SlackPlatform](https://github.com/xatkit-bot-platform/xatkit-slack-platform) and the [ZapierPlatform](https://github.com/xatkit-bot-platform/xatkit-releases/wiki/Xatkit-Zapier-Platform) to create Trello cards from Slack commands. 

## Installation

### Setup Slack

The SlackTrelloBot needs to be deployed on Slack. You can check [this article](https://github.com/xatkit-bot-platform/xatkit-releases/wiki/Deploying-on-Slack) to create a Slack app for Xatkit, and set its authentication token in `SlackTrelloBot.properties`:

```properties
xatkit.slack.token = <Your Slack app token>
```

*Xatkit Tip: Bots in Slack channels*

> If you want to test your bot in a given channel **do not forget to invite the bot to this channel**, otherwise it won't be able to receive your messages and send replies

### Setup Zapier

This bot is based on the Zapier integration with Trello. We need to create a new *Zap* that will create our Trello card on behalf of Xatkit.

Open the [Zapier dashboard](https://zapier.com/app/dashboard) and click on `Make a Zap!`

In the `When this happens` window select `Webhook by Zapier`, and select `Catch Hook` from the `Choose Trigger Event` dropdown menu. This allows to trigger your Zap from a REST request.

![Create a Zapier Webhook](https://raw.githubusercontent.com/wiki/xatkit-bot-platform/xatkit-releases/img/zapier/webhook-configuration2.png)

Click on `Continue`, and note the content of the `Custom Webhook URL` field, we will reuse it later to configure Xatkit, then click again on `Continue`. The last step allows to find some data from example REST requests, we don't need it in this example, so you can simply click on `Skip Test`. 

![Skip Find Data](https://raw.githubusercontent.com/wiki/xatkit-bot-platform/xatkit-releases/img/zapier/webhook-configuration3.png)

We now need to configure the action to perform once the Zap is triggered. In the `Do this ...` window select `Trello`, and select `Create Card` from the `Choose Action Event` dropdown menu. Zapier will then ask you to login with your Trello account in order to load your boards.



![Create a Trello Action](https://raw.githubusercontent.com/wiki/xatkit-bot-platform/xatkit-releases/img/zapier/webhook-configuration4.png)



Navigate to the `Customize Card` tab, and fill the `Board` and `List` fields with the Trello board and list to add cards in. Fill the `Name` field with the following content: `{{name}}`, this is the Zapier way to specify that we want to access the `name` value from the received JSON payload (this value is set in *ZapierPlatform.PostAction* action in the execution model). You can leave the default values for the other fields. Once this is done you should have an action similar to the one shown below (note that Zapier formatted our `{{name}}` into a user-friendly representation):

![Customize Card Example](https://raw.githubusercontent.com/wiki/xatkit-bot-platform/xatkit-releases/img/zapier/webhook-configuration5.png)



Click on continue, and skip the *Send Data* test proposed at the end of the process.

Congratulation, you have just created your Zap! Make sure that your Zap is turned on by checking it in your [dashboard](https://zapier.com/app/zaps).

We now need to tell Xatkit to use your Zap. To do it update the `SlackTrelloBot.properties` file to set the Zapier endpoint url the bot should use: use the `Custom Webhook URL` value noted previously (you can retrieve it by editing your Zap in the dashboard ;) )

```properties
zapier.endpoint = <Your Zapier endpoint>
```

*Xatkit Tip: Getting help with Zapier*

> If you experience any issue with Zapier you can take a look at the Zapier [documentation on webhooks](https://zapier.com/apps/webhook/help).

### Optional Step: setup DialogFlow

The SlackTrelloBot example relies on Xatkit RegExp intent provider, that performs exact matching of user inputs to extract intents. If you want to use a more powerful intent provider such as DialogFlow you can take a look at [this article](https://github.com/xatkit-bot-platform/xatkit-releases/wiki/Integrating-DialogFlow).

## Run your bot

Start your bot with the following command:

```bash
cd $XATKIT/bin
./start-xatkit-windows.sh <path to SlackTrelloBot.properties>
```

If you don't have a local installation of Xatkit you can check [this article](https://github.com/xatkit-bot-platform/xatkit-releases/wiki/Installation) to install and setup Xatkit on your machine.

## Test your bot

Open a direct message channel with the deployed bot and write the following message: `Create a card Test`, then check that a new card named `Test` has been created on the board and list specified in your Zap!

You can also invite your bot to a Slack channel and write the following message: `@<Your Bot Name> Create a card Test`, where `<Your Bot Name>` is the name of your bot. Note that the bot will not reply in a channel if you haven't explicitly mentioned it (this is specified by the `xatkit.slack.listen_mentions_on_group_channels = true` property in `SlackTrelloBot.properties`).

