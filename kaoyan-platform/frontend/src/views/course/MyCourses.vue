<template>
  <div class="my-courses">
    <div class="page-header">
      <h2>我的课程</h2>
      <p class="subtitle">管理已购买的课程，继续你的学习之旅</p>
    </div>

    <!-- 学习概览统计 -->
    <div class="overview-stats" v-if="courses.length > 0">
      <div class="overview-card">
        <div class="overview-icon courses-icon"><i class="el-icon-video-camera"></i></div>
        <div class="overview-body">
          <div class="overview-value">{{ courses.length }}</div>
          <div class="overview-label">已购课程</div>
        </div>
      </div>
      <div class="overview-card">
        <div class="overview-icon progress-icon"><i class="el-icon-loading"></i></div>
        <div class="overview-body">
          <div class="overview-value">{{ averageProgress }}%</div>
          <div class="overview-label">平均进度</div>
        </div>
      </div>
      <div class="overview-card">
        <div class="overview-icon time-icon"><i class="el-icon-time"></i></div>
        <div class="overview-body">
          <div class="overview-value">{{ totalStudyHours }}h</div>
          <div class="overview-label">总学习时长</div>
        </div>
      </div>
      <div class="overview-card">
        <div class="overview-icon done-icon"><i class="el-icon-circle-check"></i></div>
        <div class="overview-body">
          <div class="overview-value">{{ completedCount }}</div>
          <div class="overview-label">已完成</div>
        </div>
      </div>
    </div>

    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="4" animated />
    </div>

    <div v-else-if="courses.length === 0" class="empty-state">
      <el-empty description="还没有购买任何课程">
        <el-button type="primary" @click="$router.push('/course')">去看看课程</el-button>
      </el-empty>
    </div>

    <div v-else class="courses-wrap">
      <!-- 进行中的课程 -->
      <div class="section" v-if="inProgressCourses.length > 0">
        <h3 class="section-title"><i class="el-icon-s-data"></i> 进行中 ({{ inProgressCourses.length }})</h3>
        <div class="course-grid">
          <el-card v-for="item in inProgressCourses" :key="item.id" class="course-card" shadow="hover">
            <div class="course-cover-wrap" @click="goToCourse(item.id)">
              <img :src="getCover(item)" alt="课程封面" class="course-cover" />
              <div class="cover-overlay">
                <el-button type="primary" size="small">查看详情</el-button>
              </div>
              <div class="progress-badge" :style="{ width: (item.progress || 0) + '%' }"></div>
            </div>
            <div class="course-info">
              <h3 class="course-title" @click="goToCourse(item.id)">{{ item.title }}</h3>
              <div class="course-meta">
                <span>{{ item.lessonCount || 0 }}课时</span>
                <el-tag v-if="item.stage" size="mini">{{ item.stage }}</el-tag>
              </div>
              <div class="progress-section">
                <div class="progress-label">
                  <span>学习进度</span>
                  <span class="progress-percent">{{ item.progress || 0 }}%</span>
                </div>
                <el-progress :percentage="item.progress || 0" :stroke-width="8" :color="progressColor(item.progress)" />
              </div>
              <div class="course-stats">
                <span><i class="el-icon-time"></i> {{ formatDuration(item.studyDuration) }}</span>
                <span v-if="item.lastStudyTime"><i class="el-icon-date"></i> {{ formatDate(item.lastStudyTime) }}</span>
              </div>
              <div class="course-actions">
                <el-button type="primary" size="small" @click="continueLearning(item.id)" style="width:100%">
                  {{ item.progress > 0 ? '继续学习' : '开始学习' }}
                </el-button>
              </div>
            </div>
          </el-card>
        </div>
      </div>

      <!-- 已完成的课程 -->
      <div class="section" v-if="completedCourses.length > 0">
        <h3 class="section-title completed-title"><i class="el-icon-circle-check"></i> 已完成 ({{ completedCourses.length }})</h3>
        <div class="course-grid">
          <el-card v-for="item in completedCourses" :key="item.id" class="course-card completed-card" shadow="hover">
            <div class="course-cover-wrap" @click="goToCourse(item.id)">
              <img :src="getCover(item)" alt="课程封面" class="course-cover" />
              <div class="cover-overlay">
                <el-button type="primary" size="small">查看详情</el-button>
              </div>
              <div class="completed-tag"><i class="el-icon-check"></i> 已完成</div>
            </div>
            <div class="course-info">
              <h3 class="course-title" @click="goToCourse(item.id)">{{ item.title }}</h3>
              <div class="course-meta">
                <span>{{ item.lessonCount || 0 }}课时</span>
                <el-tag v-if="item.stage" size="mini">{{ item.stage }}</el-tag>
              </div>
              <div class="course-stats">
                <span><i class="el-icon-time"></i> {{ formatDuration(item.studyDuration) }}</span>
                <span v-if="item.lastStudyTime"><i class="el-icon-date"></i> {{ formatDate(item.lastStudyTime) }}</span>
              </div>
              <div class="course-actions">
                <el-button size="small" @click="continueLearning(item.id)" style="width:100%">复习课程</el-button>
              </div>
            </div>
          </el-card>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'MyCourses',
  data() {
    return {
      courses: [],
      loading: true
    }
  },
  computed: {
    inProgressCourses() {
      return this.courses.filter(c => (c.progress || 0) < 100)
    },
    completedCourses() {
      return this.courses.filter(c => (c.progress || 0) >= 100)
    },
    averageProgress() {
      if (this.courses.length === 0) return 0
      const total = this.courses.reduce((sum, c) => sum + (c.progress || 0), 0)
      return Math.round(total / this.courses.length)
    },
    totalStudyHours() {
      const mins = this.courses.reduce((sum, c) => sum + (c.studyDuration || 0), 0)
      return Math.round(mins / 60 * 10) / 10
    },
    completedCount() {
      return this.completedCourses.length
    }
  },
  created() {
    this.fetchMyCourses()
  },
  methods: {
    async fetchMyCourses() {
      this.loading = true
      try {
        const res = await this.$http.get('/course/user-courses')
        this.courses = Array.isArray(res) ? res : (res?.records || [])
      } catch (e) {
        console.error('获取我的课程失败:', e)
        this.$message.error('获取课程列表失败')
      } finally {
        this.loading = false
      }
    },
    getCover(item) {
      return item.cover || ''
    },
    goToCourse(courseId) {
      this.$router.push(`/course/${courseId}`)
    },
    continueLearning(courseId) {
      this.$router.push(`/course/${courseId}/learning`)
    },
    async requestRefund(item) {
      try {
        await this.$confirm('确定要申请退款吗？', '退款申请', {
          confirmButtonText: '确定退款', cancelButtonText: '取消', type: 'warning'
        })
        await this.$http.post(`/course/refund-request/${item.id}`, {
          reason: '用户申请退款'
        })
        this.$message.success('退款申请已提交，请等待管理员审核')
        this.fetchMyCourses()
      } catch (e) {
        if (e !== 'cancel') this.$message.error('申请退款失败')
      }
    },
    progressColor(progress) {
      if (progress >= 80) return '#67C23A'
      if (progress >= 40) return '#409EFF'
      return '#E6A23C'
    },
    formatDuration(minutes) {
      if (!minutes) return '0分钟'
      const h = Math.floor(minutes / 60)
      const m = minutes % 60
      return h > 0 ? `${h}小时${m}分钟` : `${m}分钟`
    },
    formatDate(dateStr) {
      if (!dateStr) return ''
      try {
        return new Date(dateStr).toLocaleDateString('zh-CN')
      } catch (e) { return dateStr }
    }
  }
}
</script>

<style scoped lang="scss">
.my-courses {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;

  .page-header {
    margin-bottom: 24px;
    h2 { font-size: 24px; color: #303133; margin: 0 0 6px; }
    .subtitle { color: #909399; font-size: 14px; margin: 0; }
  }

  .overview-stats {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 16px;
    margin-bottom: 28px;

    .overview-card {
      display: flex;
      align-items: center;
      gap: 14px;
      padding: 20px;
      background: #fff;
      border-radius: 10px;
      box-shadow: 0 2px 12px rgba(0,0,0,.06);

      .overview-icon {
        width: 48px; height: 48px; border-radius: 10px;
        display: flex; align-items: center; justify-content: center;
        font-size: 22px; flex-shrink: 0;
      }
      .courses-icon { background: #ecf5ff; color: #409eff; }
      .progress-icon { background: #fef0f0; color: #e6a23c; }
      .time-icon { background: #f0f9eb; color: #67c23a; }
      .done-icon { background: #fdf6ec; color: #e6a23c; }

      .overview-body {
        .overview-value { font-size: 24px; font-weight: 700; color: #303133; }
        .overview-label { font-size: 13px; color: #909399; }
      }
    }
  }

  .loading-container { padding: 40px; }

  .empty-state {
    padding: 80px 0; text-align: center;
  }

  .courses-wrap {
    .section {
      margin-bottom: 32px;
      .section-title {
        font-size: 17px; color: #303133; margin: 0 0 16px;
        padding-left: 12px; border-left: 3px solid #409eff;
        i { margin-right: 6px; }
      }
      .completed-title { border-left-color: #67c23a; }
    }
  }

  .course-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
    gap: 20px;
  }

  .course-card {
    transition: all 0.3s;
    &:hover { transform: translateY(-4px); box-shadow: 0 8px 24px rgba(0,0,0,.12); }
    &.completed-card { opacity: .8; }

    .course-cover-wrap {
      position: relative; cursor: pointer; overflow: hidden;
      border-radius: 4px; margin-bottom: 12px;

      .course-cover {
        width: 100%; height: 160px; object-fit: cover;
        background: #f5f7fa; display: block;
      }

      .cover-overlay {
        position: absolute; inset: 0;
        background: rgba(0,0,0,.4);
        display: flex; align-items: center; justify-content: center;
        opacity: 0; transition: opacity .3s;
      }
      &:hover .cover-overlay { opacity: 1; }

      .progress-badge {
        position: absolute; bottom: 0; left: 0; height: 3px;
        background: #67c23a; transition: width .5s;
      }

      .completed-tag {
        position: absolute; top: 10px; right: 10px;
        background: rgba(103,194,58,.9); color: #fff;
        padding: 4px 10px; border-radius: 12px; font-size: 12px;
      }
    }

    .course-info {
      .course-title {
        font-size: 15px; color: #303133; margin: 0 0 8px;
        cursor: pointer; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;
        &:hover { color: #409eff; }
      }

      .course-meta {
        display: flex; gap: 10px; font-size: 13px; color: #909399; margin-bottom: 10px;
      }

      .progress-section {
        margin-bottom: 10px;
        .progress-label {
          display: flex; justify-content: space-between;
          font-size: 13px; color: #606266; margin-bottom: 4px;
          .progress-percent { font-weight: 600; }
        }
      }

      .course-stats {
        display: flex; gap: 16px; font-size: 12px; color: #909399; margin-bottom: 12px;
        i { margin-right: 2px; }
      }
    }
  }
}

@media (max-width: 768px) {
  .overview-stats { grid-template-columns: repeat(2, 1fr); }
}
</style>
