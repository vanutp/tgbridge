import { basename } from '@std/path'
import { MAX_HEAP_SIZE, ServerType } from '../const.ts'
import { downloadFile } from '../utils.ts'
import { Server } from '../server.ts'

export class PaperServer extends Server {
  override get type() {
    return ServerType.paper
  }

  protected override async _install() {
    // Fallback to Purpur because Paper API v2 is sunset
    const versionUrl = `https://api.purpurmc.org/v2/purpur/${this.version}/latest/download`

    const serverFileName = `purpur-${this.version}.jar`
    const serverFilePath = new URL(serverFileName, this.serverDir)
    await downloadFile(versionUrl, serverFilePath)

    const runShFile = new URL('run.sh', this.serverDir)
    await Deno.writeTextFile(
      runShFile,
      `
#!/bin/sh
exec java -Xmx${MAX_HEAP_SIZE} -jar ${serverFileName} nogui
`.trimStart(),
    )
    await Deno.chmod(runShFile, 0o755)
  }

  protected override get startCommand(): string[] {
    return [
      './run.sh',
    ]
  }

  override get modsDir() {
    return new URL('plugins/', this.serverDir)
  }

  override get modConfigDir() {
    return new URL('plugins/tgbridge/', this.serverDir)
  }

  protected override getDependencies() {
    return [
      'https://cdn.modrinth.com/data/5Z9kTs3P/versions/UKOogiGF/KotlinMC-2.2.20.jar',
    ]
  }
}
