<template>
  <div class="national-score-page">
    <div class="page-header">
      <h1>全国硕士研究生招生考试国家线</h1>
      <p>教育部公布的历年考研国家分数线（考生进入复试的初试成绩基本要求）</p>
    </div>

    <!-- 筛选 -->
    <el-card>
      <div class="filter-bar">
        <el-select v-model="filterYear" placeholder="年份" clearable @change="search" style="width:110px">
          <el-option v-for="y in years" :key="y" :label="y+'年'" :value="y" />
        </el-select>
        <el-select v-model="filterCategory" placeholder="学科门类" clearable filterable @change="search" style="width:160px">
          <el-option v-for="c in categories" :key="c" :label="c" :value="c" />
        </el-select>
        <el-select v-model="filterDegreeType" placeholder="学位类型" clearable @change="search" style="width:110px">
          <el-option label="学术型" value="学术型" />
          <el-option label="专业型" value="专业型" />
        </el-select>
        <el-radio-group v-model="filterRegion" size="small" @change="search">
          <el-radio-button label="A">A类地区</el-radio-button>
          <el-radio-button label="B">B类地区</el-radio-button>
        </el-radio-group>
        <el-button type="primary" @click="search" icon="el-icon-search">搜索</el-button>
        <el-button @click="resetFilter">重置</el-button>
      </div>

      <el-table :data="tableData" stripe v-loading="loading" border style="margin-top:16px">
        <el-table-column prop="year" label="年份" width="80" align="center" />
        <el-table-column prop="subjectCategory" label="学科门类" min-width="130" />
        <el-table-column prop="degreeType" label="学位类型" width="90" align="center">
          <template slot-scope="s">
            <el-tag :type="s.row.degreeType==='学术型'?'':'success'" size="mini">{{ s.row.degreeType }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="totalScore" label="总分" width="80" align="center">
          <template slot-scope="s"><strong style="color:#409EFF">{{ s.row.totalScore }}</strong></template>
        </el-table-column>
        <el-table-column prop="politicalScore" label="政治" width="65" align="center" />
        <el-table-column prop="englishScore" label="英语" width="65" align="center" />
        <el-table-column prop="course1Score" label="业务课1" width="80" align="center" />
        <el-table-column prop="course2Score" label="业务课2" width="80" align="center">
          <template slot-scope="s">{{ s.row.course2Score || '-' }}</template>
        </el-table-column>
        <el-table-column prop="regionType" label="地区" width="75" align="center">
          <template slot-scope="s">
            <el-tag :type="s.row.regionType==='A'?'danger':'warning'" size="mini">{{ s.row.regionType }}类</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="总分变化" width="90" align="center">
          <template slot-scope="s">
            <span v-if="getYoyChange(s.row) !== null" :class="getYoyChange(s.row) > 0 ? 'up' : getYoyChange(s.row) < 0 ? 'down' : 'flat'">
              {{ getYoyChange(s.row) > 0 ? '↑' : getYoyChange(s.row) < 0 ? '↓' : '—' }}{{ Math.abs(getYoyChange(s.row)) }}
            </span>
            <span v-else style="color:#C0C4CC">-</span>
          </template>
        </el-table-column>
      </el-table>
      <div class="table-footer" style="margin-top:12px;display:flex;justify-content:space-between;align-items:center">
        <span style="color:#909399;font-size:13px">共 {{ total }} 条记录</span>
        <el-pagination
          small
          @current-change="handlePage"
          :current-page="page"
          :page-size="pageSize"
          :total="total"
          layout="prev, pager, next"
        />
      </div>
    </el-card>

    <!-- 趋势图 -->
    <el-card style="margin-top:20px">
      <div class="chart-header">
        <h3>{{ chartCategory }} — 历年国家线趋势</h3>
        <el-select v-model="chartCategory" placeholder="选择学科" size="small" style="width:160px" @change="renderTrendChart">
          <el-option v-for="c in categories" :key="c" :label="c" :value="c" />
        </el-select>
      </div>
      <div ref="trendChart" style="width:100%;height:360px" />
      <p v-if="chartHint" style="text-align:center;color:#909399;font-size:13px;margin-top:8px">{{ chartHint }}</p>
    </el-card>
  </div>
</template>

<script>
import * as echarts from 'echarts'

export default {
  name: 'NationalScore',
  data() {
    return {
      tableData: [],
      fullData: [],
      loading: false,
      page: 1,
      pageSize: 20,
      total: 0,
      filterYear: null,
      filterCategory: '',
      filterDegreeType: '',
      filterRegion: 'A',
      chartCategory: '工学',
      chartHint: '',
      years: [2024, 2023, 2022, 2021],
      categories: ['哲学','经济学','法学','教育学','文学','历史学','理学','工学','农学','医学','军事学','管理学','艺术学','工商管理','公共管理','法律（非法学）','法律（法学）','社会工作','教育','汉语国际教育'],
      trendChartInstance: null
    }
  },
  created() {
    this.loadFullData()
    this.fetchData()
  },
  methods: {
    async loadFullData() {
      try {
        const allRes = await this.$http.get('/school/national-score', { params: { size: 200 } })
        this.fullData = allRes?.records || []
      } catch (e) {
        console.error('加载全部数据失败:', e)
      }
    },
    async fetchData() {
      this.loading = true
      try {
        const params = { page: this.page, size: this.pageSize }
        if (this.filterYear) params.year = this.filterYear
        if (this.filterCategory) params.subjectCategory = this.filterCategory
        if (this.filterDegreeType) params.degreeType = this.filterDegreeType
        if (this.filterRegion) params.regionType = this.filterRegion
        const res = await this.$http.get('/school/national-score', { params })
        this.tableData = res?.records || []
        this.total = res?.total || 0
        this.$nextTick(() => this.renderTrendChart())
      } catch (e) {
        console.error('获取国家线失败:', e)
      } finally {
        this.loading = false
      }
    },
    search() {
      this.page = 1
      this.fetchData()
    },
    handlePage(p) {
      this.page = p
      this.fetchData()
    },
    resetFilter() {
      this.filterYear = null
      this.filterCategory = ''
      this.filterDegreeType = ''
      this.filterRegion = 'A'
      this.page = 1
      this.fetchData()
    },
    getYoyChange(row) {
      const prevYear = this.fullData.find(d =>
        d.year === row.year - 1 &&
        d.subjectCategory === row.subjectCategory &&
        d.degreeType === row.degreeType &&
        d.regionType === row.regionType
      )
      if (!prevYear) return null
      return row.totalScore - prevYear.totalScore
    },
    renderTrendChart() {
      if (!this.$refs.trendChart) return
      if (!this.trendChartInstance) {
        this.trendChartInstance = echarts.init(this.$refs.trendChart)
      }
      // 自动判断该学科的学位类型
      const matching = this.fullData.filter(d => d.subjectCategory === this.chartCategory && d.regionType === 'A')
      const degreeType = matching.length > 0 ? matching[0].degreeType : '学术型'

      const data = this.fullData
        .filter(d => d.subjectCategory === this.chartCategory && d.regionType === 'A' && d.degreeType === degreeType)
        .sort((a, b) => a.year - b.year)
      if (data.length === 0) {
        this.chartHint = `${this.chartCategory}暂无${degreeType}A类数据`
        this.trendChartInstance.setOption({ title: { text: '暂无数据', left: 'center', top: 'middle', textStyle: { color: '#C0C4CC' } } })
        return
      }
      this.chartHint = ''
      const xAxis = data.map(d => d.year + '年')
      const hasCourse2 = data.some(d => d.course2Score != null)
      const legendData = hasCourse2 ? ['总分', '政治', '英语', '业务课1', '业务课2'] : ['总分', '政治', '英语', '业务课1']
      this.trendChartInstance.setOption({
        tooltip: { trigger: 'axis' },
        legend: { data: legendData, bottom: 0 },
        grid: { left: '3%', right: '4%', bottom: '12%', containLabel: true },
        xAxis: { type: 'category', data: xAxis },
        yAxis: { type: 'value', name: '分数', min: 0 },
        series: [
          { name: '总分', type: 'line', data: data.map(d => d.totalScore), lineStyle: { width: 3 }, itemStyle: { color: '#409EFF' }, label: { show: true } },
          { name: '政治', type: 'bar', stack: 'sub', data: data.map(d => d.politicalScore), itemStyle: { color: '#67C23A' } },
          { name: '英语', type: 'bar', stack: 'sub', data: data.map(d => d.englishScore), itemStyle: { color: '#E6A23C' } },
          { name: '业务课1', type: 'bar', stack: 'sub', data: data.map(d => d.course1Score), itemStyle: { color: '#F56C6C' } },
          ...(hasCourse2 ? [{ name: '业务课2', type: 'bar', stack: 'sub', data: data.map(d => d.course2Score || 0), itemStyle: { color: '#909399' } }] : [])
        ]
      })
      window.addEventListener('resize', () => this.trendChartInstance && this.trendChartInstance.resize())
    }
  },
  beforeDestroy() {
    if (this.trendChartInstance) {
      this.trendChartInstance.dispose()
    }
  }
}
</script>

<style scoped lang="scss">
.national-score-page {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 24px;
  h1 { font-size: 24px; margin-bottom: 8px; color: #303133; }
  p { color: #909399; font-size: 14px; }
}

.filter-bar {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  align-items: center;
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  h3 { margin: 0; font-size: 16px; font-weight: 600; }
}

.up { color: #F56C6C; font-weight: 600; }
.down { color: #67C23A; font-weight: 600; }
.flat { color: #909399; }
</style>
