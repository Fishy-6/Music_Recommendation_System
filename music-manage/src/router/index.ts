import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router'

const routes: Array<RouteRecordRaw> = [
  {
    path: '/Home',
    component: () => import('@/views/Home.vue'),
    meta: { title: '自述文件' },
    children: [
      {
        path: '/Info',
        component: () => import('@/views/InfoPage.vue'),
        meta: { title: 'Info',
        requiresAuth: true }
      },
      {
        path: '/song',
        component: () => import('@/views/SongPage.vue'),
        meta: { title: 'Song' }
      },
      {
        path: '/songs',
        component: () => import('@/views/SongsPage.vue'),
        meta: { title: 'Songs' }
      },
      {
        path: '/singer',
        component: () => import('@/views/SingerPage.vue'),
        meta: { title: 'Singer' }
      },
      {
        path: '/SongList',
        component: () => import('@/views/SongListPage.vue'),
        meta: { title: 'SongList' }
      },
      {
        path: '/ListSong',
        component: () => import('@/views/ListSongPage.vue'),
        meta: { title: 'ListSong' }
      },
      {
        path: '/Comment',
        component: () => import('@/views/CommentPage.vue'),
        meta: { title: 'Comment' }
      },
      {
        path: '/Consumer',
        component: () => import('@/views/ConsumerPage.vue'),
        meta: { title: 'Consumer' }
      },
      {
        path: '/Collect',
        component: () => import('@/views/CollectPage.vue'),
        meta: { title: 'Collect' }
      },
      {
        path: '/banner',
        component: () => import('@/views/BannerPage.vue'),
        meta: { title: 'Banner' }
      },
      {
        path: '/admin',
        component: () => import('@/views/AdminPage.vue'),
        meta: { title: 'Admin' }
      },
      {
        path: '/rank',
        component: () => import('@/views/RankPage.vue'),
        meta: { title: 'Rank' }
      },

    ]
  },
  {
    path: '/',
    component: () => import('@/views/Login.vue')
  }
]

const router = createRouter({
  history: createWebHistory(process.env.BASE_URL),
  routes
})

// 添加全局前置守卫
router.beforeEach((to, from, next) => {
  // 获取登录状态
  const isLoggedIn = localStorage.getItem('adminInfo') !== null || 
                    sessionStorage.getItem('adminInfo') !== null ||
                    // 或者检查是否有token
                    localStorage.getItem('token') !== null;
  
  // 如果访问的是登录页
  if (to.path === '/') {
    // 已登录则重定向到主页
    if (isLoggedIn) {
      next('/info');
    } else {
      // 未登录允许访问登录页
      next();
    }
  } else {
    // 访问其他页面
    if (isLoggedIn) {
      // 已登录允许访问
      next();
    } else {
      // 未登录重定向到登录页
      next('/');
    }
  }
});

export default router
