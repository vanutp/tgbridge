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
