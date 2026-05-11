<template>
  <div class="live-list-page">
    <div class="page-header"><h1>考研直播</h1><p>名师直播授课，实时互动答疑</p></div>
    <el-card>
      <el-table :data="liveList" stripe v-loading="loading">
        <el-table-column prop="title" label="直播标题" />
        <el-table-column prop="teacherName" label="讲师" width="100" />
        <el-table-column prop="startTime" label="开始时间" width="180" />
        <el-table-column prop="viewerCount" label="观看人数" width="100" />
        <el-table-column label="状态" width="100"><template slot-scope="s"><el-tag :type="s.row.status===0?'info':s.row.status===1?'danger':'info'">{{ s.row.status===0?'未开始':s.row.status===1?'直播中':'已结束' }}</el-tag></template></el-table-column>
        <el-table-column label="操作" width="120"><template slot-scope="s"><el-button v-if="s.row.status===0" type="info" size="small" disabled>未开始</el-button><el-button v-if="s.row.status===1" type="danger" size="small" @click="enterLive(s.row)">进入直播间</el-button><el-button v-if="s.row.status===2" type="default" size="small" @click="viewReplay(s.row)">回放</el-button></template></el-table-column>
      </el-table>
      <el-pagination style="margin-top:16px" @current-change="handlePage" :current-page="page" :page-size="size" :total="total" layout="total,prev,pager,next"/>
    </el-card>
  </div>
</template>
<script>
export default {
  name: 'LiveList',
  data() { return { liveList:[], loading:false, page:1, size:10, total:0 } },
  created() { this.fetch() },
  methods: {
    async fetch() { this.loading=true; try { const res = await this.$http.get('/live/list',{params:{page:this.page,size:this.size}}); this.liveList=res?.records||[]; this.total=res?.total||0 } catch(e){} finally { this.loading=false } },
    handlePage(p) { this.page=p; this.fetch() },
    enterLive(row) { this.$router.push(`/live/${row.id}`) },
    viewReplay(row) { if(row.courseId) this.$router.push(`/course/${row.courseId}/learning`) }
  }
}
</script>
<style scoped>
.live-list-page { padding:20px; max-width:1200px; margin:0 auto; }
.page-header { margin-bottom:20px; } .page-header h1 { font-size:24px; margin-bottom:8px; } .page-header p { color:#909399; }
</style>
