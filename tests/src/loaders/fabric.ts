import { basename } from '@std/path'
import { MAX_HEAP_SIZE, ServerType } from '../const.ts'
import { Server } from '../server.ts'
import { downloadFile } from '../utils.ts'
import { ServerInstallError } from '../errors.ts'

export class FabricServer extends Server {
  override get type(): ServerType {
    return ServerType.fabric;
  }
  protected override async _install() {
    const installerVersionsResp = await fetch(
      'https://meta.fabricmc.net/v2/versions/installer/',
    );
    const installerVersions = await installerVersionsResp.json();
    // deno-lint-ignore no-explicit-any
    const installerUrl = installerVersions.filter((x: any) => x.stable)[0].url;
    const installerPath = new URL(basename(installerUrl), this.serverDir);
    await downloadFile(installerUrl, installerPath);

    const command = new Deno.Command('java', {
      args: [
        '-jar',
        installerPath.pathname,
        'server',
        '-mcversion',
        this.version,
        '-downloadMinecraft',
      ],
      stdin: 'null',
      stdout: 'null',
      cwd: this.serverDir,
    });
    const child = command.spawn();
    const status = await child.status;
    if (!status.success) {
      throw new ServerInstallError(
        `Failed to install ${this.type} ${this.version}`,
      );
    }
  }

  protected override get startCommand(): string[] {
    return [
      'java',
      `-Xmx${MAX_HEAP_SIZE}`,
      '-jar',
      'fabric-server-launch.jar',
      'nogui',
    ];
  }

  override get modsDir() {
    return new URL('mods/', this.serverDir);
  }

  override get modConfigDir() {
    return new URL('config/tgbridge/', this.serverDir);
  }

  protected override getDependencies() {
    const FABRIC_API_MAP = {
      '1.21.8': 'https://cdn.modrinth.com/data/P7dR8mSH/versions/CF23l2iP/fabric-api-0.133.4%2B1.21.8.jar',
      '1.21.4': 'https://cdn.modrinth.com/data/P7dR8mSH/versions/UnrycCWP/fabric-api-0.115.1%2B1.21.4.jar',
      '1.21.3': 'https://cdn.modrinth.com/data/P7dR8mSH/versions/FjU3tsgY/fabric-api-0.107.0%2B1.21.3.jar',
      '1.21.1': 'https://cdn.modrinth.com/data/P7dR8mSH/versions/thGkUOxt/fabric-api-0.107.0%2B1.21.1.jar',
      '1.20.6': 'https://cdn.modrinth.com/data/P7dR8mSH/versions/ocg4hG3t/fabric-api-0.100.8%2B1.20.6.jar',
      '1.20.4': 'https://cdn.modrinth.com/data/P7dR8mSH/versions/QVBohPm2/fabric-api-0.97.2%2B1.20.4.jar',
      '1.20.2': 'https://cdn.modrinth.com/data/P7dR8mSH/versions/8GVp7wDk/fabric-api-0.91.6%2B1.20.2.jar',
      '1.20.1': 'https://cdn.modrinth.com/data/P7dR8mSH/versions/P7uGFii0/fabric-api-0.92.2%2B1.20.1.jar',
      '1.19.4': 'https://cdn.modrinth.com/data/P7dR8mSH/versions/nyAmoHlr/fabric-api-0.87.2%2B1.19.4.jar',
      '1.19.2': 'https://cdn.modrinth.com/data/P7dR8mSH/versions/6g95K303/fabric-api-0.77.0%2B1.19.2.jar'
    }
    if (!(this.version in FABRIC_API_MAP)) {
      throw new Error(`Unsupported fabric version ${this.version}`)
    }
    return [
      'https://cdn.modrinth.com/data/Ha28R6CL/versions/i6MmXDwA/fabric-language-kotlin-1.13.6%2Bkotlin.2.2.20.jar',
      FABRIC_API_MAP[this.version as keyof typeof FABRIC_API_MAP],
    ]
  }
}
