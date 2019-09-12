# GitHubBot Example
An example bot using the [SlackPlatform](https://github.com/xatkit-bot-platform/xatkit-slack-platform) and the [GitHubPlatform](https://github.com/xatkit-bot-platform/xatkit-github-platform) to receive events from GitHub and manage opened issues from Slack. You can check the [blog article](https://livablesoftware.com/slack-chatbot-github-repositories/) to know more about this bot!

## Installation

### Setup Slack

The GitHubBot needs to be deployed on Slack. You can check [this article](https://github.com/xatkit-bot-platform/xatkit-releases/wiki/Deploying-on-Slack) to create a Slack app for Xatkit, and set its authentication token in `GithubBot.properties`:

```properties
xatkit.slack.token = <Your Slack app token>
```

You can also set the channel used by the GithubBot to post messages (the `general` channel is used by default):

```properties
slack.channel = general
```

**Do not forget to invite the bot to your channel, otherwise it won't be able to reply to you**

### Setup GitHub

This bot requires GitHub credentials to receive events from the Github API and perform actions, you can navigate to [this page](https://github.com/settings/tokens) and click on **Generate new token** to create a new personal access token, give it a name, and select the **repo** scope. Copy the created access token in the `GithubBot.properties`:

```properties
xatkit.github.oauth.token = <Your Github access token>
```

Finally, you'll need to set the Github repository that is managed by the GithubBot. To do so, create a new Github repository (or use an existing one) and perform the following actions:

- Navigate in the **Settings** of your repository, open the **Webhooks** tab, click on **Add webhook**, and set the following informations:
  - **Payload URL**: https://xatkit.serveo.net/github/
  - **Content type**: *application/json*
  - Under the **Which events would you like to trigger this webhook?** menu select Send me **everything**.
  - Update the `github.repository.username` and `github.repository.name` properties in `GithubBot.properties` with your repository's information.
  
Serveo is used to forward the payload to your local port. If you're already running Xatkit in a public server just use that server URL. Instead of Serveo, you could also use ngrok.

### Optional Step: setup DialogFlow

The GithubBot example relies on Xatkit RegExp intent provider, that performs exact matching of user inputs to extract intents. If you want to use a more powerful intent provider such as DialogFlow you can take a look at [this article](https://github.com/xatkit-bot-platform/xatkit-releases/wiki/Integrating-DialogFlow).

## Run your bot

Open a terminal and execute this command:

```bash
ssh -R xatkit.serveo.net:80:localhost:5000 serveo.net
```

This will redirect the payloads sent by Github to your localhost, allowing Xatkit to capture them and trigger events.

*Xatkit Tip*
> If serveo is not up you can check out [ngrok](https://ngrok.com/) or [packetriot](https://packetriot.com/) that provide similar features. 

Start your bot:

```bash
cd $XATKIT/bin
./start-xatkit-windows.sh <path to GithubBot.properties>
```

If you don't have a local installation of Xatkit you can check [this article](https://github.com/xatkit-bot-platform/xatkit-releases/wiki/Installation) to install and setup Xatkit on your machine.

## Test your bot

Open an issue in the repository you configured before. The bot should send a new message in your Slack channel you configured. You can reply to this message with the following test sentences:

- Set label bug
- Assign user `<github username>` (e.g. "Assign user gdaniel")

Check the issue in your repository, it should now be labeled `bug`, and assigned to the user you defined.
