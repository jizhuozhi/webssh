const path = require("path")
const {CleanWebpackPlugin} = require('clean-webpack-plugin');
const HtmlWebpackPlugin = require('html-webpack-plugin');

module.exports = {
    entry: {
        terminal: "./src/terminal.js"
    },
    output: {
        filename: "[name].[contenthash].js",
        path: path.resolve(__dirname, '../static'),
    },
    module: {
        rules: [
            {
                test: /\.css$/,
                use: [
                    {loader: 'style-loader'},
                    {loader: 'css-loader'}
                ]
            }
        ]
    },
    plugins: [
        new CleanWebpackPlugin(),
        new HtmlWebpackPlugin({
            title: "WebSSH",
            filename: "terminal.html",
            template: "./public/terminal.html"
        })
    ]
}
