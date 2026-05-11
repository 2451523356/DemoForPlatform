<template>
  <div class="message">
    <div class="message-header">
      <h1>私信</h1>
    </div>

    <div class="message-content">
      <el-row :gutter="20">
        <el-col :span="6">
          <div class="contact-list">
            <h3>联系人</h3>
            <!-- 搜索用户 -->
            <div class="contact-search">
              <el-input v-model="searchKeyword" placeholder="搜索用户发起私聊" size="small" clearable
                prefix-icon="el-icon-search" @input="searchUsers" />
            </div>
            <!-- 搜索结果 -->
            <div v-if="searchKeyword && searchResults.length > 0" class="search-results">
              <div class="search-results-header">搜索结果</div>
              <div v-for="user in searchResults" :key="'s'+user.id" class="contact-item search-item"
                @click="startChat(user)">
                <el-avatar :size="36" :src="user.avatar">{{ (user.nickname || user.username).charAt(0) }}</el-avatar>
                <div class="contact-info">
                  <div class="contact-name">{{ user.nickname || user.username }}</div>
                  <div class="contact-last-message">@{{ user.username }}</div>
                </div>
                <i class="el-icon-chat-dot-round" style="color:#409EFF"></i>
              </div>
            </div>
            <div v-if="searchKeyword && searchResults.length === 0" class="search-results">
              <div class="search-results-header">搜索结果</div>
              <el-empty description="未找到用户" :image-size="40" />
            </div>
            <div class="contacts">
              <div v-if="contacts.length === 0 && searchResults.length === 0" class="empty-contacts">
                <el-empty description="暂无联系人，搜索用户发起私聊" :image-size="60" />
              </div>
              <div
                v-for="contact in contacts"
                :key="contact.userId"
                class="contact-item"
                :class="{ active: activeContact === contact.userId }"
                @click="selectContact(contact)"
              >
                <el-avatar :size="40" :src="contact.avatar">{{ contact.nickname?.charAt(0) || contact.username?.charAt(0) }}</el-avatar>
                <div class="contact-info">
                  <div class="contact-name">{{ contact.nickname || contact.username }}</div>
                  <div class="contact-last-message">{{ contact.lastMessage || '暂无消息' }}</div>
                </div>
                <div v-if="contact.unreadCount > 0" class="unread-badge">{{ contact.unreadCount }}</div>
              </div>
            </div>
          </div>
        </el-col>

        <el-col :span="18">
          <div v-if="activeContact" class="chat-container">
            <div class="chat-header">
              <el-avatar :size="40" :src="currentContact?.avatar">{{ currentContact?.nickname?.charAt(0) || currentContact?.username?.charAt(0) }}</el-avatar>
              <div class="chat-title">
                <h3>{{ currentContact?.nickname || currentContact?.username }}</h3>
                <span class="online-status">在线</span>
              </div>
            </div>
            <div ref="messagesContainer" class="chat-messages">
              <div
                v-for="msg in messages"
                :key="msg.id || msg.timestamp"
                class="message-item"
                :class="{ 'own-message': msg.fromUserId === currentUserId }"
              >
                <el-avatar
                  v-if="msg.fromUserId !== currentUserId"
                  :size="36"
                  :src="currentContact?.avatar"
                >{{ currentContact?.nickname?.charAt(0) || currentContact?.username?.charAt(0) }}</el-avatar>
                <div class="message-body">
                  <div class="message-content">
                    {{ msg.content }}
                  </div>
                  <div class="message-time">{{ formatTime(msg.createTime) }}</div>
                </div>
              </div>
            </div>
            <div class="chat-input">
              <el-input
                v-model="messageContent"
                type="textarea"
                :rows="2"
                placeholder="输入消息..."
                @keyup.enter.native="sendMessage"
                @input="handleInput"
              ></el-input>
              <el-button
                type="primary"
                @click="sendMessage"
                :disabled="!messageContent.trim()"
              >发送</el-button>
            </div>
          </div>
          <div v-else class="no-contact">
            <el-empty description="请选择一个联系人开始聊天" />
          </div>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script>
import { mapGetters } from 'vuex'

export default {
  name: 'Message',
  data() {
    return {
      contacts: [],
      activeContact: null,
      currentContact: null,
      messages: [],
      messageContent: '',
      currentUserId: 0,
      messageRefreshTimer: null,
      searchKeyword: '',
      searchResults: []
    }
  },
  computed: {
    ...mapGetters('user', ['userInfo'])
  },
  async created() {
    this.getCurrentUserId()
    await this.getContacts()
    // 检查URL参数是否有指定要聊天的用户（必须在 getContacts 之后，防止被覆盖）
    await this.checkUrlParamUser()
    // 定时刷新消息
    this.startMessageRefresh()
  },
  beforeDestroy() {
    if (this.messageRefreshTimer) {
      clearInterval(this.messageRefreshTimer)
    }
  },
  methods: {
    getCurrentUserId() {
      this.currentUserId = this.userInfo?.id || 0
      if (!this.currentUserId) {
        try {
          const raw = localStorage.getItem('userInfo')
          if (raw) {
            const parsed = JSON.parse(raw)
            this.currentUserId = parsed?.id || 0
          }
        } catch (e) {
          this.currentUserId = 0
        }
      }
      if (!this.currentUserId) {
        this.$message.warning('用户信息加载失败，请重新登录')
        this.$router.push('/login')
      }
    },
    async searchUsers() {
      const keyword = this.searchKeyword.trim()
      if (!keyword) { this.searchResults = []; return }
      try {
        const res = await this.$http.get('/community/message/search-user', {
          params: { keyword }, hideError: true
        })
        this.searchResults = Array.isArray(res) ? res.filter(u => u.id !== this.currentUserId) : []
      } catch (e) {
        this.searchResults = []
      }
    },
    async loadSuggestedContacts() {
      try {
        const res = await this.$http.get('/community/message/search-user', {
          params: { keyword: '' }, hideError: true
        })
        if (Array.isArray(res) && res.length > 0) {
          this.contacts = res.filter(u => u.id !== this.currentUserId).map(u => ({
            userId: u.id,
            username: u.username,
            nickname: u.nickname || u.username,
            avatar: u.avatar,
            lastMessage: '发起私聊',
            unreadCount: 0
          }))
        }
      } catch (e) { /* ignore */ }
    },
    async startChat(user) {
      if (!user || !user.id) return
      const exists = this.contacts.find(c => c.userId === user.id)
      if (exists) {
        this.selectContact(exists)
        this.searchKeyword = ''
        this.searchResults = []
        return
      }
      const newContact = {
        userId: user.id,
        username: user.username,
        nickname: user.nickname || user.username,
        avatar: user.avatar,
        lastMessage: '开始聊天',
        unreadCount: 0
      }
      this.contacts.unshift(newContact)
      this.searchKeyword = ''
      this.searchResults = []
      this.selectContact(newContact)
    },
    async getContacts() {
      try {
        const res = await this.$http.get('/community/message/contacts', { hideError: true })
        if (Array.isArray(res)) {
          this.contacts = res.map(contact => ({
            userId: contact.userId || contact.id,
            username: contact.username,
            nickname: contact.nickname,
            avatar: contact.avatar,
            lastMessage: contact.lastMessage || '暂无消息',
            unreadCount: contact.unreadCount || 0,
            lastTime: contact.lastTime
          }))
        } else {
          this.contacts = []
        }
      } catch (error) {
        console.error('获取联系人失败:', error)
        this.contacts = []
      }
      // 无消息联系人的话，显示推荐用户列表
      if (this.contacts.length === 0) {
        this.loadSuggestedContacts()
      }
    },
    async selectContact(contact) {
      this.activeContact = contact.userId
      this.currentContact = contact
      await this.getMessages(contact.userId)
      // 标记为已读
      await this.markAsRead(contact.userId)
    },
    async getMessages(otherUserId) {
      try {
        const res = await this.$http.get(`/community/message/chat/${otherUserId}`, { hideError: true })
        if (res && res.records) {
          // 处理分页数据
          this.messages = res.records.map(msg => ({
            id: msg.id,
            fromUserId: msg.fromUserId || msg.from_user_id,
            toUserId: msg.toUserId || msg.to_user_id,
            content: msg.content,
            createTime: msg.createTime || msg.create_time
          }))
        } else {
          this.messages = []
        }

        this.scrollToBottom()
      } catch (error) {
        console.error('获取消息失败:', error)
        this.messages = []
        this.scrollToBottom()
      }
    },
    async sendMessage() {
      if (!this.messageContent.trim() || !this.activeContact) return

      // 先添加到本地消息列表
      const newMessage = {
        id: Date.now(),
        fromUserId: this.currentUserId,
        toUserId: this.activeContact,
        content: this.messageContent.trim(),
        createTime: new Date().toISOString()
      }
      this.messages.push(newMessage)

      try {
        await this.$http.post('/community/message', {
          toUserId: this.activeContact,
          content: this.messageContent.trim()
        })

        // 更新联系人列表中的最后消息
        this.updateContactLastMessage(this.activeContact, this.messageContent.trim())
        // 发送后立即刷新联系人列表
        this.getContacts()
      } catch (error) {
        console.error('发送消息失败:', error)
        // 移除已添加的消息
        this.messages.pop()
        this.$message.error('发送消息失败，请稍后重试')
      } finally {
        this.messageContent = ''
        this.scrollToBottom()
      }
    },
    updateContactLastMessage(userId, message) {
      const contact = this.contacts.find(c => c.userId === userId)
      if (contact) {
        contact.lastMessage = message.length > 20 ? message.substring(0, 20) + '...' : message
        contact.lastTime = new Date().toISOString()
      }
    },
    async markAsRead(userId) {
      try {
        await this.$http.post(`/community/message/${userId}/read/all`, null, { hideError: true })
        const contact = this.contacts.find(c => c.userId === userId)
        if (contact) {
          contact.unreadCount = 0
        }
        window.dispatchEvent(new Event('refresh-unread-messages'))
      } catch (error) {
        console.error('标记已读失败:', error)
      }
    },
    scrollToBottom() {
      setTimeout(() => {
        const container = this.$refs.messagesContainer
        if (container) {
          container.scrollTop = container.scrollHeight
        }
      }, 100)
    },
    async checkUrlParamUser() {
      // 从URL参数获取要聊天的用户ID
      const userId = this.$route.query.userId
      if (userId) {
        const targetUserId = parseInt(userId)
        if (targetUserId && targetUserId !== this.currentUserId) {
          // 检查是否已在联系人列表中
          const existingContact = this.contacts.find(c => c.userId === targetUserId)
          if (existingContact) {
            await this.selectContact(existingContact)
          } else {
            // 如果不在联系人列表中，创建一个临时联系人
            await this.createTempContact(targetUserId)
          }
        }
      }
    },
    async createTempContact(userId) {
      try {
        // 获取用户信息
        const res = await this.$http.get(`/user/${userId}`)
        if (res && res.id) {
          const tempContact = {
            userId: res.id,
            username: res.username,
            nickname: res.nickname,
            avatar: res.avatar,
            lastMessage: '开始聊天',
            unreadCount: 0,
            lastTime: new Date().toISOString()
          }
          this.contacts.unshift(tempContact)
          this.selectContact(tempContact)
        }
      } catch (error) {
        console.error('获取用户信息失败:', error)
        this.$message.error('无法加载该用户信息')
      }
    },
    handleInput() {
      // 可以在这里添加输入状态提示
    },
    formatTime(time) {
      if (!time) return ''
      try {
        const date = new Date(time)
        const now = new Date()
        const diff = now - date

        // 今天的消息显示时分
        if (diff < 86400000 && date.toDateString() === now.toDateString()) {
          return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
        }
        // 今年的消息显示月日时分
        if (date.getFullYear() === now.getFullYear()) {
          return `${date.getMonth() + 1}月${date.getDate()}日 ${date.getHours()}:${String(date.getMinutes()).padStart(2, '0')}`
        }
        // 其他显示完整日期
        return date.toLocaleString('zh-CN', { month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' })
      } catch (error) {
        return time
      }
    },
    startMessageRefresh() {
      this.messageRefreshTimer = setInterval(() => {
        const prevCount = this.totalUnread()
        this.getContacts().then(() => {
          const newCount = this.totalUnread()
          if (newCount > prevCount) {
            this.$message.info(`您有 ${newCount - prevCount} 条新消息`)
          }
        })
        if (this.activeContact) {
          this.getMessages(this.activeContact)
        }
      }, 10000)
    },
    totalUnread() {
      return this.contacts.reduce((sum, c) => sum + (c.unreadCount || 0), 0)
    },
  }
}
</script>

<style scoped lang="scss">
.message {
  padding: 20px;
  max-width: 1400px;
  margin: 0 auto;

  .message-header {
    margin-bottom: 20px;
    padding-bottom: 15px;
    border-bottom: 1px solid #EBEEF5;

    h1 {
      font-size: 24px;
      color: #303133;
      margin: 0;
    }
  }

  .contact-list {
    background: #fff;
    border-radius: 8px;
    box-shadow: 0 2px 12px rgba(0,0,0,0.08);
    overflow: hidden;

    h3 {
      font-size: 16px;
      margin: 0;
      padding: 15px 20px;
      color: #303133;
      background: #F5F7FA;
      border-bottom: 1px solid #EBEEF5;
    }

    .contact-search { padding: 10px 15px; border-bottom: 1px solid #EBEEF5; }
    .search-results {
      border-bottom: 1px solid #EBEEF5;
      .search-results-header { padding: 8px 15px; font-size: 12px; color: #909399; background: #fafafa; }
      .search-item { padding: 10px 15px; }
    }
    .empty-contacts { padding: 20px; }

    .contacts {
      max-height: 600px;
      overflow-y: auto;

      .contact-item {
        display: flex;
        align-items: center;
        gap: 12px;
        padding: 15px 20px;
        border-bottom: 1px solid #F0F2F5;
        cursor: pointer;
        transition: all 0.3s ease;

        &:hover {
          background: #F5F7FA;
        }

        &.active {
          background: #ECF5FF;
          border-left: 3px solid #409EFF;
        }

        .contact-info {
          flex: 1;
          min-width: 0;

          .contact-name {
            font-size: 14px;
            font-weight: 600;
            color: #303133;
            margin-bottom: 4px;
          }

          .contact-last-message {
            font-size: 12px;
            color: #909399;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
          }
        }

        .unread-badge {
          background: #F56C6C;
          color: #fff;
          border-radius: 10px;
          min-width: 20px;
          height: 20px;
          line-height: 20px;
          text-align: center;
          font-size: 12px;
          flex-shrink: 0;
        }
      }
    }
  }

  .chat-container {
    background: #fff;
    border-radius: 8px;
    box-shadow: 0 2px 12px rgba(0,0,0,0.08);
    height: 600px;
    display: flex;
    flex-direction: column;
    overflow: hidden;

    .chat-header {
      display: flex;
      align-items: center;
      gap: 12px;
      padding: 15px 20px;
      border-bottom: 1px solid #EBEEF5;
      background: #F5F7FA;

      .chat-title {
        flex: 1;

        h3 {
          margin: 0 0 4px 0;
          font-size: 16px;
          color: #303133;
        }

        .online-status {
          font-size: 12px;
          color: #67C23A;
        }
      }
    }

    .chat-messages {
      flex: 1;
      padding: 20px;
      overflow-y: auto;
      background: #F8F9FA;

      .message-item {
        display: flex;
        gap: 10px;
        margin-bottom: 20px;
        max-width: 75%;

        &.own-message {
          margin-left: auto;
          flex-direction: row-reverse;

          .message-body {
            align-items: flex-end;
          }

          .message-content {
            background: #409EFF;
            color: #fff;
            border-radius: 18px 18px 4px 18px;
          }
        }

        .message-body {
          display: flex;
          flex-direction: column;
        }

        .message-content {
          background: #fff;
          padding: 12px 16px;
          border-radius: 18px 18px 18px 4px;
          font-size: 14px;
          line-height: 1.6;
          box-shadow: 0 1px 3px rgba(0,0,0,0.05);
        }

        .message-time {
          font-size: 12px;
          color: #909399;
          margin-top: 6px;
          text-align: right;
          padding: 0 4px;
        }
      }
    }

    .chat-input {
      padding: 15px 20px;
      border-top: 1px solid #EBEEF5;
      display: flex;
      gap: 12px;
      background: #fff;

      .el-input {
        flex: 1;

        textarea {
          resize: none;
          border-radius: 8px;
        }
      }

      .el-button {
        flex-shrink: 0;
        height: 40px;
        align-self: flex-end;
      }
    }
  }

  .no-contact {
    height: 600px;
    display: flex;
    align-items: center;
    justify-content: center;
    background: #fff;
    border-radius: 8px;
    box-shadow: 0 2px 12px rgba(0,0,0,0.08);
  }
}
</style>
