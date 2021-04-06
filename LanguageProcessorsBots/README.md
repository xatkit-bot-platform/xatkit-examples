# Xatkit Bots Using Language Processors
This folder contains Xatkit bots (built with the help of the [xatkit-bot-template](https://github.com/xatkit-bot-platform/xatkit-bot-template/)) which use some of the [Xatkit Language Processors](https://github.com/xatkit-bot-platform/xatkit/wiki/Processors)

## RemoveStopWordsBot

This example bot uses the [RemoveEnglishStopWordsPostProcessor](https://github.com/xatkit-bot-platform/xatkit-runtime/blob/master/src/main/java/com/xatkit/core/recognition/processor/RemoveEnglishStopWordsPostProcessor.java)

In this bot the user inputs a list of words, and the bot outputs the same list but without the stop words the user might have used in the input.

## YesNoAndSentimentBot

This example bot uses [IsEnglishYesNoQuestionPostProcessor](https://github.com/xatkit-bot-platform/xatkit-runtime/blob/master/src/main/java/com/xatkit/core/recognition/processor/IsEnglishYesNoQuestionPostProcessor.java) and [EnglishSentimentPostProcessor](https://github.com/xatkit-bot-platform/xatkit-runtime/blob/master/src/main/java/com/xatkit/core/recognition/processor/EnglishSentimentPostProcessor.java)

This bot allows the user to perform questions and affirmations.

If the input is an affirmation, the bot acts like a psychologist and answers with the sentiment analysis provided by the EnglishSentimentPostProcessor. For instance, if the user input is "I love you" the bot will answer "You have a positive attitude".

If the input is a yes/no question, the bot answers randomly with "Yes" or "No". Otherwise, he answers "I only answer yes/no questions".

## ToxicityDetectorBot

This example bot uses the [ToxicityPostProcessor](https://github.com/xatkit-bot-platform/xatkit-runtime/blob/master/src/main/java/com/xatkit/core/recognition/processor/ToxicityPostProcessor.java)

This chatbot can tell you the toxicity that your messages contain, computed using 2 language models: PerspectiveAPI and Detoxify.

See the [wiki](https://github.com/xatkit-bot-platform/xatkit/wiki/Processors) to learn how to install and use them.

Moreover, you have to edit these lines of code in the bot, adding the proper parameters depending on your preferred toxicity model:

```java
botConfiguration.setProperty(PerspectiveApiConfiguration.API_KEY, "YOUR PERSPECTIVEAPI KEY");        
botConfiguration.setProperty(DetoxifyConfiguration.DETOXIFY_SERVER_URL, "YOUR SERVER URL");
```

## LanguageDetectorBot

This example bot uses the [LanguageDetectionPostProcessor](https://github.com/xatkit-bot-platform/xatkit-runtime/blob/master/src/main/java/com/xatkit/core/recognition/processor/LanguageDetectionPostProcessor.java)

This chatbot can guess the language of the last user message and, for a more accurate prediction, the language of the last 10 messages.

See the [wiki](https://github.com/xatkit-bot-platform/xatkit/wiki/Processors) to learn how to use it and set its parameters.

