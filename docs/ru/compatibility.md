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

Статус поддержки других плагинов: [#71](https://github.com/vanutp/tgbridge/issues/71).
Если вам нужна поддержка вашего плагина, оставьте комментарий под этим issue.

Чтобы использовать tgbridge с неподдерживаемыми плагинами, задайте опцию
[`integrations.incompatiblePluginChatPrefix`](/ru/reference#incompatiblepluginchatprefix).
Она использует костыль для работы с любым плагином, но некоторые функции могут работать неправильно.
Например, игроки в "муте" все равно смогут отправлять сообщения в Telegram-чат.

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
