# Localization

This mod uses 2 language files: `lang.yml` for bot's own messages and `minecraft_lang.json`
for Minecraft messages, such as advancements and death messages.
It's a bit hard to use another language due to the fact that the minecraft server
installation only supports English language.

## Bot's own messages

You can download `lang.yml` for other languages from the
[translations](https://github.com/vanutp/tgbridge/blob/master/translations) directory of this repository.
If you want your language to be added, feel free to submit a Pull Request with your translation.

Also, `lang.yml` is meant to be customized by the server owner and is never overwritten by the mod.

## Minecraft messages

By default `minecraft_lang.json` doesn't exist and the bot will use the default English translations
provided by the server.
If you want to use another language, you need to install this mod on the client, change the language
to the desired one and execute the `/tgbridge dump_lang` command.
It will create this file in the mod configuration directory. You can then copy it to the server.
The generated file will include translations for all currently installed mods, so if you add a new mod, 
you'll need to generate the language again.
