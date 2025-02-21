import {defineConfig} from 'vitepress'
import enConfig from './en.ts'
import ruConfig from './ru.ts'

// https://vitepress.dev/reference/site-config
export default defineConfig({
  title: 'tgbridge',
  description: 'Minecraft to Telegram Bridge',
  locales: {
    root: {
      label: 'English',
      lang: 'en',
      link: '/en/',
      themeConfig: enConfig,
    },
    ru: {
      label: 'Русский',
      lang: 'ru',
      link: '/ru/',
      themeConfig: ruConfig,
    },
  },
  head: [
    ['link', {rel: 'icon', type: 'image/png', sizes: '96x96', href: '/favicon-96x96.png'}],
    ['link', {rel: 'shortcut icon', href: '/favicon.ico'}],
    ['link', {rel: 'apple-touch-icon', sizes: '180x180', href: '/apple-touch-icon.png'}],
    ['script', {
      defer: '',
      src: 'https://zond.vanutp.dev/script.js',
      'data-website-id': 'd40a106a-971b-4d9e-8329-8d9060901041',
    }],
  ],
  cleanUrls: true,
  themeConfig: {
    logo: '/logo.png',
    search: {
      provider: 'local',
      options: {
        locales: {
          ru: {
            translations: {
              button: {
                buttonText: 'Поиск',
                buttonAriaLabel: 'Поиск',
              },
              modal: {
                displayDetails: 'Подробный список',
                resetButtonTitle: 'Сбросить',
                backButtonTitle: 'Закрыть',
                noResultsText: 'Нет результатов по запросу',
                footer: {
                  selectText: 'выбрать',
                  navigateText: 'перейти',
                  navigateUpKeyAriaLabel: 'стрелка вверх',
                  navigateDownKeyAriaLabel: 'стрелка вниз',
                  closeText: 'закрыть',
                },
              },
            },
          },
        },
      },
    },
    socialLinks: [
      {icon: 'github', link: 'https://github.com/vanutp/tgbridge'},
      {icon: 'modrinth', link: 'https://modrinth.com/mod/tgbridge'},
    ],
  },
  sitemap: {
    hostname: 'https://tgbridge.vanutp.dev',
  },
})
