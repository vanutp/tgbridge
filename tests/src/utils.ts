import { customAlphabet } from 'nanoid'
import { AssertionError } from './errors.ts'
import { ServerType } from './const.ts'
import { FabricServer } from './loaders/fabric.ts'
import { ForgeServer, NeoforgeServer } from './loaders/forge.ts'
import { PaperServer } from './loaders/paper.ts'

export async function downloadFile(url: string, path: URL): Promise<void> {
  const resp = await fetch(url)
  const file = await Deno.open(path, { create: true, write: true })
  await resp.body!.pipeTo(file.writable)
}

export const nanoidAlnum = customAlphabet('ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789')

export function assert(condition: unknown, message?: string): asserts condition {
  if (!condition) {
    throw new AssertionError(message)
  }
}

export function makeServer(type: ServerType, version: string, gradleProject: string) {
  switch (type) {
    case ServerType.fabric:
      return new FabricServer(version, gradleProject);
    case ServerType.forge:
      return new ForgeServer(version, gradleProject);
    case ServerType.neoforge:
      return new NeoforgeServer(version, gradleProject);
    case ServerType.paper:
      return new PaperServer(version, gradleProject);
    default:
      throw new Error(`Unknown server type: ${type}`);
  }
}

export async function logNoNewline(message: string) {
  await Deno.stdout.write(new TextEncoder().encode(message))
}
