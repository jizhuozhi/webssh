import {Terminal} from "xterm";
import {FitAddon} from "xterm-addon-fit";
import 'xterm/css/xterm.css'

window.onload = function (event) {
    const terminal = new Terminal()

    const fitAddon = new FitAddon()
    terminal.loadAddon(fitAddon)
    window.onresize = function (event) {
        fitAddon.fit()
    }

    terminal.open(document.querySelector("#terminal"))
    terminal.write("Connecting...")
    const socket = new WebSocket(location.origin.replace("http", "ws") + "/ws/webssh")
    socket.onopen = function (event) {
        terminal.write("\x1b[H\x1b[2J")
    }
    socket.onmessage = function (event) {
        terminal.write(event.data)
    }
    socket.onclose = function (event) {
        terminal.write("\r\n")
        terminal.write("Connection closed!")
    }
    socket.onerror = function (event) {
        terminal.write("\r\n")
        terminal.write("Connection error observed!")
        console.error("Connection error observed!", event)
    }
    terminal.onResize(function (event) {
        socket.send(JSON.stringify({
            type: "RESIZE",
            timestamp: Date.now(),
            payload: {
                cols: event.cols,
                rows: event.rows
            }
        }))
    })
    terminal.onData(function (data) {
        socket.send(JSON.stringify({type: "DATA", timestamp: Date.now(), payload: data}))
    })
}
