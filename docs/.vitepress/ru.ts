import {DefaultTheme} from 'vitepress'

export default {
  nav: [],

  sidebar: [
    {text: 'Быстрый старт', link: '/ru/quickstart'},
    {text: 'Фичи', link: '/ru/features'},
    {text: 'Опции конфигурации', link: '/ru/reference'},
    {text: 'Локализация', link: '/ru/localization'},
    {text: 'Совместимость', link: '/ru/compatibility'},
    {text: 'Решение проблем', link: '/ru/troubleshooting'},
  ],

  lastUpdated: {
    text: 'Обновлено',
    formatOptions: {
      dateStyle: 'long',
      forceLocale: true,
    },
  },
  outline: {
    label: 'Содержание',
    level: 'deep',
  },
  docFooter: {
    prev: 'Назад',
    next: 'Вперёд',
  },
  darkModeSwitchLabel: 'Тема',
  lightModeSwitchTitle: 'Cветлая тема',
  darkModeSwitchTitle: 'Темная тема',
  sidebarMenuLabel: 'Меню',
  returnToTopLabel: 'Наверх',
  langMenuLabel: 'Сменить язык',
  skipToContentLabel: 'Перейти к содержимому',
} satisfies DefaultTheme.Config
