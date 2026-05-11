<template>
  <div class="layout">
    <el-container>
      <el-header class="header">
        <div class="logo" @click="$router.push('/')">
          <i class="el-icon-reading"></i>
          <span>考研学习平台</span>
        </div>
        <el-menu
          :default-active="activeMenu"
          class="nav-menu"
          mode="horizontal"
          router
        >
          <el-menu-item index="/">首页</el-menu-item>

          <el-submenu index="info-group">
            <template slot="title">信息查询</template>
            <el-menu-item index="/schools">院校库</el-menu-item>
            <el-menu-item index="/national-score">国家线</el-menu-item>
            <el-menu-item index="/adjustment">调剂信息</el-menu-item>
          </el-submenu>

          <el-menu-item index="/news">考研资讯</el-menu-item>

          <el-submenu index="study-group">
            <template slot="title">备考学习</template>
            <el-menu-item index="/study">学习中心</el-menu-item>
            <el-menu-item index="/course">在线课程</el-menu-item>
            <el-menu-item index="/resource">资料下载</el-menu-item>
          </el-submenu>

          <el-submenu index="community-group">
            <template slot="title">研友社区</template>
            <el-menu-item index="/community">圈子广场</el-menu-item>
          </el-submenu>

          <el-menu-item v-if="isLoggedIn && userInfo && userInfo.role === 'ADMIN'" index="/admin/dashboard">后台管理</el-menu-item>
        </el-menu>
        <div class="user-area">
          <template v-if="isLoggedIn">
            <el-badge :value="unreadNotificationCount" :hidden="unreadNotificationCount === 0" class="message-badge">
              <el-button type="text" @click="goToNotifications" class="message-btn" title="通知">
                <i class="el-icon-bell"></i>
              </el-button>
            </el-badge>
            <el-badge :value="unreadMessageCount" :hidden="unreadMessageCount === 0" class="message-badge">
              <el-button type="text" @click="goToMessages" class="message-btn" title="私信">
                <i class="el-icon-message"></i>
              </el-button>
            </el-badge>
            <el-badge v-if="!isAdmin" :value="urgentTaskCount" :hidden="urgentTaskCount === 0" class="message-badge">
              <el-button type="text" @click="goToStudyCenter" class="message-btn" title="任务提醒">
                <i class="el-icon-alarm-clock"></i>
              </el-button>
            </el-badge>
            <el-dropdown @command="handleCommand">
              <span class="user-info">
                <el-avatar :size="32" :src="userInfo && userInfo.avatar"></el-avatar>
                <span class="username">{{ userInfo && userInfo.nickname }}</span>
                <i class="el-icon-arrow-down"></i>
              </span>
              <el-dropdown-menu slot="dropdown">
                <el-dropdown-item command="study">学习中心</el-dropdown-item>
                <el-dropdown-item command="my-courses">我的课程</el-dropdown-item>
                <el-dropdown-item command="user">个人中心</el-dropdown-item>
                <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </el-dropdown>
          </template>
          <template v-else>
            <el-button type="primary" size="small" @click="$router.push('/login')">登录</el-button>
            <el-button size="small" @click="$router.push('/register')">注册</el-button>
          </template>
        </div>
      </el-header>
      <el-main class="main">
        <router-view />
      </el-main>
      <el-footer class="footer">
        <p>2026 考研学习社区平台 - 毕业设计项目</p>
      </el-footer>
    </el-container>
  </div>
</template>

<script>
import { mapGetters, mapActions } from 'vuex'
import request from '@/utils/request'

export default {
  name: 'Layout',
  data() {
    return {
      unreadMessageCount: 0,
      unreadNotificationCount: 0,
      urgentTaskCount: 0,
      messagePollTimer: null,
      notificationPollTimer: null,
      reminderPollTimer: null
    }
  },
  computed: {
    ...mapGetters('user', ['isLoggedIn', 'userInfo']),
    isAdmin() { return this.userInfo && this.userInfo.role === 'ADMIN' },
    activeMenu() {
      const path = this.$route.path
      // 匹配顶级菜单
      if (path === '/') return '/'
      if (path.startsWith('/schools') || path.startsWith('/national-score') || path.startsWith('/adjustment')) return 'info-group'
      if (path.startsWith('/news')) return '/news'
      if (path.startsWith('/study') || path.startsWith('/course') || path.startsWith('/resource')) return 'study-group'
      if (path.startsWith('/community')) return 'community-group'
      if (path.startsWith('/admin')) return '/admin/dashboard'
      return ''
    }
  },
  watch: {
    isLoggedIn(val) {
      if (val) {
        this.startMessagePolling()
        this.startNotificationPolling()
        this.startReminderPolling()
      } else {
        this.stopMessagePolling()
        this.stopNotificationPolling()
        this.stopReminderPolling()
        this.unreadMessageCount = 0
        this.unreadNotificationCount = 0
        this.urgentTaskCount = 0
      }
    }
  },
  mounted() {
    if (this.isLoggedIn) {
      this.startMessagePolling()
      this.startNotificationPolling()
      this.startReminderPolling()
    }
    window.addEventListener('refresh-unread-notifications', this.fetchUnreadNotificationCount)
    window.addEventListener('refresh-unread-messages', this.fetchUnreadCount)
  },
  beforeDestroy() {
    this.stopMessagePolling()
    this.stopNotificationPolling()
    this.stopReminderPolling()
    window.removeEventListener('refresh-unread-notifications', this.fetchUnreadNotificationCount)
    window.removeEventListener('refresh-unread-messages', this.fetchUnreadCount)
  },
  methods: {
    ...mapActions('user', ['logout']),
    handleCommand(command) {
      switch (command) {
        case 'study': this.$router.push('/study'); break
        case 'my-courses': this.$router.push('/my-courses'); break
        case 'user': this.$router.push('/user'); break
        case 'logout':
          this.logout()
          this.$router.push('/login')
          break
      }
    },
    goToMessages() {
      this.$router.push('/community/message')
    },
    goToNotifications() {
      if (this.urgentTaskCount > 0) {
        this.$router.push('/news?showNotifications=1&notifTab=tasks')
      } else {
        this.$router.push('/news?showNotifications=1')
      }
    },
    goToStudyCenter() {
      this.$router.push('/study')
    },
    async fetchUrgentTaskCount() {
      try {
        const res = await request.get('/study/urgent-tasks', { hideError: true })
        if (res && Array.isArray(res)) {
          this.urgentTaskCount = res.length
          // 如果有紧急任务，弹出浏览器通知
          if (res.length > 0) {
            this.$notify({
              title: '任务提醒',
              message: `您有 ${res.length} 个即将到期或已过期的任务`,
              type: 'warning',
              duration: 6000,
              onClick: () => { this.$router.push('/study') }
            })
          }
        }
      } catch (e) { /* silent */ }
    },
    startReminderPolling() {
      this.stopReminderPolling()
      this.fetchUrgentTaskCount()
      this.reminderPollTimer = setInterval(() => { this.fetchUrgentTaskCount() }, 120000) // 每2分钟
    },
    stopReminderPolling() {
      if (this.reminderPollTimer) { clearInterval(this.reminderPollTimer); this.reminderPollTimer = null }
    },
    async fetchUnreadNotificationCount() {
      try {
        const res = await request.get('/notification/unread-count', { hideError: true })
        this.unreadNotificationCount = typeof res === 'number' ? res : 0
      } catch (error) { /* silent */ }
    },
    startNotificationPolling() {
      this.stopNotificationPolling()
      this.fetchUnreadNotificationCount()
      this.notificationPollTimer = setInterval(() => { this.fetchUnreadNotificationCount() }, 60000)
    },
    stopNotificationPolling() {
      if (this.notificationPollTimer) { clearInterval(this.notificationPollTimer); this.notificationPollTimer = null }
    },
    async fetchUnreadCount() {
      try {
        const res = await request.get('/community/message/unread', { hideError: true })
        this.unreadMessageCount = typeof res === 'number' ? res : 0
      } catch (error) { /* silent */ }
    },
    startMessagePolling() {
      this.stopMessagePolling()
      this.fetchUnreadCount()
      this.messagePollTimer = setInterval(() => { this.fetchUnreadCount() }, 10000)
    },
    stopMessagePolling() {
      if (this.messagePollTimer) { clearInterval(this.messagePollTimer); this.messagePollTimer = null }
    }
  }
}
</script>

<style scoped lang="scss">
.layout {
  min-height: 100vh;

  .header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    background: #fff;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
    padding: 0 20px;

    .logo {
      display: flex;
      align-items: center;
      gap: 10px;
      cursor: pointer;
      i { font-size: 28px; color: #409EFF; }
      span { font-size: 18px; font-weight: bold; color: #303133; }
    }

    .nav-menu {
      flex: 1;
      justify-content: center;
      border-bottom: none;
    }

    .user-area {
      display: flex;
      align-items: center;
      gap: 10px;
      .message-badge {
        .message-btn { font-size: 22px; color: #606266; padding: 0; &:hover { color: #409EFF; } }
      }
      .user-info {
        display: flex;
        align-items: center;
        gap: 8px;
        cursor: pointer;
        .username { font-size: 14px; color: #303133; }
      }
    }
  }

  .main {
    min-height: calc(100vh - 120px);
    background: #f5f7fa;
  }

  .footer {
    text-align: center;
    background: #fff;
    color: #909399;
    font-size: 14px;
  }
}
</style>
