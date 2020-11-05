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
    const socket = new WebSocket(location.origin.replace("http", "ws") + "/ws/webssh")
    socket.onmessage = function (event) {
        terminal.write(event.data)
    }
    terminal.onResize(function (event) {
        socket.send(`stty cols ${event.cols} rows ${event.rows}\r`)
    })
    terminal.onData(function (data) {
        socket.send(data)
    })
}
