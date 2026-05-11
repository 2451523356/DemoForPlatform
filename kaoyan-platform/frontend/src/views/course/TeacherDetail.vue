<template>
  <div class="teacher-detail">
    <!-- Loading -->
    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="8" animated />
    </div>

    <template v-else-if="teacher">
      <!-- Teacher Header -->
      <div class="teacher-header">
        <div class="teacher-header-bg"></div>
        <div class="teacher-header-content">
          <div class="teacher-avatar-wrap">
            <el-avatar :size="100" :src="teacherAvatar" class="teacher-avatar" />
          </div>
          <div class="teacher-info">
            <h1 class="teacher-name">{{ teacher.name || teacher.nickname }}</h1>
            <p class="teacher-bio">{{ teacherBio }}</p>
            <div class="teacher-tags">
              <el-tag size="small" type="warning" v-if="teacher.title">{{ teacher.title }}</el-tag>
              <el-tag size="small" type="info" v-if="teacher.role">{{ teacher.role === 'TEACHER' ? '讲师' : teacher.role }}</el-tag>
            </div>
          </div>
        </div>
      </div>

      <!-- Stats -->
      <div class="teacher-stats">
        <div class="stat-card">
          <div class="stat-icon course-icon">
            <i class="el-icon-reading"></i>
          </div>
          <div class="stat-body">
            <div class="stat-value">{{ filteredCourses.length }}</div>
            <div class="stat-label">开设课程</div>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon student-icon">
            <i class="el-icon-user"></i>
          </div>
          <div class="stat-body">
            <div class="stat-value">{{ totalStudents.toLocaleString() }}</div>
            <div class="stat-label">累计学员</div>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon rating-icon">
            <i class="el-icon-star-on"></i>
          </div>
          <div class="stat-body">
            <div class="stat-value">{{ avgRating }}</div>
            <div class="stat-label">平均评分</div>
          </div>
        </div>
      </div>

      <!-- Courses Section -->
      <div class="teacher-courses-section">
        <h2 class="section-title">
          <i class="el-icon-video-camera"></i> Ta的课程
          <span class="course-count-badge">{{ filteredCourses.length }}</span>
        </h2>

        <el-empty v-if="filteredCourses.length === 0" description="暂无课程" :image-size="120" />

        <el-row :gutter="20" v-else>
          <el-col :span="6" v-for="course in filteredCourses" :key="course.id">
            <el-card class="course-card" shadow="hover" @click.native="goToCourse(course.id)">
              <img :src="course.cover || '/static/images/default-cover.png'" alt="课程封面" class="course-cover" />
              <div class="course-info">
                <h3 class="course-title">{{ course.title }}</h3>
                <div class="course-meta">
                  <el-tag size="mini" type="warning">{{ stageLabel(course.stage) }}</el-tag>
                  <span class="meta-student"><i class="el-icon-user"></i> {{ course.studentCount || 0 }}</span>
                </div>
                <div class="course-footer">
                  <span class="course-price" v-if="course.price > 0">¥{{ course.price }}</span>
                  <span class="course-price free" v-else>免费</span>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>
    </template>

    <!-- Error / Not Found -->
    <div v-else class="not-found">
      <el-empty description="讲师信息不存在" :image-size="160">
        <el-button type="primary" @click="$router.push('/course')">返回课程列表</el-button>
      </el-empty>
    </div>
  </div>
</template>

<script>
export default {
  name: 'TeacherDetail',
  data() {
    return {
      loading: true,
      teacher: null,
      allCourses: [],
      coursesLoading: false
    }
  },
  computed: {
    teacherId() {
      return parseInt(this.$route.params.id) || 0
    },
    teacherAvatar() {
      if (this.teacher?.avatar) return this.teacher.avatar
      return '/static/images/default-avatar.png'
    },
    teacherBio() {
      return this.teacher?.intro || this.teacher?.title || this.teacher?.bio || '暂无简介'
    },
    filteredCourses() {
      return this.allCourses.filter(c => c.teacherId === this.teacherId)
    },
    totalStudents() {
      return this.filteredCourses.reduce((sum, c) => sum + (c.studentCount || 0), 0)
    },
    avgRating() {
      const rated = this.filteredCourses.filter(c => c.rating != null)
      if (rated.length === 0) return '暂无'
      const avg = rated.reduce((sum, c) => sum + c.rating, 0) / rated.length
      return avg.toFixed(1)
    }
  },
  created() {
    this.fetchTeacher()
    this.fetchCourses()
  },
  methods: {
    stageLabel(stage) {
      const m = {
        '基础': '基础夯实',
        '强化': '强化提升',
        '冲刺': '冲刺模考',
        '真题': '真题精讲'
      }
      return m[stage] || stage || ''
    },
    goToCourse(id) {
      this.$router.push(`/course/${id}`)
    },
    async fetchTeacher() {
      try {
        const res = await this.$http.get(`/user/${this.teacherId}`)
        this.teacher = res || {}
      } catch (e) {
        console.error('获取讲师信息失败', e)
        this.teacher = null
      }
    },
    async fetchCourses() {
      this.coursesLoading = true
      try {
        // Fetch a large batch of courses and filter by teacherId client-side
        const res = await this.$http.get('/course/list', {
          params: { page: 1, size: 100 }
        })
        this.allCourses = res?.records || []
      } catch (e) {
        console.error('获取课程列表失败', e)
        this.allCourses = []
      } finally {
        this.coursesLoading = false
        this.loading = false
      }
    }
  }
}
</script>

<style scoped lang="scss">
.teacher-detail {
  max-width: 1100px;
  margin: 0 auto;
  padding: 20px 20px 40px;
}

.loading-container {
  background: #fff;
  border-radius: 10px;
  padding: 40px;
}

// Header
.teacher-header {
  border-radius: 12px;
  overflow: hidden;
  margin-bottom: 24px;
  background: #fff;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  position: relative;

  .teacher-header-bg {
    height: 120px;
    background: linear-gradient(135deg, #1a3a5c 0%, #2d6aa0 100%);
  }

  .teacher-header-content {
    display: flex;
    align-items: flex-end;
    gap: 24px;
    padding: 0 32px 28px;
    margin-top: -50px;
    position: relative;
    z-index: 1;

    .teacher-avatar-wrap {
      flex-shrink: 0;

      .teacher-avatar {
        border: 4px solid #fff;
        box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
        background: #f5f7fa;
      }
    }

    .teacher-info {
      padding-bottom: 4px;

      .teacher-name {
        font-size: 24px;
        font-weight: 700;
        color: #303133;
        margin: 0 0 8px;
      }

      .teacher-bio {
        font-size: 14px;
        color: #606266;
        margin: 0 0 10px;
        line-height: 1.6;
        max-width: 600px;
      }

      .teacher-tags {
        display: flex;
        gap: 8px;
        flex-wrap: wrap;
      }
    }
  }
}

// Stats
.teacher-stats {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
  margin-bottom: 24px;

  .stat-card {
    background: #fff;
    border-radius: 10px;
    padding: 20px 24px;
    display: flex;
    align-items: center;
    gap: 16px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);

    .stat-icon {
      width: 52px;
      height: 52px;
      border-radius: 12px;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 24px;
      flex-shrink: 0;

      &.course-icon {
        background: #ecf5ff;
        color: #409eff;
      }

      &.student-icon {
        background: #fef0f0;
        color: #f56c6c;
      }

      &.rating-icon {
        background: #fdf6ec;
        color: #e6a23c;
      }
    }

    .stat-body {
      .stat-value {
        font-size: 24px;
        font-weight: 700;
        color: #303133;
        line-height: 1.2;
      }

      .stat-label {
        font-size: 13px;
        color: #909399;
        margin-top: 4px;
      }
    }
  }
}

// Courses
.teacher-courses-section {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);

  .section-title {
    font-size: 20px;
    font-weight: 600;
    color: #303133;
    margin: 0 0 20px;
    display: flex;
    align-items: center;
    gap: 8px;

    i {
      color: #409eff;
    }

    .course-count-badge {
      display: inline-flex;
      align-items: center;
      justify-content: center;
      min-width: 24px;
      height: 24px;
      padding: 0 6px;
      border-radius: 12px;
      background: #ecf5ff;
      color: #409eff;
      font-size: 13px;
      font-weight: 600;
      margin-left: 4px;
    }
  }

  .course-card {
    cursor: pointer;
    transition: transform 0.3s;
    margin-bottom: 20px;

    &:hover {
      transform: translateY(-5px);
    }

    ::v-deep .el-card__body {
      padding: 0;
    }

    .course-cover {
      width: 100%;
      height: 150px;
      object-fit: cover;
      border-radius: 4px 4px 0 0;
      background: #f5f7fa;
    }

    .course-info {
      padding: 14px 16px;

      .course-title {
        font-size: 15px;
        font-weight: 600;
        color: #303133;
        margin: 0 0 10px;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }

      .course-meta {
        display: flex;
        align-items: center;
        gap: 10px;
        margin-bottom: 12px;

        .meta-student {
          font-size: 12px;
          color: #909399;
          display: flex;
          align-items: center;
          gap: 3px;
        }
      }

      .course-footer {
        .course-price {
          font-size: 18px;
          font-weight: 700;
          color: #f56c6c;

          &.free {
            color: #67c23a;
          }
        }
      }
    }
  }
}

.not-found {
  padding: 80px 0;
}

@media (max-width: 768px) {
  .teacher-stats {
    grid-template-columns: 1fr;
  }

  .teacher-header {
    .teacher-header-content {
      flex-direction: column;
      align-items: center;
      text-align: center;
      padding: 0 20px 24px;

      .teacher-info .teacher-bio {
        max-width: 100%;
      }
    }
  }
}
</style>
