<template>
  <div class="admin-layout">
    <el-container>
      <el-aside width="200px" class="sidebar">
        <div class="logo">
          <i class="el-icon-s-tools"></i>
          <span>管理后台</span>
        </div>
        <el-menu
          :default-active="activeMenu"
          class="sidebar-menu"
          router
          background-color="#304156"
          text-color="#bfcbd9"
          active-text-color="#409EFF"
        >
          <el-menu-item index="/admin/dashboard">
            <i class="el-icon-s-home"></i>
            <span>仪表盘</span>
          </el-menu-item>
          <el-menu-item index="/admin/users">
            <i class="el-icon-user"></i>
            <span>用户管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/news">
            <i class="el-icon-document"></i>
            <span>资讯管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/resources">
            <i class="el-icon-folder"></i>
            <span>资源管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/courses">
            <i class="el-icon-video-camera"></i>
            <span>课程管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/course-categories">
            <i class="el-icon-menu"></i>
            <span>课程分类</span>
          </el-menu-item>
          <el-menu-item index="/admin/orders">
            <i class="el-icon-tickets"></i>
            <span>订单管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/refunds">
            <i class="el-icon-refresh-left"></i>
            <span>退款审核</span>
          </el-menu-item>
          <el-menu-item index="/admin/comments">
            <i class="el-icon-chat-line-round"></i>
            <span>评论管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/reports">
            <i class="el-icon-warning-outline"></i>
            <span>举报管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/posts">
            <i class="el-icon-chat-dot-round"></i>
            <span>帖子管理</span>
          </el-menu-item>
        </el-menu>
      </el-aside>
      <el-container>
        <el-header class="header">
          <div class="header-left">
            <el-breadcrumb separator="/" v-if="currentPageTitle">
              <el-breadcrumb-item :to="{ path: '/admin/dashboard' }">首页</el-breadcrumb-item>
              <el-breadcrumb-item>{{ currentPageTitle }}</el-breadcrumb-item>
            </el-breadcrumb>
            <el-breadcrumb separator="/" v-else>
              <el-breadcrumb-item :to="{ path: '/admin/dashboard' }">首页</el-breadcrumb-item>
            </el-breadcrumb>
          </div>
          <div class="header-right">
            <el-dropdown @command="handleCommand">
              <span class="user-info">
                <el-avatar :size="32" :src="userInfo && userInfo.avatar"></el-avatar>
                <span class="username">{{ userInfo && userInfo.nickname }}</span>
                <i class="el-icon-arrow-down"></i>
              </span>
              <el-dropdown-menu slot="dropdown">
                <el-dropdown-item command="home">前台首页</el-dropdown-item>
                <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </el-dropdown>
          </div>
        </el-header>
        <el-main class="main">
          <router-view />
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script>
import { mapGetters, mapActions } from 'vuex'

export default {
  name: 'AdminLayout',
  computed: {
    ...mapGetters('user', ['userInfo']),
    activeMenu() {
      return this.$route.path
    },
    currentPageTitle() {
      const titles = {
        '/admin/dashboard': '仪表盘',
        '/admin/users': '用户管理',
        '/admin/news': '资讯管理',
        '/admin/resources': '资源管理',
        '/admin/courses': '课程管理',
        '/admin/course-categories': '课程分类',
        '/admin/orders': '订单管理',
        '/admin/refunds': '退款审核',
        '/admin/comments': '评论管理',
        '/admin/reports': '举报管理',
        '/admin/posts': '帖子管理'
      }
      return titles[this.$route.path] || ''
    }
  },
  methods: {
    ...mapActions('user', ['logout']),
    handleCommand(command) {
      switch (command) {
        case 'home':
          this.$router.push('/')
          break
        case 'logout':
          this.logout()
          this.$router.push('/login')
          break
      }
    }
  }
}
</script>

<style scoped lang="scss">
.admin-layout {
  height: 100vh;
  display: flex;
  flex-direction: column;
  
  .el-container {
    display: flex;
    flex: 1;
    overflow: hidden;
  }
  
  .sidebar {
    background: #304156;
    
    .logo {
      height: 60px;
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 10px;
      color: #fff;
      font-size: 18px;
      font-weight: bold;
      border-bottom: 1px solid #3a4a5b;
      
      i {
        font-size: 24px;
      }
    }
    
    .sidebar-menu {
      border: none;
    }
  }
  
  .el-container:nth-child(2) {
    display: flex;
    flex-direction: column;
    flex: 1;
  }
  
  .header {
    background: #fff;
    display: flex;
    justify-content: space-between;
    align-items: center;
    box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
    height: 60px;
    
    .header-left {
      .el-breadcrumb {
        font-size: 14px;
      }
    }
    
    .header-right {
      .user-info {
        display: flex;
        align-items: center;
        gap: 10px;
        cursor: pointer;
        
        .username {
          font-size: 14px;
          color: #606266;
        }
      }
    }
  }
  
  .main {
    background: #f0f2f5;
    padding: 20px;
    flex: 1;
    overflow-y: auto;
  }
}
</style>
