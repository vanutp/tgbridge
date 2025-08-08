import { Bot, createBot } from 'mineflayer'
import type { ChatMessage } from 'prismarine-chat'
import { TestFrameworkError } from './errors.ts'

export class Client {
  private readonly port: number;
  private readonly bot: Bot;
  // TODO: remove?
  private chatMessages: ChatMessage[];
  readonly username: string;

  constructor(port: number, username: string) {
    this.port = port;
    this.username = username;
    this.bot = createBot({
      host: 'localhost',
      port,
      username: this.username,
    });
    this.chatMessages = [];
    this.setupHandlers();
  }

  waitForSpawn(): Promise<void> {
    return new Promise((resolve) => {
      this.bot.once('spawn', () => {
        resolve();
      });
    });
  }

  private setupHandlers() {
    this.bot.on('message', (msg) => {
      this.chatMessages.push(msg);
    });
  }

  reset() {
    this.chatMessages = [];
  }

  sendMessage(text: string): Promise<void> {
    this.bot.chat(text);
    return new Promise((resolve, reject) => {
      this.bot.once('message', (msg) => {
        if (!JSON.stringify(msg.json).includes(text)) {
          reject(new TestFrameworkError('Wrong message received'));
        }
        resolve();
      });
    });
  }

  sendCommand(text: string) {
    this.bot.chat(`/${text}`);
  }

  stop() {
    this.bot.end();
  }

  findMessage(
    predicate: (msg: ChatMessage) => unknown,
  ): ChatMessage | undefined {
    return this.chatMessages.find(predicate);
  }

  // async getMessage() {
  //   try {
  //     return await Promise.race([
  //       new Promise<ChatMessage>((resolve) =>
  //         this.bot.once('message', resolve)
  //       ),
  //       new Promise<ChatMessage>(
  //         (_, reject) =>
  //           setTimeout(
  //             () => reject(new TimeoutError()),
  //             1000,
  //           ),
  //       ),
  //     ]);
  //   } catch (e) {
  //     // if (e instanceof TimeoutError) {
  //     //   return undefined
  //     // }
  //     throw e;
  //   }
  // }
}
