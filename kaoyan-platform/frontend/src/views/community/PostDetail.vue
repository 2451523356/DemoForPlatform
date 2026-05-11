<template>
  <div class="post-detail">
    <div class="post-header">
      <el-button type="primary" @click="$router.back()" plain>
        <i class="el-icon-arrow-left"></i> 返回
      </el-button>
      <div class="post-info">
        <div class="author-info">
          <el-avatar :size="50" :src="post.authorAvatar"></el-avatar>
          <div class="author-meta">
            <h3 class="author-name">{{ post.authorName }}</h3>
            <span class="post-time">{{ formatTime(post.createTime) }}</span>
          </div>
        </div>
        <h1 class="post-title">{{ post.title }}</h1>
        <div class="post-meta">
          <span class="topic" v-if="post.topic">{{ post.topic }}</span>
          <span class="post-stats">
            <span><i class="el-icon-view"></i> {{ post.viewCount || 0 }}</span>
            <span><svg class="like-icon" viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2"><path d="M14 9V5a3 3 0 0 0-3-3l-4 9v11h11.28a2 2 0 0 0 2-1.7l1.38-9a2 2 0 0 0-2-2.3zM7 22H4a2 2 0 0 1-2-2v-7a2 2 0 0 1 2-2h3"/></svg> {{ post.likeCount || 0 }}</span>
            <span><i class="el-icon-chat-dot-round"></i> {{ post.commentCount || 0 }}</span>
          </span>
        </div>
      </div>
    </div>

    <div class="post-content" v-html="post.content"></div>

    <div class="post-actions">
      <el-button @click="likePost" :disabled="likeLoading">
        <svg class="like-icon" viewBox="0 0 24 24" width="15" height="15" fill="none" stroke="currentColor" stroke-width="2"><path d="M14 9V5a3 3 0 0 0-3-3l-4 9v11h11.28a2 2 0 0 0 2-1.7l1.38-9a2 2 0 0 0-2-2.3zM7 22H4a2 2 0 0 1-2-2v-7a2 2 0 0 1 2-2h3"/></svg>
        点赞 ({{ post.likeCount }})
      </el-button>
      <el-button @click="shareDialogVisible = true">
        <i class="el-icon-share"></i> 分享
      </el-button>
      <el-button v-if="isLoggedIn && post.userId !== currentUserId" type="danger" plain @click="showReportDialog()">
        <i class="el-icon-warning-outline"></i> 举报
      </el-button>
    </div>

    <ShareDialog v-model="shareDialogVisible" :share-title="post.title"
      :share-desc="post.content ? post.content.substring(0, 100) : ''" />

    <el-dialog :title="reportForm.targetType === 2 ? '举报评论' : '举报内容'" :visible.sync="reportDialogVisible" width="420px">
      <el-form :model="reportForm" label-width="80px">
        <el-form-item v-if="reportForm.targetType !== 2" label="举报类型">
          <el-radio-group v-model="reportForm.targetType">
            <el-radio :label="1">帖子</el-radio>
            <el-radio :label="2">评论</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-else label="举报类型">
          <span style="color:#606266">评论</span>
        </el-form-item>
        <el-form-item label="举报原因" prop="reason">
          <el-input v-model="reportForm.reason" type="textarea" :rows="3"
            placeholder="请描述举报原因..." maxlength="200" show-word-limit />
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="reportDialogVisible = false">取消</el-button>
        <el-button type="danger" @click="submitReport">提交举报</el-button>
      </span>
    </el-dialog>

    <div class="comments-section">
      <h3>评论 ({{ comments.length }})</h3>
      <div class="comment-form" v-if="isLoggedIn">
        <el-input v-model="commentContent" type="textarea" :rows="3"
          placeholder="写下你的评论..." maxlength="500" show-word-limit />
        <el-button type="primary" @click="submitComment" class="comment-btn" :loading="commentSubmitting">
          提交评论
        </el-button>
      </div>
      <div class="comment-form" v-else>
        <p style="text-align:center;color:#909399;padding:20px">
          请先<router-link to="/login">登录</router-link>后发表评论
        </p>
      </div>

      <div class="comments-list" v-if="comments.length > 0">
        <div v-for="comment in topLevelComments" :key="comment.id" class="comment-card">
          <div class="comment-header">
            <el-avatar :size="36" :src="comment.avatar || ''"></el-avatar>
            <div class="comment-meta">
              <span class="comment-author">{{ comment.username || '用户' + comment.userId }}</span>
              <span class="comment-time">{{ formatTime(comment.createTime) }}</span>
            </div>
          </div>
          <div class="comment-content">{{ comment.content }}</div>
          <div class="comment-actions">
            <span @click="likeComment(comment)" class="action-btn" :class="{ liked: likedComments.has(comment.id) }">
              <svg class="like-icon" viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2"><path d="M14 9V5a3 3 0 0 0-3-3l-4 9v11h11.28a2 2 0 0 0 2-1.7l1.38-9a2 2 0 0 0-2-2.3zM7 22H4a2 2 0 0 1-2-2v-7a2 2 0 0 1 2-2h3"/></svg> {{ comment.likes || comment.likeCount || 0 }}
            </span>
            <span v-if="isLoggedIn" @click="showReplyInput(comment)" class="action-btn">
              <i class="el-icon-chat-line-round"></i> 回复
            </span>
            <span v-if="isLoggedIn && comment.userId === currentUserId" @click="deleteComment(comment.id)" class="action-btn delete-btn">
              <i class="el-icon-delete"></i> 删除
            </span>
            <span v-if="isLoggedIn && comment.userId !== currentUserId" @click="showReportDialog(comment)" class="action-btn report-btn">
              <i class="el-icon-warning-outline"></i> 举报
            </span>
          </div>

          <!-- 回复输入框 -->
          <div v-if="replyingTo === comment.id" class="reply-input-area">
            <el-input v-model="replyContent" type="textarea" :rows="2"
              placeholder="写下你的回复..." maxlength="200" show-word-limit size="small" />
            <div class="reply-input-actions">
              <el-button size="mini" @click="cancelReply">取消</el-button>
              <el-button size="mini" type="primary" @click="submitReply(comment)">回复</el-button>
            </div>
          </div>

          <!-- 回复列表 -->
          <div v-if="getReplies(comment.id).length > 0" class="reply-list">
            <div v-for="reply in getReplies(comment.id)" :key="reply.id" class="reply-item">
              <div class="reply-header">
                <el-avatar :size="24" :src="reply.avatar || ''"></el-avatar>
                <span class="reply-author">{{ reply.username || '用户' + reply.userId }}</span>
                <span v-if="reply.parentId && reply.parentId !== comment.id" class="reply-to">
                  回复 @{{ getReplyTarget(reply.parentId) }}
                </span>
                <span class="reply-time">{{ formatTime(reply.createTime) }}</span>
              </div>
              <div class="reply-content">{{ reply.content }}</div>
              <div class="reply-actions">
                <span @click="likeComment(reply)" class="action-btn" :class="{ liked: likedComments.has(reply.id) }">
                  <svg class="like-icon" viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2"><path d="M14 9V5a3 3 0 0 0-3-3l-4 9v11h11.28a2 2 0 0 0 2-1.7l1.38-9a2 2 0 0 0-2-2.3zM7 22H4a2 2 0 0 1-2-2v-7a2 2 0 0 1 2-2h3"/></svg> {{ reply.likes || reply.likeCount || 0 }}
                </span>
                <span v-if="isLoggedIn" @click="showReplyInput(reply)" class="action-btn">回复</span>
                <span v-if="isLoggedIn && reply.userId === currentUserId" @click="deleteComment(reply.id)" class="action-btn delete-btn">删除</span>
                <span v-if="isLoggedIn && reply.userId !== currentUserId" @click="showReportDialog(reply)" class="action-btn report-btn">举报</span>
              </div>
            </div>
          </div>
        </div>
      </div>
      <el-empty v-else description="暂无评论，来发表第一条评论吧" :image-size="80" />
    </div>
  </div>
</template>

<script>
import { mapGetters } from 'vuex'
import ShareDialog from '@/components/ShareDialog'

export default {
  name: 'PostDetail',
  components: { ShareDialog },
  data() {
    return {
      post: { id: 0, title: '', content: '', authorName: '', authorAvatar: '', topic: '', likeCount: 0, commentCount: 0, createTime: '' },
      comments: [],
      commentContent: '',
      commentSubmitting: false,
      liked: false,
      likeLoading: false,
      likedComments: new Set(),
      shareDialogVisible: false,
      reportDialogVisible: false,
      reportForm: { targetType: 1, reason: '' },
      replyingTo: null,
      replyContent: ''
    }
  },
  computed: {
    ...mapGetters('user', ['isLoggedIn', 'userInfo']),
    currentUserId() { return this.userInfo ? this.userInfo.id : null },
    topLevelComments() {
      return this.comments.filter(c => !c.parentId || c.parentId === 0)
    }
  },
  created() {
    this.getPostDetail()
    this.getComments()
  },
  methods: {
    getReplies(parentId) {
      return this.comments.filter(c => c.parentId === parentId)
    },
    getReplyTarget(parentId) {
      const p = this.comments.find(c => c.id === parentId)
      return p ? (p.username || '用户' + p.userId) : '未知'
    },
    formatTime(time) {
      if (!time) return ''
      try {
        if (typeof time === 'string' && time.includes('T')) time = time.replace('T', ' ')
        const d = new Date(time)
        if (isNaN(d.getTime())) return time
        const now = new Date()
        const diff = now - d
        if (diff < 60000) return '刚刚'
        if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前'
        if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前'
        return d.toLocaleDateString('zh-CN')
      } catch (e) { return time }
    },
    async likePost() {
      if (this.liked) { this.$message.warning('已点过赞'); return }
      this.likeLoading = true
      try {
        await this.$http.post(`/community/post/${this.post.id}/like`)
        this.post.likeCount++
        this.liked = true
      } catch (e) { this.$message.error('点赞失败') }
      finally { this.likeLoading = false }
    },
    showReportDialog(comment) {
      if (!this.isLoggedIn) { this.$message.warning('请先登录'); return }
      if (comment) {
        this.reportForm = { targetType: 2, targetId: comment.id, reason: '' }
      } else {
        this.reportForm = { targetType: 1, targetId: this.post.id, reason: '' }
      }
      this.reportDialogVisible = true
    },
    async submitReport() {
      if (!this.reportForm.reason.trim()) { this.$message.warning('请填写举报原因'); return }
      try {
        await this.$http.post('/community/report', {
          targetType: this.reportForm.targetType,
          targetId: this.reportForm.targetId,
          reason: this.reportForm.reason
        })
        this.$message.success('举报已提交')
        this.reportDialogVisible = false
      } catch (e) { this.$message.error('举报提交失败') }
    },
    async likeComment(comment) {
      if (this.likedComments.has(comment.id)) { this.$message.warning('已点过赞'); return }
      try {
        await this.$http.post(`/community/comment/${comment.id}/like`)
        comment.likes = (comment.likes || comment.likeCount || 0) + 1
        comment.likeCount = comment.likes
        this.likedComments.add(comment.id)
      } catch (e) { this.$message.error('点赞失败') }
    },
    showReplyInput(comment) {
      if (comment.userId === this.currentUserId) { this.$message.warning('不能回复自己的评论'); return }
      this.replyingTo = comment.id; this.replyContent = ''
    },
    cancelReply() { this.replyingTo = null; this.replyContent = '' },
    async submitReply(parentComment) {
      if (!this.replyContent.trim()) { this.$message.warning('请输入回复内容'); return }
      try {
        await this.$http.post('/community/comment', {
          postId: this.post.id,
          parentId: parentComment.id,
          content: this.replyContent
        })
        this.$message.success('回复成功')
        this.cancelReply()
        await this.getComments()
        this.post.commentCount++
      } catch (e) { this.$message.error('回复失败') }
    },
    async submitComment() {
      if (!this.commentContent.trim()) { this.$message.warning('请输入评论内容'); return }
      this.commentSubmitting = true
      try {
        await this.$http.post('/community/comment', {
          postId: this.post.id,
          content: this.commentContent
        })
        this.commentContent = ''
        this.commentSubmitting = false
        await this.getComments()
        this.post.commentCount++
        this.$message.success('评论成功')
      } catch (e) { this.$message.error('评论失败'); this.commentSubmitting = false }
    },
    async getPostDetail() {
      try {
        const id = this.$route.params.id
        const res = await this.$http.get(`/community/post/${id}`)
        this.post = res || {}
      } catch (e) { this.$message.error('获取帖子详情失败') }
    },
    async getComments() {
      try {
        const id = this.$route.params.id
        const res = await this.$http.get(`/community/post/${id}/comments`)
        this.comments = Array.isArray(res) ? res : []
      } catch (e) { this.comments = [] }
    },
    async deleteComment(commentId) {
      try {
        await this.$confirm('确定要删除这条评论吗？', '提示', { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' })
        await this.$http.delete(`/community/comment/${commentId}`)
        this.$message.success('删除成功')
        await this.getComments()
        this.post.commentCount = Math.max(0, this.post.commentCount - 1)
      } catch (e) { if (e !== 'cancel') this.$message.error('删除失败') }
    }
  }
}
</script>

<style scoped lang="scss">
.like-icon { display: inline-block; vertical-align: -2px; margin-right: 2px; }
.post-detail { padding: 20px; max-width: 860px; margin: 0 auto; }

.post-header {
  margin-bottom: 28px;
  .post-info {
    margin-top: 18px;
    .author-info {
      display: flex; align-items: center; gap: 14px; margin-bottom: 18px;
      .author-meta {
        .author-name { font-size: 17px; font-weight: 600; margin: 0 0 4px 0; color: #303133; }
        .post-time { font-size: 13px; color: #909399; }
      }
    }
    .post-title { font-size: 22px; color: #303133; margin: 0 0 12px 0; line-height: 1.4; }
    .post-meta {
      display: flex; align-items: center; gap: 16px; flex-wrap: wrap;
      .topic { background: #ecf5ff; color: #409eff; padding: 4px 12px; border-radius: 4px; font-size: 13px; }
      .post-stats { display: flex; gap: 14px; font-size: 13px; color: #909399; margin-left: auto;
        span { display: flex; align-items: center; gap: 3px; }
      }
    }
  }
}

.post-content {
  margin-bottom: 28px; line-height: 1.9; font-size: 15px; color: #303133;
  background: #fff; padding: 28px; border-radius: 10px; box-shadow: 0 2px 12px rgba(0,0,0,.06);
}

.post-actions {
  margin-bottom: 36px; display: flex; gap: 16px; justify-content: center;
}

.comments-section {
  background: #fff; border-radius: 10px; padding: 24px; box-shadow: 0 2px 12px rgba(0,0,0,.06);
  h3 { font-size: 17px; margin: 0 0 18px; color: #303133; }
  .comment-form { margin-bottom: 24px; .comment-btn { margin-top: 10px; } }

  .comments-list {
    .comment-card {
      padding: 16px 0; border-bottom: 1px solid #f0f0f0;
      &:last-child { border-bottom: none; }
      .comment-header {
        display: flex; align-items: center; gap: 10px; margin-bottom: 8px;
        .comment-meta {
          .comment-author { font-weight: 600; font-size: 14px; color: #303133; }
          .comment-time { font-size: 12px; color: #c0c4cc; margin-left: 8px; }
        }
      }
      .comment-content { font-size: 14px; color: #606266; line-height: 1.6; margin-bottom: 10px; }
      .comment-actions {
        display: flex; gap: 16px; font-size: 13px; color: #909399;
        .action-btn { cursor: pointer; transition: color .2s;
          &:hover { color: #409eff; }
          &.liked { color: #409eff; font-weight: 600; }
          &.delete-btn { color: #f56c6c; &:hover { color: #f78989; } }
          &.report-btn { color: #e6a23c; &:hover { color: #f0c78e; } }
        }
      }
    }
  }

  .reply-input-area {
    margin: 10px 0 10px 46px;
    .reply-input-actions { display: flex; gap: 8px; justify-content: flex-end; margin-top: 6px; }
  }

  .reply-list {
    margin-left: 46px; margin-top: 8px;
    .reply-item {
      padding: 10px 14px; background: #f9f9f9; border-radius: 8px; margin-bottom: 8px;
      .reply-header {
        display: flex; align-items: center; gap: 8px; margin-bottom: 6px;
        .reply-author { font-weight: 500; font-size: 13px; color: #303133; }
        .reply-to { font-size: 12px; color: #409eff; }
        .reply-time { font-size: 11px; color: #c0c4cc; margin-left: auto; }
      }
      .reply-content { font-size: 13px; color: #606266; line-height: 1.5; margin-bottom: 6px; }
      .reply-actions { display: flex; gap: 12px; font-size: 12px; color: #909399;
        .action-btn { cursor: pointer; &.liked { color: #409eff; } &.delete-btn { color: #f56c6c; } &.report-btn { color: #e6a23c; } }
      }
    }
  }
}
</style>
