# Xatkit Bot Template
This repository contains a template project pre-configured with the required dependencies to help you get started with your new bot.



## What's included in this template?

The maven project in this template embeds the following dependencies:
- Xatkit Core (_com.xatkit.core_): the Xatkit SDK, this dependency contains the DSL to create Xatkit bots, as well as the runtime engine to execute them.
- Xatkit Chat Platform (_com.xatkit.chat-platform-runtime_): a generic platform representing a chat
- Xatkit React Platform (_com.xatkit.react-platform-runtime_): a platform able to connect with the [Xatkit chat widget](https://github.com/xatkit-bot-platform/xatkit-chat-widget)
- Lombok (_org.projectlombok.lombok_): a library to ease the development of Java applications (more information [here](https://projectlombok.org/))
We also configured a few testing dependencies we regularly use in our bots:
- JUnit 4.12
- AssertJ 3.14
- Mockito 3.3.3
Finally, we put an example _GreetingsBot_ in this template to help you find the API method you need to create, configure, and run your bot. You can find more information on the API in our [wiki](https://github.com/xatkit-bot-platform/xatkit/wiki).

**Note:** you can remove the Xatkit Chat Platform and Xatkit React Platform dependencies if you don't need them, but they are required to run the _GreetingsBot_ example.

In addition, this template also includes a Dockerfile you can customize to deploy your bot as a [Docker container](#docker).

## How to use the template?
Click on the button below to create a new repository from this template and follow the instructions.
[![Use this template](docs/img/template_button.png)](https://github.com/xatkit-bot-platform/xatkit-bot-template/generate)



## Packaging

Run the following command to package your bot
```bash
mvn package
```
This creates a self-contained jar `greetings-bot-jar-with-dependencies.jar` in the `target` directory. You can run you bot with the following command
```bash
java -jar greetings-bot-jar-with-dependencies.jar
```
You can now navigate to `http://localhost:5000` and start chatting with your bot! 

📚 You need to edit the bot's `pom.xml` file to change the name of the produced `jar` or the main class used to start the bot, see the example below
```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-assembly-plugin</artifactId>
    <version>${maven-assembly-plugin.version}</version>
    <executions>
        <execution>
            <phase>package</phase>
            <goals>
                <goal>single</goal>
            </goals>
        </execution>
    </executions>
    <configuration>
        <descriptorRefs>
            <descriptorRef>jar-with-dependencies</descriptorRef>
        </descriptorRefs>
        <archive>
            <manifest>
                <!-- 💡 Change here the main class used to start the bot !-->
                <mainClass>com.xatkit.example.GreetingsBot</mainClass>
            </manifest>
        </archive>
            <!-- 💡 Change here the name of your bot -->
            <!-- The resulting jar will be named <your bot name>-jar-with-dependency.jar -->
            <finalName>greetings-bot</finalName>
        </configuration>
</plugin>
```



## Docker

This template provides a [Dockerfile](https://github.com/xatkit-bot-platform/xatkit-bot-template/blob/master/Dockerfile) to build a Docker image from your packaged bot. Run the following commands to build the image and start your bot as a container

```bash
docker build --tag myorg/greetingsbot:1.0 .
docker run -p5000:5000 -p5001:5001 -d --name greetingsBot myorg/greetingsbot:1.0
```

You can now navigate to `http://localhost:5000` and start chatting with your bot! 

📚 You need to edit the bot's `Dockerfile` to change the name of the copied bot `jar` based on your `pom.xml` configuration, see the example below

```dockerfile
FROM openjdk:8
# Update this line with the name of jar created with mvn package
COPY target/greetings-bot-jar-with-dependencies.jar /bot.jar
WORKDIR /
# You can configure Xatkit properties from the command line, e.g.
# -Dxatkit.server.port=5010 will set Xatkit's server port to 5010
CMD java -jar bot.jar
```



## Troubleshooting

- IntelliJ error: `java: incompatible types: com.xatkit.dsl.intent.IntentOptionalTrainingSentenceStep cannot be converted to lombok.val` ➡ You need to enable annotation processing in your project (see image below).
![Enable annotation processing in IntelliJ](docs/img/enable_annotation_processing_intellij.png)


