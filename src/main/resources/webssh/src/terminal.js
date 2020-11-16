import 'xterm/css/xterm.css'
import WsTerminal from "./WsTerminal";

window.onload = function (event) {
    new WsTerminal(document.querySelector("#terminal"), "terminal")
}
