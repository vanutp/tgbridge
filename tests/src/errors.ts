export class ServerInstallError extends Error {
  constructor(message?: string) {
    super(message);
    this.name = 'ServerInstallError';
    Object.setPrototypeOf(this, new.target.prototype);
  }
}

export class BadRequestError extends Error {
  constructor(message?: string) {
    super(message);
    this.name = 'BadRequestError';
    Object.setPrototypeOf(this, new.target.prototype);
  }
}

export class ReplyNotFoundError extends BadRequestError {
  constructor() {
    super('message to be replied not found');
    this.name = 'ReplyNotFoundError';
    Object.setPrototypeOf(this, new.target.prototype);
  }
}

export class ChatNotFoundError extends BadRequestError {
  constructor() {
    super('chat not found');
    this.name = 'ReplyNotFoundError';
    Object.setPrototypeOf(this, new.target.prototype);
  }
}

export class MessageToEditNotFoundError extends BadRequestError {
  constructor() {
    super('message to edit not found');
    this.name = 'MessageToEditNotFoundError';
    Object.setPrototypeOf(this, new.target.prototype);
  }
}

export class AssertionError extends Error {
  constructor(message?: string) {
    super(message);
    this.name = 'AssertionError';
    Object.setPrototypeOf(this, new.target.prototype);
  }
}

export class TestFrameworkError extends Error {
  constructor(message?: string) {
    super(message);
    this.name = 'TestFrameworkError';
    Object.setPrototypeOf(this, new.target.prototype);
  }
}

export class TimeoutError extends Error {
  constructor(message?: string) {
    super(message);
    this.name = 'TimeoutError';
    Object.setPrototypeOf(this, new.target.prototype);
  }
}
