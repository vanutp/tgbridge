# Integrations

This mod aims to be compatible with most of existing mods/plugins,
and to provide useful integrations,
See below for the current list of implemented integrations.

If you would like to see support for a mod/plugin not in this list,
feel free to submit a [feature request](https://github.com/vanutp/tgbridge/issues/new?template=feature_request.yml).

## Chat plugins { #chat }

If a supported plugin is installed,
messages are forwarded between Telegram chats configured in [`chats[]`](/en/reference#chats)
and chats with matching names configured in your plugin.

Supported plugins:

- [Chatty](https://www.spigotmc.org/resources/chatty-lightweight-universal-bukkit-chat-system-solution-1-7-10-1-20.59411/)
- [HeroChat](https://www.spigotmc.org/resources/herochat-pro-multi-server-chat-channels-and-more-50-off.34305/)
- [Carbon Chat](https://modrinth.com/plugin/carbon) (Paper, Fabric)
- [FlectonePulse](https://modrinth.com/plugin/flectonepulse) (Paper, Fabric)

Support for other plugins is tracked in [#71](https://github.com/vanutp/tgbridge/issues/71).
If you need support for your plugin, please leave a comment there.
In the meantime, you can use the workaround below.

## Unsupported chat plugins

To use tgbridge with unsupported plugins, set the [`integrations.incompatiblePluginChatPrefix`](/en/reference#integrations-incompatiblepluginchatprefix)
config option. It uses a workaround to work with any plugin, but some features may not function correctly.
For example, players "muted" by a plugin will still be able to send messages to the Telegram chat.
Also, only a single (global) chat is supported in this mode.

## DiscordSRV

Messages are forwarded between Telegram chats and Discord channels
with the same names in tgbridge and DiscordSRV configs.

Message format can be configured in the [`integrations.discord`](/en/reference#integrations-discord) config section.

## Vanish

Join/leave messages from vanished players are hidden, and these players are hidden in the `/list` command in Telegram.

Works out of the box with:

- [EssentialsX](https://modrinth.com/plugin/essentialsx)
- [SuperVanish](https://www.spigotmc.org/resources/supervanish-be-invisible.1331/)
- [Fabric Vanish](https://modrinth.com/mod/vanish)

## Voice messages

If the [Voice Messages](https://modrinth.com/plugin/voicemessages) mod/plugin is installed,
voice messages will be forwarded between Minecraft and Telegram.

## Client-side emoji mods/text replacement

This integration replaces text patterns in messages forwarded from Minecraft to Telegram
using rules in the `replacements.json` config file.

By default, `replacements.json` contains a list of common emoji shortcodes like `:fox:` or `:skull:`.
It should cover most mods that replace shortcodes with emojis on the client.
Can also be configured to replace any text with any other text.

## Other

- [BlueMap](https://modrinth.com/plugin/bluemap) +
  [Xaero's Minimap](https://modrinth.com/mod/xaeros-minimap)/[Xaero's World Map](https://modrinth.com/mod/xaeros-world-map) integration:
  shows shared waypoints as BlueMap links in the Telegram chat. Set the [`integrations.bluemapUrl`](/en/reference#integrations-bluemapurl)
  option to enable

- If you send a formatted message in Minecraft using [StyledChat](https://modrinth.com/mod/styled-chat)
  or a similar mod/plugin, the formatting will be visible in Telegram 

- If [spark](https://modrinth.com/mod/spark) is installed, `/tps` command will be available in Telegram
