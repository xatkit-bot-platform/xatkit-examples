# Xatkit Examples
Example Bots built with the Xatkit framework

## Installation

The bots in this repository require the [latest version of Xatkit](https://github.com/xatkit-bot-platform/xatkit-releases), unless stated explicitly in the bot directory's README. Installing the Eclipse plugins is not required to execute the bot, but is needed to open/edit their execution models and intent libraries.

## Running a Bot Example

Each bot directory contains a `.properties` file you need to edit to deploy the bot in your own environment. You can read [this article](https://github.com/xatkit-bot-platform/xatkit/wiki/Deploying-chatbots) to setup DialogFlow and Slack credentials, and additional information is provided in the comments of the `.properties` file. If you have any issue to run your bot do not hesitate to report is on our [issue tracker](https://github.com/xatkit-bot-platform/xatkit-examples/issues)!

Once you have set up the credentials and required information in the `.properties` file you can run the following command to execute your bot:

```bash
cd $XATKIT/bin
./start-xatkit-windows.sh <path to the bot property file>
```

The execution logs of the bot will be printed in the current console, and a `data/` folder will be created to store runtime results and monitoring information.

