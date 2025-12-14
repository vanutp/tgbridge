import {DefaultTheme} from 'vitepress'

export default {
  nav: [],

  sidebar: [
    {text: 'Quick start', link: '/en/quickstart'},
    {text: 'Features', link: '/en/features'},
    {text: 'Integrations', link: '/en/integrations'},
    {text: 'Commands', link: '/en/commands'},
    {text: 'Config reference', link: '/en/reference'},
    {text: 'Localization', link: '/en/localization'},
    {text: 'Troubleshooting', link: '/en/troubleshooting'},
    {text: 'API', link: '/en/api'},
  ],

  lastUpdated: {
    formatOptions: {
      dateStyle: 'long',
      forceLocale: true,
    },
  },
  outline: 'deep',
} satisfies DefaultTheme.Config
