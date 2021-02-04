FROM openjdk:8
# Update this line with the name of jar created with mvn package
COPY target/greetings-bot-jar-with-dependencies.jar /bot.jar
WORKDIR /
# You can configure Xatkit properties from the command line, e.g.
# -Dxatkit.server.port=5010 will set Xatkit's server port to 5010
CMD java -jar bot.jar