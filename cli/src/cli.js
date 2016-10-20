import vorpal from 'vorpal'
import { words } from 'lodash'
import { connect } from 'net'
import { Message } from './Message'

export const cli = vorpal()

let username
let server
let temp = '1'

cli
  .delimiter(cli.chalk['yellow']('ftd~$'))
cli
  .mode('connect <username>')
  .delimiter(cli.chalk['green']('connected>'))
  .init(function (args, callback) {
    username = args.username
    server = connect({ host: 'localhost', port: 8080 }, () => {
      server.write(new Message({ username, command: 'connect' }).toJSON() + '\n')
      callback()
    })
    this.log('command required')
    let chalk = require('chalk')

    server.on('data', (buffer) => {
      let time = new Date()
      let s = time.getHours() + ':' + time.getMinutes() + ':' + time.getSeconds()
      let mess = Message.fromJSON(buffer)
      if (mess.getCommand().charAt(0) === '@') {
        this.log(s + ' ' + chalk.magenta(mess.toString()))
      }
      switch (mess.getCommand()) {
        case 'echo':
          this.log(s + ' ' + chalk.gray(mess.toString()))
          break
        case 'broadcast':
          this.log(s + ' ' + chalk.cyan(mess.toString()))
          break
        case 'users':
          this.log(s + '\n' + chalk.yellow(mess.toString()))
          break
        case 'connect':
          this.log(s + ' ' + chalk.green(mess.toString()))
          break
        case 'disconnect':
          this.log(s + ' ' + chalk.red(mess.toString()))
          break
      }
    })

    server.on('end', () => {
      cli.exec('exit')
    })
  })
  .action(function (input, callback) {
    const [ command, ...rest ] = words(input, /[^, ]+/g)
    const contents = rest.join(' ')
    if (command === 'disconnect') {
      server.end(new Message({ username, command }).toJSON() + '\n')
    } else if (command === 'echo') {
      temp = command
      server.write(new Message({ username, command, contents }).toJSON() + '\n')
    } else if (command === 'broadcast') {
      temp = command
      server.write(new Message({ username, command, contents }).toJSON() + '\n')
    } else if (command.charAt(0) === '@') {
      temp = command
      server.write(new Message({ username, command, contents }).toJSON() + '\n')
    } else if (command === 'users') {
      server.write(new Message({ username, command }).toJSON() + '\n')
    } else if (temp === '1') {
      temp === '1'
      console.log('not a command')
    } else {
      let x = new Message({ username, command, contents })
      x.setContent(command + ' ' + contents)
      x.setCommand(temp)
      server.write(x.toJSON() + '\n')
    }
    callback()
  })
