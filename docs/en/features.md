# Features

## Supports modern Telegram features

tgbridge supports most of Telegram message types and markup, including modern features,
such as spoilers and quoted replies. You can change how the messages are rendered
in the `lang.yml` config file.

The mod also supports groups with topics enabled.
For the mod to work in such groups, set [`topicId`](/en/reference#chats-topicid)
for the corresponding chat.

## All event messages are configurable

You can toggle forwarding of start/stop, leave/join and death messages.
For advancements, you can toggle forwarding of each advancement type individually.
You can also toggle advancement description forwarding.

Config options: [`events`](/en/reference#events)

## Join messages

You can enable all join messages or only the first join message for each player.

Config option: [`events.joinMessages`](/en/reference#events-joinmessages)

## Message merge window

You can enable merging messages sent within a specified time window into
one to reduce amount of messages sent to the Telegram chat.

Config option: [`messages.mergeWindow`](/en/reference#messages-mergewindow)

## Leave/join merge window

Enable this feature to delete leave and join messages if a player
leaves and then joins within a specified time window.

Config option: [`events.leaveJoinMergeWindow`](/en/reference#events-leavejoinmergewindow)

## Telegram commands

- `/list` &mdash; shows the list of online players
- `/tps` &mdash; shows the current TPS if [spark](https://modrinth.com/mod/spark) is installed

## Bot message customization

Feel free to edit `lang.yml` however you see fit.
It's meant to be customized by the server owner and is never overwritten by the mod.

## Compatibility and integrations

See [Integrations](/en/integrations) for the list of compatible/incompatible mods
and implemented integrations.

