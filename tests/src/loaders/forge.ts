import { parse as parseXml } from '@libs/xml'
import { MAX_HEAP_SIZE, ServerType } from '../const.ts'
import { Server } from '../server.ts'
import { downloadFile } from '../utils.ts'
import { basename } from '@std/path'
import { ServerInstallError } from '../errors.ts'

abstract class AnyForgeServer extends Server {
  protected abstract getForgeUrl(): Promise<string>;

  protected override async configureServer(): Promise<void> {
    await super.configureServer();
    await Deno.writeTextFile(
      new URL('user_jvm_args.txt', this.serverDir),
      `-Xmx${MAX_HEAP_SIZE}\n`,
    );
  }

  protected override async _install() {
    const installerUrl = await this.getForgeUrl();
    const installerPath = new URL(basename(installerUrl), this.serverDir);
    await downloadFile(installerUrl, installerPath);

    const command = new Deno.Command('java', {
      args: [
        '-jar',
        installerPath.pathname,
        '--installServer',
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
    const runShFile = new URL('run.sh', this.serverDir);
    let runShContent = await Deno.readTextFile(runShFile);
    runShContent = runShContent.replace('java', 'exec java');
    await Deno.writeTextFile(runShFile, runShContent);
  }

  protected override get startCommand() {
    return [
      './run.sh',
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
    if (this.version.split('.')[1] == '21' || this.version == '1.20.6') {
      return [
        'https://cdn.modrinth.com/data/ordsPcFz/versions/5Vlx7W4o/kotlinforforge-5.6.0-all.jar',
      ];
    } else if (this.version.split('.')[1] == '20' || this.version == '1.19.4') {
      return [
        'https://cdn.modrinth.com/data/ordsPcFz/versions/hmeyC41q/kotlinforforge-4.11.0-all.jar',
      ];
    } else if (['19', '18'].includes(this.version.split('.')[1])) {
      return [
        'https://cdn.modrinth.com/data/ordsPcFz/versions/NBn3sEQk/kotlinforforge-3.12.0-all.jar',
      ];
    } else {
      throw new Error(`Unsupported forge version ${this.version}`);
    }
  }
}

export class ForgeServer extends AnyForgeServer {
  override get type(): ServerType {
    return ServerType.forge;
  }
  protected override async getForgeUrl(): Promise<string> {
    const versionsListResponse = await fetch(
      'https://files.minecraftforge.net/net/minecraftforge/forge/maven-metadata.json',
    );
    const versionsList = await versionsListResponse.json();
    const versions = versionsList[this.version];
    const version = versions[versions.length - 1];

    const fileName = `forge-${version}-installer.jar`;
    return `https://maven.minecraftforge.net/net/minecraftforge/forge/${version}/${fileName}`;
  }
}

export class NeoforgeServer extends AnyForgeServer {
  override get type(): ServerType {
    return ServerType.neoforge;
  }
  protected override async getForgeUrl(): Promise<string> {
    const versionsDataResponse = await fetch(
      'https://maven.neoforged.net/releases/net/neoforged/neoforge/maven-metadata.xml',
    );
    const versionsData = parseXml(await versionsDataResponse.text());
    const versions: string[] =
      versionsData.metadata.versioning.versions.version;
    const versionPrefix = this.version.replace(/^1\./, '') + '.';
    const suitableVersions = versions.filter((v) =>
      v.startsWith(versionPrefix)
    );
    const version = suitableVersions[suitableVersions.length - 1];

    const fileName = `neoforge-${version}-installer.jar`;
    return `https://maven.neoforged.net/releases/net/neoforged/neoforge/${version}/${fileName}`;
  }
}
