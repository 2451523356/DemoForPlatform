<template>
  <div class="dashboard">
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon user"><i class="el-icon-user"></i></div>
            <div class="stat-info">
              <p class="stat-label">用户总数</p>
              <p class="stat-value">{{ stats.userCount }}</p>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon news"><i class="el-icon-document"></i></div>
            <div class="stat-info">
              <p class="stat-label">资讯数量</p>
              <p class="stat-value">{{ stats.newsCount }}</p>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon course"><i class="el-icon-video-camera"></i></div>
            <div class="stat-info">
              <p class="stat-label">课程数量</p>
              <p class="stat-value">{{ stats.courseCount }}</p>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon order"><i class="el-icon-s-order"></i></div>
            <div class="stat-info">
              <p class="stat-label">已支付订单</p>
              <p class="stat-value">{{ stats.paidOrderCount }}</p>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon revenue"><i class="el-icon-money"></i></div>
            <div class="stat-info">
              <p class="stat-label">销售收入</p>
              <p class="stat-value">¥{{ formatRevenue(stats.totalRevenue) }}</p>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon post"><i class="el-icon-chat-dot-round"></i></div>
            <div class="stat-info">
              <p class="stat-label">帖子数量</p>
              <p class="stat-value">{{ stats.postCount }}</p>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon comment"><i class="el-icon-chat-line-round"></i></div>
            <div class="stat-info">
              <p class="stat-label">评论数量</p>
              <p class="stat-value">{{ stats.commentCount }}</p>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20">
      <el-col :span="12">
        <el-card class="chart-card">
          <h3>用户增长趋势</h3>
          <div ref="userChart" class="chart"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card class="chart-card">
          <h3>课程销售趋势</h3>
          <div ref="salesChart" class="chart"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="12">
        <el-card class="chart-card">
          <h3>资源下载排行</h3>
          <div ref="downloadChart" class="chart"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card class="chart-card">
          <h3>社区活跃度</h3>
          <div ref="activityChart" class="chart"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="12">
        <el-card>
          <h3>最新用户</h3>
          <el-table :data="latestUsers" style="width: 100%">
            <el-table-column prop="username" label="用户名"></el-table-column>
            <el-table-column prop="nickname" label="昵称"></el-table-column>
            <el-table-column label="注册时间">
              <template slot-scope="scope">
                {{ formatTime(scope.row.createTime) }}
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <h3>最新帖子</h3>
          <el-table :data="latestPosts" style="width: 100%">
            <el-table-column prop="title" label="标题" show-overflow-tooltip></el-table-column>
            <el-table-column prop="authorName" label="作者" width="100"></el-table-column>
            <el-table-column label="发布时间" width="160">
              <template slot-scope="scope">
                {{ formatTime(scope.row.createTime) }}
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
export default {
  name: 'Dashboard',
  data() {
    return {
      stats: { userCount: 0, newsCount: 0, courseCount: 0, postCount: 0, orderCount: 0, commentCount: 0, paidOrderCount: 0, totalRevenue: 0 },
      latestUsers: [],
      latestPosts: [],
      userTrend: [],
      downloadRank: [],
      salesTrend: [],
      activityTrend: [],
      charts: []
    }
  },
  mounted() {
    this.getStats()
  },
  beforeDestroy() {
    window.removeEventListener('resize', this._resizeCharts)
    this.charts.forEach(c => c.dispose())
    this.charts = []
  },
  methods: {
    _resizeCharts() {
      this.charts.forEach(c => { try { c.resize() } catch (e) { /* ignore */ } })
    },
    formatTime(time) {
      if (!time) return ''
      return new Date(time).toLocaleString('zh-CN')
    },
    formatRevenue(amount) {
      if (!amount && amount !== 0) return '0.00'
      return Number(amount).toFixed(2)
    },
    async getStats() {
      try {
        const res = await this.$http.get('/admin/dashboard')
        if (res) {
          this.stats = {
            userCount: res.userCount || 0,
            newsCount: res.newsCount || 0,
            courseCount: res.courseCount || 0,
            postCount: res.postCount || 0,
            orderCount: res.orderCount || 0,
            commentCount: res.commentCount || 0,
            paidOrderCount: res.paidOrderCount || 0,
            totalRevenue: res.totalRevenue || 0
          }
          this.latestUsers = res.latestUsers || []
          this.latestPosts = res.latestPosts || []
          this.userTrend = res.userTrend || []
          this.downloadRank = res.downloadRank || []
          this.salesTrend = res.salesTrend || []
          this.activityTrend = res.activityTrend || []
          this.$nextTick(() => this.initCharts())
        }
      } catch (error) {
        console.error('获取数据看板失败:', error)
      }
    },
    initCharts() {
      this.charts.forEach(c => c.dispose())
      this.charts = []
      this.initUserChart()
      this.initSalesChart()
      this.initDownloadChart()
      this.initActivityChart()
      window.removeEventListener('resize', this._resizeCharts)
      window.addEventListener('resize', this._resizeCharts)
    },
    initUserChart() {
      if (!this.$refs.userChart) return
      const chart = this.$echarts.init(this.$refs.userChart)
      this.charts.push(chart)
      const months = this.userTrend.map(i => i.month)
      const data = this.userTrend.map(i => i.count)
      chart.setOption({
        tooltip: { trigger: 'axis' },
        xAxis: { type: 'category', data: months },
        yAxis: { type: 'value' },
        series: [{ data, type: 'line', smooth: true, areaStyle: { color: 'rgba(102,126,234,0.2)' } }]
      })
    },
    initSalesChart() {
      if (!this.$refs.salesChart) return
      const chart = this.$echarts.init(this.$refs.salesChart)
      this.charts.push(chart)
      const months = this.salesTrend.map(i => i.month)
      const data = this.salesTrend.map(i => i.amount)
      chart.setOption({
        tooltip: { trigger: 'axis', formatter: '{b}: ¥{c}' },
        xAxis: { type: 'category', data: months },
        yAxis: { type: 'value', name: '销售额(元)' },
        series: [{ data, type: 'bar', itemStyle: { color: '#67C23A' } }]
      })
    },
    initDownloadChart() {
      if (!this.$refs.downloadChart) return
      const chart = this.$echarts.init(this.$refs.downloadChart)
      this.charts.push(chart)
      const names = this.downloadRank.map(i => i.title)
      const data = this.downloadRank.map(i => i.count)
      chart.setOption({
        tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
        xAxis: { type: 'value' },
        yAxis: { type: 'category', data: names, axisLabel: { width: 100, overflow: 'truncate' } },
        series: [{ type: 'bar', data, itemStyle: { color: '#E6A23C' } }]
      })
    },
    initActivityChart() {
      if (!this.$refs.activityChart) return
      const chart = this.$echarts.init(this.$refs.activityChart)
      this.charts.push(chart)
      const months = this.activityTrend.map(i => i.month)
      const data = this.activityTrend.map(i => i.count)
      chart.setOption({
        tooltip: { trigger: 'axis' },
        xAxis: { type: 'category', data: months },
        yAxis: { type: 'value' },
        series: [{ data, type: 'line', smooth: true, itemStyle: { color: '#F56C6C' }, areaStyle: { color: 'rgba(245,108,108,0.2)' } }]
      })
    }
  }
}
</script>

<style scoped lang="scss">
.dashboard {
  .stats-row { margin-bottom: 20px; }
  .stat-card {
    .stat-content {
      display: flex; align-items: center; gap: 20px;
      .stat-icon {
        width: 60px; height: 60px; border-radius: 8px;
        display: flex; align-items: center; justify-content: center;
        i { font-size: 30px; color: #fff; }
        &.user { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); }
        &.news { background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%); }
        &.course { background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%); }
        &.order { background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%); }
        &.revenue { background: linear-gradient(135deg, #f7971e 0%, #ffd200 100%); }
        &.post { background: linear-gradient(135deg, #fa709a 0%, #fee140 100%); }
        &.comment { background: linear-gradient(135deg, #a18cd1 0%, #fbc2eb 100%); }
      }
      .stat-info {
        .stat-label { font-size: 14px; color: #909399; margin-bottom: 5px; }
        .stat-value { font-size: 24px; font-weight: bold; color: #303133; }
      }
    }
  }
  .chart-card {
    h3 { font-size: 16px; margin-bottom: 20px; color: #303133; }
    .chart { height: 300px; }
  }
}
</style>
