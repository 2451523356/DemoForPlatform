const { defineConfig } = require('@vue/cli-service')

module.exports = defineConfig({
  transpileDependencies: true,
  lintOnSave: false,
  devServer: {
    port: 3000,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '/avatars': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '/videos': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '/files': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  },
  chainWebpack: config => {
    config.plugin('copy').tap(args => {
      args[0].patterns[0].globOptions.ignore = [
        '**/.DS_Store',
        '**/index.html'
      ]
      return args
    })
  },
  configureWebpack: {
    resolve: {
      alias: {
        '@': require('path').resolve(__dirname, 'src')
      }
    }
  }
})
