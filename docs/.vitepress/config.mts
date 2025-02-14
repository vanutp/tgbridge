import {defineConfig} from 'vitepress'
import enConfig from './en.ts'

// https://vitepress.dev/reference/site-config
export default defineConfig({
  title: 'tgbridge',
  description: 'Minecraft to Telegram Bridge',
  locales: {
    root: {
      label: 'English',
      lang: 'en',
      link: '/en/',
    },
    ru: {
      label: 'Русский',
      lang: 'ru',
      link: '/ru/',
    },
  },
  head: [
    ['script', {
      defer: '',
      src: 'https://zond.vanutp.dev/script.js',
      'data-website-id': 'd40a106a-971b-4d9e-8329-8d9060901041',
    }],
  ],
  cleanUrls: true,
  themeConfig: {
    ...enConfig,
    logo: '/logo.png',
    outline: 'deep',
    search: {
      provider: 'local',
    },
    lastUpdated: {
      formatOptions: {
        dateStyle: 'long',
        forceLocale: true,
      },
    },
    socialLinks: [
      {icon: 'github', link: 'https://github.com/vanutp/tgbridge'},
    ],
  },
  sitemap: {
    hostname: 'https://tgbridge.vanutp.dev',
  },
})
