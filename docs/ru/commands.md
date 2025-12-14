# Команды

## /tgbridge reload

Перезагружает файл конфигурации

## /tgbridge toggle

Включает или отключает показ сообщений из Telegram для текущего игрока.

## /tgbridge send

Использование: `/tgbridge send <format> <chat_name> <message>`

Поддерживаемые форматы:

- `plain`
- `mm` ([MiniMessage](https://docs.advntr.dev/minimessage/format.html))
- `html` (в формате Telegram)
- `json` (Minecraft JSON, как в `/tellraw`).

Отправляет сообщение в настроенный чат Telegram по его имени.

## /tgbridge dump_lang

Это клиентская команда. Сохраняет текущие строки перевода в файл `minecraft_lang.json`
в папке конфигурации мода (`config/tgbridge`).

См. [Локализация](/ru/localization) для подробной информации.
