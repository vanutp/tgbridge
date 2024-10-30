class PortAllocator {
  private nextPort: number

  constructor(start: number) {
    this.nextPort = start
  }

  next() {
    return this.nextPort++
  }
}

export const portAllocator = new PortAllocator(11874)
