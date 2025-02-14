import type { Theme } from 'vitepress'
import DefaultTheme from 'vitepress/theme'

export default {
  extends: DefaultTheme,
  enhanceApp({ router }) {
    router.onBeforeRouteChange = async (to) => {
      if (to == '/') {
        await router.go('/en/')
        return false
      }
    }
  }
} satisfies Theme
