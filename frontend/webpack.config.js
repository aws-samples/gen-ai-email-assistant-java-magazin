/* eslint-disable no-undef */
const dotenv = require("dotenv");
const dotenvExpand = require("dotenv-expand");
const devCerts = require("office-addin-dev-certs");
const webpack = require("webpack");
const CopyWebpackPlugin = require("copy-webpack-plugin");
const HtmlWebpackPlugin = require("html-webpack-plugin");
const path = require("path");
const PLACEHOLDER_FRONTEND_URL = "https://localhost:3000";

async function getHttpsOptions() {
  const httpsOptions = await devCerts.getHttpsServerOptions();
  return { ca: httpsOptions.ca, key: httpsOptions.key, cert: httpsOptions.cert };
}

module.exports = async (env, options) => {
  console.log(`Using environment file: .env_${env.env || "dev"}`);

  const dotenvConfig = dotenv.config({
    path: env.env ? path.resolve(__dirname, `./.env_${env.env}`) : path.resolve(__dirname, "./.env_dev"),
  });
  dotenvExpand.expand(dotenvConfig);

  const config = {
    devtool: "source-map",
    entry: {
      polyfill: ["core-js/stable", "regenerator-runtime/runtime"],
      taskpane: ["./src/taskpane/taskpane.ts", "./src/taskpane/taskpane.html"],
      commands: "./src/commands/commands.ts",
    },
    output: {
      clean: true,
    },
    resolve: {
      extensions: [".ts", ".tsx", ".html", ".js"],
    },
    module: {
      rules: [
        {
          test: /\.ts$/,
          exclude: /node_modules/,
          use: {
            loader: "babel-loader",
            options: {
              presets: ["@babel/preset-typescript"],
            },
          },
        },
        {
          test: /\.tsx?$/,
          exclude: /node_modules/,
          use: "ts-loader",
        },
        {
          test: /\.html$/,
          exclude: /node_modules/,
          use: "html-loader",
        },
        {
          test: /\.(png|jpg|jpeg|gif|ico)$/,
          type: "asset/resource",
          generator: {
            filename: "assets/[name][ext][query]",
          },
        },
      ],
    },
    plugins: [
      new HtmlWebpackPlugin({
        filename: "taskpane.html",
        template: "./src/taskpane/taskpane.html",
        chunks: ["polyfill", "taskpane"],
      }),
      new CopyWebpackPlugin({
        patterns: [
          {
            from: "assets/*",
            to: "assets/[name][ext][query]",
          },
          {
            from: "manifest*.xml",
            to: "[name]" + "[ext]",
            transform(content) {
              return content.toString().replace(new RegExp(PLACEHOLDER_FRONTEND_URL, "g"), process.env.FRONTEND_URL);
            },
          },
        ],
      }),
      new HtmlWebpackPlugin({
        filename: "commands.html",
        template: "./src/commands/commands.html",
        chunks: ["polyfill", "commands"],
      }),
      new HtmlWebpackPlugin({
        filename: "auth.html",
        template: "./src/taskpane/auth.html",
        chunks: ["polyfill", "auth"],
      }),
      new webpack.DefinePlugin({
        INJECTED_API_URL: JSON.stringify(process.env.API_URL),
        INJECTED_FRONTEND_URL: JSON.stringify(process.env.FRONTEND_URL),
        INJECTED_COGNITO_ENDPOINT: JSON.stringify(process.env.COGNITO_ENDPOINT),
        INJECTED_COGNITO_CLIENT_ID: JSON.stringify(process.env.COGNITO_CLIENT_ID),
      }),
    ],
    devServer: {
      headers: {
        "Access-Control-Allow-Origin": "*",
      },
      server: {
        type: "https",
        options: env.WEBPACK_BUILD || options.https !== undefined ? options.https : await getHttpsOptions(),
      },
      port: process.env.npm_package_config_dev_server_port || 3000,
    },
  };

  return config;
};
