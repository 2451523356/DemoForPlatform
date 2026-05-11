<template>
  <div class="home">
    <!-- 备考阶段导航栏 -->
    <div class="stage-nav">
      <div class="stage-container">
        <div
          v-for="stage in examStages"
          :key="stage.key"
          class="stage-item"
          :class="{ active: activeStage === stage.key }"
          @click="activeStage = stage.key"
        >
          <div class="stage-dot" :style="{ background: stage.color }"></div>
          <span class="stage-label">{{ stage.label }}</span>
          <span class="stage-time">{{ stage.time }}</span>
        </div>
      </div>
    </div>

    <!-- 当前阶段引导卡片 -->
    <div class="stage-guide" v-if="currentStage">
      <div class="guide-card" :style="{ borderLeftColor: currentStage.color }">
        <div class="guide-left">
          <div class="guide-stage-badge" :style="{ background: currentStage.color }">
            {{ currentStage.label }}
          </div>
          <div class="guide-content">
            <h3>{{ currentStage.title }}</h3>
            <p>{{ currentStage.description }}</p>
            <div class="guide-actions">
              <el-button
                v-for="action in currentStage.actions"
                :key="action.label"
                :type="action.type || 'primary'"
                size="small"
                @click="$router.push(action.path)"
              >
                {{ action.label }}
              </el-button>
            </div>
          </div>
        </div>
        <div class="guide-right">
          <exam-countdown />
        </div>
      </div>
    </div>

    <div class="home-content">
      <!-- 左侧主内容区 -->
      <div class="main-column">
        <!-- 重要通知/政策 -->
        <div class="section" v-if="importantNews.length > 0">
          <div class="section-header">
            <h2><i class="el-icon-warning-outline"></i> 重要通知</h2>
            <el-button type="text" @click="$router.push('/news')">全部资讯 <i class="el-icon-arrow-right"></i></el-button>
          </div>
          <div class="important-list">
            <div
              v-for="item in importantNews"
              :key="item.id"
              class="important-item"
              @click="$router.push(`/news/${item.id}`)"
            >
              <el-tag :type="item.isTop ? 'danger' : 'warning'" size="mini" class="stage-tag">
                {{ item.examStage || item.category }}
              </el-tag>
              <span class="important-title">{{ item.title }}</span>
              <span class="important-time">{{ formatDate(item.createTime) }}</span>
            </div>
          </div>
        </div>

        <!-- 按备考阶段分类的资讯 -->
        <div class="section" v-if="stageNews.length > 0">
          <div class="section-header">
            <h2><i class="el-icon-document"></i> {{ activeStageLabel }}相关资讯</h2>
          </div>
          <div class="news-grid">
            <el-card
              v-for="item in stageNews"
              :key="item.id"
              class="news-card"
              shadow="hover"
              @click.native="$router.push(`/news/${item.id}`)"
            >
              <div class="news-tag-row">
                <el-tag size="mini" type="primary">{{ item.category }}</el-tag>
                <el-tag size="mini" type="info">{{ item.examStage || '' }}</el-tag>
              </div>
              <h4>{{ item.title }}</h4>
              <p>{{ item.summary }}</p>
              <div class="news-footer">
                <span><i class="el-icon-view"></i> {{ item.viewCount || 0 }}</span>
                <span>{{ formatDate(item.createTime) }}</span>
              </div>
            </el-card>
          </div>
        </div>

        <!-- 院校推荐 -->
        <div class="section">
          <div class="section-header">
            <h2><i class="el-icon-school"></i> 热门院校</h2>
            <el-button type="text" @click="$router.push('/schools')">院校库 <i class="el-icon-arrow-right"></i></el-button>
          </div>
          <div class="school-grid">
            <div
              v-for="school in hotSchools"
              :key="school.id"
              class="school-card"
              @click="$router.push(`/schools/${school.id}`)"
            >
              <div class="school-rank" :class="getRankClass(school.ranking)">{{ school.ranking }}</div>
              <div class="school-info">
                <h4>{{ school.name }}</h4>
                <div class="school-tags">
                  <el-tag size="mini" :type="school.type === '985' ? 'danger' : school.type === '211' ? 'warning' : 'info'">
                    {{ school.type }}
                  </el-tag>
                  <span class="school-city">{{ school.city }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧栏 -->
      <div class="side-column">
        <!-- 快速入口 -->
        <el-card class="side-card">
          <h3 class="side-title">快速入口</h3>
          <div class="quick-links">
            <div class="quick-link" @click="$router.push('/national-score')">
              <i class="el-icon-data-line"></i>
              <span>国家线查询</span>
            </div>
            <div class="quick-link" @click="$router.push('/schools')">
              <i class="el-icon-search"></i>
              <span>院校分数线</span>
            </div>
            <div class="quick-link" @click="$router.push('/adjustment')">
              <i class="el-icon-connection"></i>
              <span>调剂信息</span>
            </div>
            <div class="quick-link" @click="$router.push('/course')">
              <i class="el-icon-video-camera"></i>
              <span>在线课程</span>
            </div>
            <div class="quick-link" @click="$router.push('/news')">
              <i class="el-icon-document"></i>
              <span>考研资讯</span>
            </div>
            <div class="quick-link" @click="$router.push('/study')">
              <i class="el-icon-reading"></i>
              <span>学习中心</span>
            </div>
            <div class="quick-link" @click="$router.push('/resource')">
              <i class="el-icon-folder-opened"></i>
              <span>资料下载</span>
            </div>
            <div class="quick-link" @click="$router.push('/community')">
              <i class="el-icon-chat-dot-round"></i>
              <span>研友社区</span>
            </div>
          </div>
        </el-card>

        <!-- 考试关键时间节点 -->
        <el-card class="side-card">
          <h3 class="side-title">2027考研时间轴</h3>
          <el-timeline class="exam-timeline">
            <el-timeline-item
              v-for="m in examMilestones"
              :key="m.date"
              :timestamp="m.date"
              :type="m.passed ? 'success' : 'primary'"
              :color="m.passed ? '#67C23A' : m.isNext ? '#409EFF' : '#C0C4CC'"
              size="small"
            >
              <span :class="{ 'next-milestone': m.isNext }">{{ m.label }}</span>
            </el-timeline-item>
          </el-timeline>
        </el-card>

        <!-- 热门话题 -->
        <el-card class="side-card" v-if="hotPosts.length > 0">
          <h3 class="side-title">社区热帖</h3>
          <div
            v-for="post in hotPosts"
            :key="post.id"
            class="hot-post-item"
            @click="$router.push(`/community/post/${post.id}`)"
          >
            <span class="hot-post-title">{{ post.title }}</span>
            <span class="hot-post-meta">
              <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2" style="vertical-align:-2px;margin-right:2px"><path d="M14 9V5a3 3 0 0 0-3-3l-4 9v11h11.28a2 2 0 0 0 2-1.7l1.38-9a2 2 0 0 0-2-2.3zM7 22H4a2 2 0 0 1-2-2v-7a2 2 0 0 1 2-2h3"/></svg> {{ post.likeCount || 0 }}
              <i class="el-icon-chat-dot-round"></i> {{ post.commentCount || 0 }}
            </span>
          </div>
        </el-card>
      </div>
    </div>
  </div>
</template>

<script>
import ExamCountdown from '@/components/ExamCountdown.vue'

export default {
  name: 'Home',
  components: { ExamCountdown },
  data() {
    return {
      activeStage: 'prepare',
      examStages: [
        { key: 'select', label: '择校评估', time: '1-3月', icon: 'el-icon-aim', color: '#409EFF' },
        { key: 'prepare', label: '基础复习', time: '3-6月', icon: 'el-icon-reading', color: '#67C23A' },
        { key: 'strengthen', label: '强化提升', time: '7-9月', icon: 'el-icon-s-promotion', color: '#E6A23C' },
        { key: 'register', label: '报名报考', time: '9-10月', icon: 'el-icon-edit', color: '#F56C6C' },
        { key: 'sprint', label: '冲刺模考', time: '10-12月', icon: 'el-icon-thumb', color: '#E4393C' },
        { key: 'reexam', label: '复试调剂', time: '次年2-4月', icon: 'el-icon-trophy', color: '#B37FEB' }
      ],
      stageGuides: {
        select: { title: '科学择校，赢在起点', description: '结合历年分数线、报录比、院校实力、个人兴趣等多维度数据，找到最适合你的目标院校和专业。', actions: [{ label: '查看院校库', path: '/schools', type: 'primary' }, { label: '国家线查询', path: '/national-score' }, { label: '择校经验帖', path: '/community' }] },
        prepare: { title: '夯实基础，稳步前进', description: '系统复习数学、英语等公共课基础知识，建立完整的知识框架。每天保持规律学习习惯，记录学习进度。', actions: [{ label: '开始学习', path: '/study' }, { label: '在线课程', path: '/course' }, { label: '下载资料', path: '/resource' }] },
        strengthen: { title: '重点突破，全面提升', description: '进入强化阶段，专业课与公共课同步推进，大量做题巩固知识点，查漏补缺，不留死角。', actions: [{ label: '精选课程', path: '/course' }, { label: '学习中心', path: '/study' }, { label: '资料下载', path: '/resource' }] },
        register: { title: '按时报名，确认信息', description: '关注研招网报名时间节点，准备好证件照、学历证明等材料，仔细核对报考信息，完成网上确认。', actions: [{ label: '报考资讯', path: '/news' }, { label: '招生简章', path: '/news' }] },
        sprint: { title: '全力冲刺，模拟实战', description: '严格按照考试时间进行全真模拟，重点背诵政治分析题和英语作文模板，查漏补缺。', actions: [{ label: '冲刺课程', path: '/course' }, { label: '真题资料', path: '/resource' }, { label: '学习轨迹', path: '/study' }] },
        reexam: { title: '备战复试，关注调剂', description: '初试结束后预估分数，准备复试内容。如需要调剂，及时关注各院校调剂信息，把握上岸机会。', actions: [{ label: '调剂信息', path: '/adjustment' }, { label: '复试经验', path: '/community' }, { label: '国家线', path: '/national-score' }] }
      },
      examMilestones: [
        { label: '考研大纲发布', date: '2026-09-15', passed: false },
        { label: '网上预报名', date: '2026-09-24', passed: false },
        { label: '正式报名', date: '2026-10-05', passed: false },
        { label: '网上确认', date: '2026-11-01', passed: false },
        { label: '准考证打印', date: '2026-12-10', passed: false },
        { label: '考研初试', date: '2026-12-26', passed: false },
        { label: '初试成绩公布', date: '2027-02-15', passed: false },
        { label: '国家线公布', date: '2027-03-15', passed: false },
        { label: '复试/调剂', date: '2027-03-04月', passed: false }
      ],
      importantNews: [],
      stageNews: [],
      hotSchools: [],
      hotPosts: []
    }
  },
  computed: {
    currentStage() {
      const guide = this.stageGuides[this.activeStage] || this.stageGuides.prepare
      return { ...guide, label: this.activeStageLabel, color: this.activeStageColor }
    },
    activeStageLabel() {
      const s = this.examStages.find(s => s.key === this.activeStage)
      return s ? s.label : '基础复习'
    },
    activeStageColor() {
      const s = this.examStages.find(s => s.key === this.activeStage)
      return s ? s.color : '#67C23A'
    }
  },
  created() {
    this.fetchImportantNews()
    this.fetchStageNews()
    this.fetchHotSchools()
    this.fetchHotPosts()
    this.updateMilestoneStatus()
  },
  mounted() {
    // 根据当前月份自动定位备考阶段
    const month = new Date().getMonth() + 1
    if (month >= 1 && month <= 3) this.activeStage = 'select'
    else if (month >= 4 && month <= 6) this.activeStage = 'prepare'
    else if (month >= 7 && month <= 8) this.activeStage = 'strengthen'
    else if (month >= 9 && month <= 10) this.activeStage = 'register'
    else if (month >= 11 && month <= 12) this.activeStage = 'sprint'
    else this.activeStage = 'reexam'
  },
  methods: {
    formatDate(time) {
      if (!time) return ''
      return new Date(time).toLocaleDateString('zh-CN')
    },
    getRankClass(rank) {
      if (rank <= 5) return 'rank-top'
      if (rank <= 10) return 'rank-high'
      return 'rank-normal'
    },
    updateMilestoneStatus() {
      const now = new Date()
      let foundNext = false
      this.examMilestones.forEach(m => {
        const d = new Date(m.date)
        m.passed = d < now && !isNaN(d.getTime())
        if (!m.passed && !foundNext) {
          m.isNext = true
          foundNext = true
        } else {
          m.isNext = false
        }
      })
    },
    async fetchImportantNews() {
      try {
        const res = await this.$http.get('/news/list', { params: { page: 1, size: 6, isTop: 1 } })
        this.importantNews = (Array.isArray(res) ? res : (res?.records || [])).slice(0, 6)
      } catch (e) { console.error('获取重要通知失败:', e) }
    },
    async fetchStageNews() {
      try {
        const res = await this.$http.get('/news/list', { params: { page: 1, size: 6 } })
        this.stageNews = Array.isArray(res) ? res : (res?.records || [])
      } catch (e) { console.error('获取资讯失败:', e) }
    },
    async fetchHotSchools() {
      try {
        const res = await this.$http.get('/school/list', { params: { page: 1, size: 8 } })
        const list = res?.records || []
        this.hotSchools = list.sort((a, b) => (a.ranking || 99) - (b.ranking || 99)).slice(0, 8)
      } catch (e) { console.error('获取院校失败:', e) }
    },
    async fetchHotPosts() {
      try {
        const res = await this.$http.get('/community/posts', { params: { page: 1, size: 5 } })
        const list = res?.records || (Array.isArray(res) ? res : [])
        this.hotPosts = list.slice(0, 5)
      } catch (e) { console.error('获取热帖失败:', e) }
    }
  }
}
</script>

<style scoped lang="scss">
.home {
  background: #f5f7fa;
  min-height: calc(100vh - 120px);
}

// 备考阶段导航
.stage-nav {
  background: #fff;
  border-bottom: 2px solid #EBEEF5;
  padding: 8px 0;
  position: sticky;
  top: 0;
  z-index: 99;
  .stage-container {
    max-width: 1200px;
    margin: 0 auto;
    display: flex;
    justify-content: space-between;
    padding: 0 20px;
  }
  .stage-item {
    display: flex;
    align-items: center;
    gap: 8px;
    cursor: pointer;
    padding: 10px 18px;
    border-radius: 6px;
    transition: all 0.25s;
    border-bottom: 3px solid transparent;
    &:hover { background: #f5f7fa; }
    &.active {
      background: #ecf5ff;
      border-bottom-color: #409EFF;
    }
    .stage-dot {
      width: 10px; height: 10px;
      border-radius: 50%;
      flex-shrink: 0;
    }
    .stage-label { font-size: 14px; font-weight: 600; color: #303133; white-space: nowrap; }
    .stage-time { font-size: 12px; color: #909399; white-space: nowrap; }
  }
}

// 阶段引导卡片
.stage-guide {
  max-width: 1200px;
  margin: 20px auto;
  padding: 0 20px;
  .guide-card {
    background: #fff;
    border-radius: 12px;
    padding: 24px 30px;
    display: flex;
    justify-content: space-between;
    align-items: center;
    border-left: 5px solid #409EFF;
    box-shadow: 0 2px 12px rgba(0,0,0,0.06);
    .guide-left {
      display: flex;
      gap: 20px;
      align-items: flex-start;
      flex: 1;
    }
    .guide-stage-badge {
      color: #fff;
      padding: 6px 18px;
      border-radius: 20px;
      font-size: 14px;
      font-weight: 600;
      white-space: nowrap;
      margin-top: 2px;
    }
    .guide-content {
      h3 { font-size: 20px; margin: 0 0 8px; color: #303133; }
      p { font-size: 14px; color: #606266; margin: 0 0 12px; line-height: 1.6; max-width: 500px; }
      .guide-actions { display: flex; gap: 10px; flex-wrap: wrap; }
    }
    .guide-right { flex-shrink: 0; }
  }
}

.home-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px 40px;
  display: grid;
  grid-template-columns: 1fr 340px;
  gap: 20px;
}

.main-column {
  .section {
    background: #fff;
    border-radius: 12px;
    padding: 20px;
    margin-bottom: 20px;
    box-shadow: 0 1px 8px rgba(0,0,0,0.04);
    .section-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 16px;
      h2 { font-size: 17px; margin: 0; color: #303133; i { margin-right: 6px; color: #409EFF; } }
    }
  }
}

// 重要通知
.important-list {
  .important-item {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 12px 0;
    border-bottom: 1px solid #f5f5f5;
    cursor: pointer;
    transition: all 0.2s;
    &:hover { background: #fafafa; padding-left: 8px; border-radius: 4px; }
    &:last-child { border-bottom: none; }
    .stage-tag { flex-shrink: 0; }
    .important-title { flex: 1; font-size: 14px; color: #303133; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
    .important-time { font-size: 12px; color: #C0C4CC; flex-shrink: 0; }
  }
}

// 资讯网格
.news-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
  .news-card {
    cursor: pointer;
    transition: all 0.3s;
    border-radius: 8px;
    &:hover { transform: translateY(-3px); box-shadow: 0 4px 16px rgba(0,0,0,0.1); }
    .news-tag-row { display: flex; gap: 6px; margin-bottom: 10px; }
    h4 { font-size: 15px; color: #303133; margin: 0 0 8px; overflow: hidden; text-overflow: ellipsis; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; }
    p { font-size: 13px; color: #909399; margin: 0 0 12px; line-height: 1.5; overflow: hidden; text-overflow: ellipsis; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; }
    .news-footer { display: flex; justify-content: space-between; font-size: 12px; color: #C0C4CC; }
  }
}

// 院校网格
.school-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
  .school-card {
    cursor: pointer;
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 14px;
    border: 1px solid #EBEEF5;
    border-radius: 8px;
    transition: all 0.2s;
    &:hover { border-color: #409EFF; background: #ecf5ff; }
    .school-rank {
      width: 36px; height: 36px;
      border-radius: 8px;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 16px;
      font-weight: 700;
      color: #fff;
      background: #C0C4CC;
      flex-shrink: 0;
      &.rank-top { background: #E4393C; }
      &.rank-high { background: #E6A23C; }
    }
    .school-info {
      h4 { font-size: 14px; margin: 0 0 4px; color: #303133; }
      .school-tags { display: flex; align-items: center; gap: 6px; }
      .school-city { font-size: 12px; color: #909399; }
    }
  }
}

// 右侧栏
.side-column {
  .side-card {
    margin-bottom: 16px;
    border-radius: 12px;
    .side-title { font-size: 16px; margin: 0 0 14px; color: #303133; font-weight: 600; }
  }
}

.quick-links {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 8px;
  .quick-link {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 12px;
    border-radius: 8px;
    cursor: pointer;
    transition: all 0.2s;
    background: #f5f7fa;
    font-size: 13px;
    color: #606266;
    &:hover { background: #ecf5ff; color: #409EFF; }
    i { font-size: 20px; color: #409EFF; }
  }
}

.exam-timeline {
  ::v-deep .el-timeline-item__timestamp { font-size: 12px; }
  .next-milestone { color: #409EFF; font-weight: 600; }
}

.hot-post-item {
  padding: 10px 0;
  border-bottom: 1px solid #f5f5f5;
  cursor: pointer;
  display: flex;
  flex-direction: column;
  gap: 4px;
  &:hover .hot-post-title { color: #409EFF; }
  &:last-child { border-bottom: none; }
  .hot-post-title { font-size: 13px; color: #303133; transition: color 0.2s; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
  .hot-post-meta { font-size: 12px; color: #C0C4CC; display: flex; gap: 12px; }
}

// 响应式
@media (max-width: 1024px) {
  .home-content { grid-template-columns: 1fr; }
  .news-grid { grid-template-columns: 1fr; }
  .school-grid { grid-template-columns: repeat(2, 1fr); }
  .stage-nav .stage-container { flex-wrap: wrap; justify-content: center; gap: 4px; }
  .stage-item { padding: 6px 12px; .stage-time { display: none; } }
  .stage-guide .guide-card { flex-direction: column; align-items: flex-start; gap: 16px; }
}
@media (max-width: 768px) {
  .school-grid { grid-template-columns: 1fr; }
  .stage-item { padding: 6px 10px; .stage-label { font-size: 12px; } .stage-dot { width: 8px; height: 8px; } }
}
</style>
