# Minecraft Telegram Bridge

A feature-rich multiloader Telegram bridge for Minecraft

## Quick start

1. Download and install the latest release for your loader from the Modrinth page: https://modrinth.com/mod/tgbridge
2. Start the server
3. Follow the instructions in the generated config file located at config/tgbridge/config.yml (
   plugins/tgbridge/config.yml for Paper)
4. Restart the server (currently restart is required when changing `botToken`)
5. The bot should work now
6. You can look through the config file and change the settings however you like.
   Use `/tgbridge reload` to reload the config.

## Features

- Supports most of Telegram messages
- `/list` command in Telegram will print the list of currently online players
- If you have BlueMap and Xaero's Minimap/World Map installed, you can set the `messages.bluemapUrl` option
  to be able to view shared waypoints from Telegram on the BlueMap
- If you don't want every Minecraft message to be forwarded, you can set the `messages.requirePrefixInMinecraft`
  option to your desired prefix (for example `!`). Only messages starting with that prefix will be forwarded.
- If you do want every message to be forwarded, but don't like that tons of messages are sent in the Telegram chat,
  you can set the `messages.mergeWindow` option to some value (for example, `60`). Messages subsequently send
  within this interval will be merged into one.
- If some of your players have connection problems, you can set the `events.leaveJoinMergeWindow` option to some value
  (for example, `120`). If a player leaves and then joins within this amount of seconds, leave and join messages will
  be deleted from Telegram.
- You can customize which event messages are sent to Telegram under the `events` config section

## Using other languages for bot messages

This mod uses 2 language files: `lang.yml` for bot's own translation strings and `minecraft_lang.json`
for Minecraft translations. It's a bit hard to use another language due to the fact that the minecraft server installation
only supports English language.

You can download `lang.yml` for other languages from the 
[translations](https://github.com/vanutp/tgbridge/blob/master/translations) directory of this repository.


By default `minecraft_lang.json` doesn't exist and the bot will use the default English translations provided by the server.
If you want to use another language, you need to install this mod on the client, change the language 
to the desired one and execute the `/tgbridge dump_lang` command.
It will create this file in the mod configuration directory. You can then copy it to the server.
The generated file will include translations for all currently installed mods, so if you add a new mod, you'll need to
generate the language again.

## Server or client?

This mod will only work on dedicated server. When installed on the client, the main logic won't load. However, it will
provide a `/tgbridge dump_lang` command to dump minecraft translations to the current language.

## Contributions

Feel free to open an issue if you think something is missing/can be done better.
Please ask in an issue before creating a pull request

## Acknowledgements

This project is inspired by these projects:

- https://github.com/CuteNekoOwO/FabricTgBridge
- https://github.com/ntoneee/minecraft-telegram-bridge
