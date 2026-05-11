<template>
  <div class="resource-detail">
    <div class="detail-container">
      <!-- 面包屑 -->
      <div class="breadcrumb">
        <el-breadcrumb separator=">">
          <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
          <el-breadcrumb-item :to="{ path: '/resource' }">学习资源</el-breadcrumb-item>
          <el-breadcrumb-item>{{ resource.title }}</el-breadcrumb-item>
        </el-breadcrumb>
      </div>

      <!-- 资源信息卡片 -->
      <div class="resource-main">
        <div class="resource-file-area">
          <div class="file-preview" :class="getFileIconClass(resource)">
            <i :class="getFileIcon(resource)"></i>
            <span class="file-ext">{{ getFileExtension(resource) }}</span>
          </div>
        </div>
        <div class="resource-detail-info">
          <h1>{{ resource.title }}</h1>
          <div class="meta-row">
            <el-tag size="small" type="warning">{{ resource.category }}</el-tag>
            <el-tag size="small" type="info">{{ resource.subject }}</el-tag>
            <span class="meta-item"><i class="el-icon-document"></i> {{ formatFileSize(resource.fileSize) }}</span>
            <span class="meta-item"><i class="el-icon-download"></i> {{ resource.downloadCount }} 次下载</span>
            <span class="meta-item"><i class="el-icon-view"></i> {{ resource.viewCount }} 次浏览</span>
          </div>
          <p class="description">{{ resource.description || '暂无描述' }}</p>
          <div class="rating-row">
            <el-rate
              v-model="resource.rating"
              disabled
              show-score
              text-color="#ff9900"
              :score-template="resource.rating ? resource.rating.toFixed(1) : '0.0'" />
            <span class="rating-count">({{ resource.ratingCount || 0 }}人评价)</span>
          </div>
          <div class="action-row">
            <el-button type="primary" size="medium" @click="downloadResource" class="btn-download">
              <i class="el-icon-download"></i> 下载资源
              <span v-if="resource.points > 0" class="points-tag">{{ resource.points }}积分</span>
            </el-button>
            <el-button size="medium" @click="previewResource" v-if="canPreview">
              <i class="el-icon-view"></i> 预览
            </el-button>
          </div>
        </div>
      </div>

      <!-- 评论区 -->
      <div class="resource-content">
        <el-tabs v-model="activeTab">
          <el-tab-pane label="资源评论" name="comments">
            <div class="comment-section">
              <div class="add-comment">
                <h3>发表评论</h3>
                <el-rate v-model="newComment.rating" :texts="['很差', '较差', '一般', '推荐', '力荐']" show-text></el-rate>
                <el-input
                  v-model="newComment.content"
                  type="textarea"
                  :rows="4"
                  placeholder="请输入评论内容"
                  maxlength="500"
                  show-word-limit
                ></el-input>
                <el-button type="primary" @click="submitComment" :disabled="!isLoggedIn" style="margin-top:12px">提交评论</el-button>
              </div>

              <div class="comment-list">
                <div v-if="comments.length === 0" class="empty-comments">
                  <el-empty description="暂无评论，来发表第一条评论吧" />
                </div>
                <el-card v-for="comment in comments" :key="comment.id" class="comment-card">
                  <div class="comment-header">
                    <div class="user-info">
                      <el-avatar :size="40" :src="comment.userAvatar"></el-avatar>
                      <div class="user-details">
                        <span class="username">{{ comment.username }}</span>
                        <span class="time">{{ formatTime(comment.createTime) }}</span>
                      </div>
                    </div>
                    <el-rate v-model="comment.rating" disabled show-score text-color="#ff9900"
                      :score-template="comment.rating ? comment.rating.toFixed(1) : '0.0'" />
                  </div>
                  <p class="comment-content">{{ comment.content }}</p>
                  <div class="comment-footer">
                    <el-button type="text" size="small" @click="likeComment(comment.id)">
                      <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2" style="vertical-align:-2px;margin-right:2px"><path d="M14 9V5a3 3 0 0 0-3-3l-4 9v11h11.28a2 2 0 0 0 2-1.7l1.38-9a2 2 0 0 0-2-2.3zM7 22H4a2 2 0 0 1-2-2v-7a2 2 0 0 1 2-2h3"/></svg> {{ comment.likeCount }}
                    </el-button>
                    <el-button v-if="isLoggedIn && comment.userId !== currentUserId" type="text" size="small" @click="showReplyDialog(comment)">
                      <i class="el-icon-chat-line-round"></i> 回复
                    </el-button>
                    <el-button v-if="isLoggedIn && comment.userId === currentUserId" type="text" size="small" @click="deleteComment(comment.id)">
                      删除
                    </el-button>
                  </div>

                  <!-- 回复列表 -->
                  <div v-if="comment.replies && comment.replies.length > 0" class="reply-list">
                    <div v-for="reply in comment.replies" :key="reply.id" class="reply-item">
                      <div class="reply-header">
                        <div class="user-info">
                          <el-avatar :size="24" :src="reply.userAvatar"></el-avatar>
                          <div class="user-details">
                            <span class="username">{{ reply.username }}</span>
                            <span class="time">{{ formatTime(reply.createTime) }}</span>
                          </div>
                        </div>
                      </div>
                      <div class="reply-content">
                        <span class="reply-to">回复 @{{ comment.username }}：</span>
                        {{ reply.content }}
                      </div>
                      <div class="reply-footer">
                        <el-button type="text" size="small" @click="likeReply(reply.id)">
                          <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2" style="vertical-align:-2px;margin-right:2px"><path d="M14 9V5a3 3 0 0 0-3-3l-4 9v11h11.28a2 2 0 0 0 2-1.7l1.38-9a2 2 0 0 0-2-2.3zM7 22H4a2 2 0 0 1-2-2v-7a2 2 0 0 1 2-2h3"/></svg> {{ reply.likeCount }}
                        </el-button>
                        <el-button v-if="isLoggedIn && reply.userId === currentUserId" type="text" size="small" @click="deleteReply(reply.id)">
                          删除
                        </el-button>
                        <el-button v-if="isLoggedIn && reply.userId !== currentUserId" type="text" size="small" @click="showReplyDialog(reply)">
                          回复
                        </el-button>
                      </div>
                    </div>
                  </div>
                </el-card>
              </div>

              <div class="pagination" v-if="total > pageSize">
                <el-pagination
                  @current-change="handleCurrentChange"
                  :current-page="currentPage"
                  :page-size="pageSize"
                  layout="prev, pager, next"
                  :total="total"
                />
              </div>
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>

      <!-- 相关资源推荐 -->
      <div class="related-resources" v-if="relatedResources.length > 0">
        <h3>相关资源推荐</h3>
        <div class="related-grid">
          <div class="related-card" v-for="item in relatedResources" :key="item.id" @click="$router.push(`/resource/${item.id}`)">
            <div class="related-icon" :class="getFileIconClass(item)">
              <i :class="getFileIcon(item)"></i>
            </div>
            <div class="related-body">
              <span class="related-title">{{ item.title }}</span>
              <span class="related-meta">
                {{ item.category }} · {{ formatFileSize(item.fileSize) }}
                <span v-if="item.points > 0" class="points"> · {{ item.points }}积分</span>
                <span v-else class="free"> · 免费</span>
              </span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 回复对话框 -->
    <el-dialog title="回复评论" :visible.sync="replyDialogVisible" width="500px">
      <div class="reply-dialog">
        <p class="comment-preview">回复: {{ currentComment?.content }}</p>
        <el-input
          v-model="replyContent"
          type="textarea"
          :rows="4"
          placeholder="请输入回复内容"
          maxlength="200"
          show-word-limit
        ></el-input>
      </div>
      <div slot="footer">
        <el-button @click="replyDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitReply" :disabled="!isLoggedIn">提交回复</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { mapGetters } from 'vuex'

export default {
  name: 'ResourceDetail',
  data() {
    return {
      activeTab: 'comments',
      resource: {
        id: 0,
        title: '',
        description: '',
        fileUrl: '',
        fileSize: 0,
        fileType: '',
        category: '',
        subject: '',
        downloadCount: 0,
        viewCount: 0,
        rating: 0,
        ratingCount: 0,
        points: 0
      },
      comments: [],
      currentPage: 1,
      pageSize: 10,
      total: 0,
      newComment: {
        content: '',
        rating: 5
      },
      replyDialogVisible: false,
      currentComment: null,
      replyContent: '',
      likedComments: new Set(),
      likedReplies: new Set(),
      relatedResources: []
    }
  },
  computed: {
    ...mapGetters('user', ['isLoggedIn', 'userInfo']),
    currentUserId() {
      return this.userInfo ? this.userInfo.id : null
    },
    canPreview() {
      const url = this.resource.fileUrl || ''
      // 外部URL一律支持预览
      if (url.startsWith('http')) return true
      if (!url) return false
      const ext = this.getFileExtension(this.resource)
      if (!ext) {
        // 根据 fileType 判断
        const ft = (this.resource.fileType || '').toLowerCase()
        return ['pdf', 'jpg', 'jpeg', 'png', 'gif', 'mp4'].includes(ft)
      }
      return ['pdf', 'jpg', 'jpeg', 'png', 'gif', 'mp4'].includes(ext)
    },
    fileTypeDisplay() {
      const ext = this.getFileExtension(this.resource)
      if (!ext) return '未知'
      return ext.toUpperCase()
    }
  },
  created() {
    this.getResourceDetail()
    this.getComments()
  },
  watch: {
    '$route.params.id'() {
      this.getResourceDetail()
      this.getComments()
      this.currentPage = 1
    }
  },
  methods: {
    getFileIcon(item) {
      const ext = this.getFileExtension(item)
      const iconMap = {
        pdf: 'el-icon-document',
        doc: 'el-icon-document', docx: 'el-icon-document',
        xls: 'el-icon-s-data', xlsx: 'el-icon-s-data',
        ppt: 'el-icon-present', pptx: 'el-icon-present',
        zip: 'el-icon-folder-opened', rar: 'el-icon-folder-opened',
        mp4: 'el-icon-video-play',
        txt: 'el-icon-tickets'
      }
      return iconMap[ext] || 'el-icon-document'
    },
    getFileIconClass(item) {
      const ext = this.getFileExtension(item)
      if (ext === 'pdf') return 'icon-pdf'
      if (['zip', 'rar'].includes(ext)) return 'icon-archive'
      if (ext === 'mp4') return 'icon-video'
      if (['doc', 'docx', 'xls', 'xlsx', 'ppt', 'pptx'].includes(ext)) return 'icon-office'
      return 'icon-other'
    },
    getFileExtension(item) {
      if (item.fileUrl) {
        const parts = item.fileUrl.split('.')
        if (parts.length > 1) return parts[parts.length - 1].toLowerCase()
      }
      if (item.fileType) return item.fileType.toLowerCase()
      return ''
    },
    formatFileSize(size) {
      if (!size) return '0 B'
      const units = ['B', 'KB', 'MB', 'GB']
      let index = 0
      while (size >= 1024 && index < units.length - 1) {
        size /= 1024
        index++
      }
      return size.toFixed(2) + ' ' + units[index]
    },
    formatTime(time) {
      if (!time) return ''
      return new Date(time).toLocaleString('zh-CN')
    },
    async getResourceDetail() {
      try {
        const id = this.$route.params.id
        const res = await this.$http.get(`/resource/detail/${id}`)
        this.resource = res || {}
        // 加载相关资源
        if (this.resource.subject || this.resource.category) {
          this.loadRelatedResources()
        }
      } catch (error) {
        console.error('获取资源详情失败:', error)
        this.$message.error('获取资源详情失败')
      }
    },
    async loadRelatedResources() {
      try {
        const res = await this.$http.get('/resource/list', {
          params: {
            page: 1,
            size: 6,
            subject: this.resource.subject,
            category: this.resource.category
          }
        })
        let records = res?.records || []
        this.relatedResources = records.filter(r => r.id !== this.resource.id).slice(0, 4)
      } catch (e) {
        console.error('加载相关资源失败:', e)
      }
    },
    async getComments() {
      try {
        const id = this.$route.params.id
        const res = await this.$http.get(`/resource-comment/list/${id}`, {
          params: { page: this.currentPage, size: this.pageSize }
        })
        this.comments = res?.records || []
        this.total = res?.total || 0
      } catch (error) {
        console.error('获取评论失败:', error)
      }
    },
    async submitComment() {
      if (!this.isLoggedIn) {
        this.$message.warning('请先登录')
        this.$router.push('/login')
        return
      }
      if (!this.newComment.content.trim()) {
        this.$message.warning('请输入评论内容')
        return
      }
      try {
        await this.$http.post('/resource-comment/add', {
          resourceId: this.resource.id,
          content: this.newComment.content,
          rating: this.newComment.rating
        })
        this.$message.success('评论成功')
        this.newComment.content = ''
        this.newComment.rating = 5
        setTimeout(async () => {
          await this.getResourceDetail()
          await this.getComments()
        }, 1000)
      } catch (error) {
        console.error('评论失败:', error)
        this.$message.error('评论失败，请稍后重试')
      }
    },
    async deleteComment(commentId) {
      try {
        await this.$confirm('确定要删除这条评论吗？', '提示', {
          confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning'
        })
        await this.$http.delete(`/resource-comment/delete/${commentId}`)
        this.$message.success('删除成功')
        setTimeout(async () => {
          await this.getResourceDetail()
          await this.getComments()
        }, 1000)
      } catch (error) {
        if (error !== 'cancel') {
          console.error('删除评论失败:', error)
          this.$message.error('删除失败，请稍后重试')
        }
      }
    },
    async likeComment(commentId) {
      if (this.likedComments.has(commentId)) {
        this.$message.warning('您已经点过赞了')
        return
      }
      try {
        await this.$http.post(`/resource-comment/like/${commentId}`)
        this.likedComments.add(commentId)
        await this.getComments()
      } catch (error) {
        console.error('点赞失败:', error)
      }
    },
    showReplyDialog(comment) {
      if (!this.isLoggedIn) {
        this.$message.warning('请先登录')
        this.$router.push('/login')
        return
      }
      this.currentComment = comment
      this.replyContent = ''
      this.replyDialogVisible = true
    },
    async submitReply() {
      if (!this.replyContent.trim()) {
        this.$message.warning('请输入回复内容')
        return
      }
      try {
        await this.$http.post(`/resource-comment/reply/${this.currentComment.id}`, {
          resourceId: this.resource.id,
          content: this.replyContent
        })
        this.$message.success('回复成功')
        this.replyDialogVisible = false
        await this.getComments()
      } catch (error) {
        console.error('回复失败:', error)
        this.$message.error('回复失败，请稍后重试')
      }
    },
    async likeReply(replyId) {
      if (this.likedReplies.has(replyId)) {
        this.$message.warning('您已经点过赞了')
        return
      }
      try {
        await this.$http.post(`/resource-comment/like-reply/${replyId}`)
        this.likedReplies.add(replyId)
        await this.getComments()
      } catch (error) {
        console.error('点赞回复失败:', error)
      }
    },
    async deleteReply(replyId) {
      try {
        await this.$confirm('确定要删除这条回复吗？', '提示', {
          confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning'
        })
        await this.$http.delete(`/resource-comment/delete-reply/${replyId}`)
        this.$message.success('删除成功')
        await this.getComments()
      } catch (error) {
        if (error !== 'cancel') {
          console.error('删除回复失败:', error)
          this.$message.error('删除失败，请稍后重试')
        }
      }
    },
    hasDownloadedBefore() {
      const downloads = JSON.parse(localStorage.getItem('downloadedResources') || '[]')
      return downloads.includes(this.resource.id)
    },
    async downloadResource() {
      if (!this.isLoggedIn) {
        this.$message.warning('请先登录后再下载资源')
        this.$router.push('/login')
        return
      }
      const alreadyDownloaded = this.hasDownloadedBefore()
      try {
        if (alreadyDownloaded) {
          await this.$confirm('您已下载过该资源，无需消耗积分，确定要重新下载吗？', '提示', {
            confirmButtonText: '确定', cancelButtonText: '取消', type: 'info'
          })
        } else if (this.resource.points > 0) {
          await this.$confirm(`确定要下载该资源吗？需要消耗 ${this.resource.points} 积分。`, '提示', {
            confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning'
          })
        } else {
          await this.$confirm('确定要下载该资源吗？（免费资源，无需积分）', '提示', {
            confirmButtonText: '确定', cancelButtonText: '取消', type: 'info'
          })
        }
      } catch (e) {
        return
      }
      try {
        // 统一通过后端代理下载（支持外部URL和本地文件）
        const token = this.$store.state.user.token
        const resp = await fetch(`/api/resource/download/${this.resource.id}`, {
          headers: { 'Authorization': `Bearer ${token}` }
        })
        if (resp.status === 403) {
          this.$message.error('积分不足，无法下载资源')
          return
        }
        if (!resp.ok) {
          this.$message.error('文件不存在或下载失败')
          return
        }
        const blob = await resp.blob()
        const url = window.URL.createObjectURL(blob)
        const a = document.createElement('a')
        a.href = url
        a.download = this.resource.title || 'resource'
        document.body.appendChild(a)
        a.click()
        window.URL.revokeObjectURL(url)
        document.body.removeChild(a)
        this.$message.success('开始下载')
        this.recordDownload()
      } catch (error) {
        console.error('下载资源失败:', error)
        this.$message.error('下载失败，请稍后重试')
      }
    },
    recordDownload() {
      const downloads = JSON.parse(localStorage.getItem('downloadedResources') || '[]')
      if (!downloads.includes(this.resource.id)) {
        downloads.push(this.resource.id)
        localStorage.setItem('downloadedResources', JSON.stringify(downloads))
      }
      setTimeout(async () => {
        await this.getResourceDetail()
        try {
          const statsRes = await this.$http.get('/user/stats')
          if (statsRes) {
            this.$store.dispatch('user/updateUserInfo', { ...this.userInfo, points: statsRes.points })
          }
        } catch (e) { /* ignore */ }
      }, 1000)
    },
    previewResource() {
      if (!this.isLoggedIn) {
        this.$message.warning('请先登录后再预览资源')
        this.$router.push('/login')
        return
      }
      const fileUrl = this.resource.fileUrl || ''
      // 外部URL直接打开（如 sample PDF）
      if (fileUrl.startsWith('http')) {
        window.open(fileUrl, '_blank')
        return
      }
      // 本地文件通过后端代理预览
      if (!this.resource.id) {
        this.$message.error('资源ID不存在，无法预览')
        return
      }
      this.$http.get(`/resource/preview/${this.resource.id}`, { responseType: 'blob' }).then(res => {
        const url = URL.createObjectURL(res)
        window.open(url, '_blank')
      }).catch(() => {
        this.$message.error('预览失败')
      })
    },
    handleCurrentChange(current) {
      this.currentPage = current
      this.getComments()
    }
  }
}
</script>

<style scoped lang="scss">
.detail-container {
  max-width: 900px;
  margin: 0 auto;
  padding: 20px;
}

.breadcrumb {
  margin-bottom: 20px;
  padding: 12px 16px;
  background: #f5f7fa;
  border-radius: 6px;
}

.resource-main {
  display: flex;
  gap: 30px;
  background: #fff;
  border-radius: 12px;
  padding: 32px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  margin-bottom: 24px;

  .resource-file-area {
    flex-shrink: 0;

    .file-preview {
      width: 120px;
      height: 140px;
      border-radius: 12px;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      gap: 8px;

      i {
        font-size: 48px;
      }

      .file-ext {
        font-size: 13px;
        font-weight: 600;
        text-transform: uppercase;
      }

      &.icon-pdf { background: #fef0f0; color: #f56c6c; i { color: #f56c6c; } }
      &.icon-archive { background: #fdf6ec; color: #e6a23c; i { color: #e6a23c; } }
      &.icon-video { background: #ecf5ff; color: #409eff; i { color: #409eff; } }
      &.icon-office { background: #f0f9eb; color: #67c23a; i { color: #67c23a; } }
      &.icon-other { background: #f4f4f5; color: #909399; i { color: #909399; } }
    }
  }

  .resource-detail-info {
    flex: 1;
    min-width: 0;

    h1 {
      font-size: 22px;
      color: #303133;
      margin: 0 0 14px 0;
      line-height: 1.4;
    }

    .meta-row {
      display: flex;
      flex-wrap: wrap;
      align-items: center;
      gap: 12px;
      margin-bottom: 16px;
      font-size: 13px;

      .meta-item {
        color: #909399;
        display: flex;
        align-items: center;
        gap: 4px;
      }
    }

    .description {
      font-size: 14px;
      color: #606266;
      line-height: 1.7;
      margin-bottom: 16px;
    }

    .rating-row {
      display: flex;
      align-items: center;
      gap: 8px;
      margin-bottom: 20px;

      .rating-count {
        color: #909399;
        font-size: 13px;
      }
    }

    .action-row {
      display: flex;
      gap: 12px;

      .btn-download {
        .points-tag {
          margin-left: 6px;
          background: rgba(255, 255, 255, 0.25);
          padding: 2px 8px;
          border-radius: 10px;
          font-size: 12px;
        }
      }
    }
  }
}

.resource-content {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  padding: 24px;
  margin-bottom: 24px;

  .comment-section {
    .add-comment {
      margin-bottom: 28px;
      padding: 20px;
      background: #f5f7fa;
      border-radius: 8px;

      h3 {
        font-size: 16px;
        margin: 0 0 12px 0;
        color: #303133;
      }
    }

    .comment-list {
      .empty-comments {
        text-align: center;
        padding: 40px 0;
      }

      .comment-card {
        margin-bottom: 14px;

        .comment-header {
          display: flex;
          justify-content: space-between;
          align-items: center;
          margin-bottom: 12px;

          .user-info {
            display: flex;
            align-items: center;
            gap: 10px;

            .user-details {
              display: flex;
              flex-direction: column;
              gap: 3px;

              .username { font-size: 14px; font-weight: 600; color: #303133; }
              .time { font-size: 12px; color: #909399; }
            }
          }
        }

        .comment-content {
          margin: 0 0 12px 0;
          font-size: 14px;
          color: #606266;
          line-height: 1.6;
        }

        .comment-footer {
          display: flex;
          gap: 12px;
        }

        .reply-list {
          margin-top: 14px;
          margin-left: 50px;

          .reply-item {
            margin-bottom: 12px;
            padding: 10px 14px;
            background: #f9f9f9;
            border-radius: 8px;

            .reply-header {
              margin-bottom: 4px;

              .user-info {
                display: flex;
                align-items: center;
                gap: 8px;

                .user-details {
                  .username { font-size: 13px; font-weight: 500; color: #303133; }
                  .time { font-size: 11px; color: #909399; }
                }
              }
            }

            .reply-content {
              margin: 4px 0 8px;
              font-size: 14px;
              color: #303133;
              line-height: 1.5;

              .reply-to { color: #409eff; font-weight: 500; margin-right: 4px; }
            }

            .reply-footer {
              display: flex;
              gap: 8px;
            }
          }
        }
      }
    }

    .pagination {
      text-align: center;
      margin-top: 24px;
    }
  }
}

.related-resources {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  padding: 24px;

  h3 {
    font-size: 17px;
    color: #303133;
    margin: 0 0 16px;
    padding-left: 12px;
    border-left: 3px solid #409EFF;
  }

  .related-grid {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;

    .related-card {
      display: flex;
      align-items: center;
      gap: 12px;
      padding: 14px 16px;
      border: 1px solid #ebeef5;
      border-radius: 8px;
      cursor: pointer;
      transition: all 0.2s;

      &:hover {
        border-color: #409EFF;
        background: #f5f9ff;

        .related-title { color: #409EFF; }
      }

      .related-icon {
        width: 40px;
        height: 40px;
        border-radius: 8px;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 18px;
        flex-shrink: 0;

        &.icon-pdf { background: #fef0f0; color: #f56c6c; }
        &.icon-archive { background: #fdf6ec; color: #e6a23c; }
        &.icon-video { background: #ecf5ff; color: #409eff; }
        &.icon-office { background: #f0f9eb; color: #67c23a; }
        &.icon-other { background: #f4f4f5; color: #909399; }
      }

      .related-body {
        flex: 1;
        min-width: 0;

        .related-title {
          display: block;
          font-size: 14px;
          color: #303133;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
          margin-bottom: 4px;
          transition: color 0.2s;
        }

        .related-meta {
          font-size: 12px;
          color: #909399;

          .free { color: #67c23a; }
          .points { color: #e6a23c; }
        }
      }
    }
  }
}

.reply-dialog {
  .comment-preview {
    margin-bottom: 14px;
    padding: 10px;
    background: #f5f7fa;
    border-radius: 4px;
    font-size: 14px;
    color: #606266;
  }
}
</style>
