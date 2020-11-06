import {Terminal} from "xterm";
import {FitAddon} from "xterm-addon-fit";
import 'xterm/css/xterm.css'

window.onload = function (event) {
    const terminal = new Terminal()
    terminal.open(document.querySelector("#terminal"))

    const socket = new WebSocket(location.origin.replace("http", "ws") + "/ws/webssh")

    const fitAddon = new FitAddon()
    terminal.loadAddon(fitAddon)
    window.onresize = function (event) {
        fitAddon.fit()
    }

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
    document.querySelector("#command").addEventListener("keydown", function (event) {
        if (event.keyCode === 13 /* Enter */) {
            const command = document.querySelector("#command").value
            socket.send(JSON.stringify({
                type: "COMMAND",
                timestamp: Date.now(),
                payload: command
            }))
        }
    })
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
