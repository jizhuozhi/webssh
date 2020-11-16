import 'xterm/css/xterm.css'
import WsTerminal from "./WsTerminal";

window.onload = function (event) {
    const wsTerminal = new WsTerminal(document.querySelector("#terminal"), "command")
    document.querySelector("#command").addEventListener("keydown", function (event) {
        if (event.keyCode === 13 /* Enter */) {
            const command = document.querySelector("#command").value
            wsTerminal.sendCommand(command)
        } else if (event.ctrlKey === true && event.keyCode === 67 /* Ctrl + C */) {
            wsTerminal.intercept()
        }
    })
    document.querySelector("#execute").addEventListener("click", function (event) {
        const command = document.querySelector("#command").value
        wsTerminal.sendCommand(command)
    })
    document.querySelector("#intercept").addEventListener("click", function (event) {
        wsTerminal.intercept()
    })
    document.querySelector("#clear").addEventListener("click", function (event) {
        wsTerminal.clearScreen()
    })
}
