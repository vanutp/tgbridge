# Compatibility

This mod aims to be compatible with most of existing mods/plugins,
and to provide useful integrations,
See below for the current list of implemented integrations.

If you would like to see support for a mod/plugin not in this list,
feel free to submit a [feature request](https://github.com/vanutp/tgbridge/issues/new?template=feature_request.yml).

## Chat plugins { #chat }

::: info Integration behaviour
Only messages sent to the global chat will be forwarded
:::

Works out of the box with:

- [Chatty](https://www.spigotmc.org/resources/chatty-lightweight-universal-bukkit-chat-system-solution-1-7-10-1-20.59411/)

Support for other plugins is tracked in [#71](https://github.com/vanutp/tgbridge/issues/71).
If you need support for your plugin, please leave a comment there.

To use tgbridge with unsupported plugins, set the [`integrations.incompatiblePluginChatPrefix`](/en/reference#incompatiblepluginchatprefix)
config option. It uses a workaround to work with any plugin, but some features may not function correctly.
For example, players "muted" by a plugin will still be able to send messages to the Telegram chat.

## Vanish

::: info Integration behaviour
Join/leave messages from vanished players will be hidden, `/list` command won't show them
:::

Works out of the box with:

- [EssentialsX](https://modrinth.com/plugin/essentialsx)
- [SuperVanish](https://www.spigotmc.org/resources/supervanish-be-invisible.1331/)
- [Fabric Vanish](https://modrinth.com/mod/vanish)

## Other

- [BlueMap](https://modrinth.com/plugin/bluemap) +
  [Xaero's Minimap](https://modrinth.com/mod/xaeros-minimap)/[Xaero's World Map](https://modrinth.com/mod/xaeros-world-map) integration:
  shows shared waypoints as BlueMap links in the Telegram chat. Set the [`integrations.bluemapUrl`](/en/reference#bluemapurl)
  option to enable

- If you send a formatted message in Minecraft using [StyledChat](https://modrinth.com/mod/styled-chat)
  or a similar mod/plugin, the formatting will be visible in Telegram 

- If [spark](https://modrinth.com/mod/spark) is installed, `/tps` command will be available in Telegram
