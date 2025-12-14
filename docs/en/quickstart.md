# Quick start

## Server or client?

This mod mostly works server-side, but can optionally be installed client-side for features
such as `/tgbridge dump_lang` command (see [Localization](/en/localization)) and image rendering support (coming soon).

## Getting your chat and topic ids

To use the mod, you'll need the id of the chat you want to link with.
You'll also need a topic id if you have topics enabled.
There are multiple ways to get them:

::: details If your chat is private (i.e. no t.me/username link)
Right click/tap on any message in your chat/topic and choose "Copy Message Link".
If there is no such option, enable "Chat history for new members" in the group settings. It can then be disabled.

The copied link will be in the format `https://t.me/c/<chat_id>/<topic_id>/<message_id>`.
Topic id will only be present if the chat has topics enabled, otherwise you don't need it.
:::

:::: details Using a bot
Add the @getmyid_bot to your group (it can then be immediately removed). 
It will send your user id and chat id.

::: warning
The chat id should start with `-100`. If it doesn't, enable "Chat history for new members" in the group settings. It can then be disabled.
:::

You can then get your topic id using the first method, if you need it.
::::

## Creating your bot

1. Open https://t.me/BotFather and use the /newbot command to create a bot
2. *Disable* the "Group Privacy" setting in the "Bot Settings" menu
3. Add the created bot to your group

## Installing the mod

1. Download and install the latest release for your loader from the Modrinth page: https://modrinth.com/mod/tgbridge.
   Don't forget to install the dependencies listed on the version page.
2. Start the server
3. Put your bot token, chat id and topic id (if applicable) in the generated config file located at
`config/tgbridge/config.yml` (or `plugins/tgbridge/config.yml` for Paper)
4. Use the `/tgbridge reload` command to reload the config
5. The bot should work now

If you require proxy to connect to Telegram, you can configure it in the
[`advanced.proxy`](/en/reference#advanced-proxy) config section.

To learn more about mod features, check out [Features](/en/features) and [Commands](/en/commands).

If something doesn't work, check the [Troubleshooting](/en/troubleshooting) section.
