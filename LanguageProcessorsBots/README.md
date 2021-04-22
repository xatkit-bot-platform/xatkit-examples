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

## EmojiBots

In this directory there are currently 2 different chatbots for emoji processing, which use [EmojiPostProcessor](https://github.com/xatkit-bot-platform/xatkit-runtime/blob/master/src/main/java/com/xatkit/core/recognition/processor/EmojiPostProcessor.java) and [EmojiToTextPreProcessor](https://github.com/xatkit-bot-platform/xatkit-runtime/blob/master/src/main/java/com/xatkit/core/recognition/processor/EmojiToTextPreProcessor.java) Note that these language processors should not be used together.

See the [wiki](https://github.com/xatkit-bot-platform/xatkit/wiki/Processors) to learn more about them.

### EmojiSentimentBot

This example bot uses the [EmojiPostProcessor](https://github.com/xatkit-bot-platform/xatkit-runtime/blob/master/src/main/java/com/xatkit/core/recognition/processor/EmojiPostProcessor.java) to guess the sentiment of the user based on the emojis written.

We decided to compute the sentiment of an emoji as ```(positiveSentiment * occurrences) - (negativeSentiment * 
occurrences)```, where ```occurrences``` is the number of times this emoji appears in the message. Then, the 
sentiment of the message is the sum of all emoji sentiments. This is to enhance the influence of the sentiment of 
repeated emojis.

In this example chatbot, if the absolute value of the message's sentiment is less than 0.1, it is considered a 
neutral sentiment. Else if it is positive, a positive sentiment. Else if it is negative, a negative sentiment. Note 
that this is just an approach, and obviously there are different ways to compute sentiments.

### EmojiToTextBot

This example bot uses the [EmojiToTextPreProcessor](https://github.com/xatkit-bot-platform/xatkit-runtime/blob/master/src/main/java/com/xatkit/core/recognition/processor/EmojiToTextPreProcessor.java)

This chatbot asks the user where is he/she from. It can be answered with the flag of a country (an emoji), and the 
chatbot will translate it to the original country name.

Then, it asks the user what is his/her favourite animal, which can be also answered with an emoji. Then, the chatbot 
will write the animal (or whatever emoji the user provides) name to demonstrate that it knows the emoji name. 

