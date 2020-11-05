import {Terminal} from "xterm";
import 'xterm/css/xterm.css'

window.onload = function (event) {
    const terminal = new Terminal()
    terminal.open(document.querySelector("#terminal"))
    const socket = new WebSocket(location.origin.replace("http", "ws") + "/ws/webssh")
    socket.onmessage = function (event) {
        terminal.write(event.data)
    }
    document.querySelector("#execute").addEventListener("click", function (event) {
        const command = document.querySelector("#command").value
        socket.send(JSON.stringify({
            type: "COMMAND",
            timestamp: Date.now(),
            payload: command
        }))
    })
    document.querySelector("#intercept").addEventListener("click", function (event) {
        socket.send(JSON.stringify({
            type: "INTERCEPT",
            timestamp: Date.now(),
            payload: null
        }))
    })
    document.querySelector("#clear").addEventListener("click", function (event) {
        terminal.write("\x1b[H\x1b[2J")
        socket.send(JSON.stringify({
            type: "NULL",
            timestamp: Date.now(),
            payload: null
        }))
    })
}
