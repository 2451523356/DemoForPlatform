<template>
  <div class="user">
    <el-row :gutter="20">
      <el-col :span="8">
        <el-card class="user-card">
          <div class="user-avatar">
            <el-avatar :size="100" :src="profileForm.avatar || '/static/images/default-avatar.png'"></el-avatar>
            <h2>{{ profileForm.nickname || '未知用户' }}</h2>
            <p class="username">@{{ profileForm.username || 'unknown' }}</p>
          </div>
          <div class="user-stats">
            <div class="stat">
              <span class="value">{{ userStats.points }}</span>
              <span class="label">积分</span>
            </div>
            <div class="stat">
              <span class="value">{{ userStats.courses }}</span>
              <span class="label">课程</span>
            </div>
            <div class="stat" @click="activeTab = 'following'">
              <span class="value">{{ userStats.following }}</span>
              <span class="label">关注</span>
            </div>
            <div class="stat" @click="activeTab = 'followers'">
              <span class="value">{{ userStats.followers }}</span>
              <span class="label">粉丝</span>
            </div>
          </div>
          <div class="sign-in-section">
            <template v-if="isOwnProfile">
              <el-button
                type="primary"
                :disabled="isSignedToday"
                @click="handleSignIn"
                style="margin-right: 10px"
              >
                {{ isSignedToday ? '今日已签到' : '每日签到' }}
              </el-button>
              <el-button
                type="success"
                @click="handleShare"
              >
                <i class="el-icon-share"></i> 分享得积分
              </el-button>
            </template>
            <template v-else>
              <el-button
                v-if="isLoggedIn"
                :type="isFollowingOtherUser ? 'default' : 'primary'"
                @click="toggleFollowOtherUser"
              >
                {{ isFollowingOtherUser ? '已关注' : '+ 关注' }}
              </el-button>
              <el-button
                type="success"
                @click="sendMessageToUser"
              >
                <i class="el-icon-message"></i> 私信
              </el-button>
            </template>
          </div>
        </el-card>
      </el-col>

      <el-col :span="16">
        <el-card>
          <el-tabs v-model="activeTab">
            <el-tab-pane label="个人资料" name="profile">
              <el-form v-if="isOwnProfile" ref="profileForm" :model="profileForm" :rules="profileRules" label-width="80px">
                <el-form-item label="昵称" prop="nickname">
                  <el-input v-model="profileForm.nickname" placeholder="请输入昵称"></el-input>
                </el-form-item>
                <el-form-item label="邮箱" prop="email">
                  <el-input v-model="profileForm.email" placeholder="请输入邮箱"></el-input>
                </el-form-item>
                <el-form-item label="头像" prop="avatar">
                  <el-upload
                    class="avatar-uploader"
                    action="/api/upload/avatar"
                    :show-file-list="false"
                    :on-success="handleAvatarSuccess"
                    :before-upload="beforeAvatarUpload"
                  >
                    <img v-if="profileForm.avatar" :src="profileForm.avatar" class="avatar">
                    <i v-else class="el-icon-plus avatar-uploader-icon"></i>
                  </el-upload>
                </el-form-item>
                <el-form-item label="简介" prop="bio">
                  <el-input v-model="profileForm.bio" type="textarea" :rows="3" placeholder="请输入个人简介"></el-input>
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="updateProfile">保存修改</el-button>
                  <el-button @click="resetProfile">重置</el-button>
                </el-form-item>
              </el-form>
              <div v-else class="user-profile-view">
                <div class="profile-item">
                  <span class="profile-label">昵称</span>
                  <span class="profile-value">{{ profileForm.nickname }}</span>
                </div>
                <div class="profile-item">
                  <span class="profile-label">简介</span>
                  <span class="profile-value">{{ profileForm.bio || '暂无简介' }}</span>
                </div>
              </div>
            </el-tab-pane>

            <el-tab-pane v-if="isOwnProfile" label="修改密码" name="password">
              <el-form ref="passwordForm" :model="passwordForm" :rules="passwordRules" label-width="100px">
                <el-form-item label="当前密码" prop="oldPassword">
                  <el-input v-model="passwordForm.oldPassword" type="password" show-password></el-input>
                </el-form-item>
                <el-form-item label="新密码" prop="newPassword">
                  <el-input v-model="passwordForm.newPassword" type="password" show-password></el-input>
                </el-form-item>
                <el-form-item label="确认新密码" prop="confirmPassword">
                  <el-input v-model="passwordForm.confirmPassword" type="password" show-password></el-input>
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="changePassword">修改密码</el-button>
                </el-form-item>
              </el-form>
            </el-tab-pane>

            <el-tab-pane v-if="isOwnProfile" label="下载历史" name="downloads">
              <el-table :data="downloadHistory" style="width: 100%">
                <el-table-column prop="resourceTitle" label="资源名称"></el-table-column>
                <el-table-column prop="resourceCategory" label="分类"></el-table-column>
                <el-table-column prop="pointsConsumed" label="消耗积分"></el-table-column>
                <el-table-column prop="downloadTime" label="下载时间"></el-table-column>
              </el-table>
            </el-tab-pane>

            <el-tab-pane v-if="isOwnProfile" label="积分记录" name="points">
              <el-table :data="pointsRecord" style="width: 100%">
                <el-table-column prop="type" label="类型">
                  <template slot-scope="scope">
                    <el-tag :type="scope.row.type === 'earn' ? 'success' : 'danger'">
                      {{ scope.row.type === 'earn' ? '获得' : '消耗' }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="description" label="描述"></el-table-column>
                <el-table-column prop="points" label="积分" width="120">
                  <template slot-scope="scope">
                    <span :class="scope.row.type === 'earn' ? 'text-success' : 'text-danger'">
                      {{ scope.row.type === 'earn' ? '+' : '-' }}{{ scope.row.points }}
                    </span>
                  </template>
                </el-table-column>
                <el-table-column prop="createTime" label="时间"></el-table-column>
              </el-table>
            </el-tab-pane>

            <el-tab-pane v-if="isOwnProfile" label="积分规则" name="pointsRules">
              <el-card class="points-rules-card" shadow="never">
                <div class="points-rules-list">
                  <div class="points-rule-item">
                    <el-tag type="success">+5 积分</el-tag>
                    <span class="points-rule-desc">每日签到</span>
                  </div>
                  <div class="points-rule-item">
                    <el-tag type="success">+20 积分</el-tag>
                    <span class="points-rule-desc">上传资源审核通过</span>
                  </div>
                  <div class="points-rule-item">
                    <el-tag type="success">+1 积分</el-tag>
                    <span class="points-rule-desc">发表评论</span>
                  </div>
                  <div class="points-rule-item">
                    <el-tag type="success">+1 积分</el-tag>
                    <span class="points-rule-desc">点赞</span>
                  </div>
                  <div class="points-rule-item">
                    <el-tag type="warning">根据资源设置</el-tag>
                    <span class="points-rule-desc">下载资源（消耗积分）</span>
                  </div>
                </div>
              </el-card>
            </el-tab-pane>

            <el-tab-pane label="我的关注" name="following">
              <div class="following-list">
                <div class="user-item" v-for="user in followingList" :key="user.id">
                  <div class="user-item-main" @click="goToUserProfile(user.id)">
                    <el-avatar :size="50" :src="user.avatar"></el-avatar>
                    <div class="user-info">
                      <span class="nickname">{{ user.nickname }}</span>
                      <span class="username">@{{ user.username }}</span>
                    </div>
                  </div>
                  <el-button type="default" size="small" @click.stop="toggleFollow(user.id)">
                    已关注
                  </el-button>
                </div>
                <div v-if="followingList.length === 0" class="empty-state">
                  <el-empty description="暂无关注，去社区看看吧！" />
                </div>
              </div>
            </el-tab-pane>

            <el-tab-pane label="我的粉丝" name="followers">
              <div class="followers-list">
                <div class="user-item" v-for="user in followersList" :key="user.id">
                  <div class="user-item-main" @click="goToUserProfile(user.id)">
                    <el-avatar :size="50" :src="user.avatar"></el-avatar>
                    <div class="user-info">
                      <span class="nickname">{{ user.nickname }}</span>
                      <span class="username">@{{ user.username }}</span>
                    </div>
                  </div>
                  <el-button
                    :type="isFollowingUser(user.id) ? 'default' : 'primary'"
                    size="small"
                    @click.stop="toggleFollow(user.id)"
                  >
                    {{ isFollowingUser(user.id) ? '已关注' : '关注' }}
                  </el-button>
                </div>
                <div v-if="followersList.length === 0" class="empty-state">
                  <el-empty description="暂无粉丝" />
                </div>
              </div>
            </el-tab-pane>

            <el-tab-pane v-if="isOwnProfile" label="我的收藏" name="bookmarks">
              <div class="bookmarks-list" v-loading="bookmarksLoading">
                <div v-if="bookmarkedNewsList.length === 0" class="empty-state">
                  <el-empty description="暂无收藏" />
                </div>
                <div v-for="news in bookmarkedNewsList" :key="news.id" class="bookmark-card">
                  <div class="bookmark-card-body">
                    <h3 class="bookmark-title">{{ news.title }}</h3>
                    <p class="bookmark-summary">{{ news.summary }}</p>
                    <div class="bookmark-meta">
                      <el-tag size="mini" type="warning" v-if="news.category">{{ news.category }}</el-tag>
                      <el-tag size="mini" v-if="news.examStage">{{ news.examStage }}</el-tag>
                      <span class="bookmark-date">{{ formatDate(news.createTime) }}</span>
                    </div>
                  </div>
                  <div class="bookmark-actions">
                    <el-button type="text" size="small" @click="viewNewsDetail(news.id)">
                      <i class="el-icon-view"></i> 查看全文
                    </el-button>
                    <el-button type="text" size="small" class="unbookmark-btn" @click="removeBookmark(news.id)">
                      <i class="el-icon-star-off"></i> 取消收藏
                    </el-button>
                  </div>
                </div>
              </div>
            </el-tab-pane>
          </el-tabs>
        </el-card>
      </el-col>
    </el-row>

    <ShareDialog v-model="shareDialogVisible" :share-title="shareTitle" :share-desc="shareDesc" />
  </div>
</template>

<script>
import { mapGetters, mapActions } from 'vuex'
import ShareDialog from '@/components/ShareDialog'

export default {
  name: 'User',
  components: { ShareDialog },
  data() {
    const validateConfirmPassword = (rule, value, callback) => {
      if (value !== this.passwordForm.newPassword) {
        callback(new Error('两次输入的密码不一致'))
      } else {
        callback()
      }
    }
    return {
      activeTab: 'profile',
      userStats: {
        points: 0,
        courses: 0,
        followers: 0,
        following: 0
      },
      followingList: [],
      followersList: [],
      profileForm: {
        nickname: '',
        email: '',
        avatar: '',
        bio: ''
      },
      profileRules: {
        nickname: [
          { required: true, message: '请输入昵称', trigger: 'blur' }
        ],
        email: [
          { required: true, message: '请输入邮箱', trigger: 'blur' },
          { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
        ]
      },
      passwordForm: {
        oldPassword: '',
        newPassword: '',
        confirmPassword: ''
      },
      passwordRules: {
        oldPassword: [
          { required: true, message: '请输入当前密码', trigger: 'blur' }
        ],
        newPassword: [
          { required: true, message: '请输入新密码', trigger: 'blur' },
          { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
        ],
        confirmPassword: [
          { required: true, message: '请确认新密码', trigger: 'blur' },
          { validator: validateConfirmPassword, trigger: 'blur' }
        ]
      },
      myCourses: [],
      downloadHistory: [],
      pointsRecord: [],
      isSignedToday: false,
      isFollowingOtherUser: false,
      shareDialogVisible: false,
      bookmarkedNewsList: [],
      bookmarksLoading: false
    }
  },
  computed: {
    ...mapGetters('user', ['userInfo']),
    // 当前查看的用户ID（可能是其他用户）
    viewingUserId() {
      const userId = this.$route.params.id
      return userId ? parseInt(userId) : null
    },
    // 是否查看的是当前登录用户
    isOwnProfile() {
      return !this.viewingUserId || this.viewingUserId === (this.userInfo?.id || 0)
    },
    // 是否已登录
    isLoggedIn() {
      return this.$store.state.user.token && this.userInfo
    },
    inProgressCount() {
      return this.myCourses.filter(c => (c.progress || 0) < 100).length
    },
    completedCourseCount() {
      return this.myCourses.filter(c => (c.progress || 0) >= 100).length
    },
    shareTitle() {
      return (this.profileForm.nickname || '用户') + ' 的个人主页'
    },
    shareDesc() {
      return this.profileForm.bio || '考研学习社区平台'
    }
  },
  watch: {
    activeTab(newVal) {
      if (newVal === 'bookmarks') {
        this.loadBookmarkedNews()
      }
    }
  },
  created() {
    // 如果有用户ID参数，获取该用户的资料
    if (this.viewingUserId) {
      this.getUserProfile(this.viewingUserId)
    } else {
      // 否则显示当前登录用户的资料
      this.initProfileForm()
      this.getUserStats()
      this.getMyCourses()
      this.getDownloadHistory()
      this.getPointsRecord()
      this.getFollowingList()
      this.getFollowersList()
      this.checkSignInStatus()
    }
  },
  methods: {
    ...mapActions('user', ['updateUserInfo']),
    initProfileForm() {
      if (this.userInfo) {
        this.profileForm = {
          username: this.userInfo.username || '',
          nickname: this.userInfo.nickname || '',
          email: this.userInfo.email || '',
          avatar: this.userInfo.avatar || '',
          bio: this.userInfo.bio || ''
        }
      }
    },
    updateProfile() {
      this.$refs.profileForm.validate(async (valid) => {
        if (valid) {
          try {
            await this.$http.put('/user/profile', this.profileForm)
            this.updateUserInfo({
              ...this.userInfo,
              ...this.profileForm
            })
            this.$message.success('保存成功')
          } catch (error) {
            console.error('更新个人资料失败:', error)
            this.$message.error('保存失败，请稍后重试')
          }
        }
      })
    },
    resetProfile() {
      this.initProfileForm()
      this.$message.info('已重置表单')
    },
    handleAvatarSuccess(response, file, fileList) {
      if (response && response.data) {
        this.profileForm.avatar = response.data
        this.$message.success('头像上传成功')
      } else {
        this.$message.error('头像上传失败')
      }
    },
    beforeAvatarUpload(file) {
      const isJPG = file.type === 'image/jpeg' || file.type === 'image/png' || file.type === 'image/gif'
      const isLt2M = file.size / 1024 / 1024 < 2

      if (!isJPG) {
        this.$message.error('只能上传 JPG、PNG 或 GIF 格式的图片')
      }
      if (!isLt2M) {
        this.$message.error('图片大小不能超过 2MB')
      }
      return isJPG && isLt2M
    },
    changePassword() {
      this.$refs.passwordForm.validate(async (valid) => {
        if (valid) {
          try {
            await this.$http.put('/user/password', this.passwordForm)
            this.$message.success('密码修改成功')
            this.passwordForm = {
              oldPassword: '',
              newPassword: '',
              confirmPassword: ''
            }
          } catch (error) {
            console.error('修改密码失败:', error)
            if (error.response && error.response.data && error.response.data.message) {
              this.$message.error(error.response.data.message)
            } else {
              this.$message.error('修改密码失败，请稍后重试')
            }
          }
        }
      })
    },
    async getUserProfile(userId) {
      try {
        const res = await this.$http.get(`/user/${userId}`)
        console.log('User profile response:', res)
        if (res) {
          // 使用返回的用户数据
          this.profileForm = {
            username: res.username || '',
            nickname: res.nickname || '',
            email: res.email || '',
            avatar: res.avatar || '',
            bio: res.bio || ''
          }
          // 设置用户统计信息
          this.userStats = {
            points: res.points || 0,
            courses: 0,
            followers: 0,
            following: 0
          }
          // 获取关注统计
          await this.getOtherUserFollowStats(userId)
          // 检查是否已关注该用户
          await this.checkIfFollowing()
        }
      } catch (error) {
        console.error('获取用户资料失败:', error)
        this.$message.error('获取用户资料失败')
      }
    },
    async getOtherUserFollowStats(userId) {
      try {
        const res = await this.$http.get(`/community/follow/count?userId=${userId}`)
        console.log('Follow count response:', res)
        if (res) {
          this.userStats.following = res.following || 0
          this.userStats.followers = res.followers || 0
        }
      } catch (error) {
        console.error('获取关注统计失败:', error)
      }
    },
    async checkIfFollowing() {
      try {
        const res = await this.$http.get(`/community/follow/check?followUserId=${this.viewingUserId}`)
        console.log('Check following response:', res)
        if (res) {
          this.isFollowingOtherUser = res.following || res || false
        }
      } catch (error) {
        console.error('检查关注状态失败:', error)
        this.isFollowingOtherUser = false
      }
    },
    async toggleFollowOtherUser() {
      try {
        if (!this.isLoggedIn) {
          this.$message.warning('请先登录')
          this.$router.push('/login')
          return
        }

        if (this.isFollowingOtherUser) {
          // 取消关注
          await this.$http.post(`/community/unfollow/${this.viewingUserId}`)
          this.isFollowingOtherUser = false
          this.userStats.following = Math.max(0, this.userStats.following - 1)
          this.$message.success('已取消关注')
        } else {
          // 关注
          await this.$http.post(`/community/follow/${this.viewingUserId}`)
          this.isFollowingOtherUser = true
          this.userStats.following += 1
          this.$message.success('关注成功')
        }
      } catch (error) {
        console.error('关注操作失败:', error)
        this.$message.error('操作失败，请稍后重试')
      }
    },
    sendMessageToUser() {
      if (!this.isLoggedIn) {
        this.$message.warning('请先登录')
        this.$router.push('/login')
        return
      }
      // 跳转到私信页面，传递用户ID
      this.$router.push(`/community/message?userId=${this.viewingUserId}`)
    },
    async getUserStats() {
      try {
        const res = await this.$http.get('/user/stats')
        console.log('User stats response:', res)
        // 拦截器已经返回 res.data，但需要确保有默认值
        if (res && typeof res === 'object') {
          this.userStats = {
            points: res.points || 0,
            courses: res.courses || 0,
            followers: res.followers || 0,
            following: res.following || 0
          }
        } else {
          this.userStats = { points: 0, courses: 0, followers: 0, following: 0 }
        }
      } catch (error) {
        console.error('获取用户统计信息失败:', error)
        this.userStats = { points: 0, courses: 0, followers: 0, following: 0 }
      }
    },
    async getFollowingList() {
      try {
        const targetUserId = this.viewingUserId || undefined
        const params = targetUserId ? { userId: targetUserId } : {}
        const res = await this.$http.get('/community/follow/following', { params })
        this.followingList = Array.isArray(res) ? res : []
      } catch (error) {
        this.followingList = []
      }
    },
    async getFollowersList() {
      try {
        const targetUserId = this.viewingUserId || undefined
        const params = targetUserId ? { userId: targetUserId } : {}
        const res = await this.$http.get('/community/follow/followers', { params })
        this.followersList = Array.isArray(res) ? res : []
      } catch (error) {
        this.followersList = []
      }
    },
    isFollowingUser(userId) {
      return this.followingList.some(user => user.id === userId)
    },
    goToUserProfile(userId) {
      this.$router.push(`/user/${userId}`)
    },
    async toggleFollow(userId) {
      try {
        if (!this.$store.state.user.token) {
          this.$message.warning('请先登录')
          this.$router.push('/login')
          return
        }

        // 检查是否已关注
        const isFollowing = this.isFollowingUser(userId)

        if (isFollowing) {
          // 取消关注 - 使用 POST /community/unfollow/{followUserId}
          await this.$confirm('确定要取消关注该用户吗？', '取消关注', {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
          })
          await this.$http.post(`/community/unfollow/${userId}`)
          this.followingList = this.followingList.filter(user => user.id !== userId)
          this.userStats.following--
          this.$message.success('已取消关注')
        } else {
          // 添加关注 - 使用 POST /community/follow/{followUserId}
          await this.$http.post(`/community/follow/${userId}`)
          const follower = this.followersList.find(user => user.id === userId)
          if (follower) {
            this.followingList.push(follower)
          }
          this.userStats.following++
          this.$message.success('关注成功')
        }
      } catch (error) {
        if (error !== 'cancel') {
          console.error('关注操作失败:', error)
          this.$message.error('关注操作失败，请稍后重试')
        }
      }
    },
    async getMyCourses() {
      try {
        const res = await this.$http.get('/course/user-courses')
        console.log('My courses response:', res)
        // 确保是数组
        this.myCourses = Array.isArray(res) ? res : (res.data || [])
      } catch (error) {
        console.error('获取我的课程失败:', error)
        this.myCourses = []
      }
    },
    async getDownloadHistory() {
      try {
        const res = await this.$http.get('/user/downloads')
        console.log('Download history response:', res)
        // 确保是数组
        this.downloadHistory = Array.isArray(res) ? res : []
      } catch (error) {
        console.error('获取下载历史失败:', error)
        this.downloadHistory = []
      }
    },
    async getPointsRecord() {
      try {
        const res = await this.$http.get('/user/points-record')
        console.log('Points record response:', res)
        // 处理后端返回的PageResult对象
        this.pointsRecord = Array.isArray(res) ? res : (res.records || [])
      } catch (error) {
        console.error('获取积分记录失败:', error)
        this.pointsRecord = []
      }
    },
    // 处理下载成功事件
    handleDownloadSuccess() {
      // 更新用户统计信息
      this.getUserStats()
      // 更新下载历史
      this.getDownloadHistory()
      // 更新积分记录
      this.getPointsRecord()
    },
    async checkSignInStatus() {
      // This is a placeholder. In a real app, you'd fetch the user's last sign-in time
      // from the backend and compare it with the current date.
      // For now, we'll assume the user has not signed in today.
      // You might also get this info as part of the user's profile data.
      if (this.userInfo && this.userInfo.lastSignInTime) {
        const lastSignInDate = new Date(this.userInfo.lastSignInTime).toDateString();
        const today = new Date().toDateString();
        this.isSignedToday = lastSignInDate === today;
      } else {
        this.isSignedToday = false;
      }
    },
    async handleSignIn() {
      try {
        // 直接调用API，因为响应拦截器已经处理了错误情况
        await this.$http.post('/user/signIn');
        // 如果没有抛出异常，说明签到成功
        this.$message.success('签到成功，获得5积分');
        this.isSignedToday = true;
        // Update user points immediately
        this.getUserStats();
        // Update userInfo in Vuex store with new lastSignInTime
        const updatedUserInfo = { ...this.userInfo, lastSignInTime: new Date().toISOString() };
        this.updateUserInfo(updatedUserInfo);
      } catch (error) {
        console.error('签到失败:', error);
        // 错误消息已经由拦截器处理
      }
    },
    handleShare() {
      this.shareDialogVisible = true
    },
    async loadBookmarkedNews() {
      try {
        const bookmarkedIds = JSON.parse(localStorage.getItem('bookmarkedNews') || '[]')
        if (!bookmarkedIds.length) {
          this.bookmarkedNewsList = []
          return
        }
        this.bookmarksLoading = true
        const results = await Promise.allSettled(
          bookmarkedIds.map(id => this.$http.get(`/news/detail/${id}`))
        )
        this.bookmarkedNewsList = results
          .filter(r => r.status === 'fulfilled' && r.value)
          .map(r => r.value)
        this.bookmarksLoading = false
      } catch (error) {
        console.error('加载收藏失败:', error)
        this.bookmarkedNewsList = []
        this.bookmarksLoading = false
      }
    },
    async removeBookmark(id) {
      try {
        await this.$confirm('确定要取消收藏该文章吗？', '取消收藏', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        let bookmarkedIds = JSON.parse(localStorage.getItem('bookmarkedNews') || '[]')
        bookmarkedIds = bookmarkedIds.filter(bid => bid !== id)
        localStorage.setItem('bookmarkedNews', JSON.stringify(bookmarkedIds))
        this.bookmarkedNewsList = this.bookmarkedNewsList.filter(n => n.id !== id)
        this.$message.success('已取消收藏')
      } catch (error) {
        if (error !== 'cancel') {
          console.error('取消收藏失败:', error)
          this.$message.error('操作失败，请稍后重试')
        }
      }
    },
    viewNewsDetail(id) {
      this.$router.push(`/news/${id}`)
    },
    formatDate(dateStr) {
      if (!dateStr) return ''
      const d = new Date(dateStr)
      const y = d.getFullYear()
      const m = String(d.getMonth() + 1).padStart(2, '0')
      const day = String(d.getDate()).padStart(2, '0')
      return `${y}-${m}-${day}`
    }
  }
}
</script>

<style scoped lang="scss">
.user {
  padding: 20px;

  .user-profile-view {
    padding: 20px 0;

    .profile-item {
      display: flex;
      align-items: flex-start;
      margin-bottom: 20px;
      padding-bottom: 15px;
      border-bottom: 1px solid #f0f0f0;

      .profile-label {
        width: 80px;
        color: #909399;
        font-size: 14px;
        flex-shrink: 0;
      }

      .profile-value {
        flex: 1;
        color: #303133;
        font-size: 14px;
        word-break: break-all;
      }
    }
  }

  .user-card {
    text-align: center;

    .user-avatar {
      padding: 20px 0;

      h2 {
        margin: 15px 0 5px;
        color: #303133;
      }

      .username {
        color: #909399;
        font-size: 14px;
      }
    }

    .user-stats {
      display: flex;
      justify-content: space-around;
      padding: 20px 0;
      border-top: 1px solid #EBEEF5;

      .stat {
        text-align: center;

        .value {
          display: block;
          font-size: 24px;
          font-weight: bold;
          color: #303133;
        }

        .label {
          font-size: 14px;
          color: #909399;
        }
      }
    }

    .sign-in-section {
      padding: 15px 0;
      border-top: 1px solid #EBEEF5;
      text-align: center;
    }
  }

  .avatar-uploader {
    width: 100px;
    height: 100px;

    .avatar {
      width: 100px;
      height: 100px;
      border-radius: 50%;
    }

    .avatar-uploader-icon {
      width: 100px;
      height: 100px;
      font-size: 28px;
      line-height: 100px;
      color: #8c939d;
      background-color: #f0f2f5;
      border-radius: 50%;
    }
  }

  .course-list {
    .course-item {
      display: flex;
      gap: 20px;
      margin-bottom: 15px;

      .course-cover {
        width: 120px;
        height: 80px;
        object-fit: cover;
        border-radius: 4px;
      }

      .course-info {
        flex: 1;

        h3 {
          margin: 0 0 10px;
          font-size: 16px;
          color: #303133;
        }

        p {
          color: #909399;
          font-size: 14px;
          margin-bottom: 10px;
        }
      }
    }

    .empty-state {
      text-align: center;
      padding: 40px 0;
    }
  }

  .text-success {
    color: #67c23a;
  }

  .text-danger {
    color: #f56c6c;
  }

  .following-list, .followers-list {
    .user-item {
      display: flex;
      align-items: center;
      gap: 15px;
      padding: 15px;
      border-bottom: 1px solid #EBEEF5;
      .user-item-main {
        display: flex; align-items: center; gap: 15px; flex: 1;
        cursor: pointer; &:hover { opacity: 0.8; }
      }
      &:last-child {
        border-bottom: none;
      }

      .user-info {
        flex: 1;

        .nickname {
          display: block;
          font-size: 15px;
          font-weight: 600;
          color: #303133;
        }

        .username {
          font-size: 13px;
          color: #909399;
        }
      }
    }

    .empty-state {
      text-align: center;
      padding: 40px 0;
    }
  }

  .user-stats {
    .stat {
      cursor: pointer;
      transition: all 0.3s ease;

      &:hover {
        .value, .label {
          color: #409EFF;
        }
      }
    }
  }

  .points-rules-card {
    margin-top: 10px;

    .points-rules-list {
      .points-rule-item {
        display: flex;
        align-items: center;
        padding: 12px 0;
        border-bottom: 1px solid #f0f0f0;

        &:last-child {
          border-bottom: none;
        }

        .points-rule-desc {
          margin-left: 12px;
          font-size: 14px;
          color: #303133;
        }
      }
    }
  }

  .bookmarks-list {
    .empty-state {
      text-align: center;
      padding: 40px 0;
    }

    .bookmark-card {
      display: flex;
      align-items: flex-start;
      gap: 16px;
      padding: 16px;
      margin-bottom: 12px;
      border: 1px solid #EBEEF5;
      border-radius: 8px;
      transition: all 0.3s ease;

      &:hover {
        border-color: #C0C4CC;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
      }

      &:last-child {
        margin-bottom: 0;
      }

      .bookmark-card-body {
        flex: 1;
        min-width: 0;

        .bookmark-title {
          margin: 0 0 8px;
          font-size: 15px;
          font-weight: 600;
          color: #303133;
          line-height: 1.4;
        }

        .bookmark-summary {
          margin: 0 0 10px;
          font-size: 13px;
          color: #606266;
          line-height: 1.5;
          display: -webkit-box;
          -webkit-line-clamp: 2;
          -webkit-box-orient: vertical;
          overflow: hidden;
        }

        .bookmark-meta {
          display: flex;
          align-items: center;
          gap: 8px;
          flex-wrap: wrap;

          .bookmark-date {
            font-size: 12px;
            color: #909399;
          }
        }
      }

      .bookmark-actions {
        display: flex;
        flex-direction: column;
        gap: 4px;
        flex-shrink: 0;

        .unbookmark-btn {
          color: #F56C6C;

          &:hover {
            color: #E14545;
          }
        }
      }
    }
  }
}
</style>
