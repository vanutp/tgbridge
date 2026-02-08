# Localization

This mod uses 2 language files: `lang.yml` for bot's own messages and `minecraft_lang.json`
for Minecraft messages, such as advancements and death messages.
You need to set up both files to fully localize tgbridge messages.

## Changing bot's own messages

You can download `lang.yml` for other languages from the
[translations](https://github.com/vanutp/tgbridge/blob/master/translations) directory of this repository.
If you want your language to be added, feel free to submit a Pull Request with your translation.

Also, `lang.yml` is meant to be customized by the server owner and is never overwritten by the mod.

## Localizing Minecraft messages

By default, tgbridge will use the default English translations provided by the server.

To use another language, you need a `minecraft_lang.json` file containing
the translations for all advancement and death messages. There are 2 ways to generate it:

### Automatic generation

Set the [`messages.autoMessagesLanguage`](/en/reference#messages-automessageslanguage) option
(see the reference for details).

If this option is set, tgbridge will do the following:
- Download a vanilla translation file for the specified language from Mojang's servers
- Extract translation files from all installed mods (if running on Fabric or (Neo)Forge)
- Merge all translations into a single `minecraft_lang.json` file in the mod configuration directory
(`plugins/tgbridge` or `config/tgbridge` depending on the loader)

The file will be updated automatically when needed
(i.e. when the server version, the mod list, or the specified language changes).

### Manual generation

Automatic generation should work properly in most cases, but you can still use
the manual method.

To generate `minecraft_lang.json` manually, install tgbridge on the client, change the language
to the desired one and execute the `/tgbridge dump_lang` command.

It will create this file in the mod configuration directory (`config/tgbridge`).
You can then copy it in the mod config dir on the server (`plugins/tgbridge` or `config/tgbridge` depending on the loader).
The generated file will include translations for all currently installed mods, so if you add a new mod, 
you'll need to generate the language again.
