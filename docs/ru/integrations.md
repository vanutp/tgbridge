# Интеграции

Этот мод старается быть совместимым с большинством существующих
модов/плагинов.
Ниже представлен список текущих существующих интеграций.

Если вы хотите видеть поддержку другого мода/плагина,
создайте [feature request](https://github.com/vanutp/tgbridge/issues/new?template=feature_request.yml).

## Плагины чата { #chat }

Если установлен поддерживаемый плагин,
сообщения пересылаются между Telegram чатами, настроенными в [`chats[]`](/ru/reference#chats),
и чатами с одинаковыми именами, настроенными в вашем плагине.

Поддерживаемые плагины:

- [Chatty](https://www.spigotmc.org/resources/chatty-lightweight-universal-bukkit-chat-system-solution-1-7-10-1-20.59411/)
- [HeroChat](https://www.spigotmc.org/resources/herochat-pro-multi-server-chat-channels-and-more-50-off.34305/)
- [Carbon Chat](https://modrinth.com/plugin/carbon) (Paper, Fabric)
- [FlectonePulse](https://modrinth.com/plugin/flectonepulse) (Paper, Fabric)

Статус поддержки других плагинов: [#71](https://github.com/vanutp/tgbridge/issues/71).
Если вам нужна поддержка вашего плагина, оставьте комментарий под этим issue.
До добавления поддержки вы можете использовать костыль ниже.

## Неподдерживаемые плагины чата

Чтобы использовать tgbridge с неподдерживаемыми плагинами, задайте опцию
[`integrations.incompatiblePluginChatPrefix`](/ru/reference#integrations-incompatiblepluginchatprefix).
Она использует костыль для работы с любым плагином, но некоторые функции могут работать неправильно.
Например, игроки в "муте" все равно смогут отправлять сообщения в Telegram-чат.
Также в этом режиме поддерживается только один (глобальный) чат.

## DiscordSRV

Сообщения пересылаются между Telegram чатами и Discord каналами с одинаковыми
именами в конфигах tgbridge и DiscordSRV.

Формат сообщений можно настроить в секции конфига [`integrations.discord`](/en/reference#integrations-discord).

## Vanish

Для игроков в ванише не отправляются сообщения входа/выхода, также эти игроки скрыты в команде `/list` в Telegram.

Работает из коробки с:

- [EssentialsX](https://modrinth.com/plugin/essentialsx)
- [SuperVanish](https://www.spigotmc.org/resources/supervanish-be-invisible.1331/)
- [Fabric Vanish](https://modrinth.com/mod/vanish)

## Клиентские моды для эмодзи/замена текста

Эта интеграция заменяет паттерны текста в сообщениях, пересылаемых из Minecraft в Telegram,
используя правила из файла `replacements.json`.

По умолчанию `replacements.json` содержит список частых эмодзи-шорткатов, таких как `:fox:` или `:skull:`.
Он должен быть совместим с большинством модов, которые заменяют шорткаты на клиенте.
Также можно настроить замену любого текста на любой другой текст.

## Голосовые сообщения

Если установлен мод/плагин [Voice Messages](https://modrinth.com/plugin/voicemessages),
голосовые сообщения будут пересылаться между Minecraft и Telegram.

## Другие

- [BlueMap](https://modrinth.com/plugin/bluemap) +
  [Xaero's Minimap](https://modrinth.com/mod/xaeros-minimap)/[Xaero's World Map](https://modrinth.com/mod/xaeros-world-map):
  показывает отправленные в чат вейпойнты как ссылки на BlueMap.
  Задайте опцию [`integrations.bluemapUrl`](/ru/reference#integrations-bluemapurl) чтобы использовать.

- Форматирование сообщений, отправленных в игре с помощью [StyledChat](https://modrinth.com/mod/styled-chat)
  или похожего мода/плагина, будет видно в Telegram.

- Если [spark](https://modrinth.com/mod/spark) установлен, то в Телеграме будет доступна команда `/tps`
