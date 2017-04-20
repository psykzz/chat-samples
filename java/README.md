# Twitch Chatbot Java Sample
Here you will find a simple Java chatbot using IRC that can help demonstrate how to interact with chat on Twitch.
It is based on PircBotX which will work for most of your twitch goodies, If it does not support a twitch event you can use UnknownEvent to parse the raw IRC message into one you can use.

## Installation
After you have cloned this repository, You may code out of the java folder or move the classes to a package of your choice.

You can setup gradle by opening a console window in this folder and typing
```
gradle init
```

To build a jar you can do
```
gradle jar
```

## Usage
To run the chatbot, you will need to provide details in the Main class.
You must supply a channel, a nickname and your OAuth token.
You can reference an authentication sample to accomplish this, or simply use the [Twitch Chat OAuth Password Generator](http://twitchapps.com/tmi/).

## Next Steps
Feel free to augment the chatbot with your own new commands by adding them in the parseMessage() function.

## PircBotX Documentation
You can check out all the information you would need over at https://github.com/TheLQ/pircbotx