<!-- Generated by codegen/generate_config.py. Do not modify-->
# Config reference

## general

### botToken

- **Type:** `string`
- **Required**

### chatId

- **Type:** `number`
- **Required**

### topicId

- **Type:** `number | null`

## messages

### requirePrefixInMinecraft

- **Type:** `string | null`
- **Default:** `null` (disabled)
- **Example:** `"!"` (quotes are required)

::: warning
Don't enable this if you have a chat plugin installed.
See [Compatibility](https://tgbridge.vanutp.dev/en/compatibility#chat) for more info
:::
If this value is set, messages without specified prefix won't be forwarded to Telegram.


### keepPrefix

- **Type:** `boolean`

Set to `true` to keep the prefix specified in the above setting in the message


### mergeWindow

- **Type:** `number`
- **Default:** `0` (disabled)

Chat messages sent within the specified interval will be merged in one.
The value is specified in seconds


### useRealUsername

- **Type:** `boolean`

Set to `true` to use real player username instead of display name in all Telegram messages


## integrations

### bluemapUrl

- **Type:** `string | null`
- **Default:** `null` (disabled)
- **Example:** `https://map.example.com`

If this value is set, waypoints shared from Xaero's Minimap/World Map will be rendered
as links to a specified BlueMap instance.


### globalChatName

- **Type:** `string`

The chat name to forward messages from.
Only has an effect when a compatible chat plugin with support for named chats, such as Chatty, is installed.
(see [Compatibility](https://tgbridge.vanutp.dev/en/compatibility#chat))


### incompatiblePluginChatPrefix

- **Type:** `string | null`
- **Default:** `null` (disabled)
- **Example:** `"!"` (quotes are required)

See also: [Compatibility](https://tgbridge.vanutp.dev/en/compatibility#chat)

Use this if you have an incompatible plugin, such as CMI or AdvancedChat installed.
Will register a legacy chat listener with LOWEST priority
and only forward messages that start with the specified string.
Currently this only has an effect on Paper.


## events

### advancementMessages

#### enable

- **Type:** `boolean`

#### enableTask

- **Type:** `boolean`

Configure forwarding of each advancement type


#### enableGoal

- **Type:** `boolean`

Configure forwarding of each advancement type


#### enableChallenge

- **Type:** `boolean`

Configure forwarding of each advancement type


#### showDescription

- **Type:** `boolean`

Include advancement descriptions in Telegram messages


### enableDeathMessages

- **Type:** `boolean`

### enableJoinMessages

- **Type:** `boolean`

### enableLeaveMessages

- **Type:** `boolean`

### leaveJoinMergeWindow

- **Type:** `number`
- **Default:** `0` (disabled)

If a player leaves and then joins within the specified time interval,
the leave and join messages will be deleted.
This is useful when players frequently re-join, for example because of connection problems.
Only has effect when both enableJoinMessages and enableLeaveMessages are set to true.
The value is specified in seconds


### enableStartMessages

- **Type:** `boolean`

Whether to send a Telegram message when the server starts


### enableStopMessages

- **Type:** `boolean`

Whether to send a Telegram message when the server stops


## advanced

### botApiUrl

- **Type:** `string`

## version

- **Type:** `number`

Config file version. Don't change manually

