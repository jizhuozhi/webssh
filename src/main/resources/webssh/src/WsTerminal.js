import {Terminal} from 'xterm';
import {FitAddon} from "xterm-addon-fit/lib/xterm-addon-fit";
import {WebLinksAddon} from "xterm-addon-web-links/lib/xterm-addon-web-links";

export default class WsTerminal {
    constructor(element, type, options = {}) {
        this.options = {
            cursorBlink: true,
            screenKeys: true,
            ...options,
        };

        this.container = element;
        this.type = type

        this.setSocketUrl()
        this.createTerminal()

    }

    setSocketUrl() {
        const {protocol, hostname, port} = window.location;
        const wsProtocol = protocol === 'https:' ? 'wss://' : 'ws://';
        const path = this.container.dataset.endpoint;

        this.socketUrl = `${wsProtocol}${hostname}:${port}${path}`;
    }

    createTerminal() {
        this.terminal = new Terminal(this.options);

        this.fitAddon = new FitAddon()
        this.webLinksAddon = new WebLinksAddon()
        this.terminal.loadAddon(this.fitAddon)
        this.terminal.loadAddon(this.webLinksAddon)

        this.socket = new WebSocket(this.socketUrl);
        this.socket.binaryType = 'arraybuffer';

        this.terminal.open(this.container);
        this.fitAddon.fit();
        this.terminal.focus();

        window.onresize = function (event) {
            this.fitAddon.fit()
        }

        this.socket.onopen = () => {
            this.runTerminal();
        };
        this.socket.onclose = () => {
            this.handleSocketClose()
        }
        this.socket.onerror = () => {
            this.handleSocketFailure();
        };
    }

    runTerminal() {
        this.clearScreen()

        // Only terminal type is interactive
        if (this.type === 'terminal') {
            this.terminal.on('data', data => {
                this.socket.send(JSON.stringify({type: "DATA", timestamp: Date.now(), payload: data}));
            })
        }

        // Resize remote channel size
        this.terminal.onResize(function (event) {
            this.socket.send(JSON.stringify({
                type: "RESIZE",
                timestamp: Date.now(),
                payload: {
                    cols: event.cols,
                    rows: event.rows
                }
            }))
        })

        this.socket.addEventListener('message', event => {
            this.terminal.write(event.data);
        });

        this.fitAddon.fit();
    }

    handleSocketClose() {
        this.terminal.write('\r\n')
        this.terminal.write("Connection closed")
    }

    handleSocketFailure() {
        this.terminal.write('\r\n');
        this.terminal.write('Connection failure');
    }

    /* Commands */

    clearScreen() {
        this.terminal.write("\x1b[H\x1b[2J")
        this.socket.send(JSON.stringify({
            type: "NULL",
            timestamp: Date.now(),
            payload: null
        }))
    }

    intercept() {
        this.socket.send(JSON.stringify({
            type: "INTERCEPT",
            timestamp: Date.now(),
            payload: null
        }))
    }

    sendCommand(command) {
        this.socket.send(JSON.stringify({
            type: "COMMAND",
            timestamp: Date.now(),
            payload: command
        }))
    }
}
