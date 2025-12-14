# Commands

## /tgbridge reload

Reloads the configuration file

## /tgbridge toggle

Toggles messages from Telegram for the current player.

## /tgbridge send

Usage: `/tgbridge send <format> <chat_name> <message>`

Supported formats are:

- `plain`
- `mm` ([MiniMessage](https://docs.advntr.dev/minimessage/format.html))
- `html` (in the Telegram format)
- `json` (Minecraft JSON, as in `/tellraw`).

Sends a message to a configured Telegram chat by its name.

## /tgbridge dump_lang

This is a client command. Saves the current locale strings to `minecraft_lang.json`
in the mod configuration directory (`config/tgbridge`).

See [Localization](/en/localization) for more info.
