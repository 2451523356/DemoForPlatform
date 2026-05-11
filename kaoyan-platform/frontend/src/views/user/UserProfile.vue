<template>
  <div class="user-profile">
    <div class="profile-header" v-if="!loading">
      <el-button type="primary" plain @click="$router.back()">
        <i class="el-icon-arrow-left"></i> 返回
      </el-button>
      <div class="profile-card">
        <el-avatar :size="80" :src="profile.avatar"></el-avatar>
        <div class="profile-info">
          <h2>{{ profile.nickname || profile.username }}</h2>
          <p class="bio">{{ profile.bio || '这个人很懒，什么都没写~' }}</p>
          <div class="profile-tags">
            <span v-if="profile.targetSchool">
              <i class="el-icon-school"></i> {{ profile.targetSchool }}
            </span>
            <span v-if="profile.targetMajor">
              <i class="el-icon-reading"></i> {{ profile.targetMajor }}
            </span>
          </div>
          <div class="profile-stats">
            <div class="stat" @click="showFollowing = true">
              <span class="count">{{ followCount.following || 0 }}</span>
              <span class="label">关注</span>
            </div>
            <div class="stat" @click="showFollowers = true">
              <span class="count">{{ followCount.followers || 0 }}</span>
              <span class="label">粉丝</span>
            </div>
            <div class="stat">
              <span class="count">{{ profile.points || 0 }}</span>
              <span class="label">积分</span>
            </div>
          </div>
          <div class="profile-actions" v-if="!isSelf">
            <el-button
              :type="isFollowingUser ? 'default' : 'primary'"
              size="small"
              @click="toggleFollowUser"
            >
              {{ isFollowingUser ? '已关注' : '+ 关注' }}
            </el-button>
            <el-button size="small" @click="sendMessage">发私信</el-button>
          </div>
        </div>
      </div>
    </div>

    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="6" animated />
    </div>

    <div v-else class="profile-content">
      <el-tabs v-model="activeTab">
        <el-tab-pane label="TA的帖子" name="posts">
          <div v-if="userPosts.length === 0" class="empty-state">
            <el-empty description="暂无帖子" />
          </div>
          <el-card v-for="post in userPosts" :key="post.id" class="post-card" shadow="hover"
            @click.native="$router.push(`/community/post/${post.id}`)">
            <h3>{{ post.title }}</h3>
            <p>{{ post.content }}</p>
            <div class="post-meta">
              <span><i class="el-icon-view"></i> {{ post.viewCount || 0 }}</span>
              <span><svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2" style="vertical-align:-2px;margin-right:2px"><path d="M14 9V5a3 3 0 0 0-3-3l-4 9v11h11.28a2 2 0 0 0 2-1.7l1.38-9a2 2 0 0 0-2-2.3zM7 22H4a2 2 0 0 1-2-2v-7a2 2 0 0 1 2-2h3"/></svg> {{ post.likeCount || 0 }}</span>
              <span><i class="el-icon-chat-dot-round"></i> {{ post.commentCount || 0 }}</span>
              <span class="time">{{ formatDate(post.createTime) }}</span>
            </div>
          </el-card>
        </el-tab-pane>

        <el-tab-pane label="TA的动态" name="activity">
          <div v-if="activities.length === 0" class="empty-state">
            <el-empty description="暂无动态" />
          </div>
          <el-timeline v-else>
            <el-timeline-item
              v-for="(item, idx) in activities"
              :key="idx"
              :timestamp="formatDate(item.time)"
              :type="item.type"
            >
              {{ item.content }}
            </el-timeline-item>
          </el-timeline>
        </el-tab-pane>
      </el-tabs>
    </div>

    <!-- 关注列表对话框 -->
    <el-dialog title="关注列表" :visible.sync="showFollowing" width="450px">
      <div v-if="followingList.length === 0" class="empty-state">
        <el-empty description="暂无关注" :image-size="80" />
      </div>
      <div v-for="user in followingList" :key="user.id" class="follow-item"
        @click="goToUser(user.id); showFollowing = false">
        <el-avatar :size="40" :src="user.avatar"></el-avatar>
        <div class="follow-info">
          <span class="name">{{ user.nickname || user.username }}</span>
          <span class="bio-line">{{ user.bio || '' }}</span>
        </div>
      </div>
    </el-dialog>

    <!-- 粉丝列表对话框 -->
    <el-dialog title="粉丝列表" :visible.sync="showFollowers" width="450px">
      <div v-if="followerList.length === 0" class="empty-state">
        <el-empty description="暂无粉丝" :image-size="80" />
      </div>
      <div v-for="user in followerList" :key="user.id" class="follow-item"
        @click="goToUser(user.id); showFollowers = false">
        <el-avatar :size="40" :src="user.avatar"></el-avatar>
        <div class="follow-info">
          <span class="name">{{ user.nickname || user.username }}</span>
          <span class="bio-line">{{ user.bio || '' }}</span>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: 'UserProfile',
  data() {
    return {
      profile: {},
      followCount: { following: 0, followers: 0 },
      userPosts: [],
      activities: [],
      followingList: [],
      followerList: [],
      isFollowingUser: false,
      showFollowing: false,
      showFollowers: false,
      loading: true,
      activeTab: 'posts'
    }
  },
  computed: {
    userId() {
      return this.$route.params.id
    },
    isSelf() {
      const userInfo = this.$store.state.user.userInfo
      return userInfo && String(userInfo.id) === String(this.userId)
    }
  },
  created() {
    this.fetchProfile()
    this.fetchFollowCount()
    this.checkFollowStatus()
    this.fetchUserPosts()
  },
  methods: {
    formatDate(dateStr) {
      if (!dateStr) return ''
      try {
        return new Date(dateStr).toLocaleDateString('zh-CN')
      } catch (e) {
        return dateStr
      }
    },
    goToUser(id) {
      this.$router.push(`/user/${id}`)
    },
    async fetchProfile() {
      this.loading = true
      try {
        const res = await this.$http.get(`/user/profile/${this.userId}`)
        this.profile = res || {}
      } catch (e) {
        console.error('获取用户信息失败:', e)
      } finally {
        this.loading = false
      }
    },
    async fetchFollowCount() {
      try {
        const res = await this.$http.get('/community/follow/count', {
          params: { userId: this.userId }
        })
        this.followCount = res || { following: 0, followers: 0 }
      } catch (e) {
        console.error('获取关注数失败:', e)
      }
    },
    async checkFollowStatus() {
      try {
        const res = await this.$http.get('/community/follow/check', {
          params: { followUserId: this.userId }
        })
        this.isFollowingUser = res || false
      } catch (e) {
        console.error('检查关注状态失败:', e)
      }
    },
    async fetchUserPosts() {
      try {
        const res = await this.$http.get('/community/posts', {
          params: { page: 1, size: 20, userId: this.userId }
        })
        this.userPosts = res?.records || []
      } catch (e) {
        console.error('获取用户帖子失败:', e)
      }
    },
    async toggleFollowUser() {
      try {
        if (!this.$store.state.user.token) {
          this.$message.warning('请先登录')
          this.$router.push('/login')
          return
        }
        if (this.isFollowingUser) {
          await this.$http.post(`/community/unfollow/${this.userId}`)
          this.isFollowingUser = false
          this.followCount.followers = Math.max(0, (this.followCount.followers || 1) - 1)
          this.$message.success('已取消关注')
        } else {
          await this.$http.post(`/community/follow/${this.userId}`)
          this.isFollowingUser = true
          this.followCount.followers = (this.followCount.followers || 0) + 1
          this.$message.success('关注成功')
        }
      } catch (e) {
        console.error('关注操作失败:', e)
      }
    },
    sendMessage() {
      if (!this.$store.state.user.token) {
        this.$message.warning('请先登录')
        this.$router.push('/login')
        return
      }
      this.$router.push(`/community/message?userId=${this.userId}`)
    },
    async showFollowingList() {
      try {
        const res = await this.$http.get('/community/follow/following')
        this.followingList = Array.isArray(res) ? res : []
        this.showFollowing = true
      } catch (e) {
        console.error(e)
      }
    },
    async showFollowerList() {
      try {
        const res = await this.$http.get('/community/follow/followers')
        this.followerList = Array.isArray(res) ? res : []
        this.showFollowers = true
      } catch (e) {
        console.error(e)
      }
    }
  },
  watch: {
    userId() {
      this.fetchProfile()
      this.fetchFollowCount()
      this.checkFollowStatus()
      this.fetchUserPosts()
    }
  }
}
</script>

<style scoped lang="scss">
.user-profile {
  padding: 20px;
  max-width: 900px;
  margin: 0 auto;

  .loading-container { padding: 40px; }

  .profile-header {
    margin-bottom: 30px;
    .profile-card {
      display: flex;
      gap: 24px;
      background: #fff;
      padding: 30px;
      border-radius: 12px;
      box-shadow: 0 2px 12px rgba(0,0,0,0.08);
      margin-top: 16px;
      align-items: flex-start;

      .profile-info {
        flex: 1;
        h2 { margin: 0 0 8px 0; font-size: 22px; color: #303133; }
        .bio { color: #606266; font-size: 14px; margin: 0 0 12px 0; }
        .profile-tags {
          display: flex; gap: 16px; margin-bottom: 12px;
          span { color: #409EFF; font-size: 14px; i { margin-right: 4px; } }
        }
        .profile-stats {
          display: flex; gap: 24px; margin-bottom: 12px;
          .stat { cursor: pointer; text-align: center; &:hover .count { color: #409EFF; } }
          .count { font-size: 20px; font-weight: 600; color: #303133; display: block; }
          .label { font-size: 12px; color: #909399; }
        }
      }
    }
  }

  .profile-content {
    background: #fff;
    border-radius: 12px;
    box-shadow: 0 2px 12px rgba(0,0,0,0.08);
    padding: 20px;

    .post-card {
      cursor: pointer;
      margin-bottom: 16px;
      transition: all 0.3s;
      &:hover { transform: translateY(-2px); box-shadow: 0 4px 12px rgba(0,0,0,0.12); }
      h3 { font-size: 16px; color: #303133; margin-bottom: 8px; }
      p { font-size: 14px; color: #606266; margin-bottom: 10px;
        overflow: hidden; text-overflow: ellipsis; display: -webkit-box;
        -webkit-line-clamp: 2; -webkit-box-orient: vertical; }
      .post-meta {
        display: flex; gap: 16px; font-size: 13px; color: #909399;
        .time { margin-left: auto; }
      }
    }

    .empty-state {
      text-align: center; padding: 60px 0;
    }
  }

  .follow-item {
    display: flex; align-items: center; gap: 12px;
    padding: 10px; cursor: pointer; border-radius: 6px;
    &:hover { background: #f5f7fa; }
    .follow-info {
      .name { font-size: 14px; color: #303133; font-weight: 500; display: block; }
      .bio-line { font-size: 12px; color: #909399; }
    }
  }
}
</style>
