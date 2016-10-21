import vorpal from 'vorpal'
import { words } from 'lodash'
import { connect } from 'net'
import { Message } from './Message'

export const cli = vorpal()
let chalk = require('chalk')
let username
let server
let host
let port
// holds the previous command
let prevCommand = '1'
// variable to know if there was a preivous command
let indiCommand = '0'
// method to get the timestamp in hh:mm:ss
function getTime () {
  let tempsec = new Date().getSeconds()
  let tempmin = new Date().getMinutes()
  let temphour = new Date().getHours()
  tempsec = tempsec < 10 ? '0' + tempsec : tempsec
  tempmin = tempmin < 10 ? '0' + tempmin : tempmin
  temphour = temphour < 10 ? '0' + temphour : temphour
  return temphour + ':' + tempmin + ':' + tempsec
}
cli
  .delimiter(cli.chalk['yellow']('ftd~$'))
cli
  .mode('connect <username> <host> <port>')
  .delimiter(cli.chalk['green']('connected>'))
  .init(function (args, callback) {
    username = args.username
    host = args.host
    port = args.port
    // host: 'localhost', port: 8080
    server = connect({ host: host, port: port }, () => {
      let connectMessage = `${getTime()}: <${username}> has connected`
      server.write(new Message({ username, command: 'connect', contents: connectMessage }).toJSON() + '\n')
      callback()
    })
    server.on('data', (buffer) => {
      // created mess to directly access the command
      let mess = Message.fromJSON(buffer)
      if (mess.getCommand().charAt(0) === '@') {
        this.log(chalk.magenta(mess.toString()))
      }
      switch (mess.getCommand()) {
        case 'echo':
          this.log(chalk.gray(mess.toString()))
          break
        case 'broadcast':
          this.log(chalk.cyan(mess.toString()))
          break
        case 'users':
          this.log(chalk.yellow(mess.toString()))
          break
        case 'connect':
          this.log(chalk.green(mess.toString()))
          break
        case 'disconnect':
          this.log(chalk.red(mess.toString()))
          break
      }
    })
    this.log('available commands: echo, broadcast, @username, users, disconnect')

    server.on('end', () => {
      cli.exec('exit')
    })
  })
  .action(function (input, callback) {
    let [ command, ...rest ] = words(input, /[^, ]+/g)
    let contents = rest.join(' ')

    if (command === 'disconnect') {
      contents = `${getTime()}: <${username}> has disconnected`
      server.end(new Message({ username, command, contents }).toJSON())
    } else if (command === 'echo') {
      prevCommand = command
      contents = `${getTime()} <${username}> (echo): ${contents}`
      server.write(new Message({ username, command, contents }).toJSON() + '\n')
    } else if (command === 'broadcast') {
      prevCommand = command
      contents = `${getTime()} <${username}> (all): ${contents}`
      server.write(new Message({ username, command, contents }).toJSON() + '\n')
    } else if (command.charAt(0) === '@') {
      prevCommand = command
      contents = `${getTime()} <${username}> (whisper): ${contents}`
      server.write(new Message({ username, command, contents }).toJSON() + '\n')
    } else if (command === 'users') {
      prevCommand = command
      contents = `${getTime()}: currently connected users:`
      server.write(new Message({ username, command, contents }).toJSON() + '\n')
    } else if (indiCommand === '0') {
      this.log(`${command} is not a command`)
      this.log('available commands: echo, broadcast, @username, users, disconnect')
    } else {
      // The first complete word that is typed will be considered as the command,
      // so in order to get the complete intended message, we have to concatenate
      // command and contents together.
      contents = `${command} ${contents}`
      command = prevCommand
      // Created a function that does the funcitonalities from lines 75-93
      // to not have such a long paragraph of code.
      // This implementation is for the previous command. After the command is given,
      // indicCommand is switched to 1 to not run into the else if statement on
      // line 94. This way, typing anything will be set as the contents and the
      // previous command will sent into the function to produce the message.
      // If a new command is requested, this will be solved from lines 75-93,
      sendPrevCommand(command, contents)
    }
    indiCommand = '1'

    callback()
  })

function sendPrevCommand (command, contents) {
  if (command === 'disconnect') {
    contents = `${getTime()}: <${username}> has disconnected`
    server.end(new Message({ username, command }).toJSON())
  } else if (command === 'echo') {
    prevCommand = command
    contents = `${getTime()} <${username}> (echo): ${contents}`
    server.write(new Message({ username, command, contents }).toJSON() + '\n')
  } else if (command === 'broadcast') {
    prevCommand = command
    contents = `${getTime()} <${username}> (all): ${contents}`
    server.write(new Message({ username, command, contents }).toJSON() + '\n')
  } else if (command.charAt(0) === '@') {
    prevCommand = command
    contents = `${getTime()} <${username}> (whisper): ${contents}`
    server.write(new Message({ username, command, contents }).toJSON() + '\n')
  } else if (command === 'users') {
    contents = `${getTime()}: currently connected users:`
    server.write(new Message({ username, command, contents }).toJSON() + '\n')
  }
}
