<template>
  <div class="news">
    <el-card>
      <div class="news-header">
        <h1>考研资讯</h1>
        <div class="header-actions">
          <el-badge :value="unreadCount" :hidden="unreadCount === 0">
            <el-button size="small" @click="showNotifications" icon="el-icon-bell">通知</el-button>
          </el-badge>
          <el-button size="small" type="info" @click="showSubscriptionDialog">
            <i class="el-icon-star-off"></i> 订阅
          </el-button>
        </div>
      </div>

      <!-- 统一筛选栏 -->
      <div class="news-filter">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索资讯关键词"
          clearable
          prefix-icon="el-icon-search"
          style="width: 200px; margin-right: 10px;"
          @keyup.enter.native="search"
        />
        <el-select v-model="filterCategory" placeholder="资讯分类" clearable @change="search" style="width:140px;margin-right:10px">
          <el-option label="全部分类" value=""></el-option>
          <el-option
            v-for="group in categoryGroups"
            :key="group.name"
            :label="group.name"
            :value="group.name"
          />
        </el-select>
        <el-select v-model="selectedStage" placeholder="备考阶段" clearable @change="search" style="width:130px;margin-right:10px">
          <el-option label="全部阶段" value=""></el-option>
          <el-option label="择校评估" value="择校评估"></el-option>
          <el-option label="基础复习" value="基础复习"></el-option>
          <el-option label="强化提升" value="强化提升"></el-option>
          <el-option label="报名报考" value="报名报考"></el-option>
          <el-option label="冲刺模考" value="冲刺模考"></el-option>
          <el-option label="复试准备" value="复试准备"></el-option>
          <el-option label="调剂录取" value="调剂录取"></el-option>
        </el-select>
        <el-button type="primary" @click="search" icon="el-icon-search">搜索</el-button>
        <el-button @click="resetFilters">重置</el-button>
        <span class="filter-result" v-if="total > 0">共 {{ total }} 条结果</span>
      </div>

      <!-- 置顶资讯 -->
      <div v-if="topNewsList.length > 0" class="top-news">
        <div class="top-news-item" v-for="news in topNewsList" :key="'top'+news.id" @click="goToDetail(news.id)">
          <el-tag size="mini" type="danger" effect="dark">置顶</el-tag>
          <span class="top-news-title">{{ news.title }}</span>
          <span class="top-news-time">{{ formatTime(news.createTime) }}</span>
        </div>
      </div>

      <!-- 资讯列表 -->
      <div class="news-list">
        <div class="news-list-item" v-for="news in normalNewsList" :key="news.id" @click="goToDetail(news.id)">
          <div class="news-item-body">
            <h3 class="news-title">{{ news.title }}</h3>
            <p class="news-summary">{{ news.summary }}</p>
            <div class="news-meta">
              <el-tag size="mini" type="warning">{{ news.category }}</el-tag>
              <el-tag size="mini" v-if="news.examStage">{{ news.examStage }}</el-tag>
              <span class="meta-item"><i class="el-icon-view"></i> {{ news.viewCount }}</span>
              <span class="meta-item"><svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2" style="vertical-align:-2px;margin-right:2px"><path d="M14 9V5a3 3 0 0 0-3-3l-4 9v11h11.28a2 2 0 0 0 2-1.7l1.38-9a2 2 0 0 0-2-2.3zM7 22H4a2 2 0 0 1-2-2v-7a2 2 0 0 1 2-2h3"/></svg> {{ news.likeCount }}</span>
              <span class="meta-item time">{{ formatTime(news.createTime) }}</span>
            </div>
          </div>
          <div class="news-item-cover" v-if="news.cover">
            <img :src="news.cover" alt="封面" />
          </div>
        </div>

        <div v-if="newsList.length === 0" class="empty-news">
          <el-empty description="暂无相关资讯" />
        </div>
      </div>

      <div class="pagination">
        <el-pagination
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          :current-page="currentPage"
          :page-sizes="[10, 20, 30]"
          :page-size="pageSize"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
        />
      </div>
    </el-card>

    <!-- 通知弹窗 - 双 Tab -->
    <el-dialog title="消息中心" :visible.sync="notificationVisible" width="650px">
      <el-tabs v-model="notificationTab">
        <el-tab-pane label="订阅更新" name="updates">
          <div class="notification-list" v-if="updateNotifications.length > 0">
            <div v-for="item in updateNotifications" :key="item.id" class="notif-item" :class="{ unread: !item.isRead }">
              <div class="notif-header">
                <el-tag size="mini" :type="getNotifyTypeTag(item.type)">{{ getNotifyTypeLabel(item.type) }}</el-tag>
                <span class="notif-title">{{ item.title }}</span>
                <el-tag size="small" :type="item.isRead ? 'info' : 'danger'">{{ item.isRead ? '已读' : '未读' }}</el-tag>
              </div>
              <p class="notif-content">{{ item.content }}</p>
              <div class="notif-footer">
                <span class="time">{{ formatTime(item.createTime) }}</span>
                <el-button v-if="!item.isRead" type="text" size="small" @click="markAsRead(item.id)">标为已读</el-button>
              </div>
            </div>
          </div>
          <el-empty v-else description="暂无订阅更新" />
        </el-tab-pane>
        <el-tab-pane label="学习提醒" name="tasks">
          <div class="notification-list" v-if="taskNotifications.length > 0">
            <div v-for="item in taskNotifications" :key="item.id" class="notif-item" :class="{ unread: !item.isRead }">
              <div class="notif-header">
                <el-tag size="mini" type="danger">备考任务</el-tag>
                <span class="notif-title">{{ item.title }}</span>
                <el-tag size="small" :type="item.isRead ? 'info' : 'danger'">{{ item.isRead ? '已读' : '未读' }}</el-tag>
              </div>
              <p class="notif-content">{{ item.content }}</p>
              <div class="notif-footer">
                <span class="time">{{ formatTime(item.createTime) }}</span>
                <el-button v-if="!item.isRead" type="text" size="small" @click="markAsRead(item.id)">标为已读</el-button>
                <el-button type="text" size="small" @click="goToStudy" style="margin-left:8px">去完成 →</el-button>
              </div>
            </div>
          </div>
          <el-empty v-else description="暂无学习提醒">
            <el-button type="primary" size="small" @click="goToStudy">前往学习中心</el-button>
          </el-empty>
        </el-tab-pane>
        <el-tab-pane label="系统通知" name="system">
          <div class="notification-list" v-if="systemNotifications.length > 0">
            <div v-for="item in systemNotifications" :key="item.id" class="notif-item" :class="{ unread: !item.isRead }">
              <div class="notif-header">
                <el-tag size="mini" type="info">系统通知</el-tag>
                <span class="notif-title">{{ item.title }}</span>
                <el-tag size="small" :type="item.isRead ? 'info' : 'danger'">{{ item.isRead ? '已读' : '未读' }}</el-tag>
              </div>
              <p class="notif-content">{{ item.content }}</p>
              <div class="notif-footer">
                <span class="time">{{ formatTime(item.createTime) }}</span>
                <el-button v-if="!item.isRead" type="text" size="small" @click="markAsRead(item.id)">标为已读</el-button>
              </div>
            </div>
          </div>
          <el-empty v-else description="暂无系统通知" />
        </el-tab-pane>
      </el-tabs>
      <div slot="footer">
        <el-button @click="notificationVisible = false">关闭</el-button>
        <el-button type="primary" @click="markAllAsRead">全部已读</el-button>
      </div>
    </el-dialog>

    <!-- 订阅管理弹窗 - 卡片式 -->
    <el-dialog title="资讯订阅" :visible.sync="subscriptionVisible" width="550px">
      <p class="subscription-hint">选择你关注的资讯类型，有新内容时第一时间通知你</p>
      <div class="subscription-cards">
        <div
          v-for="card in subscriptionCards"
          :key="card.key"
          class="sub-card"
          :class="{ active: subscribedKeys.includes(card.key) }"
          @click="toggleSubCard(card.key)"
        >
          <div class="sub-card-icon"><i :class="card.icon"></i></div>
          <div class="sub-card-body">
            <h4>{{ card.label }}</h4>
            <p>{{ card.desc }}</p>
          </div>
          <i class="sub-card-check" :class="subscribedKeys.includes(card.key) ? 'el-icon-circle-check' : 'el-icon-circle-plus-outline'"></i>
        </div>
      </div>
      <div slot="footer">
        <el-button @click="subscriptionVisible = false">取消</el-button>
        <el-button type="primary" @click="saveSubscriptions">保存订阅</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: 'News',
  data() {
    return {
      searchKeyword: '',
      filterCategory: '',
      selectedStage: '',
      newsList: [],
      currentPage: 1,
      pageSize: 10,
      total: 0,
      notificationVisible: false,
      notificationTab: 'updates',
      notifications: [],
      unreadCount: 0,
      subscriptionVisible: false,
      subscribedKeys: [],
      categoryGroups: [
        { name: '政策公告', categories: ['国家政策', '教育部文件', '院校政策'] },
        { name: '招生信息', categories: ['招生简章', '专业目录', '报名通知', '网报公告'] },
        { name: '考试资讯', categories: ['考试大纲', '初试信息', '成绩查询', '国家线/复试线'] },
        { name: '复试调剂', categories: ['复试通知', '复试经验', '调剂信息', '录取公示'] },
        { name: '备考指导', categories: ['备考指南', '经验分享', '复习方法', '考研常识'] },
        { name: '院校动态', categories: ['院校新闻', '学科排名', '招生宣讲'] }
      ],
      subscriptionCards: [
        { key: '报考信息', label: '报考信息', icon: 'el-icon-document', desc: '招生简章、专业目录、报名通知等', tags: ['招生简章', '专业目录', '报名通知', '网报公告'] },
        { key: '考试动态', label: '考试动态', icon: 'el-icon-date', desc: '考试大纲、初试信息、成绩查询等', tags: ['考试大纲', '初试信息', '成绩查询', '国家线/复试线'] },
        { key: '备考干货', label: '备考干货', icon: 'el-icon-edit-outline', desc: '备考指南、经验分享、复习方法等', tags: ['备考指南', '经验分享', '复习方法', '考研常识'] },
        { key: '复试调剂', label: '复试调剂', icon: 'el-icon-refresh', desc: '复试通知、复试经验、调剂信息等', tags: ['复试通知', '复试经验', '调剂信息', '录取公示'] },
        { key: '政策通知', label: '政策通知', icon: 'el-icon-s-flag', desc: '国家政策、教育部文件、院校政策', tags: ['国家政策', '教育部文件', '院校政策'] },
        { key: '院校资讯', label: '院校资讯', icon: 'el-icon-school', desc: '院校新闻、学科排名、招生宣讲', tags: ['院校新闻', '学科排名', '招生宣讲'] }
      ]
    }
  },
  computed: {
    topNewsList() {
      return this.newsList.filter(n => n.isTop === 1)
    },
    normalNewsList() {
      return this.newsList.filter(n => n.isTop !== 1)
    },
    updateNotifications() {
      return this.notifications.filter(n => ['news_update', 'resource_update', 'course_update'].includes(n.type))
    },
    taskNotifications() {
      return this.notifications.filter(n => n.type === 'deadline' || (n.type === 'system' && n.title && n.title.includes('任务')))
    },
    systemNotifications() {
      return this.notifications.filter(n => n.type === 'system' && (!n.title || !n.title.includes('任务')))
    }
  },
  created() {
    this.getNewsList()
    this.getUnreadCount()
  },
  mounted() {
    if (this.$route.query.showNotifications === '1') {
      this.notificationTab = this.$route.query.notifTab || 'updates'
      this.$nextTick(() => { this.showNotifications() })
    }
  },
  methods: {
    goToStudy() {
      this.notificationVisible = false
      this.$router.push('/study')
    },
    getNotifyTypeLabel(type) {
      const m = { news_update: '资讯更新', resource_update: '资源更新', course_update: '课程更新', deadline: '备考任务', follow_post: '关注动态', system: '系统通知' }
      return m[type] || '通知'
    },
    getNotifyTypeTag(type) {
      const m = { news_update: '', resource_update: 'success', course_update: 'warning', deadline: 'danger', follow_post: 'primary', system: 'info' }
      return m[type] || 'info'
    },
    formatTime(time) {
      if (!time) return ''
      return new Date(time).toLocaleDateString('zh-CN')
    },
    goToDetail(id) {
      this.$router.push(`/news/${id}`)
    },
    search() {
      this.currentPage = 1
      this.getNewsList()
    },
    resetFilters() {
      this.currentPage = 1
      this.searchKeyword = ''
      this.filterCategory = ''
      this.selectedStage = ''
      this.getNewsList()
    },
    handleSizeChange(size) {
      this.pageSize = size
      this.getNewsList()
    },
    handleCurrentChange(current) {
      this.currentPage = current
      this.getNewsList()
    },
    async getNewsList() {
      try {
        let categoryParam = undefined
        if (this.filterCategory) {
          const group = this.categoryGroups.find(g => g.name === this.filterCategory)
          if (group) {
            categoryParam = group.categories.join(',')
          }
        }
        const res = await this.$http.get('/news/list', {
          params: {
            page: this.currentPage,
            size: this.pageSize,
            category: categoryParam,
            examStage: this.selectedStage || undefined,
            keyword: this.searchKeyword || undefined
          }
        })
        this.newsList = res?.records || []
        this.total = res?.total || 0
      } catch (error) {
        console.error('获取资讯列表失败:', error)
      }
    },
    async showNotifications() {
      const token = this.$store.state.user.token
      if (!token) {
        this.$message.warning('请先登录')
        this.$router.push('/login')
        return
      }
      this.notificationVisible = true
      await this.getNotifications()
      // 自动切换到有未读通知的 tab
      if (this.systemNotifications.some(n => !n.isRead)) {
        this.notificationTab = 'system'
      } else if (this.taskNotifications.some(n => !n.isRead)) {
        this.notificationTab = 'tasks'
      } else {
        this.notificationTab = 'updates'
      }
    },
    async getNotifications() {
      try {
        const res = await this.$http.get('/notification/list', {
          params: { page: 1, size: 50 }
        })
        this.notifications = res?.records || []
      } catch (error) {
        console.error(error)
        if (error.response && error.response.status === 401) {
          this.$message.warning('登录已过期，请重新登录')
          this.$router.push('/login')
        }
      }
    },
    async getUnreadCount() {
      try {
        const res = await this.$http.get('/notification/unread-count')
        this.unreadCount = res || 0
      } catch (error) {
        console.error(error)
      }
    },
    async markAsRead(id) {
      try {
        await this.$http.post(`/notification/read/${id}`)
        await this.getNotifications()
        await this.getUnreadCount()
        window.dispatchEvent(new Event('refresh-unread-notifications'))
      } catch (error) {
        console.error(error)
      }
    },
    async markAllAsRead() {
      try {
        await this.$http.post('/notification/read-all')
        await this.getNotifications()
        await this.getUnreadCount()
        window.dispatchEvent(new Event('refresh-unread-notifications'))
      } catch (error) {
        console.error(error)
      }
    },
    showSubscriptionDialog() {
      const token = this.$store.state.user.token
      if (!token) {
        this.$message.warning('请先登录')
        this.$router.push('/login')
        return
      }
      this.subscriptionVisible = true
      this.loadSubscriptions()
    },
    async loadSubscriptions() {
      try {
        const res = await this.$http.get('/news/subscriptions')
        const tags = Array.isArray(res) ? res : []
        this.subscribedKeys = this.subscriptionCards
          .filter(card => card.tags.some(t => tags.includes(t)))
          .map(card => card.key)
      } catch (error) {
        console.error('获取订阅标签失败:', error)
      }
    },
    toggleSubCard(key) {
      const idx = this.subscribedKeys.indexOf(key)
      if (idx > -1) {
        this.subscribedKeys.splice(idx, 1)
      } else {
        this.subscribedKeys.push(key)
      }
    },
    async saveSubscriptions() {
      try {
        const currentRes = await this.$http.get('/news/subscriptions')
        const currentTags = Array.isArray(currentRes) ? currentRes : []

        const selectedTags = this.subscriptionCards
          .filter(card => this.subscribedKeys.includes(card.key))
          .flatMap(card => card.tags)

        const toUnsubscribe = currentTags.filter(tag => !selectedTags.includes(tag))
        const toSubscribe = selectedTags.filter(tag => !currentTags.includes(tag))

        for (const tag of toUnsubscribe) {
          await this.$http.post(`/news/unsubscribe/${encodeURIComponent(tag)}`)
        }
        for (const tag of toSubscribe) {
          await this.$http.post(`/news/subscribe/${encodeURIComponent(tag)}`)
        }

        this.$message.success('订阅保存成功')
        this.subscriptionVisible = false
      } catch (error) {
        console.error('保存订阅失败:', error)
        this.$message.error('保存订阅失败')
      }
    }
  }
}
</script>

<style scoped lang="scss">
.news {
  .news-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;
    padding-bottom: 16px;
    border-bottom: 1px solid #ebeef5;

    h1 {
      font-size: 24px;
      color: #303133;
      margin: 0;
    }

    .header-actions {
      display: flex;
      gap: 10px;
    }
  }

  .news-filter {
    display: flex;
    align-items: center;
    flex-wrap: wrap;
    gap: 6px;
    margin-bottom: 20px;
    padding: 14px 16px;
    background: #f5f7fa;
    border-radius: 8px;

    .filter-result {
      margin-left: auto;
      font-size: 13px;
      color: #909399;
    }
  }

  .top-news {
    background: #fef0f0;
    border-radius: 6px;
    padding: 10px 16px;
    margin-bottom: 20px;

    .top-news-item {
      display: flex;
      align-items: center;
      gap: 10px;
      padding: 8px 0;
      cursor: pointer;
      border-bottom: 1px dashed #fde2e2;

      &:last-child {
        border-bottom: none;
      }

      &:hover .top-news-title {
        color: #409EFF;
      }

      .top-news-title {
        flex: 1;
        font-size: 14px;
        color: #303133;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
        transition: color 0.3s;
      }

      .top-news-time {
        font-size: 12px;
        color: #909399;
        flex-shrink: 0;
      }
    }
  }

  .news-list {
    margin-bottom: 30px;

    .news-list-item {
      display: flex;
      gap: 20px;
      padding: 20px 0;
      border-bottom: 1px solid #f2f2f2;
      cursor: pointer;
      transition: all 0.3s;

      &:hover {
        background: #fafbfc;
        padding-left: 12px;
      }

      .news-item-body {
        flex: 1;
        min-width: 0;

        .news-title {
          font-size: 16px;
          font-weight: 600;
          color: #303133;
          margin: 0 0 10px 0;
          line-height: 1.5;
          overflow: hidden;
          text-overflow: ellipsis;
          display: -webkit-box;
          -webkit-line-clamp: 2;
          -webkit-box-orient: vertical;
        }

        .news-summary {
          font-size: 14px;
          color: #909399;
          margin-bottom: 12px;
          line-height: 1.6;
          overflow: hidden;
          text-overflow: ellipsis;
          display: -webkit-box;
          -webkit-line-clamp: 2;
          -webkit-box-orient: vertical;
        }

        .news-meta {
          display: flex;
          align-items: center;
          gap: 12px;
          flex-wrap: wrap;

          .meta-item {
            font-size: 12px;
            color: #c0c4cc;
            display: flex;
            align-items: center;
            gap: 3px;

            &.time {
              margin-left: auto;
            }
          }
        }
      }

      .news-item-cover {
        flex-shrink: 0;
        width: 160px;
        height: 100px;
        border-radius: 6px;
        overflow: hidden;

        img {
          width: 100%;
          height: 100%;
          object-fit: cover;
        }
      }
    }

    .empty-news {
      padding: 60px 0;
      text-align: center;
    }
  }

  .pagination {
    text-align: center;
    margin-top: 20px;
  }

  .notification-list {
    max-height: 420px;
    overflow-y: auto;

    .notif-item {
      padding: 14px 16px;
      margin-bottom: 10px;
      background: #fff;
      border-radius: 8px;
      border: 1px solid #ebeef5;
      transition: all 0.2s;

      &:hover {
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
      }

      &.unread {
        background: #fdf6ec;
        border-left: 3px solid #e6a23c;
      }

      .notif-header {
        display: flex;
        align-items: center;
        gap: 8px;
        margin-bottom: 8px;

        .notif-title {
          flex: 1;
          font-size: 14px;
          font-weight: 600;
          color: #303133;
        }
      }

      .notif-content {
        margin: 0 0 8px 0;
        font-size: 13px;
        color: #606266;
        line-height: 1.5;
      }

      .notif-footer {
        display: flex;
        align-items: center;
        justify-content: space-between;

        .time {
          font-size: 12px;
          color: #909399;
        }
      }
    }
  }

  .subscription-hint {
    margin-bottom: 20px;
    color: #606266;
    font-size: 14px;
    text-align: center;
  }

  .subscription-cards {
    display: flex;
    flex-direction: column;
    gap: 10px;

    .sub-card {
      display: flex;
      align-items: center;
      gap: 14px;
      padding: 14px 16px;
      border: 1px solid #ebeef5;
      border-radius: 10px;
      cursor: pointer;
      transition: all 0.25s;

      &:hover {
        border-color: #c6e2ff;
        background: #f5f9ff;
      }

      &.active {
        border-color: #409EFF;
        background: #ecf5ff;

        .sub-card-icon {
          background: #409EFF;
          color: #fff;
        }

        .sub-card-check {
          color: #409EFF;
        }
      }

      .sub-card-icon {
        width: 42px;
        height: 42px;
        border-radius: 10px;
        background: #f5f7fa;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 20px;
        color: #909399;
        flex-shrink: 0;
        transition: all 0.25s;
      }

      .sub-card-body {
        flex: 1;
        min-width: 0;

        h4 {
          margin: 0 0 4px 0;
          font-size: 14px;
          color: #303133;
        }

        p {
          margin: 0;
          font-size: 12px;
          color: #909399;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }
      }

      .sub-card-check {
        font-size: 22px;
        color: #c0c4cc;
        flex-shrink: 0;
        transition: all 0.25s;
      }
    }
  }
}
</style>
