# Совместимость

Этот мод старается быть совместимым с большинством существующих
модов/плагинов.
Ниже представлен список текущих существующих интеграций.

Если вы хотите видеть поддержку другого мода/плагина,
создайте [feature request](https://github.com/vanutp/tgbridge/issues/new?template=feature_request.yml).

## Плагины чата { #chat }

::: info Поведение
Только сообщения, отправленные в глобальный чат, будут пересылаться в Telegram
:::

Работает из коробки с:

- [Chatty](https://www.spigotmc.org/resources/chatty-lightweight-universal-bukkit-chat-system-solution-1-7-10-1-20.59411/)

Поддержка из коробки для [AdvancedChat](https://www.spigotmc.org/resources/1-17-1-21-4-%E2%AD%95-advancedchat-%E2%AD%95-ai-powered-chat-moderation-%E2%AD%90-chat-format-%E2%9C%85-50-major-features.116061/)
и [CMI](https://www.spigotmc.org/resources/cmi-300-commands-insane-kits-portals-essentials-economy-mysql-sqlite-much-more.3742/)
на данный момент не реализована из-за отсутствия у этих проектов опубликованных API
(см. [#49](https://github.com/vanutp/tgbridge/issues/49), [#50](https://github.com/vanutp/tgbridge/issues/50)).

Чтобы использовать tgbridge с этими плагинами, задайте опцию
[`integrations.incompatiblePluginChatPrefix`](/ru/reference#incompatiblepluginchatprefix)

## Vanish

::: info Поведение
Для игроков в ванише будут скрыты сообщения входа/выхода,
также они не будут показываться в `/list`
:::

Работает из коробки с:

- [EssentialsX](https://modrinth.com/plugin/essentialsx)
- [SuperVanish](https://www.spigotmc.org/resources/supervanish-be-invisible.1331/)
- [Fabric Vanish](https://modrinth.com/mod/vanish)

## Другие

- [BlueMap](https://modrinth.com/plugin/bluemap) +
  [Xaero's Minimap](https://modrinth.com/mod/xaeros-minimap)/[Xaero's World Map](https://modrinth.com/mod/xaeros-world-map):
  показывает отправленные в чат вейпойнты как ссылки на BlueMap.
  Задайте опцию [`integrations.bluemapUrl`](/ru/reference#bluemapurl) чтобы использовать.

- Форматирование сообщений, отправленных в игре с помощью [StyledChat](https://modrinth.com/mod/styled-chat)
  или похожего мода/плагина, будет видно в Telegram.

- Если [spark](https://modrinth.com/mod/spark) установлен, то в Телеграме будет доступна команда `/tps`
