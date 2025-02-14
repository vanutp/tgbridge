# Compatibility

This mod aims to be compatible with most of existing mods/plugins.

If you would like to see support for a mod/plugin not in this list,
feel free to submit a [feature request](https://github.com/vanutp/tgbridge/issues/new?template=feature_request.yml).

## Chat plugins { #chat }

::: info Integration behaviour
Only messages sent to the global chat will be forwarded
:::

Works out of the box with:

- [Chatty](https://www.spigotmc.org/resources/chatty-lightweight-universal-bukkit-chat-system-solution-1-7-10-1-20.59411/)

Out-of-the-box support for [AdvancedChat](https://www.spigotmc.org/resources/1-17-1-21-4-%E2%AD%95-advancedchat-%E2%AD%95-ai-powered-chat-moderation-%E2%AD%90-chat-format-%E2%9C%85-50-major-features.116061/)
and [CMI](https://www.spigotmc.org/resources/cmi-300-commands-insane-kits-portals-essentials-economy-mysql-sqlite-much-more.3742/)
is currently not implemented due to the lack of published APIs by these projects
(see [#49](https://github.com/vanutp/tgbridge/issues/49), [#50](https://github.com/vanutp/tgbridge/issues/50)).

To use tgbridge with these plugins, set the [`integrations.incompatiblePluginChatPrefix`](/en/reference#integrations-incompatiblepluginchatprefix)
config option
<!-- TODO: add config reference link -->

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
  shows shared waypoints as BlueMap links in the Telegram chat. Set the [`integrations.bluemapUrl`](/en/reference#integrations-bluemapurl)
  option to enable
<!-- TODO: add config reference link -->

- If you send a formatted message in Minecraft using [StyledChat](https://modrinth.com/mod/styled-chat)
  or a similar mod/plugin, the formatting will be visible in Telegram 

- Install [spark](https://modrinth.com/mod/spark) to use `/tps` command in Telegram
