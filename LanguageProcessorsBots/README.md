# Xatkit Bots Using Language Processors
This folder contains Xatkit bots (built on [xatkit-bot-template](https://github.com/xatkit-bot-platform/xatkit-bot-template/)) which use some of the [Xatkit Language Processors](https://github.com/xatkit-bot-platform/xatkit/wiki/Processors)

## RemoveStopWordsBot

This example bot uses the [RemoveEnglishStopWordsPostProcessor](https://github.com/xatkit-bot-platform/xatkit-runtime/blob/master/src/main/java/com/xatkit/core/recognition/processor/RemoveEnglishStopWordsPostProcessor.java)

In this bot the user inputs a list of words, and the bot outputs the same list but without the stop words the user might have used in the input.

## YesNoAndSentimentBot

This example bot uses [IsEnglishYesNoQuestionPostProcessor](https://github.com/xatkit-bot-platform/xatkit-runtime/blob/master/src/main/java/com/xatkit/core/recognition/processor/IsEnglishYesNoQuestionPostProcessor.java) and [EnglishSentimentPostProcessor](https://github.com/xatkit-bot-platform/xatkit-runtime/blob/master/src/main/java/com/xatkit/core/recognition/processor/EnglishSentimentPostProcessor.java)

This bot allows the user to perform questions and affirmations.

If the input is an affirmation, the bot acts like a psychologist and answers with the sentiment analysis provided by the EnglishSentimentPostProcessor. For instance, if the user input is "I love you" the bot will answer "You have a positive attitude".

If the input is a yes/no question, the bot answers randomly with "Yes" or "No". Otherwise, he answers "I only answer yes/no questions".

## Build the bots

1- [Build the latest version of Xatkit](https://github.com/xatkit-bot-platform/xatkit/wiki/Build-Xatkit)

2- [Create a Dialogflow agent](https://github.com/xatkit-bot-platform/xatkit/wiki/Integrating-DialogFlow) to use it with the bot (if needed).

If the bot uses DialogFLow, you will have to edit these lines on the bot class.
```java
botConfiguration.setProperty("xatkit.dialogflow.projectId", "YOUR PROJECT ID");
botConfiguration.setProperty("xatkit.dialogflow.credentials.path", "PATH TO YOUR DIALOGFLOW CREDENTIALS");
```

# Website

[xatkit.com](https://xatkit.com/)
