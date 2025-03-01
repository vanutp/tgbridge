import { ensureDir, exists } from '@std/fs'
import { delay } from '@std/async'
import { basename } from '@std/path'
import { Rcon } from 'rcon-client'
import { portAllocator } from './portAllocator.ts'
import { RELEASE_VER, RELEASES_DIR, RUN_DIR, ServerType } from './const.ts'
import { BotApiMock } from './botApiMock.ts'
import { downloadFile, nanoidAlnum } from './utils.ts'
import { Client } from './client.ts'

export abstract class Server {
  readonly tg: BotApiMock;
  abstract get type(): ServerType;
  readonly version: string;

  private readonly port: number;
  private readonly rconPort: number;
  private readonly rconPassword: string;
  private readonly gradleProject: string;
  private proc?: Deno.ChildProcess;
  private hasStopped: boolean;
  private _rcon?: Rcon;
  get rcon() {
    return this._rcon!;
  }
  private _client?: Client;
  get client() {
    return this._client!;
  }

  protected abstract get modsDir(): URL;
  protected abstract get modConfigDir(): URL;
  protected abstract get startCommand(): string[];

  protected abstract _install(): Promise<void>;

  constructor(version: string, gradleProject: string) {
    this.version = version;
    this.port = portAllocator.next();
    this.rconPort = portAllocator.next();
    this.tg = new BotApiMock();
    this.rconPassword = nanoidAlnum();
    this.gradleProject = gradleProject;
    this.hasStopped = false;
  }

  protected getDependencies(): string[] {
    return [];
  }

  get serverDir() {
    return new URL(`${this.type}-${this.version}/`, RUN_DIR);
  }

  get key() {
    return `${this.type}-${this.version}`;
  }

  // deno-lint-ignore no-explicit-any
  async configureMod(settings: any) {
    await ensureDir(this.modConfigDir);
    const data = {
      ...settings,
      general: settings.general ? { ...settings.general } : {},
      advanced: settings.advanced ? { ...settings.advanced } : {},
    };
    data.general.botToken = this.tg.token;
    data.general.chatId = this.tg.chatId;
    data.advanced.botApiUrl = this.tg.apiUrl;
    await Deno.writeTextFile(
      new URL('config.yml', this.modConfigDir),
      JSON.stringify(data),
    );

    if (this.rcon) {
      const reloadResp = await this.rcon.send('tgbridge reload');
      if (!reloadResp.match('reloaded')) {
        throw new Error('failed to reload tgbridge config');
      }
    }
  }

  protected async configureServer() {
    await Deno.writeTextFile(new URL('eula.txt', this.serverDir), 'eula=true\n');
    await Deno.writeTextFile(
      new URL('server.properties', this.serverDir),
      `
server-port=${this.port}
enable-rcon=true
rcon.port=${this.rconPort}
rcon.password=${this.rconPassword}
level-type=minecraft\\:flat
spawn-protection=0
spawn-monsters=false
online-mode=false
view-distance=2
simulation-distance=2
`,
    );
    const logsDir = new URL('logs/', this.serverDir)
    await ensureDir(logsDir)
    for await (const logFile of Deno.readDir(logsDir)) {
      await Deno.remove(new URL(logFile.name, logsDir))
    }
    await this.configureMod({});
  }

  private get installedFile() {
    return new URL('.server-installed', this.serverDir);
  }

  async isInstalled() {
    return await exists(this.installedFile);
  }

  async initialize() {
    await ensureDir(this.serverDir);
    await ensureDir(this.modsDir);
    if (!(await this.isInstalled())) {
      await this._install();
      await Deno.writeTextFile(this.installedFile, '');
    }

    const depFilenames: string[] = [];
    for (const depUrl of this.getDependencies()) {
      const depFilename = basename(depUrl);
      depFilenames.push(decodeURIComponent(depFilename));
      const depPath = new URL(depFilename, this.modsDir);
      if (!(await exists(depPath))) {
        await downloadFile(depUrl, depPath);
      }
    }
    for await (const entry of Deno.readDir(this.modsDir)) {
      if (entry.isFile && !depFilenames.includes(entry.name)) {
        await Deno.remove(new URL(entry.name, this.modsDir));
      }
    }

    const releaseFile = new URL(
      `tgbridge-${RELEASE_VER}-${this.gradleProject}.jar`,
      RELEASES_DIR,
    );
    await Deno.copyFile(releaseFile, new URL('tgbridge.jar', this.modsDir));

    await this.configureServer();
  }

  async start() {
    if (this.proc) {
      throw new Error('Server already started');
    }
    if (this.hasStopped) {
      throw new Error('Server already stopped');
    }
    const command = new Deno.Command(this.startCommand[0], {
      args: this.startCommand.slice(1),
      stdin: 'null',
      stdout: 'null',
      cwd: this.serverDir,
    });
    this.proc = command.spawn();
    // TODO: detect "Exception stopping the server" in logs and kill immediately
    this.proc.status.then(() => {
      this.hasStopped = true;
    });

    let timeWaited = 0;
    while (true) {
      try {
        this._rcon = await Rcon.connect({
          host: 'localhost',
          port: this.rconPort,
          password: this.rconPassword,
        });
        break;
      } catch {
        if (this.hasStopped) {
          throw new Error('Failed to start the server');
        }
        await delay(500);
        timeWaited += 500;
        if (timeWaited >= 60_000) {
          throw new Error("Server hasn't started after 60s");
        }
      }
    }

    if (this.type == ServerType.forge || this.type == ServerType.neoforge) {
      // пиздец
      await delay(500)
    }

    try {
      this._client = new Client(this.port);
    } catch {
      if (this.hasStopped) {
        throw new Error('Failed to start the server');
      }
      await delay(500);
      timeWaited += 500;
      if (timeWaited >= 60_000) {
        throw new Error("Server hasn't started after 60s");
      }
    }
    await this.client.waitForSpawn();
  }

  async reset() {
    this.tg.reset();
    this.client.reset();
    // await this.rcon.send(`kill ${this.client.username}`)
    await this.rcon.send(`advancement revoke ${this.client.username} everything`)
    await this.rcon.send(`clear ${this.client.username}`)
  }

  async stop() {
    this.client?.stop();
    if (!this.proc) {
      return;
    }
    this.proc.kill();
    await this.proc.status;
    await this.tg.stop();
  }
}
