import { delay } from '@std/async';
import { Server } from './server.ts'
import { assert } from './utils.ts'

async function minecraftToTelegram(server: Server) {
  const text = 'test minecraftToTelegram'
  await server.client.sendMessage(text)
  await delay(50)
  assert(server.tg.findMessage(msg => (msg.text ?? '').includes(text)))
}

async function telegramToMinecraft(server: Server) {
  const text = 'test telegramToMinecraft'
  server.tg.sendMessage({text})
  await delay(200)
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

async function command(server: Server) {
  const resp = await server.rcon.send('tgbridge reload')
  assert(resp.includes('Config reloaded.'))
}

// deno-lint-ignore no-explicit-any
export const TESTS: ((server: Server) => any)[] = [
  minecraftToTelegram,
  telegramToMinecraft,
  advancement,
  command,
]

// TODO: test start/stop and join/leave
