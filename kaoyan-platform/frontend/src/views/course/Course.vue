<template>
  <div class="course">
    <el-card>
      <div class="course-header">
        <h1>精品课程</h1>
      </div>

      <div class="course-filter">
        <el-form :inline="true" :model="filterForm" @submit.native.prevent="search">
          <el-form-item>
            <el-input v-model="searchKeyword" placeholder="搜索课程名称" clearable prefix-icon="el-icon-search"
              style="width:200px" @keyup.enter.native="search" />
          </el-form-item>
          <el-form-item label="学科">
            <el-select v-model="filterForm.subject" placeholder="选择学科" clearable @change="search" style="width:140px">
              <el-option label="全部" value=""></el-option>
              <el-option-group label="公共课">
                <el-option label="数学" value="数学"></el-option>
                <el-option label="英语" value="英语"></el-option>
                <el-option label="政治" value="政治"></el-option>
              </el-option-group>
              <el-option-group label="统考专业课">
                <el-option label="计算机（408）" value="计算机408"></el-option>
                <el-option label="金融学（431）" value="金融学431"></el-option>
                <el-option label="法学" value="法学"></el-option>
                <el-option label="教育学" value="教育学"></el-option>
                <el-option label="心理学" value="心理学"></el-option>
                <el-option label="医学" value="医学"></el-option>
                <el-option label="管理学" value="管理学"></el-option>
              </el-option-group>
            </el-select>
          </el-form-item>
          <el-form-item label="阶段">
            <el-select v-model="filterForm.stage" placeholder="选择阶段" clearable @change="search" style="width:120px">
              <el-option label="全部" value=""></el-option>
              <el-option label="基础夯实" value="基础"></el-option>
              <el-option label="强化提升" value="强化"></el-option>
              <el-option label="冲刺模考" value="冲刺"></el-option>
              <el-option label="真题精讲" value="真题"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="形式">
            <el-select v-model="filterForm.form" placeholder="选择形式" clearable @change="search" style="width:130px">
              <el-option label="全部" value=""></el-option>
              <el-option label="录播视频" value="视频"></el-option>
              <el-option label="直播授课" value="直播"></el-option>
              <el-option label="录播+直播" value="录播+直播"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="search">搜索</el-button>
            <el-button @click="resetFilters">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <div class="course-list">
        <el-row :gutter="20">
          <el-col :span="6" v-for="course in courseList" :key="course.id">
            <el-card class="course-card" shadow="hover" @click.native="goToDetail(course.id)">
              <div class="course-cover-wrap">
                <img :src="course.cover" alt="课程封面" class="course-cover" @error="e => e.target.src=''" />
                <el-tag v-if="isLive(course) && course.liveStatus === 1" class="live-badge live-now" type="danger" size="small" effect="dark">
                  <i class="el-icon-video-camera-solid"></i> 直播中
                </el-tag>
                <el-tag v-else-if="isLive(course) && course.liveStatus === 0" class="live-badge live-upcoming" type="warning" size="small" effect="dark">
                  <i class="el-icon-time"></i> 即将直播
                </el-tag>
                <el-tag v-else-if="isLive(course) && course.liveStatus === 2" class="live-badge live-replay" type="info" size="small" effect="dark">
                  <i class="el-icon-refresh"></i> 可看回放
                </el-tag>
              </div>
              <div class="course-info">
                <h3 class="course-title">{{ course.title }}</h3>
                <div class="course-meta">
                  <span class="lesson">{{ course.lessonCount }}课时</span>
                  <span class="student">{{ course.studentCount }}人学习</span>
                  <el-tag v-if="isLive(course)" size="mini" type="danger" effect="plain">直播</el-tag>
                  <span class="stage">{{ course.stage }}</span>
                </div>
                <p class="course-description">{{ course.description }}</p>
                <div class="course-footer">
                  <div class="course-price">
                    <span v-if="course.price > 0">¥{{ course.price }}</span>
                    <span v-else class="free">免费</span>
                  </div>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>

      <div class="pagination">
        <el-pagination
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          :current-page="currentPage"
          :page-sizes="[10, 20, 50]"
          :page-size="pageSize"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
        />
      </div>
    </el-card>
  </div>
</template>

<script>
export default {
  name: 'Course',
  data() {
    return {
      searchKeyword: '',
      filterForm: {
        stage: '',
        form: '',
        subject: ''
      },
      courseList: [],
      currentPage: 1,
      pageSize: 10,
      total: 0
    }
  },
  created() {
    this.getCourseList()
  },
  methods: {
    isLive(course) {
      return course.form === '直播' || course.form === '录播+直播'
    },
    goToDetail(id) {
      this.$router.push(`/course/${id}`)
    },
    search() {
      this.currentPage = 1
      this.getCourseList()
    },
    resetFilters() {
      this.currentPage = 1
      this.searchKeyword = ''
      this.filterForm.stage = ''
      this.filterForm.form = ''
      this.filterForm.subject = ''
      this.getCourseList()
    },
    handleSizeChange(size) {
      this.pageSize = size
      this.getCourseList()
    },
    handleCurrentChange(current) {
      this.currentPage = current
      this.getCourseList()
    },
    async getCourseList() {
      try {
        const res = await this.$http.get('/course/list', {
          params: {
            page: this.currentPage,
            size: this.pageSize,
            stage: this.filterForm.stage || undefined,
            form: this.filterForm.form || undefined,
            subject: this.filterForm.subject || undefined,
            keyword: this.searchKeyword || undefined
          }
        })
        console.log('Course list response:', res)
        // 拦截器已经返回 res.data，直接使用
        this.courseList = res?.records || []
        this.total = res?.total || 0
      } catch (error) {
        console.error('获取课程列表失败:', error)
        this.$message.error('获取课程列表失败')
      }
    }
  }
}
</script>

<style scoped lang="scss">
.course {
  .course-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;

    h1 {
      font-size: 24px;
      color: #303133;
    }

    .search-input {
      width: 300px;
    }
  }

  .course-filter {
    margin-bottom: 20px;
  }

  .course-list {
    margin-bottom: 30px;

    .course-card {
      cursor: pointer;
      transition: transform 0.3s;
      margin-bottom: 20px;

      &:hover {
        transform: translateY(-5px);
      }

      .course-cover {
        width: 100%;
        height: 150px;
        object-fit: cover;
        border-radius: 4px;
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      }

      .course-info {
        padding: 15px 0;

        .course-title {
          font-size: 16px;
          margin-bottom: 10px;
          color: #303133;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }

        .course-meta {
          display: flex;
          gap: 10px;
          margin-bottom: 10px;
          font-size: 12px;
          color: #909399;

          .stage {
            background: #F0F9EB;
            color: #67C23A;
            padding: 2px 8px;
            border-radius: 4px;
          }
        }

        .course-description {
          font-size: 14px;
          color: #606266;
          margin-bottom: 15px;
          line-height: 1.4;
          overflow: hidden;
          text-overflow: ellipsis;
          display: -webkit-box;
          -webkit-line-clamp: 2;
          -webkit-box-orient: vertical;
        }

        .course-footer {
          .course-price {
            font-size: 18px;
            font-weight: bold;
            color: #F56C6C;

            .free {
              color: #67C23A;
            }
          }
        }
      }

      .course-cover-wrap {
        position: relative;
        .live-badge {
          position: absolute; top: 8px; left: 8px;
          &.live-now { animation: livePulse 1.5s infinite; }
        }
      }
    }
  }

  .pagination {
    text-align: center;
    margin-top: 30px;
  }
}

@keyframes livePulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}
</style>
