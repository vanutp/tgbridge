// deno-lint-ignore-file no-explicit-any
import { delay } from '@std/async'
import { BadRequestError, ChatNotFoundError, MessageToEditNotFoundError, ReplyNotFoundError } from './errors.ts'
import { portAllocator } from './portAllocator.ts'

export interface TgUser {
  id: number;
  first_name: string;
  last_name: string | null;
  username: string | null;
}

export interface TgChat {
  id: number;
  title: string;
  username: string | null;
}

export type TgAny = any;

export interface TgPoll {
  question: string;
}

export interface TgMessageMedia {
  animation: TgAny | null;
  photo: TgAny[] | null;
  audio: TgAny | null;
  document: TgAny | null;
  sticker: TgAny | null;
  video: TgAny | null;
  video_note: TgAny | null;
  voice: TgAny | null;
  poll: TgPoll | null;
}

export interface TgMessageOrigin {
  sender_user: TgUser | null;
  sender_user_name: string | null;
  sender_chat: TgChat | null;
  chat: TgChat | null;
}

export interface TgExternalReplyInfo extends TgMessageMedia {
  origin: TgMessageOrigin;
  chat: TgChat | null;
}

export interface TgTextQuote {
  text: string;
}

export interface TgMessage extends TgMessageMedia {
  chat: TgChat;
  message_id: number;
  from: TgUser | null;
  sender_chat: TgChat | null;
  forward_from: TgUser | null;
  forward_from_chat: TgChat | null;
  reply_to_message: TgMessage | null;
  external_reply: TgExternalReplyInfo | null;
  quote: TgTextQuote | null;
  message_thread_id: number | null;
  author_signature: string | null;
  text: string | null;
  caption: string | null;
  pinned_message: TgMessage | null;
}

export interface TgUpdate {
  update_id: number;
  message: TgMessage | null;
}

export class BotApiMock {
  private readonly port: number;
  private readonly server: Deno.HttpServer;
  // TODO: remove?
  private messages: TgMessage[];
  private messageIdCounter: number;
  private updateQueue: TgUpdate[];
  private updateCounter: number;
  private messageListenersOnce: ((msg: TgMessage) => any)[];

  get token() {
    return 'MEOW';
  }

  get apiUrl() {
    return 'http://localhost:' + this.port;
  }

  get chatId() {
    return -1001874000000;
  }

  get botUser(): TgUser {
    return {
      id: 1874,
      first_name: 'Telegram Bridge',
      last_name: null,
      username: 'test_bot',
    };
  }

  constructor() {
    this.port = portAllocator.next();
    this.server = Deno.serve({
      hostname: 'localhost',
      port: this.port,
      onError: this.errorHandler.bind(this),
      handler: this.handler.bind(this),
      onListen() {},
    });
    this.messages = [];
    this.messageIdCounter = 1;
    this.updateQueue = [];
    this.updateCounter = 1;
    this.messageListenersOnce = [];
  }

  private static error(code: number, message: string, method?: string, body?: any) {
    return Response.json({
      description: message,
      error_code: code,
      ok: false,
      request: {
        method,
        ...body,
      },
    }, { status: code });
  }

  private static ok(result: any) {
    return Response.json({
      result,
      ok: true,
    });
  }

  private errorHandler(err: unknown): Response {
    console.error(err);
    return BotApiMock.error(500, (err as Error).toString());
  }

  private async handler(req: Request): Promise<Response> {
    const url = new URL(req.url);
    const urlParams = Object.fromEntries(url.searchParams);
    const contentType = req.headers.get('Content-Type') ?? '';
    let body = {};
    if (contentType.startsWith('application/json')) {
      body = await req.json();
    } else if (contentType.startsWith('multipart/form-data')) {
      body = Object.fromEntries((await req.formData()).entries());
    }
    const args: any = { ...urlParams, ...body };

    const [, token, _method] = url.pathname.match(/^\/bot(.*?)\/(.*)/)!;
    if (token != this.token) {
      return BotApiMock.error(401, 'Unauthorized', _method, args);
    }
    const method = 'api_' + _method;
    const allowedMethods = [
      'api_getMe',
      'api_sendMessage',
      'api_editMessageText',
      'api_deleteMessage',
      'api_getUpdates',
      'api_deleteWebhook',
      'api_setMyCommands',
    ];
    if (allowedMethods.includes(method)) {
      try {
        const _result =
          (this[method as keyof typeof this] as (args: any) => any)(args);
        const result = _result instanceof Promise ? await _result : _result;
        return BotApiMock.ok(result);
      } catch (e) {
        if (e instanceof BadRequestError) {
          return BotApiMock.error(400, 'Bad Request: ' + e.message, _method, args);
        }
        throw e;
      }
    } else {
      console.warn(`Method ${_method} is not implemented in mock Bot API`);
      return BotApiMock.error(404, 'Not found', _method, args);
    }
  }

  private api_getMe(): TgUser {
    return this.botUser;
  }

  private api_sendMessage(
    req: {
      chat_id: number;
      text: string;
      reply_to_message_id: number | null;
    },
  ): TgMessage {
    if (req.chat_id != this.chatId) {
      throw new ChatNotFoundError();
    }
    return this.sendMessage({
      text: req.text,
      reply_to_message_id: req.reply_to_message_id,
      isFromBot: true,
    });
  }

  private api_editMessageText(req: {
    chat_id: number;
    message_id: number;
    text: string;
  }): TgMessage {
    if (req.chat_id != this.chatId) {
      throw new ChatNotFoundError();
    }
    const msgs = this.messages.filter((x) => x.message_id == req.message_id);
    if (msgs.length == 0) {
      throw new MessageToEditNotFoundError();
    }
    const msg = msgs[0];
    msg.text = req.text;
    return msg;
  }

  private api_deleteMessage(req: {
    chat_id: number;
    message_id: number;
  }): boolean {
    if (req.chat_id != this.chatId) {
      throw new ChatNotFoundError();
    }
    this.messages = this.messages.filter((x) => x.message_id != req.message_id);
    return true;
  }

  private async api_getUpdates(
    req: { offset: number; timeout: number },
  ): Promise<TgUpdate[]> {
    while (
      this.updateQueue.length > 0 && this.updateQueue[0].update_id < req.offset
    ) {
      this.updateQueue.shift();
    }

    if (this.updateQueue.length == 0) {
      await delay(100);
    }

    return this.updateQueue;
  }

  private api_deleteWebhook(): boolean {
    return true;
  }

  private api_setMyCommands(): boolean {
    return true;
  }

  sendMessage(
    data: {
      text: string;
      reply_to_message_id?: number | null;
      isFromBot?: boolean;
    },
  ): TgMessage {
    const from: TgUser = data.isFromBot ? this.botUser : {
      id: 304493639,
      first_name: 'Ванюта',
      last_name: null,
      username: 'vanutp',
    };
    const reply_to_message: TgMessage | null = data.reply_to_message_id
      ? this.messages.find((x) => x.message_id == data.reply_to_message_id) ??
        null
      : null;
    if (data.reply_to_message_id && !reply_to_message) {
      throw new ReplyNotFoundError();
    }
    const msg: TgMessage = {
      chat: {
        id: this.chatId,
        title: 'Test',
        username: null,
      },
      message_id: this.messageIdCounter++,
      from,
      sender_chat: null,
      forward_from: null,
      forward_from_chat: null,
      reply_to_message,
      external_reply: null,
      quote: null,
      message_thread_id: null,
      author_signature: null,
      text: data.text,
      caption: null,
      pinned_message: null,
      animation: null,
      photo: null,
      audio: null,
      document: null,
      sticker: null,
      video: null,
      video_note: null,
      voice: null,
      poll: null,
    };
    this.onMessage(msg);
    return msg;
  }

  reset() {
    this.messages = [];
    this.updateQueue = [];
    this.messageListenersOnce = [];
  }

  async stop() {
    await this.server.shutdown();
  }

  private onMessage(msg: TgMessage) {
    this.messages.push(msg);
    if (msg.from?.id != this.botUser.id) {
      this.updateQueue.push({
        update_id: this.updateCounter++,
        message: msg,
      });
    }

    this.messageListenersOnce.forEach((fn) => fn(msg));
    this.messageListenersOnce = [];
  }

  findMessage(predicate: (msg: TgMessage) => unknown): TgMessage | undefined {
    return this.messages.find(predicate);
  }

  findBotMessage(
    predicate: (msg: TgMessage) => unknown,
  ): TgMessage | undefined {
    return this.messages.find((msg) =>
      msg.from?.id == this.botUser.id && predicate(msg)
    );
  }

  // async getBotMessage() {
  //   try {
  //     return await Promise.race([
  //       new Promise<TgMessage>((resolve) =>
  //         this.messageListenersOnce.push(resolve)
  //       ),
  //       new Promise<TgMessage>(
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
