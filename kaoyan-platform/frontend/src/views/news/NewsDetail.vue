<template>
  <div class="news-detail">
    <div class="reading-progress" :style="{ width: progressPercent + '%' }"></div>

    <div class="detail-container">
      <!-- 面包屑导航 -->
      <div class="breadcrumb">
        <el-breadcrumb separator=">">
          <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
          <el-breadcrumb-item :to="{ path: '/news' }">考研资讯</el-breadcrumb-item>
          <el-breadcrumb-item v-if="news.category" :to="{ path: '/news', query: { category: news.categoryGroup }}">{{ news.category }}</el-breadcrumb-item>
          <el-breadcrumb-item>{{ news.title }}</el-breadcrumb-item>
        </el-breadcrumb>
      </div>

      <div class="news-header">
        <h1>{{ news.title }}</h1>
        <div class="news-meta">
          <span class="category">{{ news.category }}</span>
          <span v-if="news.examStage" class="stage">{{ news.examStage }}</span>
          <span class="time"><i class="el-icon-time"></i> {{ formatTime(news.createTime) }}</span>
          <span class="view-count"><i class="el-icon-view"></i> {{ news.viewCount }} 阅读</span>
          <span class="read-time"><i class="el-icon-reading"></i> 约{{ readMinutes }}分钟</span>
        </div>
      </div>

      <!-- 文章内容 -->
      <div class="news-content" ref="content" v-html="renderedContent"></div>

      <!-- 底部操作栏 -->
      <div class="news-actions">
        <el-button @click="likeNews" :type="liked ? 'danger' : 'default'" size="medium">
          <svg viewBox="0 0 24 24" width="15" height="15" fill="none" stroke="currentColor" stroke-width="2" style="vertical-align:-3px;margin-right:2px"><path d="M14 9V5a3 3 0 0 0-3-3l-4 9v11h11.28a2 2 0 0 0 2-1.7l1.38-9a2 2 0 0 0-2-2.3zM7 22H4a2 2 0 0 1-2-2v-7a2 2 0 0 1 2-2h3"/></svg> 点赞 ({{ news.likeCount }})
        </el-button>
        <el-button @click="toggleBookmark" :type="bookmarked ? 'warning' : 'default'" size="medium">
          <i :class="bookmarked ? 'el-icon-star-on' : 'el-icon-collection-tag'"></i> {{ bookmarked ? '已收藏' : '收藏' }}
        </el-button>
        <el-button @click="shareDialogVisible = true" size="medium">
          <i class="el-icon-share"></i> 分享
        </el-button>
      </div>

      <!-- 上一篇 / 下一篇 -->
      <div class="article-nav" v-if="prevArticle || nextArticle">
        <div class="nav-item prev" v-if="prevArticle" @click="goToArticle(prevArticle.id)">
          <span class="nav-label"><i class="el-icon-arrow-left"></i> 上一篇</span>
          <span class="nav-title">{{ prevArticle.title }}</span>
        </div>
        <div class="nav-item next" v-if="nextArticle" @click="goToArticle(nextArticle.id)">
          <span class="nav-label">下一篇 <i class="el-icon-arrow-right"></i></span>
          <span class="nav-title">{{ nextArticle.title }}</span>
        </div>
      </div>

      <share-dialog v-model="shareDialogVisible" :share-title="news.title" :share-desc="news.summary" />

      <!-- 相关推荐 -->
      <div class="related-news" v-if="relatedNews.length > 0">
        <h3>相关推荐</h3>
        <div class="related-list">
          <div class="related-item" v-for="item in relatedNews" :key="item.id" @click="goToArticle(item.id)">
            <div class="related-body">
              <span class="related-title">{{ item.title }}</span>
              <span class="related-summary">{{ item.summary }}</span>
            </div>
            <span class="related-meta">{{ item.category }} · {{ formatTime(item.createTime) }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { marked } from 'marked'
import ShareDialog from '@/components/ShareDialog'

export default {
  name: 'NewsDetail',
  components: { ShareDialog },
  data() {
    return {
      news: {
        id: 0,
        title: '',
        category: '',
        content: '',
        viewCount: 0,
        likeCount: 0,
        createTime: '',
        examStage: '',
        summary: ''
      },
      liked: false,
      bookmarked: false,
      relatedNews: [],
      prevArticle: null,
      nextArticle: null,
      shareDialogVisible: false,
      progressPercent: 0
    }
  },
  computed: {
    renderedContent() {
      return this.news.content ? marked(this.news.content) : ''
    },
    readMinutes() {
      if (!this.news.content) return 1
      const text = this.news.content.replace(/<[^>]*>/g, '')
      return Math.max(1, Math.ceil(text.length / 500))
    }
  },
  created() {
    this.loadArticle()
  },
  watch: {
    '$route'(to, from) {
      if (to.params.id !== from.params.id) {
        this.loadArticle()
      }
    }
  },
  mounted() {
    window.addEventListener('scroll', this.onScroll)
  },
  beforeDestroy() {
    window.removeEventListener('scroll', this.onScroll)
  },
  methods: {
    loadArticle() {
      const bookmarks = JSON.parse(localStorage.getItem('bookmarkedNews') || '[]')
      const id = parseInt(this.$route.params.id)
      this.bookmarked = bookmarks.includes(id)
      this.getNewsDetail()
    },
    goToArticle(id) {
      if (!id) return
      this.$router.push(`/news/${id}`)
    },
    onScroll() {
      const scrollTop = window.pageYOffset || document.documentElement.scrollTop
      const scrollHeight = document.documentElement.scrollHeight - document.documentElement.clientHeight
      this.progressPercent = scrollHeight > 0 ? Math.min(100, (scrollTop / scrollHeight) * 100) : 0
    },
    toggleBookmark() {
      const bookmarks = JSON.parse(localStorage.getItem('bookmarkedNews') || '[]')
      const id = this.news.id
      if (this.bookmarked) {
        const idx = bookmarks.indexOf(id)
        if (idx > -1) bookmarks.splice(idx, 1)
        this.$message.success('已取消收藏')
      } else {
        bookmarks.push(id)
        this.$message.success('已收藏')
      }
      this.bookmarked = !this.bookmarked
      localStorage.setItem('bookmarkedNews', JSON.stringify(bookmarks))
    },
    formatTime(time) {
      if (!time) return ''
      return new Date(time).toLocaleDateString('zh-CN')
    },
    async likeNews() {
      if (this.liked) {
        this.$message.warning('您已经点过赞了')
        return
      }
      try {
        await this.$http.post(`/news/like/${this.news.id}`)
        this.news.likeCount++
        this.liked = true
        this.$message.success('点赞成功')
      } catch (error) {
        console.error('点赞失败:', error)
        this.$message.error('点赞失败，请稍后重试')
      }
    },
    async getNewsDetail() {
      try {
        const id = this.$route.params.id
        const res = await this.$http.get(`/news/detail/${id}`)
        this.news = res || {}

        const categoryGroup = this.getCategoryGroupName(this.news.category)
        this.news.categoryGroup = categoryGroup

        // 加载相关资讯和上一篇/下一篇
        this.loadAdjacentArticles(id).then(() => {
          if (this.relatedNews.length === 0) {
            this.loadRelatedByCategory(this.news.category, id)
          }
        })
      } catch (error) {
        console.error('获取资讯详情失败:', error)
        this.$message.error('获取资讯详情失败')
      }
    },
    getCategoryGroupName(category) {
      const groups = [
        { name: '政策公告', cats: ['国家政策', '教育部文件', '院校政策'] },
        { name: '招生信息', cats: ['招生简章', '专业目录', '报名通知', '网报公告'] },
        { name: '考试资讯', cats: ['考试大纲', '初试信息', '成绩查询', '国家线/复试线'] },
        { name: '复试调剂', cats: ['复试通知', '复试经验', '调剂信息', '录取公示'] },
        { name: '备考指导', cats: ['备考指南', '经验分享', '复习方法', '考研常识'] },
        { name: '院校动态', cats: ['院校新闻', '学科排名', '招生宣讲'] }
      ]
      const group = groups.find(g => g.cats.includes(category))
      return group ? group.name : ''
    },
    async loadAdjacentArticles(currentId) {
      try {
        const res = await this.$http.get('/news/list', {
          params: { page: 1, size: 50, sort: 'create_time', order: 'desc' }
        })
        const records = res?.records || []
        const idx = records.findIndex(r => r.id === currentId)
        if (idx > 0) this.prevArticle = records[idx - 1]
        if (idx >= 0 && idx < records.length - 1) this.nextArticle = records[idx + 1]
        this.relatedNews = records.filter(r => r.id !== parseInt(currentId)).slice(0, 5)
      } catch (e) {
        console.error('加载上下文文章失败:', e)
      }
    },
    async loadRelatedByCategory(category, currentId) {
      try {
        const res = await this.$http.get('/news/list', {
          params: { page: 1, size: 6, category }
        })
        const records = res?.records || []
        this.relatedNews = records.filter(r => r.id !== parseInt(currentId)).slice(0, 5)
      } catch (e) {
        console.error('加载相关文章失败:', e)
      }
    }
  }
}
</script>

<style scoped lang="scss">
.reading-progress {
  position: fixed;
  top: 0;
  left: 0;
  height: 3px;
  background: linear-gradient(90deg, #409EFF, #66b1ff);
  z-index: 2000;
  transition: width 0.1s linear;
}

.detail-container {
  max-width: 860px;
  margin: 0 auto;
  padding: 20px;
}

.breadcrumb {
  margin-bottom: 20px;
  padding: 12px 16px;
  background: #f5f7fa;
  border-radius: 6px;
}

.news-header {
  margin-bottom: 30px;

  h1 {
    font-size: 26px;
    color: #303133;
    margin: 0 0 16px 0;
    line-height: 1.4;
  }

  .news-meta {
    display: flex;
    flex-wrap: wrap;
    gap: 16px;
    font-size: 13px;
    color: #909399;

    .category {
      background: #ecf5ff;
      color: #409EFF;
      padding: 3px 10px;
      border-radius: 4px;
      font-weight: 500;
    }

    .stage {
      background: #fef0f0;
      color: #e6a23c;
      padding: 3px 10px;
      border-radius: 4px;
    }

    span {
      display: flex;
      align-items: center;
      gap: 4px;
    }

    .read-time {
      margin-left: auto;
    }
  }
}

.news-content {
  background: #fff;
  padding: 36px 40px;
  border-radius: 10px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  margin-bottom: 30px;
  line-height: 1.9;
  color: #4a4a4a;
  font-size: 15px;

  :deep(h1) {
    font-size: 22px;
    color: #303133;
    margin: 28px 0 16px;
  }

  :deep(h2) {
    font-size: 20px;
    color: #303133;
    margin: 24px 0 14px;
    padding-bottom: 8px;
    border-bottom: 1px solid #ebeef5;
  }

  :deep(h3) {
    font-size: 18px;
    color: #303133;
    margin: 22px 0 12px;
  }

  :deep(p) {
    margin: 14px 0;
  }

  :deep(ul), :deep(ol) {
    margin: 14px 0;
    padding-left: 28px;
  }

  :deep(li) {
    margin: 6px 0;
  }

  :deep(strong) {
    font-weight: 600;
    color: #303133;
  }

  :deep(blockquote) {
    margin: 16px 0;
    padding: 12px 20px;
    background: #f5f7fa;
    border-left: 4px solid #409EFF;
    color: #606266;
  }

  :deep(img) {
    max-width: 100%;
    border-radius: 6px;
    margin: 12px 0;
  }

  :deep(table) {
    width: 100%;
    border-collapse: collapse;
    margin: 16px 0;

    th, td {
      border: 1px solid #ebeef5;
      padding: 10px 14px;
      text-align: left;
    }

    th {
      background: #f5f7fa;
      font-weight: 600;
    }
  }

  :deep(code) {
    background: #f5f7fa;
    padding: 2px 6px;
    border-radius: 3px;
    font-size: 13px;
    color: #e6a23c;
  }

  :deep(pre) {
    background: #f5f7fa;
    padding: 16px;
    border-radius: 6px;
    overflow-x: auto;

    code {
      background: none;
      padding: 0;
    }
  }
}

.news-actions {
  display: flex;
  gap: 16px;
  margin-bottom: 32px;
  justify-content: center;
}

.article-nav {
  display: flex;
  gap: 20px;
  margin-bottom: 32px;

  .nav-item {
    flex: 1;
    padding: 16px 20px;
    background: #fff;
    border-radius: 8px;
    border: 1px solid #ebeef5;
    cursor: pointer;
    transition: all 0.25s;

    &:hover {
      border-color: #409EFF;
      box-shadow: 0 2px 8px rgba(64, 158, 255, 0.1);

      .nav-title {
        color: #409EFF;
      }
    }

    .nav-label {
      display: block;
      font-size: 12px;
      color: #909399;
      margin-bottom: 6px;
    }

    .nav-title {
      font-size: 14px;
      color: #303133;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
      transition: color 0.25s;
    }

    &.next {
      text-align: right;
    }
  }
}

.related-news {
  margin-top: 0;
  padding: 24px;
  background: #fff;
  border-radius: 10px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);

  h3 {
    font-size: 18px;
    color: #303133;
    margin: 0 0 16px;
    padding-left: 12px;
    border-left: 3px solid #409EFF;
  }

  .related-list {
    .related-item {
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      padding: 14px 0;
      border-bottom: 1px solid #f5f7fa;
      cursor: pointer;
      transition: all 0.2s;

      &:last-child {
        border-bottom: none;
      }

      &:hover {
        padding-left: 8px;

        .related-title {
          color: #409EFF;
        }
      }

      .related-body {
        flex: 1;
        min-width: 0;

        .related-title {
          display: block;
          font-size: 14px;
          color: #303133;
          margin-bottom: 4px;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
          transition: color 0.2s;
        }

        .related-summary {
          font-size: 12px;
          color: #909399;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }
      }

      .related-meta {
        font-size: 12px;
        color: #c0c4cc;
        flex-shrink: 0;
        margin-left: 16px;
        margin-top: 2px;
      }
    }
  }
}
</style>
