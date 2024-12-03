'use strict';

const fs = require('fs');
const evalSourceMapMiddleware = require('react-dev-utils/evalSourceMapMiddleware');
const noopServiceWorkerMiddleware = require('react-dev-utils/noopServiceWorkerMiddleware');
const ignoredFiles = require('react-dev-utils/ignoredFiles');
const redirectServedPath = require('react-dev-utils/redirectServedPathMiddleware');
const paths = require('./paths');
const getHttpsConfig = require('./getHttpsConfig');

const host = process.env.HOST || '0.0.0.0';
const sockHost = process.env.WDS_SOCKET_HOST;
const sockPath = process.env.WDS_SOCKET_PATH; // default: '/ws'
const sockPort = process.env.WDS_SOCKET_PORT;

// Utility function to simulate delay
const delay = (ms) => new Promise((resolve) => setTimeout(resolve, ms));

module.exports = function (proxy, allowedHost) {
  const disableFirewall =
      !proxy || process.env.DANGEROUSLY_DISABLE_HOST_CHECK === 'true';

  return {
    // Update allowedHosts to 'all' or specify a list of allowed hosts
    allowedHosts: disableFirewall ? 'all' : ['localhost', '0.0.0.0', 'your-domain.com'],

    headers: {
      'Access-Control-Allow-Origin': '*',
      'Access-Control-Allow-Methods': '*',
      'Access-Control-Allow-Headers': '*',
    },

    compress: false,
    static: {
      directory: paths.appPublic,
      publicPath: [paths.publicUrlOrPath],
      watch: {
        ignored: ignoredFiles(paths.appSrc),
      },
    },
    client: {
      webSocketURL: {
        hostname: sockHost,
        pathname: sockPath,
        port: sockPort,
      },
      overlay: {
        errors: true,
        warnings: false,
      },
    },
    devMiddleware: {
      publicPath: paths.publicUrlOrPath.slice(0, -1),
    },
    https: getHttpsConfig(),
    host,
    historyApiFallback: {
      disableDotRule: true,
      index: paths.publicUrlOrPath,
    },
    proxy: {
      '/api': {
        target: 'http://localhost:8080',

        // Simulate delay for easier testing
        pathRewrite: async function (path, req) {
          await delay(1000); // Introducing a 1-second delay
          return path;
        },
        onProxyRes: (proxyRes, req, res) => {
          // Handle proxy response close events
          proxyRes.on('close', () => {
            if (!res.writableEnded) {
              res.end();
            }
          });

          res.on('close', () => {
            console.log("Request closed");
            proxyRes.destroy();
          });
        },
      },
    },

    onBeforeSetupMiddleware(devServer) {
      devServer.app.use(evalSourceMapMiddleware(devServer));

      if (fs.existsSync(paths.proxySetup)) {
        require(paths.proxySetup)(devServer.app);
      }
    },
    onAfterSetupMiddleware(devServer) {
      devServer.app.use(redirectServedPath(paths.publicUrlOrPath));
      devServer.app.use(noopServiceWorkerMiddleware(paths.publicUrlOrPath));
    },
  };
};
