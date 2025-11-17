import { basename } from '@std/path'
import { MAX_HEAP_SIZE, ServerType } from '../const.ts'
import { downloadFile } from '../utils.ts'
import { Server } from '../server.ts'

export class PaperServer extends Server {
  override get type() {
    return ServerType.paper;
  }

  protected override async _install() {
    const versionsResp = await fetch(
      `https://api.papermc.io/v2/projects/paper/versions/${this.version}/builds`,
    );
    const versionsData = await versionsResp.json();
    const version = versionsData.builds[versionsData.builds.length - 1];
    const suffix = Object.hasOwn(version.downloads, 'mojang-mappings')
      ? '-mojang'
      : '';
    const versionUrl =
      `https://api.papermc.io/v2/projects/paper/versions/${this.version}/builds/${version.build}/downloads/paper-${this.version}-${version.build}${suffix}.jar`;

    const serverFileName = basename(versionUrl);
    const serverFilePath = new URL(serverFileName, this.serverDir);
    await downloadFile(versionUrl, serverFilePath);

    const runShFile = new URL('run.sh', this.serverDir);
    await Deno.writeTextFile(
      runShFile,
      `
#!/bin/sh
exec java -Xmx${MAX_HEAP_SIZE} -jar ${serverFileName} nogui
`.trimStart(),
    );
    await Deno.chmod(runShFile, 0o755);
  }

  protected override get startCommand(): string[] {
    return [
      './run.sh',
    ];
  }

  override get modsDir() {
    return new URL('plugins/', this.serverDir);
  }

  override get modConfigDir() {
    return new URL('plugins/tgbridge/', this.serverDir);
  }

  protected override getDependencies() {
    return [
      'https://cdn.modrinth.com/data/5Z9kTs3P/versions/UKOogiGF/KotlinMC-2.2.20.jar',
    ];
  }
}
