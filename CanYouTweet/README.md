CanYouTweet Example
=====

An example bot using the [TwitterPlatform](https://github.com/xatkit-bot-platform/xatkit-releases/wiki/Xatkit-Twitter-Platform) and [SlackPlatform](https://github.com/xatkit-bot-platform/xatkit-releases/wiki/Xatkit-Slack-Platform) to look for tweets, send direct messages, and tweet on behalf of a user.

## Installation


### Build the Twitter platform

This bot relies on the TwitterPlatform that is not yet released. To run it you need to have a local installation of the [XDK](https://github.com/xatkit-bot-platform/xatkit-dev) and perform the following commands:

```bash
cd $XATKIT_DEV
cd src/platforms
git clone https://github.com/xatkit-bot-platform/xatkit-twitter-platform.git
cd $XATKIT_DEV
./build.sh --platform=xatkit-twitter-platform --product
```

This will build a local version of the Twitter platform that can be imported from execution models.

### Setup Slack

The GithubBot needs to be deployed on Slack. You can check [this article](https://github.com/xatkit-bot-platform/xatkit-runtime/wiki/Deploying-chatbots#create-a-slack-app) to create a Slack app for Xatkit, and set its authentication token in `GithubBot.properties`:

```properties
xatkit.slack.token = <Your Slack app token>
```

### Setup Twitter

In order to connect the bot to the Twitter API you need to provide a set of credentials in the `.properties` file. You can check [this article](https://github.com/xatkit-bot-platform/xatkit-releases/wiki/Xatkit-Twitter-Platform#options) to see how to setup a Twitter App for Xatkit.

### Optional Step: setup DialogFlow

The GithubBot example relies on Xatkit RegExp intent provider, that performs exact matching of user inputs to extract intents. If you want to use a more powerful intent provider such as DialogFlow you can take a look at [this article](https://github.com/xatkit-bot-platform/xatkit-runtime/wiki/Deploying-chatbots#create-a-dialogflow-project).

## Run your bot

Start your bot with the following commands:

```bash
cd $XATKIT/bin
./start-xatkit-windows.sh <path to CanYouTweet.properties>
```

## Test your bot

You can test your bot by opening a direct message channel with it on Slack and ask it the following questions:
- `Can you post <Something>`
- `Can you send a DM to <user>`
- `Can you show me my messages?`
- `Can you look for <Something>?` 

**Note**: if you didn't setup DialogFlow the bot will rely on the default regular expression recognition service, that only accepts fragments without spaces for `<Something>` (e.g. "Can you look for football" will work, while "Can you look for football matches" won't). 

You can find below an example of the bot in action:

![Twitter Bot in action](https://raw.githubusercontent.com/wiki/xatkit-bot-platform/xatkit-releases/img/twitter-bot-example.png)
