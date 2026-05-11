<template>
  <div class="live-room">
    <div class="live-header"><el-button type="text" @click="$router.back()" icon="el-icon-arrow-left">返回</el-button><h2>{{ live.title }}</h2><el-tag type="danger" v-if="live.status===1">直播中</el-tag><span class="viewer-count"><i class="el-icon-user"></i> {{ live.viewerCount || 0 }}人观看</span></div>
    <div class="live-body">
      <div class="video-area">
        <div class="player-placeholder"><i class="el-icon-video-camera" style="font-size:64px;color:#fff;opacity:0.5"/><p style="color:#fff;opacity:0.7">直播视频区域</p><p style="color:#fff;opacity:0.5;font-size:12px">{{ live.playUrl || '等待直播开始...' }}</p></div>
      </div>
      <div class="chat-area">
        <div class="danmaku-layer" ref="danmakuLayer"><div v-for="dm in danmakuList" :key="dm.id" class="danmaku-item" :style="{color:dm.color}"><b>{{ dm.nickname || '用户'+dm.userId }}:</b> {{ dm.content }}</div></div>
        <div class="chat-input"><el-input v-model="dmInput" placeholder="发送弹幕..." @keyup.enter.native="sendDanmaku" size="small"><el-button slot="append" @click="sendDanmaku">发送</el-button></el-input></div>
      </div>
    </div>
  </div>
</template>
<script>
export default {
  name: 'LiveRoom',
  data() { return { live:{}, danmakuList:[], dmInput:'', ws:null, pollTimer:null } },
  created() { this.fetchLive(); this.startPolling() },
  beforeDestroy() { this.stopPolling() },
  methods: {
    async fetchLive() { try { this.live = await this.$http.get(`/live/detail/${this.$route.params.id}`) || {} } catch(e) {} },
    startPolling() { this.fetchDanmaku(); this.pollTimer=setInterval(()=>{ this.fetchDanmaku() }, 3000) },
    stopPolling() { if(this.pollTimer) clearInterval(this.pollTimer) },
    async fetchDanmaku() { try { const res = await this.$http.get('/live/danmaku',{params:{liveStreamId:this.$route.params.id}}); if(res) this.danmakuList=res||[] } catch(e) {} },
    async sendDanmaku() { if(!this.dmInput.trim()) return; try { await this.$http.post('/live/danmaku',{liveStreamId:parseInt(this.$route.params.id),content:this.dmInput,color:'#FFFFFF'}); this.dmInput=''; this.fetchDanmaku() } catch(e){} }
  }
}
</script>
<style scoped>
.live-room { padding:20px; }
.live-header { display:flex; align-items:center; gap:12px; margin-bottom:16px; }
.viewer-count { margin-left:auto; color:#909399; }
.live-body { display:flex; gap:16px; }
.video-area { flex:1; min-height:500px; }
.player-placeholder { background:#1a1a2e; width:100%; height:500px; display:flex; flex-direction:column; justify-content:center; align-items:center; border-radius:8px; }
.chat-area { width:320px; display:flex; flex-direction:column; background:#f5f7fa; border-radius:8px; }
.danmaku-layer { flex:1; overflow-y:auto; padding:12px; max-height:460px; }
.danmaku-item { padding:4px 0; font-size:13px; line-height:1.5; }
.chat-input { padding:12px; border-top:1px solid #e4e7ed; }
</style>
