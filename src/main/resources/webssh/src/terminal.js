import {Terminal} from "xterm";
import 'xterm/css/xterm.css'

window.onload = function (event) {
    const terminal = new Terminal()
    terminal.open(document.querySelector("#terminal"))
}
