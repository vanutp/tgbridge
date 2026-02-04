# API

The API is in an early stage. If something is missing or not working, please open an issue on GitHub.

## Dependency

Start by adding tgbridge dependency to your project build system
and to your `plugin.yml`, `fabric.mod.json` or `mods.toml`.

::: code-group
```kotlin [Gradle KTS]
repositories {
    maven {
        url = uri("https://maven.vanutp.dev/main")
        content {
            includeGroup("dev.vanutp.tgbridge")
        }
    }
}

dependencies {
    compileOnly("dev.vanutp.tgbridge:common:0.9.0")
}
```
```groovy [Gradle]
repositories {
    maven {
        url = "https://maven.vanutp.dev/main"
        content {
            includeGroup "dev.vanutp.tgbridge"
        }
    }
}

dependencies {
    compileOnly "dev.vanutp.tgbridge:common:0.9.0"
}
```
:::

Unreleased versions from the `master` branch are published by CI as `1.0-SNAPSHOT`.


[//]: # (You can add `dev.vanutp.tgbridge:paper` dependency instead of `dev.vanutp.tgbridge:common`)
[//]: # (if you are developing a Paper-only plugin to get some additional functionality)
[//]: # (&#40;like `PaperTgbridgePlayer` and `AbstractPaperModule`&#41;.)

## Java interoperability

tgbridge is written in Kotlin, but you should also be able to interface with it from Java.

In some cases, you may need to add Kotlin dependency to your project

::: code-group
```kotlin [Gradle KTS]
dependencies {
    compileOnly("org.jetbrains.kotlin:kotlin-stdlib:2.2.21")
}
```
```groovy [Gradle]
dependencies {
    compileOnly "org.jetbrains.kotlin:kotlin-stdlib:2.2.21"
}
```
:::

Property values can be accessed/set from Java using getter/setter methods (e.g. `bridge.bot` -> `bridge.getBot()`).

### Coroutines

Network requests in tgbridge are asynchronous and are implemented using Kotlin coroutines.
However, since Kotlin's suspend functions cannot be called from Java,
each suspend method has a corresponding method suffixed with `Async`
that returns a `CompletableFuture` instance.
For example, `suspend fun sendMessage(...)` -> `fun sendMessageAsync(...): CompletableFuture`.

Similarly, all event registration methods that accept functions have 3 overloads:
one for `suspend` functions, one for async functions returning `CompletableFuture`,
and one for normal functions (`Consumer`).

## tgbridge instance

You can get an instance of the main tgbridge class with `TelegramBridge.INSTANCE` (or `TelegramBridge.Companion.getINSTANCE()` in Java).
In this documentation, `TelegramBridge` instance is referenced as `bridge`.

## Text formats

tgbridge uses multiple text formats for various purposes:

- [Adventure](https://docs.papermc.io/adventure/) is the main library used for storing and processing Minecraft text.
  Currently, a native Adventure library is only used on Paper. On Fabric and (Neo)Forge Adventure bundled Adventure 
  located in `tgbridge.shaded.kyori.adventure` is used, making Component instances incompatible with other
  mods using this library.
- Native Minecraft `Text` is used on Fabric and (Neo)Forge for interfacing with the game.
  It's not used for anything other that conversion from/to Adventure `Component`.
- `TelegramFormattedText` (raw text + Telegram text entities) is used for forwarding chat and death messages from
  Minecraft to Telegram. Adventure `Component` can be converted to this format using `MinecraftToTelegramConverter.convert`.
- [Telegram HTML](https://core.telegram.org/bots/api#html-style) and [MiniMessage](https://docs.papermc.io/adventure/minimessage/format/)
  are used for storing translations/templates in config and language files.
  `formatLang` and `formatMiniMessage` functions can be used to render these (see [Utility functions](#utility-functions) below)

## Events

You can use the event system to read and modify the data flowing through tgbridge.
The event listeners are called during event processing and allow to change the outcome
(e.g. to change chat message contents, list of recipients or don't forward it altogether).
Some events can be cancelled by setting `event.isCancelled = true`.
If an event is cancelled, all further processing of the event is stopped.

Events are registered using `TgbridgeEvents` object. For example:

::: code-group
```kotlin
TgbridgeEvents.MC_CHAT_MESSAGE.addListener { e ->
    // handle chat message
}
```
```java
TgbridgeEvents.INSTANCE.getMC_CHAT_MESSAGE().addListener(e -> {
    // handle chat message
});
```
:::

You can also specify a priority for a listener.
Priorities go from `LOWEST` to `HIGHEST`, like in (Neo)Forge (unlike Bukkit/Spigot/Paper).
The default priority is normal.

::: code-group
```kotlin
TgbridgeEvents.MC_CHAT_MESSAGE.addListener(EventPriority.HIGHEST) { e ->
    // handle chat message
}
```
```java
TgbridgeEvents.INSTANCE.getMC_CHAT_MESSAGE().addListener(EventPriority.HIGHEST, e -> {
    // handle chat message
});
```
:::

### Event types

Available events are:
- `TG_CHAT_MESSAGE` — called just before a message is forwarded from Telegram to Minecraft
- Minecraft events: `MC_CHAT_MESSAGE`, `DEATH`, `JOIN`, `LEAVE`, `ADVANCEMENT` —
  called immediately after a corresponding Minecraft event is received and before it's processed
- `POST_RELOAD` — called after tgbridge config is reloaded
- `PLAYER_PLACEHOLDERS` — called to retrieve custom placeholders for a player.
  Add your placeholders to `event.placeholders`. You can then use them in Telegram messages in `lang.yml`.
  `event.originalEvent` property will be set the currently handled tgbridge event
  (e.g. `TgbridgeJoinEvent(...)` for the `JOIN` event).
- `RECIPIENTS` — used for multiple chats support.
  Is called to get the list of players that should receive a chat message sent to `event.chat`.
  See [Chat module](#chat-module) below for details.

### Calling events manually

You can dispatch Minecraft events (`MC_CHAT_MESSAGE`, `DEATH`, `JOIN`, `LEAVE` and `ADVANCEMENT`)
manually using `bridge.onChatMessage`/`bridge.onPlayerDeath`/`bridge.onPlayerJoin`/`bridge.onPlayerLeave`/`bridge.onPlayerAdvancement` methods respectively.

You can call `PLAYER_PLACEHOLDERS` and `RECIPIENTS` events manually using `TgbridgeEvents.<EVENT>.invoke(event)`
(pass empty values of `placeholders`/`recipients` to the event payload constructor)

## Modules

Modules are an internal tgbridge mechanism to separate non-core features.
Currently, you only need to use modules if you want to add vanish or chat plugin integration.

To create a module, you first need to implement `ITgbridgeModule`.
The only method you must implement is `enable`. It will be called after the server is started.

Then add a module instance using `bridge.addModule(moduleInstance)`.
You must do this before the server is started (e.g. in `onEnable` method of your plugin).


[//]: # (Instead of implementing `ITgbridgeModule` directly, you can also extend `AbstractModule` or `AbstractPaperModule`)
[//]: # (if you are using `dev.vanutp.tgbridge:paper` dependency in your project.)

### Chat module

To create a chat integration

1. Implement `IChatModule`. If a module implementing `IChatModule` is loaded,
   the default `RECIPIENTS` event handler and chat event listeners are disabled
2. Register a handler for the `RECIPIENTS` event (with default priority). It should add the
   players that should receive a message sent to `event.chat` to `event.recipients`.
   `event.recipients` is a list of `ITgbridgePlayer` objects.
   `ITgbridgePlayer` is an interface used to represent a Minecraft player in a platform-agnostic way.
   You need to implement it for your integration.

   [//]: # (You can either implement it yourself, or use `PaperTgbridgePlayer`)
   [//]: # (if you use `dev.vanutp.tgbridge:paper` dependency in your project.)
3. Add a listener for your chat plugin's chat event that calls `bridge.onChatMessage`
   (see [Events](#events) for details)

See [HeroChatModule](https://github.com/vanutp/tgbridge/blob/master/implementations/paper/src/main/kotlin/dev/vanutp/tgbridge/paper/modules/HeroChatModule.kt)
for an example of a chat module.

### Vanish module

To create a vanish integration

1. Implement `IVanishModule`. If `isVanished` method of any loaded `IVanishModule` returns `true`
   for a player, that player is considered vanished
2. Call `bridge.onPlayerLeave` and `bridge.onPlayerJoin` when a player vanishes/unvanishes

See [VanishModule](https://github.com/vanutp/tgbridge/blob/master/implementations/fabric/src/main/kotlin/dev/vanutp/tgbridge/fabric/modules/VanishModule.kt)
for an example of a simple vanish module and [EssentialsVanishModule](https://github.com/vanutp/tgbridge/blob/master/implementations/paper/src/main/kotlin/dev/vanutp/tgbridge/paper/modules/EssentialsVanishModule.kt)
for a more complex example.

## Interfacing with Telegram

### Sending messages

To send a message to a Telegram chat that is configured in tgbridge, use the `ChatManager` class.
It has a more stable interface than `TelegramBot` and doesn't break [message merging](/en/features#message-merge-window).

Get a `ChatManager` instance with `bridge.chatManager`,
then send a message using `chatManager.sendMessage(messageContent)`.

There are 3 `MessageContent` classes you'll likely want to use:

- `MessageContentHTMLText` — sends a normal message using Telegram HTML formatting
- `MessageContentText` — sends a normal message using either Minecraft text (`Component`)
  or raw Telegram text and entities wrapped by `TelegramFormattedText` class.
  See [Text formats](#text-formats) for details.
- `MessageContentMergeableText` — like `MessageContentText`, but the message
  may be merged like chat messages when the [merge window setting](/en/features#message-merge-window) is enabled.

To send a message to any chat by id, use `bridge.bot.sendMessage`.
You can also use corresponding methods to edit or delete messages.
This API is not stable yet.

### Receiving messages from Telegram

You can add a listener for all incoming messages using `bridge.bot.registerMessageHandler`.
You can also add a command handler using `bridge.bot.registerCommandHandler`.

## Utility functions

Some utility functions that might be useful:

- `ConfigManager.config` — tgbridge configuration. It's not recommended to use it directly,
  as its structure may change.
- `ConfigManager.config.getDefaultChat()`
- `ConfigManager.config.getChat(...)` — get chat by name or id
- `String.formatMiniMessage(placeholders)` (`UtilsKt.formatMiniMessage` in Java) — render a MiniMessage-formatted string to `Component`
- `String.formatLang(placeholders)` (`UtilsKt.formatLang` in Java) — replace placeholders in a plain text/HTML-formatted string
