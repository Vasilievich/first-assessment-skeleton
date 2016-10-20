export class Message {
  static fromJSON (buffer) {
    return new Message(JSON.parse(buffer.toString()))
  }

  constructor ({ username, command, contents }) {
    this.username = username
    this.command = command
    this.contents = contents
  }

  toJSON () {
    return JSON.stringify({
      username: this.username,
      command: this.command,
      contents: this.contents
    })
  }
  getCommand () {
    return this.command
  }
  setCommand (temp) {
    this.command = temp
  }
  setContent (temp) {
    this.contents = temp
  }
  toString () {
    return this.contents
  }
}
