# Config reference

## botToken

- **Type:** `string`
- **Required**

<p id="chats"></p>

## chats[].name

- **Type:** `string`
- **Required**

## chats[].isDefault

- **Type:** `boolean`
- **Default:** `false`

## chats[].chatId

- **Type:** `number`
- **Required**

## chats[].topicId

- **Type:** `number | null`
- **Default:** `null`

## chats[].minecraftFormat

- **Type:** `string`
- **Default:** `"<aqua>\\<<sender>></aqua> <text>"`

Format for Telegram -> Minecraft messages.
Uses [MiniMessage](https://docs.advntr.dev/minimessage/format.html) formatting.

## chats[].telegramFormat

- **Type:** `string`
- **Default:** `"<b>[<username>]</b> <text>"`

Format for Minecraft -> Telegram messages.
Uses [MiniMessage](https://docs.advntr.dev/minimessage/format.html) formatting.

## messages.requirePrefixInMinecraft

- **Type:** `string | null`
- **Default:** `null` (disabled)
- **Example:** `"!"` (quotes are required)

::: warning
Don't enable this if you have a chat plugin installed.
See [Compatibility](https://tgbridge.vanutp.dev/en/compatibility#chat) for more info
:::
If this value is set, messages without specified prefix won't be forwarded to Telegram.

## messages.keepPrefix

- **Type:** `boolean`
- **Default:** `false`

Set to `true` to keep the prefix specified in the above setting in the message

## messages.mergeWindow

- **Type:** `number`
- **Default:** `0` (disabled)

Chat messages sent within the specified interval will be merged in one.
The value is specified in seconds

## messages.useRealUsername

- **Type:** `boolean`
- **Default:** `false`

Set to `true` to use real player username instead of display name in all Telegram messages

## integrations.bluemapUrl

- **Type:** `string | null`
- **Default:** `null` (disabled)
- **Example:** `https://map.example.com`

If this value is set, waypoints shared from Xaero's Minimap/World Map will be rendered
as links to a specified BlueMap instance.

## integrations.incompatiblePluginChatPrefix

- **Type:** `string | null`
- **Default:** `null` (disabled)
- **Example:** `"!"` (quotes are required)

See also: [Compatibility](https://tgbridge.vanutp.dev/en/compatibility#chat)

Use this if you have an incompatible plugin, such as CMI or AdvancedChat installed.
Will register a legacy chat listener with LOWEST priority (HIGHEST on Forge/NeoForge)
and only forward messages that start with the specified string.
Currently this only has an effect on Paper and Forge/NeoForge.

{ #integrations-discord }

## integrations.discord.toDiscordFmt

- **Type:** `string`
- **Default:** `"**[{sender}]** {text}"`

Format of Telegram -> Discord messages. Uses Discord Markdown formatting.

## integrations.discord.toTelegramFmt

- **Type:** `string`
- **Default:** `"<b>[<sender>]</b> <text>"`

Format of Discord -> Telegram messages.
Uses [MiniMessage](https://docs.advntr.dev/minimessage/format.html) formatting.

{ #events }

## events.advancementMessages.enable

- **Type:** `boolean`

## events.advancementMessages.enableTask

- **Type:** `boolean`

## events.advancementMessages.enableGoal

- **Type:** `boolean`

## events.advancementMessages.enableChallenge

- **Type:** `boolean`

## events.advancementMessages.showDescription

- **Type:** `boolean`

Include advancement descriptions in Telegram messages

## events.enableDeathMessages

- **Type:** `boolean`

## events.joinMessages

- **Type:** `true | false | 'first_join_only'`

## events.enableLeaveMessages

- **Type:** `boolean`

## events.leaveJoinMergeWindow

- **Type:** `number`
- **Default:** `0` (disabled)

If a player leaves and then joins within the specified time interval,
the leave and join messages will be deleted.
This is useful when players frequently re-join, for example because of connection problems.
Only has effect when both joinMessages = true and enableLeaveMessages = true.
The value is specified in seconds

## events.enableStartMessages

- **Type:** `boolean`

Whether to send a Telegram message when the server starts

## events.enableStopMessages

- **Type:** `boolean`

Whether to send a Telegram message when the server stops

## advanced.botApiUrl

- **Type:** `string`
- **Default:** `"https://api.telegram.org"`

<p id="advanced-proxy"></p>

## advanced.proxy.type

- **Type:** `'none' | 'socks5' | 'http'`
- **Default:** `'none'`

## advanced.proxy.host

- **Type:** `string`

## advanced.proxy.port

- **Type:** `number`

## advanced.proxy.username

- **Type:** `string | null`

## advanced.proxy.password

- **Type:** `string | null`

## advanced.connectionRetry.maxAttempts

- **Type:** `number`
- **Default:** `10`

Max amount of connection retries. If the value is less than 1 the number of attempts is infinite

## advanced.connectionRetry.initialDelay

- **Type:** `number`
- **Default:** `1000`

Delay before first reconnect attempt in milliseconds

## advanced.connectionRetry.maxDelay

- **Type:** `number`
- **Default:** `300000`

Maximum delay between reconnection attempts in milliseconds

## version

- **Type:** `number`

Config file version. Don't change manually
