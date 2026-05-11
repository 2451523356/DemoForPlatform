<template>
  <div class="community">
    <div class="community-header">
      <div class="header-left">
        <h1>研友社区</h1>
        <span class="header-desc">交流备考经验，分享学习心得</span>
      </div>
      <el-button type="primary" size="medium" @click="showPostDialog">
        <i class="el-icon-edit"></i> 发布帖子
      </el-button>
    </div>

    <div class="community-body">
      <!-- 左侧主内容 -->
      <div class="main-content">
        <!-- 搜索栏 -->
        <div class="search-bar">
          <el-input v-model="searchKeyword" placeholder="搜索帖子标题或内容..." clearable
            prefix-icon="el-icon-search" @keyup.enter.native="handleSearch" @clear="handleSearch"
            class="search-input" />
          <el-select v-model="searchCircleId" placeholder="选择圈子" clearable @change="handleSearch"
            filterable style="width:180px;margin-left:10px">
            <el-option label="全部圈子" :value="null"></el-option>
            <el-option-group v-for="group in circleGroups" :key="group.label" :label="group.label">
              <el-option v-for="c in group.circles" :key="c.id" :label="c.name" :value="c.id"></el-option>
            </el-option-group>
          </el-select>
        </div>

        <!-- Tab 切换 -->
        <div class="tab-bar">
          <span class="tab-item" :class="{active:activeTab==='all'}" @click="switchTab('all')">全部</span>
          <span class="tab-item" :class="{active:activeTab==='following'}" @click="switchTab('following')"
            v-if="isLoggedIn">
            <i class="el-icon-user"></i> 我的关注
          </span>
          <span class="tab-item" :class="{active:activeTab==='experience'}" @click="switchTab('experience')">经验帖</span>
          <span class="tab-item" :class="{active:activeTab==='qa'}" @click="switchTab('qa')">答疑</span>
          <span class="tab-item" :class="{active:activeTab==='resource'}" @click="switchTab('resource')">资料分享</span>
          <span class="tab-item" :class="{active:activeTab==='reexam'}" @click="switchTab('reexam')">复试经验</span>
        </div>

        <!-- 帖子列表 -->
        <div class="post-list">
          <div v-if="postList.length === 0" class="empty-state">
            <el-empty :description="activeTab==='following'?'你关注的人还没有发帖':'暂无帖子，快来发布第一个吧！'" :image-size="100" />
          </div>
          <el-card v-for="post in postList" :key="post.id" class="post-card" shadow="hover">
            <div class="post-avatar-col">
              <el-avatar :size="44" :src="post.authorAvatar" @click.native="goToUserProfile(post.userId)"></el-avatar>
            </div>
            <div class="post-body-col">
              <div class="post-top-row">
                <span class="author-name" @click="goToUserProfile(post.userId)">{{ post.authorName }}</span>
                <el-tag size="mini" :type="getPostTypeTag(post.postType)">{{ getPostTypeLabel(post.postType) }}</el-tag>
                <span class="post-time">{{ post.createTime }}</span>
                <el-button v-if="isLoggedIn && currentUserId !== post.userId" style="margin-left:auto"
                  :type="isFollowing(post.userId) ? 'default' : 'primary'" size="mini" plain
                  @click.stop="toggleFollow(post.userId)">
                  {{ isFollowing(post.userId) ? '已关注' : '+ 关注' }}
                </el-button>
              </div>
              <h3 class="post-title" @click="goToPost(post.id)">{{ post.title }}</h3>
              <p class="post-content-preview">{{ post.content }}</p>
              <div class="post-bottom-row">
                <div class="post-tags">
                  <el-tag v-if="post.circleName" size="mini" type="warning" @click.stop="filterByCircle(post.circleId)">
                    {{ post.circleName }}
                  </el-tag>
                  <el-tag v-for="tag in parseTags(post.tags)" :key="tag" size="mini" type="info"
                    @click.stop="filterByTag(tag)">
                    {{ tag }}
                  </el-tag>
                </div>
                <div class="post-stats">
                  <span @click.stop="likePost(post)" class="stat-btn">
                  <svg class="like-icon" viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2"><path d="M14 9V5a3 3 0 0 0-3-3l-4 9v11h11.28a2 2 0 0 0 2-1.7l1.38-9a2 2 0 0 0-2-2.3zM7 22H4a2 2 0 0 1-2-2v-7a2 2 0 0 1 2-2h3"/></svg>
                  {{ post.likeCount || 0 }}</span>
                  <span @click="goToPost(post.id)" class="stat-btn"><i class="el-icon-chat-dot-round"></i> {{ post.commentCount || 0 }}</span>
                  <span class="stat-btn"><i class="el-icon-view"></i> {{ post.viewCount || 0 }}</span>
                  <span v-if="canDelete(post)" @click.stop="deletePost(post.id)" class="stat-btn delete-btn">
                    <i class="el-icon-delete"></i>
                  </span>
                </div>
              </div>
            </div>
          </el-card>
        </div>

        <!-- 分页 -->
        <div class="pagination-wrap">
          <el-pagination @size-change="handleSizeChange" @current-change="handleCurrentChange"
            :current-page="currentPage" :page-sizes="[10, 20, 30]" :page-size="pageSize"
            layout="total, sizes, prev, pager, next, jumper" :total="total" />
        </div>
      </div>

      <!-- 右侧边栏 -->
      <div class="side-bar">
        <!-- 圈子卡片 -->
        <el-card class="side-card">
          <div slot="header"><h4><i class="el-icon-s-grid"></i> 热门圈子</h4></div>
          <div class="circles-mini">
            <span class="circle-chip" v-for="c in hotCircles" :key="c.id"
              :class="{active:searchCircleId===c.id}"
              @click="filterByCircle(searchCircleId===c.id?null:c.id)">
              {{ c.name }}
              <em>{{ c.postCount || 0 }}</em>
            </span>
          </div>
          <el-button v-if="circles.length > 8" type="text" size="small" style="width:100%;margin-top:8px"
            @click="showAllCircles = !showAllCircles">
            {{ showAllCircles ? '收起' : '查看全部圈子' }}
          </el-button>
        </el-card>

        <!-- 热门标签 -->
        <el-card class="side-card">
          <div slot="header"><h4><i class="el-icon-collection-tag"></i> 热门标签</h4></div>
          <div class="tags-cloud">
            <span class="tag-chip" v-for="tag in hotTags" :key="tag.name"
              :style="{fontSize:12+tag.count*2+'px'}"
              @click="filterByTag(tag.name)">{{ tag.name }}</span>
          </div>
        </el-card>

        <!-- 社区公告 -->
        <el-card class="side-card">
          <div slot="header"><h4><i class="el-icon-bell"></i> 社区公告</h4></div>
          <div class="notice-list">
            <div class="notice-item"><i class="el-icon-info"></i> 欢迎来到研友社区！请文明发言</div>
            <div class="notice-item"><i class="el-icon-info"></i> 经验帖请标注目标院校和专业</div>
            <div class="notice-item"><i class="el-icon-info"></i> 资料分享请附上预览截图</div>
          </div>
        </el-card>
      </div>
    </div>

    <!-- 发布帖子弹窗 -->
    <el-dialog title="发布帖子" :visible.sync="postDialogVisible" width="640px" :close-on-click-modal="false">
      <el-form ref="postForm" :model="postForm" :rules="postRules" label-width="80px">
        <el-form-item label="帖子类型" prop="postType">
          <el-radio-group v-model="postForm.postType">
            <el-radio-button :label="1">普通</el-radio-button>
            <el-radio-button :label="2">经验帖</el-radio-button>
            <el-radio-button :label="3">答疑</el-radio-button>
            <el-radio-button :label="4">资料分享</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="标题" prop="title">
          <el-input v-model="postForm.title" placeholder="一句话概括你想说的..." maxlength="100" show-word-limit />
        </el-form-item>
        <el-form-item label="所属圈子" prop="circleId">
          <el-select v-model="postForm.circleId" placeholder="选择圈子（选填）" clearable filterable style="width:100%">
            <el-option-group v-for="group in circleGroups" :key="group.label" :label="group.label">
              <el-option v-for="c in group.circles" :key="c.id" :label="c.name" :value="c.id"></el-option>
            </el-option-group>
          </el-select>
        </el-form-item>
        <el-form-item label="标签" prop="tags">
          <el-input v-model="postForm.tags" placeholder="输入标签，用逗号分隔（如：考研经验,英语复习）" />
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input v-model="postForm.content" type="textarea" :rows="6" placeholder="写下你的帖子内容..."
            maxlength="2000" show-word-limit />
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="postDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitPost" :loading="submitting">发布</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { mapGetters } from 'vuex'

export default {
  name: 'Community',
  data() {
    return {
      activeTab: 'all',
      searchKeyword: '',
      searchCircleId: null,
      searchTag: '',
      postList: [],
      circles: [],
      currentPage: 1,
      pageSize: 10,
      total: 0,
      postDialogVisible: false,
      submitting: false,
      likedPosts: JSON.parse(localStorage.getItem('likedPosts') || '[]'),
      followingUsers: [],
      hotTagsData: [],
      postForm: { postType: 1, title: '', circleId: null, tags: '', content: '' },
      postRules: {
        title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
        content: [{ required: true, message: '请输入内容', trigger: 'blur' }]
      },
      showAllCircles: false
    }
  },
  computed: {
    ...mapGetters('user', ['isLoggedIn', 'userInfo']),
    currentUserId() { return this.userInfo ? this.userInfo.id : null },
    isAdmin() { return this.userInfo && this.userInfo.role === 'ADMIN' },
    hotCircles() {
      const sorted = [...this.circles].sort((a, b) => (b.postCount || 0) - (a.postCount || 0))
      return this.showAllCircles ? sorted : sorted.slice(0, 8)
    },
    circleGroups() {
      const uni = this.circles.filter(c => c.type === '院校' || c.type === 'university')
      const major = this.circles.filter(c => c.type === '专业' || c.type === 'major')
      const other = this.circles.filter(c => !['院校','university','专业','major'].includes(c.type))
      const groups = []
      if (uni.length) groups.push({ label: '院校圈子', circles: uni })
      if (major.length) groups.push({ label: '专业圈子', circles: major })
      if (other.length) groups.push({ label: '综合圈子', circles: other })
      return groups
    },
    hotTags() {
      return this.hotTagsData.length > 0 ? this.hotTagsData : (() => {
        const tagCount = {}
        this.postList.forEach(p => {
          (this.parseTags(p.tags) || []).forEach(t => { tagCount[t] = (tagCount[t] || 0) + 1 })
        })
        return Object.entries(tagCount).map(([name, count]) => ({ name, count })).sort((a, b) => b.count - a.count).slice(0, 15)
      })()
    }
  },
  created() {
    this.getPostList()
    this.getCircles()
    this.fetchHotTags()
    if (this.isLoggedIn) this.getFollowingList()
  },
  activated() {
    if (this.isLoggedIn) this.getFollowingList()
  },
  methods: {
    parseTags(tags) { if (!tags) return []; return (typeof tags === 'string' ? tags.split(/[,，]/) : tags).map(t => t.trim()).filter(Boolean).slice(0, 5) },
    getPostTypeTag(type) { const m = { 1: 'info', 2: 'success', 3: 'warning', 4: '', 5: 'danger' }; return m[type] || 'info' },
    getPostTypeLabel(type) { const m = { 1: '普通', 2: '经验帖', 3: '答疑', 4: '资料分享', 5: '复试经验' }; return m[type] || '普通' },
    canDelete(post) { return (this.isLoggedIn && post.userId === this.currentUserId) || this.isAdmin },
    goToPost(id) { this.$router.push(`/community/post/${id}`) },
    goToUserProfile(userId) { this.$router.push(`/user/${userId}`) },

    switchTab(tab) {
      this.activeTab = tab
      this.currentPage = 1
      this.searchKeyword = ''
      this.searchTag = ''
      this.getPostList()
    },

    handleSearch() { this.currentPage = 1; this.getPostList() },

    filterByCircle(circleId) {
      this.searchCircleId = circleId || null
      this.currentPage = 1
      this.getPostList()
    },

    filterByTag(tag) {
      this.searchTag = tag
      this.searchKeyword = ''
      this.searchCircleId = null
      this.currentPage = 1
      this.getPostList()
    },

    isFollowing(userId) { return this.followingUsers.includes(userId) },

    async toggleFollow(userId) {
      if (!this.isLoggedIn) { this.$message.warning('请先登录'); return }
      try {
        if (this.followingUsers.includes(userId)) {
          await this.$http.post(`/community/unfollow/${userId}`)
          this.followingUsers = this.followingUsers.filter(id => id !== userId)
          this.$message.success('已取消关注')
        } else {
          await this.$http.post(`/community/follow/${userId}`)
          this.followingUsers.push(userId)
          this.$message.success('关注成功')
        }
      } catch (e) { this.$message.error('操作失败') }
    },

    async getFollowingList() {
      try {
        const res = await this.$http.get('/community/follow/following')
        if (Array.isArray(res)) this.followingUsers = res.map(u => u.id)
      } catch (e) { /* silent */ }
    },

    async likePost(post) {
      if (!this.isLoggedIn) { this.$message.warning('请先登录'); return }
      if (this.likedPosts.includes(post.id)) { this.$message.warning('已点过赞'); return }
      try {
        await this.$http.post(`/community/post/${post.id}/like`)
        this.likedPosts.push(post.id)
        localStorage.setItem('likedPosts', JSON.stringify(this.likedPosts))
        post.likeCount = (post.likeCount || 0) + 1
      } catch (e) { this.$message.error('点赞失败') }
    },

    async getCircles() {
      try {
        const res = await this.$http.get('/community/circles', { params: { page: 1, size: 50 } })
        this.circles = (res?.records || []).map(c => ({ id: c.id, name: c.name, type: c.type, postCount: c.postCount || 0 }))
      } catch (e) { /* silent */ }
    },

    async fetchHotTags() {
      try {
        const res = await this.$http.get('/community/hot-tags')
        if (Array.isArray(res)) this.hotTagsData = res
      } catch (e) { /* silent */ }
    },

    showPostDialog() {
      if (!this.isLoggedIn) { this.$message.warning('请先登录后再发布帖子'); this.$router.push('/login'); return }
      this.postForm = { postType: 1, title: '', circleId: null, tags: '', content: '' }
      this.submitting = false
      this.postDialogVisible = true
    },

    async submitPost() {
      try {
        await this.$refs.postForm.validate()
        this.submitting = true
        await this.$http.post('/community/post', {
          title: this.postForm.title,
          content: this.postForm.content,
          circleId: this.postForm.circleId || null,
          topic: this.postForm.tags,
          postType: this.postForm.postType
        })
        this.$message.success('发布成功！')
        this.postDialogVisible = false
        this.currentPage = 1
        this.getPostList()
        this.fetchHotTags()
      } catch (e) {
        if (e?.message) this.$message.error('请检查表单')
        else if (e !== 'cancel') this.$message.error('发布失败')
      } finally { this.submitting = false }
    },

    async deletePost(postId) {
      try {
        await this.$confirm('确定要删除这个帖子吗？', '提示', { type: 'warning' })
        await this.$http.delete(`/community/post/${postId}`)
        this.$message.success('删除成功')
        this.getPostList()
      } catch (e) { if (e !== 'cancel') this.$message.error('删除失败') }
    },

    handleSizeChange(size) { this.pageSize = size; this.getPostList() },
    handleCurrentChange(current) { this.currentPage = current; this.getPostList() },

    async getPostList() {
      try {
        const params = { page: this.currentPage, size: this.pageSize }
        if (this.searchKeyword) params.keyword = this.searchKeyword
        if (this.searchCircleId) params.circleId = this.searchCircleId
        if (this.searchTag) params.keyword = this.searchTag
        if (this.activeTab === 'experience') params.postType = 2
        else if (this.activeTab === 'qa') params.postType = 3
        else if (this.activeTab === 'resource') params.postType = 4
        else if (this.activeTab === 'reexam') params.postType = 5
        else if (this.activeTab === 'following') {
          if (this.followingUsers.length > 0) {
            params.followingUserIds = this.followingUsers.join(',')
          } else {
            // 未关注任何人时返回空列表
            this.postList = []
            this.total = 0
            return
          }
        }
        const res = await this.$http.get('/community/posts', { params })
        const records = res?.records || []
        this.postList = records.map(p => ({
          ...p,
          authorName: p.authorName || ('用户' + (p.userId || '')),
          authorAvatar: p.authorAvatar || '',
          circleName: p.circleName || '',
          createTime: this.formatTime(p.createTime)
        }))
        this.total = res?.total || 0
      } catch (e) { this.postList = []; this.total = 0 }
    },

    formatTime(time) {
      if (!time) return ''
      try {
        const d = new Date(time); if (isNaN(d.getTime())) return time
        const diff = Date.now() - d.getTime()
        if (diff < 60000) return '刚刚'
        if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前'
        if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前'
        if (diff < 604800000) return Math.floor(diff / 86400000) + '天前'
        return d.toLocaleDateString('zh-CN')
      } catch (e) { return time }
    }
  }
}
</script>

<style scoped lang="scss">
.like-icon { display: inline-block; vertical-align: -2px; margin-right: 2px; }
.community { padding: 20px; max-width: 1200px; margin: 0 auto; }

.community-header {
  display: flex; justify-content: space-between; align-items: center;
  margin-bottom: 24px; padding-bottom: 16px; border-bottom: 1px solid #ebeef5;
  .header-left { h1 { font-size: 24px; margin: 0 0 4px; color: #303133; } .header-desc { font-size: 14px; color: #909399; } }
}

.community-body { display: flex; gap: 24px; }
.main-content { flex: 1; min-width: 0; }
.side-bar { width: 280px; flex-shrink: 0; }

.search-bar { display: flex; margin-bottom: 16px; .search-input { flex: 1; } }

.tab-bar {
  display: flex; gap: 0; margin-bottom: 20px; border-bottom: 2px solid #ebeef5;
  .tab-item {
    padding: 10px 20px; cursor: pointer; font-size: 14px; color: #606266;
    border-bottom: 2px solid transparent; margin-bottom: -2px; transition: all .3s; white-space: nowrap;
    &:hover { color: #409eff; }
    &.active { color: #409eff; border-bottom-color: #409eff; font-weight: 600; }
  }
}

.post-list {
  .empty-state { padding: 60px 0; }
  .post-card {
    margin-bottom: 16px; border-radius: 8px;
    ::v-deep .el-card__body { display: flex; gap: 16px; padding: 20px; }
    .post-avatar-col { flex-shrink: 0; .el-avatar { cursor: pointer; } }
    .post-body-col { flex: 1; min-width: 0;
      .post-top-row { display: flex; align-items: center; gap: 10px; margin-bottom: 8px; flex-wrap: wrap;
        .author-name { font-size: 14px; font-weight: 600; color: #303133; cursor: pointer; &:hover { color: #409eff; } }
        .post-time { font-size: 12px; color: #c0c4cc; } }
      .post-title { font-size: 17px; margin: 0 0 8px; cursor: pointer; color: #303133; &:hover { color: #409eff; } }
      .post-content-preview { font-size: 14px; color: #909399; line-height: 1.6; margin: 0 0 12px;
        overflow: hidden; text-overflow: ellipsis; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; }
      .post-bottom-row { display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 8px;
        .post-tags { display: flex; gap: 6px; flex-wrap: wrap; .el-tag { cursor: pointer; } }
        .post-stats { display: flex; gap: 16px; font-size: 13px; color: #c0c4cc;
          .stat-btn { cursor: pointer; display: flex; align-items: center; gap: 3px; &:hover { color: #409eff; } }
          .delete-btn:hover { color: #f56c6c; } } } } }
}

.side-card {
  margin-bottom: 20px; border-radius: 8px;
  ::v-deep .el-card__header { padding: 14px 16px; h4 { margin: 0; font-size: 15px; i { margin-right: 4px; color: #409eff; } } }
  ::v-deep .el-card__body { padding: 14px 16px; }
}

.circles-mini { display: flex; flex-wrap: wrap; gap: 6px;
  .circle-chip { padding: 4px 10px; background: #f5f7fa; border-radius: 14px; font-size: 12px; cursor: pointer; transition: all .3s; color: #606266;
    em { font-style: normal; color: #c0c4cc; margin-left: 4px; font-size: 11px; }
    &:hover { background: #ecf5ff; color: #409eff; }
    &.active { background: #409eff; color: #fff; em { color: rgba(255,255,255,.7); } } }
}

.tags-cloud { display: flex; flex-wrap: wrap; gap: 8px;
  .tag-chip { cursor: pointer; color: #606266; transition: all .3s; &:hover { color: #409eff; } } }

.notice-list .notice-item { font-size: 13px; color: #909399; padding: 6px 0; i { color: #e6a23c; margin-right: 6px; } }

.pagination-wrap { margin-top: 24px; text-align: center; }

@media (max-width: 900px) {
  .community-body { flex-direction: column; .side-bar { width: 100%; } }
  .post-card ::v-deep .el-card__body { flex-direction: column; }
}
</style>
