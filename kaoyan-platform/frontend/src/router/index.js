import Vue from 'vue'
import VueRouter from 'vue-router'
import store from '@/store'

Vue.use(VueRouter)

const routes = [
  {
    path: '/',
    component: () => import('@/views/Layout.vue'),
    children: [
      {
        path: '',
        name: 'Home',
        component: () => import('@/views/home/Home.vue')
      },
      {
        path: 'news',
        name: 'News',
        component: () => import('@/views/news/News.vue')
      },
      {
        path: 'news/:id',
        name: 'NewsDetail',
        component: () => import('@/views/news/NewsDetail.vue')
      },
      {
        path: 'resource',
        name: 'Resource',
        component: () => import('@/views/resource/Resource.vue')
      },
      {
        path: 'resource/:id',
        name: 'ResourceDetail',
        component: () => import('@/views/resource/ResourceDetail.vue')
      },
      {
        path: 'course',
        name: 'Course',
        component: () => import('@/views/course/Course.vue')
      },
      {
        path: 'course/:id',
        name: 'CourseDetail',
        component: () => import('@/views/course/CourseDetail.vue')
      },
      {
        path: 'course/:id/learning',
        name: 'CourseLearning',
        component: () => import('@/views/course/CourseLearning.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: 'teacher/:id',
        name: 'TeacherDetail',
        component: () => import('@/views/course/TeacherDetail.vue')
      },
      {
        path: 'community',
        name: 'Community',
        component: () => import('@/views/community/Community.vue')
      },
      {
        path: 'community/post/:id',
        name: 'PostDetail',
        component: () => import('@/views/community/PostDetail.vue')
      },
      {
        path: 'community/message',
        name: 'Message',
        component: () => import('@/views/community/Message.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: 'study',
        name: 'Study',
        component: () => import('@/views/study/Study.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: 'my-courses',
        name: 'MyCourses',
        component: () => import('@/views/course/MyCourses.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: 'user',
        name: 'User',
        component: () => import('@/views/user/User.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: 'user/:id',
        name: 'UserProfile',
        component: () => import('@/views/user/UserProfile.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: 'schools',
        name: 'Schools',
        component: () => import('@/views/school/SchoolList.vue')
      },
      {
        path: 'schools/:id',
        name: 'SchoolDetail',
        component: () => import('@/views/school/SchoolDetail.vue')
      },
      {
        path: 'national-score',
        name: 'NationalScore',
        component: () => import('@/views/school/NationalScore.vue')
      },
      {
        path: 'adjustment',
        name: 'Adjustment',
        component: () => import('@/views/adjustment/Adjustment.vue')
      },
      {
        path: 'live',
        name: 'Live',
        component: () => import('@/views/live/LiveList.vue')
      },
      {
        path: 'live/:id',
        name: 'LiveRoom',
        component: () => import('@/views/live/LiveRoom.vue'),
        meta: { requiresAuth: true }
      },
    ]
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/user/Login.vue')
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/user/Register.vue')
  },
  {
    path: '/admin',
    component: () => import('@/views/admin/AdminLayout.vue'),
    meta: { requiresAuth: true, requiresAdmin: true },
    children: [
      {
        path: '',
        redirect: '/admin/dashboard'
      },
      {
        path: 'dashboard',
        name: 'AdminDashboard',
        component: () => import('@/views/admin/Dashboard.vue')
      },
      {
        path: 'users',
        name: 'AdminUsers',
        component: () => import('@/views/admin/Users.vue')
      },
      {
        path: 'news',
        name: 'AdminNews',
        component: () => import('@/views/admin/News.vue')
      },
      {
        path: 'resources',
        name: 'AdminResources',
        component: () => import('@/views/admin/Resources.vue')
      },
      {
        path: 'courses',
        name: 'AdminCourses',
        component: () => import('@/views/admin/Courses.vue')
      },
      {
        path: 'course-categories',
        name: 'AdminCourseCategories',
        component: () => import('@/views/admin/CourseCategories.vue')
      },
      {
        path: 'orders',
        name: 'AdminOrders',
        component: () => import('@/views/admin/Orders.vue')
      },
      {
        path: 'refunds',
        name: 'AdminRefunds',
        component: () => import('@/views/admin/Refunds.vue')
      },
      {
        path: 'comments',
        name: 'AdminComments',
        component: () => import('@/views/admin/Comments.vue')
      },
      {
        path: 'reports',
        name: 'AdminReports',
        component: () => import('@/views/admin/Reports.vue')
      },
      {
        path: 'posts',
        name: 'AdminPosts',
        component: () => import('@/views/admin/Posts.vue')
      }
    ]
  }
]

const router = new VueRouter({
  mode: 'history',
  base: process.env.BASE_URL,
  routes
})

router.beforeEach((to, from, next) => {
  const token = store.state.user.token
  const userInfo = store.state.user.userInfo
  
  console.log('Router beforeEach:', { to, token, userInfo })
  
  if (to.meta.requiresAuth && !token) {
    next('/login')
  } else if (to.meta.requiresAdmin && (!userInfo || userInfo.role !== 'ADMIN')) {
    next('/')
  } else {
    next()
  }
})

export default router
