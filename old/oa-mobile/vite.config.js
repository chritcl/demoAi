import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'
import Components from 'unplugin-vue-components/vite'
import { VantResolver } from 'unplugin-vue-components/resolvers'
import { fileURLToPath, URL } from 'node:url'

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd())
  return {
    plugins: [vue(), Components({ resolvers: [VantResolver()] })],
    resolve: { alias: { '@': fileURLToPath(new URL('./src', import.meta.url)) } },
    server: {
      host: '0.0.0.0',
      port: 10003,
      open: false,
      proxy: {
        '/dev-api': {
          target: env.VITE_API_BASE || 'http://localhost:10001',
          changeOrigin: true,
          rewrite: (p) => p.replace(/^\/dev-api/, '')
        }
      }
    },
    build: { outDir: 'dist-mobile', chunkSizeWarningLimit: 2000 }
  }
})
