<template>
  <div>
    <!-- 移动端底部弹出式分享面板 -->
    <el-dialog :visible.sync="visible" width="100%" :fullscreen="false" top="auto"
      :show-close="true" :close-on-click-modal="true" :modal="true"
      custom-class="share-bottom-sheet" :append-to-body="true">

      <div class="share-panel" v-if="visible">
        <h3 class="share-panel-title">分享给研友</h3>

        <!-- 分享渠道 -->
        <div class="share-channels">
          <!-- 微信 - 扫码分享 -->
          <div class="channel-item" @click="mode='wechat'">
            <div class="channel-icon wechat"><span>微</span></div>
            <span class="channel-label">微信</span>
          </div>

          <!-- QQ -->
          <div class="channel-item" @click="shareQQ">
            <div class="channel-icon qq"><span>Q</span></div>
            <span class="channel-label">QQ</span>
          </div>

          <!-- 微博 -->
          <div class="channel-item" @click="shareWeibo">
            <div class="channel-icon weibo"><span>微</span></div>
            <span class="channel-label">微博</span>
          </div>

          <!-- 复制链接 -->
          <div class="channel-item" @click="copyLink">
            <div class="channel-icon copy"><i class="el-icon-link"></i></div>
            <span class="channel-label">复制链接</span>
          </div>

          <!-- 分享海报 -->
          <div class="channel-item" @click="showPoster">
            <div class="channel-icon poster"><i class="el-icon-picture-outline"></i></div>
            <span class="channel-label">生成海报</span>
          </div>
        </div>

        <!-- 微信扫码弹窗 -->
        <div v-if="mode === 'wechat'" class="wechat-qr-section">
          <p class="qr-hint">打开微信"扫一扫"扫描二维码<br />即可分享给好友或朋友圈</p>
          <div class="qr-wrap">
            <img :src="qrCodeUrl" alt="微信扫码分享" class="qr-image" @error="onQrError" />
            <div v-if="qrFailed" class="qr-fallback">
              <p>二维码加载失败</p>
              <el-button type="primary" size="mini" @click="copyLink">复制链接发送给微信好友</el-button>
            </div>
          </div>
        </div>
      </div>
    </el-dialog>

    <!-- 海报预览弹窗 -->
    <el-dialog title="分享海报" :visible.sync="posterVisible" width="360px" :append-to-body="true"
      :close-on-click-modal="true">
      <div class="poster-container" v-if="posterVisible" ref="posterEl">
        <div class="poster-card">
          <div class="poster-card-header">考研学习平台</div>
          <div class="poster-card-title">{{ shareTitle }}</div>
          <div class="poster-card-desc" v-if="shareDesc">{{ shareDesc }}</div>
          <div class="poster-card-qr">
            <img :src="posterQrUrl" alt="扫码查看" class="poster-qr-img" />
            <span>扫码查看详情</span>
          </div>
          <div class="poster-card-footer">长按或截图保存 · 分享给研友</div>
        </div>
      </div>
      <span slot="footer">
        <el-button type="primary" size="small" style="width:100%" @click="savePosterHint">
          截图保存海报
        </el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: 'ShareDialog',
  props: {
    value: { type: Boolean, default: false },
    shareUrl: { type: String, default: '' },
    shareTitle: { type: String, default: '' },
    shareDesc: { type: String, default: '' }
  },
  data() {
    return {
      mode: '',
      qrFailed: false,
      posterVisible: false
    }
  },
  computed: {
    visible: {
      get() { return this.value },
      set(val) {
        this.$emit('input', val)
        if (!val) this.mode = ''
      }
    },
    realUrl() { return this.shareUrl || window.location.href },
    qrCodeUrl() {
      return `https://api.qrserver.com/v1/create-qr-code/?size=200x200&data=${encodeURIComponent(this.realUrl)}&margin=10`
    },
    posterQrUrl() {
      return `https://api.qrserver.com/v1/create-qr-code/?size=120x120&data=${encodeURIComponent(this.realUrl)}&margin=5`
    }
  },
  methods: {
    // 动态更新页面 meta 标签（利于微信/QQ 分享卡片）
    updateMeta() {
      const setMeta = (id, content) => {
        const el = document.getElementById(id)
        if (el) el.setAttribute('content', content || '')
      }
      setMeta('og-title', this.shareTitle || '考研学习社区平台')
      setMeta('og-desc', this.shareDesc || '考研资讯、在线课程、备考资料、研友社区')
      setMeta('og-url', this.realUrl)
      setMeta('wx-title', this.shareTitle || '考研学习社区平台')
      setMeta('wx-desc', this.shareDesc || '考研资讯、在线课程、备考资料、研友社区')
    },

    shareQQ() {
      this.updateMeta()
      const url = encodeURIComponent(this.realUrl)
      const title = encodeURIComponent(this.shareTitle || document.title)
      window.open(`https://connect.qq.com/widget/shareqq/index.html?url=${url}&title=${title}&desc=${encodeURIComponent(this.shareDesc || '')}`, '_blank', 'width=600,height=400')
    },

    shareWeibo() {
      this.updateMeta()
      const url = encodeURIComponent(this.realUrl)
      const title = encodeURIComponent(this.shareTitle + ' ' + this.realUrl)
      window.open(`https://service.weibo.com/share/share.php?url=${url}&title=${title}`, '_blank', 'width=600,height=400')
    },

    async copyLink() {
      const text = this.shareTitle ? `${this.shareTitle}\n${this.realUrl}` : this.realUrl
      try {
        await navigator.clipboard.writeText(text)
        this.$message.success('已复制到剪贴板，可直接粘贴分享')
      } catch (e) {
        const ta = document.createElement('textarea')
        ta.value = text
        ta.style.position = 'fixed'; ta.style.opacity = '0'
        document.body.appendChild(ta)
        ta.select()
        document.execCommand('copy')
        document.body.removeChild(ta)
        this.$message.success('已复制，打开微信/QQ粘贴即可分享')
      }
    },

    showPoster() {
      this.visible = false
      this.$nextTick(() => { this.posterVisible = true })
    },

    savePosterHint() {
      this.$message.success('请在预览区域长按截图保存海报图片')
    },

    onQrError() {
      this.qrFailed = true
    }
  }
}
</script>

<style lang="scss">
/* 底部弹出式面板（非 scoped 以覆盖 el-dialog 样式） */
.share-bottom-sheet {
  position: fixed !important;
  bottom: 0 !important;
  top: auto !important;
  left: 0 !important;
  right: 0 !important;
  margin: 0 !important;
  width: 100% !important;
  max-width: 100% !important;
  border-radius: 16px 16px 0 0 !important;
  overflow: hidden;

  .el-dialog__header { padding: 12px 16px 0; border-bottom: none; }
  .el-dialog__body { padding: 8px 20px 24px; }
}

.share-panel {
  .share-panel-title {
    text-align: center; font-size: 17px; font-weight: 600;
    color: #303133; margin: 0 0 20px;
  }
}

.share-channels {
  display: flex; justify-content: space-around; align-items: flex-start;
  flex-wrap: wrap; gap: 8px;
  .channel-item {
    display: flex; flex-direction: column; align-items: center; gap: 8px;
    cursor: pointer; padding: 8px 12px; transition: transform .2s; min-width: 56px;
    &:active { transform: scale(.92); }
    .channel-icon {
      width: 52px; height: 52px; border-radius: 50%; display: flex;
      align-items: center; justify-content: center; font-size: 22px;
      color: #fff; font-weight: 700; box-shadow: 0 2px 8px rgba(0,0,0,.12);
      &.wechat { background: #07c160; }
      &.qq { background: #12b7f5; }
      &.weibo { background: #e6162d; }
      &.copy { background: #606266; }
      &.poster { background: #e6a23c; }
    }
    .channel-label { font-size: 12px; color: #606266; }
  }
}

.wechat-qr-section {
  text-align: center; margin-top: 20px; padding-top: 16px;
  border-top: 1px solid #f0f0f0;
  .qr-hint { font-size: 14px; color: #606266; margin: 0 0 16px; line-height: 1.6; }
  .qr-wrap {
    .qr-image { width: 160px; height: 160px; border: 1px solid #ebeef5; border-radius: 8px; }
    .qr-fallback { padding: 20px; p { color: #909399; margin: 0 0 12px; } }
  }
}
</style>

<style scoped lang="scss">
.poster-container {
  .poster-card {
    width: 300px; margin: 0 auto; padding: 28px 20px;
    background: linear-gradient(160deg, #1a3a5c 0%, #2d6aa0 100%);
    border-radius: 12px; color: #fff; text-align: center;
    .poster-card-header { font-size: 18px; font-weight: 700; margin-bottom: 14px; opacity: .9; }
    .poster-card-title { font-size: 16px; font-weight: 600; margin-bottom: 8px; line-height: 1.5; }
    .poster-card-desc { font-size: 13px; opacity: .8; margin-bottom: 18px; line-height: 1.5; }
    .poster-card-qr {
      margin-bottom: 10px;
      .poster-qr-img { width: 100px; height: 100px; background: #fff; border-radius: 8px; padding: 4px; }
      span { display: block; font-size: 11px; opacity: .7; margin-top: 6px; }
    }
    .poster-card-footer { font-size: 11px; opacity: .5; }
  }
}
</style>
