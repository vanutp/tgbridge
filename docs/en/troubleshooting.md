# Troubleshooting

## Most/all messages aren't forwarded from Telegram to Minecraft, but everything else works
Make sure you've disabled the "Group Privacy" setting in the "Bot Settings" menu in BotFather.
After changing it, delete and re-add the bot to the group.
Alternatively, make the bot a group admin.

## I get "Conflict: terminated by other getUpdates request; make sure that only one bot instance is running" error
Make sure that there is only one server running with that bot token.
You can't use one bot for multiple servers due to Bot API limitations.

## Messages are visible in the server console, but players don't see them
If you use Paper, make sure the players have the `bukkit.broadcast` permission.
