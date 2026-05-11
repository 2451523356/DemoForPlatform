<template>
  <div class="course-detail">
    <!-- 直播播放器区域 -->
    <div class="live-section" v-if="isLiveCourse && course.liveUrl">
      <div class="live-header">
        <span class="live-dot" v-if="course.liveStatus === 1"></span>
        <el-tag :type="liveStatusTag.type" size="medium" effect="dark">
          <i v-if="course.liveStatus === 1" class="el-icon-video-camera-solid"></i>
          {{ liveStatusTag.text }}
        </el-tag>
        <span v-if="course.liveStatus === 0 && countdownText" class="live-countdown">
          <i class="el-icon-time"></i> 距开播：{{ countdownText }}
        </span>
        <span v-if="course.liveStatus === 1" class="live-viewers">
          <i class="el-icon-user"></i> {{ liveViewerCount }} 人正在观看
        </span>
      </div>
      <div class="live-player-wrapper">
        <!-- 直播预告倒计时覆盖层 -->
        <div v-if="course.liveStatus === 0" class="live-countdown-overlay">
          <div class="countdown-card">
            <div class="countdown-icon"><i class="el-icon-video-camera"></i></div>
            <h3>直播即将开始</h3>
            <div class="countdown-timer">{{ countdownText }}</div>
            <p class="countdown-time">{{ formatDateTime(course.liveStartTime) }}</p>
            <el-button type="danger" round disabled>敬请期待</el-button>
          </div>
        </div>
        <!-- B站直播用iframe -->
        <div v-if="course.livePlatform === 'bilibili' && course.liveStatus !== 0" class="bilibili-container">
          <iframe
            :src="bilibiliEmbedUrl"
            scrolling="no"
            border="0"
            frameborder="no"
            framespacing="0"
            allowfullscreen="true"
            class="bilibili-iframe"
          ></iframe>
        </div>
        <!-- DPlayer 播放器 -->
        <div v-else-if="course.liveStatus !== 0" ref="dplayerContainer" class="dplayer-container"></div>
      </div>
      <div v-if="course.liveStatus === 1" class="live-info-bar">
        <span><i class="el-icon-time"></i> 开始于 {{ formatDateTime(course.liveStartTime) }}</span>
        <span v-if="course.liveEndTime">预计结束 {{ formatDateTime(course.liveEndTime) }}</span>
      </div>
    </div>

    <!-- Hero 概览区 -->
    <div class="course-hero" :class="{ 'has-live': isLiveCourse && course.liveUrl }">
      <div class="course-hero-content">
        <div class="hero-left">
          <h1 class="hero-title">{{ course.title }}</h1>
          <p class="hero-subtitle">{{ course.description }}</p>
          <div class="hero-meta">
            <el-tag size="medium" type="warning">{{ stageLabel }}</el-tag>
            <el-tag size="medium" type="info">{{ course.form }}</el-tag>
            <el-tag size="medium" v-if="course.subject">{{ course.subject }}</el-tag>
            <span><i class="el-icon-video-camera"></i> {{ course.lessonCount || 0 }}课时</span>
            <span><i class="el-icon-time"></i> {{ formatDuration(course.duration) }}</span>
            <span><i class="el-icon-user"></i> {{ course.studentCount || 0 }}人已学</span>
          </div>
          <div class="hero-price-row">
            <span class="hero-price" v-if="course.price > 0">¥{{ course.price }}</span>
            <span class="hero-price free" v-else>免费</span>
            <el-button v-if="!isPurchased && course.price > 0" type="danger" size="large" round @click="buyCourse">
              <i class="el-icon-shopping-cart-2"></i> 立即购买
            </el-button>
            <el-button v-if="!isPurchased && course.price === 0" type="success" size="large" round @click="buyCourse">
              <i class="el-icon-plus"></i> 免费报名
            </el-button>
            <el-button v-if="isPurchased" type="primary" size="large" round @click="startLearning">
              <i class="el-icon-video-play"></i> 继续学习
            </el-button>
            <el-button v-if="isPurchased" type="default" size="large" round @click="showRefundDialog">
              <i class="el-icon-refresh-left"></i> 申请退款
            </el-button>
          </div>
        </div>
        <div class="hero-right" v-if="!(isLiveCourse && course.liveUrl)">
          <img :src="courseCover" alt="课程封面" class="hero-cover" />
        </div>
      </div>
    </div>

    <!-- 讲师信息 -->
    <div class="teacher-section" v-if="course.teacher">
      <h2 class="section-title"><i class="el-icon-user-solid"></i> 讲师介绍</h2>
      <div class="teacher-card" @click="goToTeacher">
        <div class="teacher-avatar">
          <img :src="teacherAvatar" alt="讲师" />
        </div>
        <div class="teacher-body">
          <h3>{{ course.teacher.name || course.teacher.nickname }}</h3>
          <p class="teacher-bio">{{ teacherBio || '点击查看讲师详情' }}</p>
          <div class="teacher-stats">
            <span><i class="el-icon-star-on"></i> 评分 {{ course.rating ? course.rating.toFixed(1) : '暂无' }}</span>
            <span><i class="el-icon-user"></i> {{ course.studentCount || 0 }}学员</span>
          </div>
        </div>
        <i class="el-icon-arrow-right teacher-arrow"></i>
      </div>
    </div>

    <!-- 讲师详情弹窗 -->
    <el-dialog :title="course.teacher?.name || '讲师详情'" :visible.sync="teacherDialogVisible" width="500px">
      <div class="teacher-dialog" v-if="course.teacher">
        <div class="teacher-dialog-header">
          <img :src="teacherAvatar" alt="讲师" class="teacher-dialog-avatar" />
          <div>
            <h3>{{ course.teacher.name || course.teacher.nickname }}</h3>
            <p class="teacher-dialog-subtitle">{{ teacherBio || '暂无简介' }}</p>
          </div>
        </div>
        <el-divider></el-divider>
        <div class="teacher-dialog-stats">
          <div class="td-stat">
            <div class="td-value">{{ course.rating ? course.rating.toFixed(1) : '暂无' }}</div>
            <div class="td-label">课程评分</div>
          </div>
          <div class="td-stat">
            <div class="td-value">{{ course.studentCount || 0 }}</div>
            <div class="td-label">学员数量</div>
          </div>
          <div class="td-stat">
            <div class="td-value">{{ course.lessonCount || 0 }}</div>
            <div class="td-label">课程章节</div>
          </div>
          <div class="td-stat">
            <div class="td-value">{{ formatDuration(course.duration) }}</div>
            <div class="td-label">课程时长</div>
          </div>
        </div>
      </div>
    </el-dialog>

    <!-- 课程详情区 -->
    <div class="info-grid">
      <div class="info-card" v-if="course.highlights">
        <h3><i class="el-icon-star-on"></i> 课程亮点</h3>
        <div class="info-content" v-html="course.highlights"></div>
      </div>
      <div class="info-card" v-if="course.targetAudience">
        <h3><i class="el-icon-user"></i> 适合人群</h3>
        <div class="info-content" v-html="course.targetAudience"></div>
      </div>
      <div class="info-card" v-if="course.prerequisites">
        <h3><i class="el-icon-reading"></i> 前置要求</h3>
        <div class="info-content" v-html="course.prerequisites"></div>
      </div>
      <div class="info-card">
        <h3><i class="el-icon-document"></i> 课程简介</h3>
        <div class="info-content">{{ course.description }}</div>
      </div>
    </div>

    <!-- Tab 切换 -->
    <div class="course-tabs">
      <el-tabs v-model="activeTab">
        <el-tab-pane label="课程大纲" name="outline">
          <div class="outline-section">
            <div class="outline-content" v-html="course.outline || '<p style=color:#909399;text-align:center;padding:40px>暂无大纲信息</p>'"></div>
          </div>
        </el-tab-pane>

        <el-tab-pane label="课程章节" name="chapters">
          <div class="chapters-section">
            <div class="chapters-summary">
              共 <strong>{{ chapters.length }}</strong> 章节
              <template v-if="isPurchased && chapterProgress > 0">
                · 已完成 <strong>{{ completedChapterCount }}</strong> 个
              </template>
              <el-progress v-if="isPurchased" :percentage="chapterProgress" :stroke-width="8" :color="progressColor" style="margin-top:8px" />
            </div>
            <div v-if="chapters.length === 0" style="text-align:center;padding:40px;color:#909399">暂无章节信息</div>
            <div class="chapters-list" v-else>
              <div class="chapter-item" v-for="(chapter, index) in chapters" :key="chapter.id"
                :class="{ 'is-free': chapter.isFree === 1, 'is-locked': !isPurchased && chapter.isFree !== 1, 'is-completed': isChapterCompleted(chapter.id) }">
                <div class="chapter-index">
                  <i v-if="isChapterCompleted(chapter.id)" class="el-icon-circle-check"></i>
                  <span v-else>{{ index + 1 }}</span>
                </div>
                <div class="chapter-body">
                  <div class="chapter-title">
                    {{ chapter.title }}
                    <el-tag v-if="chapter.isFree === 1" size="mini" type="success">免费试看</el-tag>
                    <el-tag v-if="!isPurchased && chapter.isFree !== 1" size="mini" type="warning">付费</el-tag>
                  </div>
                  <div class="chapter-meta">
                    <span v-if="chapter.duration"><i class="el-icon-time"></i> {{ chapter.duration }}分钟</span>
                    <span v-if="chapter.fileName"><i class="el-icon-paperclip"></i> {{ chapter.fileName }}</span>
                    <span v-if="isChapterCompleted(chapter.id)" style="color:#67c23a"><i class="el-icon-check"></i> 已学完</span>
                  </div>
                </div>
                <div class="chapter-actions">
                  <el-button v-if="chapter.fileUrl" size="small" type="info" plain @click.stop="downloadChapterFile(chapter)">
                    <i class="el-icon-download"></i> 资料
                  </el-button>
                  <el-button size="small"
                    :type="isPurchased || chapter.isFree === 1 ? 'primary' : 'warning'"
                    @click.stop="playChapter(chapter)"
                    :disabled="!isPurchased && chapter.isFree !== 1">
                    <i :class="isPurchased || chapter.isFree === 1 ? 'el-icon-video-play' : 'el-icon-lock'"></i>
                    {{ isPurchased ? (isChapterCompleted(chapter.id) ? '复习' : '学习') : (chapter.isFree === 1 ? '试看' : '付费解锁') }}
                  </el-button>
                </div>
              </div>
            </div>
          </div>
        </el-tab-pane>

        <el-tab-pane label="课程评价" name="reviews">
          <div class="reviews-section">
            <div class="add-review" v-if="isLoggedIn">
              <h3>发表评价</h3>
              <el-rate v-model="newReview.rating" :texts="['很差','较差','一般','推荐','力荐']" show-text></el-rate>
              <el-input v-model="newReview.content" type="textarea" :rows="3" placeholder="分享你的学习感受..." maxlength="500" show-word-limit style="margin-top:10px"></el-input>
              <el-button type="primary" @click="submitReview" style="margin-top:10px">提交评价</el-button>
            </div>
            <div class="add-review" v-else>
              <p style="text-align:center;color:#909399">
                请先<router-link to="/login">登录</router-link>后发表评价
              </p>
            </div>
            <div class="reviews-list" v-if="reviews.length > 0">
              <div class="review-card" v-for="review in reviews" :key="review.id">
                <div class="review-header">
                  <img :src="review.userAvatar || '/static/images/default-avatar.png'" class="review-avatar" />
                  <div class="review-user">
                    <span class="username">{{ review.username }}</span>
                    <span class="review-time">{{ formatReviewTime(review.createTime) }}</span>
                  </div>
                  <el-rate v-model="review.rating" disabled text-color="#ff9900"></el-rate>
                </div>
                <p class="review-content">{{ review.content }}</p>
                <div class="review-footer">
                  <el-button type="text" size="small" @click="likeReview(review)">
                    <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2" style="vertical-align:-2px;margin-right:2px"><path d="M14 9V5a3 3 0 0 0-3-3l-4 9v11h11.28a2 2 0 0 0 2-1.7l1.38-9a2 2 0 0 0-2-2.3zM7 22H4a2 2 0 0 1-2-2v-7a2 2 0 0 1 2-2h3"/></svg> {{ review.likeCount || 0 }}
                  </el-button>
                  <el-button v-if="isLoggedIn" type="text" size="small" @click="showReplyInput(review)">
                    <i class="el-icon-chat-line-round"></i> 回复
                  </el-button>
                  <el-button v-if="isLoggedIn && review.userId === currentUserId" type="text" size="small" @click="deleteReview(review.id)">删除</el-button>
                </div>
                <!-- 回复输入框 -->
                <div v-if="replyingTo === review.id" class="reply-input-area">
                  <el-input v-model="replyContent" type="textarea" :rows="2" placeholder="写下你的回复..." maxlength="200" show-word-limit size="small"></el-input>
                  <div class="reply-input-actions">
                    <el-button size="mini" @click="cancelReply">取消</el-button>
                    <el-button size="mini" type="primary" @click="submitReply(review)">回复</el-button>
                  </div>
                </div>
                <!-- 回复列表 -->
                <div v-if="review.replies && review.replies.length > 0" class="reply-list">
                  <div v-for="reply in review.replies" :key="reply.id" class="reply-item">
                    <span class="reply-from">{{ reply.username }}</span>
                    <span class="reply-content">{{ reply.content }}</span>
                    <span class="reply-time">{{ formatReviewTime(reply.createTime) }}</span>
                  </div>
                </div>
              </div>
            </div>
            <el-empty v-else description="暂无评价，成为第一个评价的人吧！" :image-size="80" />
            <div class="pagination-section" v-if="reviewTotal > reviewPageSize">
              <el-pagination background layout="prev, pager, next" :total="reviewTotal"
                :page-size="reviewPageSize" :current-page="reviewCurrentPage" @current-change="handleReviewPageChange" />
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>

    <!-- 支付弹窗 -->
    <el-dialog title="确认支付" :visible.sync="payDialogVisible" width="420px">
      <div class="pay-info">
        <p><strong>课程：</strong>{{ course.title }}</p>
        <p><strong>价格：</strong><span style="color:#F56C6C;font-size:20px;font-weight:700">¥{{ course.price }}</span></p>
      </div>
      <div class="pay-method">
        <p><strong>支付方式：</strong></p>
        <el-radio-group v-model="selectedPayType">
          <el-radio :label="1">支付宝</el-radio>
          <el-radio :label="2">微信支付</el-radio>
        </el-radio-group>
      </div>
      <span slot="footer"><el-button @click="payDialogVisible=false">取消</el-button><el-button type="primary" @click="confirmPay">确认支付 ¥{{ course.price }}</el-button></span>
    </el-dialog>

    <!-- 退款申请弹窗 -->
    <el-dialog title="申请退款" :visible.sync="refundDialogVisible" width="420px">
      <div class="refund-info">
        <p><strong>课程：</strong>{{ course.title }}</p>
        <p><strong>价格：</strong><span style="color:#F56C6C;font-size:20px;font-weight:700">¥{{ course.price }}</span></p>
      </div>
      <el-form label-width="80px">
        <el-form-item label="退款理由">
          <el-input
            v-model="refundReason"
            type="textarea"
            :rows="3"
            placeholder="请说明退款理由（选填）"
            maxlength="200"
            show-word-limit
          ></el-input>
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="refundDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitRefund">提交申请</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: 'CourseDetail',
  data() {
    return {
      activeTab: 'chapters',
      isPurchased: false,
      newReview: { rating: 5, content: '' },
      course: {},
      chapters: [],
      reviews: [],
      reviewCurrentPage: 1,
      reviewPageSize: 10,
      reviewTotal: 0,
      payDialogVisible: false,
      refundDialogVisible: false,
      refundReason: '',
      selectedPayType: 1,
      chapterProgress: 0,
      completedChapterCount: 0,
      completedChapterIds: [],
      dplayer: null,
      likedReviewIds: new Set(),
      teacherDialogVisible: false,
      replyingTo: null,
      replyContent: '',
      countdownText: '',
      countdownTimer: null,
      liveViewerCount: 0,
      viewerTimer: null
    }
  },
  computed: {
    isLoggedIn() { return !!this.$store.state.user.token },
    currentUserId() { return this.$store.state.user.userInfo?.id },
    courseCover() { return this.course.cover || '' },
    isLiveCourse() { return this.course.form === '直播' || this.course.form === '录播+直播' },
    teacherAvatar() {
      return this.course.teacher?.avatar || '/static/images/default-avatar.png'
    },
    teacherBio() {
      return this.course.teacher?.bio || this.course.teacher?.intro || this.course.teacher?.title || ''
    },
    liveStatusTag() {
      const m = { 0: { type: 'warning', text: '直播预告' }, 1: { type: 'danger', text: '直播中' }, 2: { type: 'info', text: '直播回放' } }
      return m[this.course.liveStatus] || { type: 'info', text: '未知' }
    },
    bilibiliEmbedUrl() {
      if (!this.course.liveUrl) return ''
      const url = this.course.liveUrl
      // B站直播间链接转换
      const match = url.match(/live\.bilibili\.com\/(\d+)/)
      if (match) return `https://live.bilibili.com/blackboard/live/${match[1]}.html`
      return url
    },
    stageLabel() {
      const m = { '基础': '基础夯实', '强化': '强化提升', '冲刺': '冲刺模考', '真题': '真题精讲' }
      return m[this.course.stage] || this.course.stage
    },
    progressColor() {
      return [
        { color: '#f56c6c', percentage: 20 },
        { color: '#e6a23c', percentage: 50 },
        { color: '#67c23a', percentage: 100 }
      ]
    }
  },
  created() { this.getCourseDetail() },
  beforeDestroy() {
    if (this.dplayer) { this.dplayer.destroy(); this.dplayer = null }
    if (this.countdownTimer) clearInterval(this.countdownTimer)
    if (this.viewerTimer) clearInterval(this.viewerTimer)
  },
  methods: {
    isChapterCompleted(chapterId) {
      return this.completedChapterIds.includes(chapterId)
    },
    formatDuration(minutes) {
      if (!minutes) return '0小时'
      const h = Math.floor(minutes / 60)
      const m = minutes % 60
      return h > 0 ? (m > 0 ? h + '小时' + m + '分' : h + '小时') : m + '分钟'
    },
    formatDateTime(time) {
      if (!time) return ''
      return new Date(time).toLocaleString('zh-CN')
    },
    formatReviewTime(time) {
      if (!time) return ''
      const d = new Date(time)
      const now = new Date()
      const diff = now - d
      if (diff < 60000) return '刚刚'
      if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前'
      if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前'
      return d.toLocaleDateString('zh-CN')
    },
    async submitReview() {
      if (!this.newReview.content.trim()) { this.$message.warning('请输入评价内容'); return }
      try {
        await this.$http.post('/course/review', { courseId: this.course.id, content: this.newReview.content, rating: this.newReview.rating })
        this.$message.success('评价成功')
        this.newReview.content = ''; this.newReview.rating = 5
        this.getCourseReviews()
      } catch (e) { this.$message.error('评价失败') }
    },
    async likeReview(review) {
      if (!this.isLoggedIn) { this.$message.warning('请先登录'); return }
      if (this.likedReviewIds.has(review.id)) { this.$message.warning('已点过赞'); return }
      try {
        await this.$http.post(`/course/review/like/${review.id}`)
        this.likedReviewIds.add(review.id)
        review.likeCount = (review.likeCount || 0) + 1
        review.liked = true
      } catch (e) { console.error('点赞失败', e) }
    },
    async deleteReview(reviewId) {
      try {
        await this.$confirm('确定删除该评价？', '提示', { type: 'warning' })
        await this.$http.delete(`/course/review/${reviewId}`)
        this.$message.success('已删除')
        this.getCourseReviews()
      } catch (e) { if (e !== 'cancel') console.error(e) }
    },
    async buyCourse() {
      if (!this.isLoggedIn) { this.$message.warning('请先登录'); this.$router.push('/login'); return }
      this.payDialogVisible = true; this.selectedPayType = 1
    },
    async confirmPay() {
      try {
        await this.$http.post('/course/buy', { courseId: this.course.id, payType: this.selectedPayType })
        this.payDialogVisible = false
        this.$message.success('报名成功！')
        await this.checkIfPurchased()
      } catch (e) {
        this.$message.error(e.response?.data?.message || '支付失败')
      }
    },
    showRefundDialog() {
      this.refundReason = ''
      this.refundDialogVisible = true
    },
    async submitRefund() {
      try {
        await this.$http.post(`/course/refund-request/${this.course.id}`, {
          reason: this.refundReason || '无理由'
        })
        this.$message.success('退款申请已提交，请等待管理员审核')
        this.refundDialogVisible = false
      } catch (e) {
        this.$message.error(e.response?.data?.message || '申请失败')
      }
    },
    playChapter(chapter) {
      this.$router.push(`/course/${this.course.id}/learning?chapterId=${chapter.id}`)
    },
    startLearning() { this.$router.push(`/course/${this.course.id}/learning`) },
    downloadChapterFile(chapter) {
      if (!chapter.fileUrl) { this.$message.warning('该章节暂无资料'); return }
      if (!this.isPurchased && chapter.isFree !== 1) { this.$message.warning('请先购买课程'); return }
      const url = chapter.fileUrl.startsWith('http') ? chapter.fileUrl : '/api' + (chapter.fileUrl.startsWith('/') ? '' : '/') + chapter.fileUrl
      const a = document.createElement('a')
      a.href = url
      a.download = chapter.fileName || 'course-material'
      document.body.appendChild(a); a.click(); document.body.removeChild(a)
    },
    async getCourseDetail() {
      try {
        const id = this.$route.params.id
        const res = await this.$http.get(`/course/detail/${id}`)
        this.course = res || {}
        await this.checkIfPurchased()
        await this.getCourseChapters()
        await this.getCourseReviews()
        if (this.isPurchased) await this.loadUserProgress()
        this.$nextTick(() => { this.initLivePlayer() })
        if (this.isLiveCourse) this.startCountdown()
      } catch (e) { this.$message.error('获取课程详情失败') }
    },
    startCountdown() {
      if (this.countdownTimer) clearInterval(this.countdownTimer)
      const update = () => {
        if (!this.course.liveStartTime) return
        const now = new Date().getTime()
        const start = new Date(this.course.liveStartTime).getTime()
        const diff = start - now
        if (diff <= 0) {
          this.countdownText = ''
          if (this.course.liveStatus === 0) {
            this.course.liveStatus = 1
            this.startViewerSimulation()
            this.$nextTick(() => this.initLivePlayer())
          }
          clearInterval(this.countdownTimer)
          return
        }
        const d = Math.floor(diff / 86400000)
        const h = Math.floor((diff % 86400000) / 3600000)
        const m = Math.floor((diff % 3600000) / 60000)
        const s = Math.floor((diff % 60000) / 1000)
        this.countdownText = d > 0 ? `${d}天 ${h}时 ${m}分 ${s}秒` : `${h}时 ${m}分 ${s}秒`
      }
      update()
      this.countdownTimer = setInterval(update, 1000)
      if (this.course.liveStatus === 1) this.startViewerSimulation()
    },
    startViewerSimulation() {
      if (this.viewerTimer) clearInterval(this.viewerTimer)
      const base = (this.course.studentCount || 100) * 0.3
      this.liveViewerCount = Math.floor(base + Math.random() * 50)
      this.viewerTimer = setInterval(() => {
        this.liveViewerCount = Math.floor(base + Math.random() * 80)
      }, 5000)
    },
    async checkIfPurchased() {
      if (!this.course.id) return
      try {
        const res = await this.$http.get(`/course/check-purchase/${this.course.id}`)
        this.isPurchased = res || false
      } catch (e) { this.isPurchased = false }
    },
    async loadUserProgress() {
      try {
        const res = await this.$http.get(`/course/user-progress/${this.course.id}`)
        if (res) {
          this.completedChapterIds = res.completedChapters || []
          this.completedChapterCount = this.completedChapterIds.length
          this.chapterProgress = this.chapters.length > 0
            ? Math.round((this.completedChapterCount / this.chapters.length) * 100)
            : 0
        }
      } catch (e) { /* ignore */ }
    },
    async getCourseChapters() {
      try {
        const id = this.$route.params.id
        const res = await this.$http.get(`/course/chapters/${id}`)
        this.chapters = Array.isArray(res) ? res : []
        // 重新计算进度
        if (this.isPurchased && this.chapters.length > 0) {
          this.chapterProgress = this.completedChapterCount > 0
            ? Math.round((this.completedChapterCount / this.chapters.length) * 100)
            : 0
        }
      } catch (e) { /* ignore */ }
    },
    async getCourseReviews() {
      try {
        const id = this.$route.params.id
        const res = await this.$http.get(`/course/reviews/${id}`, { params: { page: this.reviewCurrentPage, size: this.reviewPageSize } })
        this.reviews = (res?.records || []).map(r => ({ ...r, liked: this.likedReviewIds.has(r.id) }))
        this.reviewTotal = res?.total || 0
      } catch (e) { this.reviews = []; this.reviewTotal = 0 }
    },
    handleReviewPageChange(current) { this.reviewCurrentPage = current; this.getCourseReviews() },
    showTeacherDialog() { this.teacherDialogVisible = true },
    goToTeacher() {
      if (this.course.teacher && this.course.teacher.id) {
        this.$router.push(`/teacher/${this.course.teacher.id}`)
      }
    },
    showReplyInput(review) { this.replyingTo = review.id; this.replyContent = '' },
    cancelReply() { this.replyingTo = null; this.replyContent = '' },
    async submitReply(review) {
      if (!this.replyContent.trim()) { this.$message.warning('请输入回复内容'); return }
      try {
        await this.$http.post('/course/review/reply', {
          courseId: this.course.id,
          parentId: review.id,
          content: this.replyContent
        })
        this.$message.success('回复成功')
        this.cancelReply()
        this.getCourseReviews()
      } catch (e) { this.$message.error('回复失败') }
    },
    initLivePlayer() {
      if (!this.isLiveCourse || !this.course.liveUrl || this.course.livePlatform === 'bilibili') return
      if (!window.DPlayer) { setTimeout(() => this.initLivePlayer(), 500); return }
      const container = this.$refs.dplayerContainer
      if (!container) return
      if (this.dplayer) { this.dplayer.destroy(); this.dplayer = null }
      const liveType = this.course.liveUrl.endsWith('.m3u8') ? 'hls' : 'auto'
      this.dplayer = new window.DPlayer({
        container,
        autoplay: false,
        theme: '#409EFF',
        lang: 'zh-cn',
        video: { url: this.course.liveUrl, type: liveType, live: this.course.liveStatus === 1 },
        danmaku: true
      })
    }
  }
}
</script>

<style scoped lang="scss">
.course-detail { max-width: 1100px; margin: 0 auto; padding: 20px 20px 40px; }

.live-section {
  background: #1a1a2e; border-radius: 12px; overflow: hidden; margin-bottom: 24px;
  .live-header {
    display: flex; justify-content: space-between; align-items: center; padding: 12px 20px;
    .live-dot {
      width: 10px; height: 10px; border-radius: 50%; background: #f56c6c;
      animation: liveDot 1s infinite; margin-right: 8px;
    }
    .live-countdown { color: #e6a23c; font-size: 14px; font-weight: 600; }
    .live-viewers { color: #fff; font-size: 14px; }
  }
  .live-player-wrapper {
    aspect-ratio: 16 / 9; background: #000; position: relative;
    .bilibili-container, .dplayer-container { width: 100%; height: 100%; }
    .bilibili-iframe { width: 100%; height: 100%; border: 0; }
    .live-countdown-overlay {
      position: absolute; inset: 0; background: rgba(0,0,0,.7);
      display: flex; align-items: center; justify-content: center;
      .countdown-card {
        text-align: center; color: #fff;
        .countdown-icon { font-size: 48px; color: #e6a23c; margin-bottom: 12px; }
        h3 { font-size: 20px; margin: 0 0 12px 0; }
        .countdown-timer { font-size: 36px; font-weight: bold; color: #e6a23c; font-variant-numeric: tabular-nums; }
        .countdown-time { font-size: 14px; color: #aaa; margin: 8px 0 16px 0; }
      }
    }
  }
  .live-info-bar {
    padding: 8px 20px; background: rgba(255,255,255,.05); color: #aaa; font-size: 13px;
    display: flex; gap: 24px;
  }
}

@keyframes liveDot {
  0%, 100% { opacity: 1; box-shadow: 0 0 4px #f56c6c; }
  50% { opacity: 0.3; box-shadow: 0 0 8px #f56c6c; }
}

.course-hero {
  border-radius: 12px; overflow: hidden; margin-bottom: 24px;
  background: linear-gradient(135deg, #1a3a5c 0%, #2d6aa0 100%);
  &.has-live { border-radius: 0 0 12px 12px; margin-top: -4px; }
  .course-hero-content {
    display: flex; gap: 40px; padding: 36px;
    .hero-left {
      flex: 1; color: #fff;
      .hero-title { font-size: 26px; font-weight: 700; margin: 0 0 10px; line-height: 1.3; }
      .hero-subtitle { font-size: 14px; opacity: .85; margin: 0 0 18px; line-height: 1.6; }
      .hero-meta {
        display: flex; flex-wrap: wrap; gap: 14px; align-items: center; margin-bottom: 22px;
        font-size: 13px; opacity: .9;
        span { display: flex; align-items: center; gap: 4px; }
      }
      .hero-price-row {
        display: flex; align-items: center; gap: 18px;
        .hero-price { font-size: 30px; font-weight: 800; &.free { color: #67c23a; } }
      }
    }
    .hero-right {
      flex-shrink: 0;
      .hero-cover { width: 300px; height: 190px; object-fit: cover; border-radius: 8px; box-shadow: 0 8px 30px rgba(0,0,0,.3); }
    }
  }
}

.teacher-section {
  background: #fff; border-radius: 10px; padding: 24px; margin-bottom: 24px;
  box-shadow: 0 2px 12px rgba(0,0,0,.06);
  .section-title { font-size: 18px; margin: 0 0 16px; i { color: #409eff; margin-right: 6px; } }
  .teacher-card { display: flex; gap: 20px; align-items: center;
    .teacher-avatar img { width: 80px; height: 80px; border-radius: 50%; object-fit: cover; background: #f5f7fa; }
    .teacher-body {
      h3 { margin: 0 0 8px; font-size: 18px; }
      .teacher-bio { color: #606266; margin: 0 0 10px; line-height: 1.6; font-size: 14px; }
      .teacher-stats { display: flex; gap: 20px; font-size: 13px; color: #909399; }
    }
  }
}

.info-grid {
  display: grid; grid-template-columns: 1fr 1fr; gap: 20px; margin-bottom: 24px;
  .info-card {
    background: #fff; border-radius: 10px; padding: 24px; box-shadow: 0 2px 12px rgba(0,0,0,.06);
    h3 { font-size: 16px; margin: 0 0 14px; font-weight: 600; i { color: #409eff; margin-right: 6px; } }
    .info-content { font-size: 14px; color: #606266; line-height: 1.8; }
  }
}

.course-tabs {
  background: #fff; border-radius: 10px; padding: 24px; box-shadow: 0 2px 12px rgba(0,0,0,.06);
}

.outline-section {
  .outline-content { line-height: 1.9; color: #606266; font-size: 14px; white-space: pre-line; }
}

.chapters-section {
  .chapters-summary { margin-bottom: 18px; padding: 12px 16px; background: #f5f7fa; border-radius: 8px; font-size: 14px; color: #606266; }
  .chapters-list {
    .chapter-item {
      display: flex; align-items: center; gap: 14px; padding: 14px 12px;
      border-bottom: 1px solid #f0f0f0; transition: all .2s;
      &:hover { background: #fafbfc; }
      &.is-locked { opacity: .55; }
      &.is-completed .chapter-index { background: #67c23a; color: #fff; }
      .chapter-index {
        width: 36px; height: 36px; border-radius: 50%; background: #ecf5ff; color: #409eff;
        display: flex; align-items: center; justify-content: center; font-weight: 600; font-size: 14px; flex-shrink: 0;
      }
      .chapter-body { flex: 1; min-width: 0;
        .chapter-title { font-size: 15px; font-weight: 500; margin-bottom: 4px; display: flex; align-items: center; gap: 8px; }
        .chapter-meta { font-size: 12px; color: #909399; display: flex; gap: 14px; flex-wrap: wrap; align-items: center; }
      }
      .chapter-actions { display: flex; gap: 8px; flex-shrink: 0; }
    }
  }
}

.reviews-section {
  .add-review {
    margin-bottom: 22px; padding: 20px; background: #f5f7fa; border-radius: 8px;
    h3 { margin: 0 0 10px; font-size: 16px; }
  }
  .reviews-list .review-card {
    padding: 16px 0; border-bottom: 1px solid #f0f0f0;
    &:last-child { border-bottom: none; }
    .review-header { display: flex; align-items: center; gap: 12px; margin-bottom: 10px;
      .review-avatar { width: 42px; height: 42px; border-radius: 50%; object-fit: cover; }
      .review-user { flex: 1; .username { font-weight: 600; font-size: 14px; display: block; } .review-time { font-size: 12px; color: #c0c4cc; } }
    }
    .review-content { font-size: 14px; color: #606266; line-height: 1.6; margin: 0 0 8px; }
    .review-footer { display: flex; gap: 12px; margin-bottom: 6px; }
    .reply-input-area {
      margin: 8px 0 8px 50px;
      .reply-input-actions { display: flex; gap: 8px; justify-content: flex-end; margin-top: 6px; }
    }
    .reply-list {
      margin-left: 50px; margin-top: 6px;
      .reply-item {
        padding: 8px 12px; background: #f9f9f9; border-radius: 6px; margin-bottom: 6px; font-size: 13px;
        .reply-from { color: #409eff; font-weight: 500; margin-right: 8px; }
        .reply-content { color: #606266; }
        .reply-time { color: #c0c4cc; font-size: 11px; margin-left: 12px; }
      }
    }
  }
}

.pagination-section { text-align: center; margin-top: 20px; }
.pay-info p, .pay-method p { margin: 0 0 10px; }

.teacher-arrow { font-size: 18px; color: #c0c4cc; flex-shrink: 0; }
.teacher-card { cursor: pointer; }
.teacher-dialog {
  .teacher-dialog-header {
    display: flex; align-items: center; gap: 16px;
    .teacher-dialog-avatar { width: 72px; height: 72px; border-radius: 50%; object-fit: cover; }
    h3 { margin: 0 0 6px; font-size: 18px; }
    .teacher-dialog-subtitle { color: #606266; font-size: 14px; line-height: 1.6; margin: 0; }
  }
  .teacher-dialog-stats {
    display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; text-align: center;
    .td-stat {
      .td-value { font-size: 22px; font-weight: 700; color: #409eff; }
      .td-label { font-size: 12px; color: #909399; margin-top: 4px; }
    }
  }
}

@media (max-width: 768px) {
  .course-hero .course-hero-content { flex-direction: column; padding: 20px;
    .hero-right .hero-cover { width: 100%; height: auto; }
  }
  .info-grid { grid-template-columns: 1fr; }
  .teacher-card { flex-direction: column; text-align: center; }
}
</style>
