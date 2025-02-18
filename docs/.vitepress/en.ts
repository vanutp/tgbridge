import {DefaultTheme} from 'vitepress'

export default {
  nav: [],

  sidebar: [
    {text: 'Quick start', link: '/en/quickstart'},
    {text: 'Features', link: '/en/features'},
    {text: 'Config reference', link: '/en/reference'},
    {text: 'Localization', link: '/en/localization'},
    {text: 'Compatibility', link: '/en/compatibility'},
    {text: 'Troubleshooting', link: '/en/troubleshooting'},
  ],

  lastUpdated: {
    formatOptions: {
      dateStyle: 'long',
      forceLocale: true,
    },
  },
  outline: 'deep',
} satisfies DefaultTheme.Config
