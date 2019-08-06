# twitterBotExample

An example bot that can send and receive messages from Twitter, as well as, search or post Tweets.

Ask him:
- Can you post &lt;Whatever you want&gt;?
- Can you send a DM to &lt;@User&gt;?
- Can you show me my messages?
- Can you look for &lt;Whatever you want&gt;?

![alt text](https://raw.githubusercontent.com/ffc91/xatkit-examples/twitterBotExample/CanYouTweet/ExampleSnapshot.png)


## Execution

1. Download the latest stable Xatkit jar and the latest stable twitter-platform jar to the same folder. 
2. Download to the same folder, the CanYouBot folder.
3. Run this command (windows version) to run Xatkit with the Twitter platform.

```bash
java -cp "xatkit.jar;twitter-platform.jar" com.xatkit.Xatkit ./CanYouTweet/CanYouTweet.properties
```

> **Useful Tips** You can provide an absolute path for the `jar` files to include in the classpath.

## Requirements

Look [here](https://github.com/xatkit-bot-platform/twitter-platform/blob/master/README.md "here") for how set up a Xatkit enviroment integrated with the twitter-platform 