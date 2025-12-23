### 0.9.1

- Support 1.21.11 on Fabric

### 0.9.0

- Support multiple chats (via chat plugins; see https://tgbridge.vanutp.dev/en/integrations#chat)
- DiscordSRV integration
- FlectonePulse support
- Carbon Chat support on Fabric
- Show inline usages (via bot) in Minecraft
- Kotlin plugin now required on Paper (e.g. https://modrinth.com/plugin/kotlinmc)
- Allow to disable all join messages except first joins
- Support HTTP and Socks5 proxies for Telegram
- Add a command to send messages to Telegram (see https://tgbridge.vanutp.dev/en/commands#tgbridge-send)
- Implement API (see https://tgbridge.vanutp.dev/en/api)

### 0.8.6-p2

- fabric: mark 1.21.10 as supported

### 0.8.6

- Support 1.21.9 on Fabric and Paper
- Fabric: fix crash when a player is killed using certain enchanted items

### 0.8.5

- NeoForge: fix crash when a player is killed using certain items
- Fix Fabric Vanish and EssentialsX vanish

### 0.8.4

- Fix Voice Messages integration on Paper
- Support Carbon Chat on Paper

### 0.8.3

- Support `integrations.incompatiblePluginChatPrefix` on (Neo)Forge
- Support Herochat on Paper

### 0.8.2

- paper: Use ServerLoadEvent to detect when the server is fully started. Fixes integration initialization

### 0.8.1

- Fix Folia

### 0.8.0

- 1.21.8 support
- Voice Messages integration
- Add `/tgbridge toggle` command allowing to toggle Telegram message visibility for yourself
- Retry failed Telegram API requests (fixes startup errors when Telegram API is unreachable)

### 0.7.2-p1

- fabric: mark 1.21.7 as supported

### 0.7.2

- fabric,paper: 1.21.6 support
- fabric: Support mods that cancel death events, e.g. Incapacitated
- paper: Restore console logging for Telegram messages

### 0.7.1

- Fix first time join message displaying more than once on (Neo)Forge
- Respect `messages.useRealUsername` config option for death messages
- 1.21.5 support
- paper: Add temporary /tghide and /tgshow commands (by @RedstoneFX)

### 0.7.0

- New docs website: https://tgbridge.vanutp.dev
- paper: Implement full integration for Chatty, better support for incompatible chat plugins (CMI, AdvancedChat, etc.). See docs for details
- paper: Fix EssentialsX vanish integration
- Support formatting for death messages. Also fixes incompatibilities with some mods/plugins
- Send different message when a player joins for the first-time
- Fix mod crash when a xaero waypoint is shared while integrations.bluemapUrl isn't set
- Fix receiving group messages when the topic id is 1 (primary topic)
- Send start message to the chat only when the server is fully started
- Implement /tps Telegram command
- Make the /tgbridge reload set bot token when the mod isn't set up
- Support join/leave messages and video chat service messages
- Support Forge 1.16.5

### 0.6.1

- Add CMI to legacy chat listener exceptions list. Fixes CMI global chat compatibility.
- Fix errors when config has default values
- Don't show vanished players in the player list
- Paper: Decrease event listener priorities

### 0.6.0

- 1.21.4 support
- Add an option to disable start/stop messages
- Add an option to use real username instead of display name
- Allow customization of Minecraft message formatting
- Support message formatting. Formatting in Minecraft -> Telegram messages requires Styled Chat or similar mod/plugin
- Fabric: Add support for Fabric Vanish
- Paper: Add support for EssentialsX vanish and SuperVanish

### 0.5.0

- fabric: Merge all fabric versions in one jar
- Allow changing the Bot API URL in the config
- fabric: Fix compatibility with mods changing chat messages rendering, notably StyledChat
- paper: Relocate kotlin packages, providing better compatibility with other plugins

### 0.4.7

- 1.21.3 support
- Fix compatibility with some mods bundling adventure

### 0.4.6

- Remove prefix from messages by default, add an option to keep it

### 0.4.5

- fabric: 1.21.1 support
- paper: Fix compatibility with plugins that change displayName
- paper: Fix compatibility with Chatty
- paper: Add Folia support

### 0.4.4

- Fix random crashes
- Fix chat message when a player is killed with an item which name contains translatable components
- Fix anonymous admin names in minecraft chat
- Support xaero waypoints without height

### 0.4.3

- Add fabric 1.21 support

### 0.4.2

- Fix mod crash when players get certain advancements
- Attempt to fix the mod after a crash when calling `/tgbridge reload`

### 0.4.1

- Fix telegram -> minecraft forwarding
- Add support for fabric 1.20.2-1.20.6

### 0.4

- forge: Fix using the mod alongside other mods using adventure
- forge: Only forward advancements that are announced to chat
- Add an option to configure forwarding of each advancement type separately
- Better handling of polling errors
- Delete webhook on startup if needed
