import {Terminal} from "xterm";
import 'xterm/css/xterm.css'

window.onload = function (event) {
    const terminal = new Terminal()
    terminal.open(document.querySelector("#terminal"))
    const socket = new WebSocket(location.origin.replace("http", "ws") + "/ws/webssh")
    socket.onmessage = function (event) {
        terminal.write(event.data)
    }
    terminal.onData(function (data) {
        socket.send(data)
    })
}
