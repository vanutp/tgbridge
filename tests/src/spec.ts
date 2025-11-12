import { delay } from '@std/async';
import { Server } from './server.ts'
import { assert } from './utils.ts'
import { Client } from "./client.ts";

async function minecraftToTelegram(server: Server) {
  const text = 'test minecraftToTelegram'
  await server.client.sendMessage(text)
  await delay(50)
  assert(server.tg.findMessage(msg => (msg.text ?? '').includes(text)))
}

async function telegramToMinecraft(server: Server) {
  const text = 'test telegramToMinecraft'
  server.tg.sendMessage({text})
  await delay(300)
  assert(server.client.findMessage(msg => JSON.stringify(msg.json).includes(text)))
}

async function advancement(server: Server) {
  const resp = await server.rcon.send(`advancement grant ${server.client.username} only minecraft:story/mine_stone`)
  assert(resp.includes('Granted'), `Bad RCON response: ${resp}`)
  await delay(50)
  const msg = server.tg.findMessage(msg => (msg.text ?? '').includes('Stone Age'))
  assert(msg)
  // 1.16.5 and other versions have different casing
  assert(msg.text!.toLowerCase().includes('mine stone with your new pickaxe'))
}

async function reloadCommand(server: Server) {
  const resp = await server.rcon.send('tgbridge reload')
  assert(resp.includes('Config reloaded.'))
}

async function reloadCommandFromOp(server: Server) {
  await server.rcon.send(`op ${server.client.username}`)
  server.client.sendCommand('tgbridge reload')
  await delay(300)
  assert(server.client.findMessage(msg => JSON.stringify(msg.json).includes("Config reloaded.")))
}

async function reloadCommandFromOrdinary(server: Server) {
  await server.rcon.send(`deop ${server.client.username}`)
  server.client.sendCommand('tgbridge reload')
  await delay(300)
  assert(!server.client.findMessage(msg => JSON.stringify(msg.json).includes("Config reloaded.")))
}

async function muteUnmuteCommand(server: Server) {
  server.client.sendCommand('tgbridge toggle')
  await delay(200)

  const textMuted = 'test muted message'
  server.tg.sendMessage({text: textMuted})
  await delay(300)
  assert(!server.client.findMessage(msg => JSON.stringify(msg.json).includes(textMuted)))

  server.client.sendCommand('tgbridge toggle')
  await delay(200)

  const textUnmuted = 'test unmuted message'
  server.tg.sendMessage({text: textUnmuted})
  await delay(200)
  assert(server.client.findMessage(msg => JSON.stringify(msg.json).includes(textUnmuted)))
}

async function joinAndLeavePlayer(server: Server) {
  const secondClient = new Client(server.port, '2nd_user');
  await secondClient.waitForSpawn();

  assert(server.tg.findMessage(msg => (msg.text ?? '').includes(`${secondClient.username} joined the game`)))

  secondClient.stop()
  await delay(200)
  assert(server.tg.findMessage(msg => (msg.text ?? '').includes(`${secondClient.username} left the game`)))
}

// deno-lint-ignore no-explicit-any
export const TESTS: ((server: Server) => any)[] = [
  minecraftToTelegram,
  telegramToMinecraft,
  advancement,
  reloadCommand,
  reloadCommandFromOp,
  reloadCommandFromOrdinary,
  joinAndLeavePlayer,
  muteUnmuteCommand
]

// TODO: test start/stop
