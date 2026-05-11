<template>
  <div class="school-detail" v-loading="loading">
    <!-- 顶部横幅 -->
    <div class="school-banner">
      <el-button type="text" icon="el-icon-arrow-left" @click="$router.back()" class="back-btn">返回院校库</el-button>
      <div class="banner-content">
        <div class="banner-left">
          <div class="school-avatar">
            <span class="school-initial">{{ school.name ? school.name.charAt(0) : '校' }}</span>
          </div>
          <div class="banner-info">
            <h1>{{ school.name }}</h1>
            <div class="banner-tags">
              <el-tag :type="typeTagColor" size="small" effect="dark">{{ school.type || '院校' }}</el-tag>
              <el-tag v-if="school.isSelfDrawing" type="danger" size="small" effect="dark">34所自划线</el-tag>
              <span class="banner-meta"><i class="el-icon-location-outline"></i>{{ school.province }} · {{ school.city }}</span>
            </div>
          </div>
        </div>
        <div class="banner-stats">
          <div class="banner-stat-item">
            <div class="stat-value">{{ school.ranking || '-' }}</div>
            <div class="stat-label">全国排名</div>
          </div>
          <div class="banner-stat-divider"></div>
          <div class="banner-stat-item">
            <div class="stat-value">{{ school.masterPoints || '-' }}</div>
            <div class="stat-label">硕士点</div>
          </div>
          <div class="banner-stat-divider"></div>
          <div class="banner-stat-item">
            <div class="stat-value">{{ school.doctorPoints || '-' }}</div>
            <div class="stat-label">博士点</div>
          </div>
          <div class="banner-stat-divider"></div>
          <div class="banner-stat-item">
            <div class="stat-value">{{ school.establishedYear || '-' }}</div>
            <div class="stat-label">建校年份</div>
          </div>
        </div>
      </div>
    </div>

    <el-tabs v-model="activeTab" @tab-click="handleTabClick" class="detail-tabs">
      <!-- ========== 概况 ========== -->
      <el-tab-pane label="院校概况" name="overview">
        <el-card class="section-card">
          <h3 class="card-title"><i class="el-icon-document"></i> 院校简介</h3>
          <p class="intro-text">{{ school.introduction || '暂无简介信息' }}</p>
        </el-card>

        <el-row :gutter="16" style="margin-top:16px">
          <el-col :span="12">
            <el-card class="section-card">
              <h3 class="card-title"><i class="el-icon-star-off"></i> 重点学科</h3>
              <div v-if="school.keyDisciplines" class="tag-cloud">
                <el-tag v-for="(d, i) in disciplineList" :key="i" size="small" :type="['','success','warning','danger',''][i%5]">{{ d }}</el-tag>
              </div>
              <el-empty v-else description="暂无数据" :image-size="50"/>
            </el-card>
          </el-col>
          <el-col :span="12">
            <el-card class="section-card">
              <h3 class="card-title"><i class="el-icon-office-building"></i> 院系设置</h3>
              <div v-if="school.departments" class="tag-cloud">
                <el-tag v-for="(d, i) in departmentList" :key="i" size="small" type="info">{{ d }}</el-tag>
              </div>
              <el-empty v-else description="暂无数据" :image-size="50"/>
            </el-card>
          </el-col>
        </el-row>

        <el-card class="section-card" style="margin-top:16px">
          <h3 class="card-title"><i class="el-icon-info"></i> 基本信息</h3>
          <el-descriptions :column="3" border size="medium">
            <el-descriptions-item label="隶属部门">{{ school.affiliation || '暂无' }}</el-descriptions-item>
            <el-descriptions-item label="院校类型">{{ school.type || '暂无' }}</el-descriptions-item>
            <el-descriptions-item label="所在地">{{ school.province }} · {{ school.city }}</el-descriptions-item>
            <el-descriptions-item label="硕士点数量">{{ school.masterPoints || '-' }} 个</el-descriptions-item>
            <el-descriptions-item label="博士点数量">{{ school.doctorPoints || '-' }} 个</el-descriptions-item>
            <el-descriptions-item label="全国排名">第 {{ school.ranking || '-' }} 名</el-descriptions-item>
            <el-descriptions-item label="官网" :span="3">
              <a v-if="school.website" :href="school.website" target="_blank" rel="noopener" class="website-link">{{ school.website }}</a>
              <span v-else>暂无</span>
            </el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-tab-pane>

      <!-- ========== 分数线 ========== -->
      <el-tab-pane label="历年分数线" name="score">
        <el-card class="section-card">
          <h3 class="card-title"><i class="el-icon-data-analysis"></i> 分数线差距分析</h3>
          <p class="card-desc">输入你的预估分数，系统会与最新一年的分数线进行对比分析</p>
          <el-row :gutter="12" type="flex" style="margin-top:16px" align="bottom">
            <el-col :span="4" :xs="12">
              <div class="score-label">政治</div>
              <el-input-number v-model="myScore.political" :min="0" :max="100" size="small" controls-position="right" style="width:100%"/>
            </el-col>
            <el-col :span="4" :xs="12">
              <div class="score-label">英语</div>
              <el-input-number v-model="myScore.english" :min="0" :max="100" size="small" controls-position="right" style="width:100%"/>
            </el-col>
            <el-col :span="4" :xs="12">
              <div class="score-label">业务课1</div>
              <el-input-number v-model="myScore.course1" :min="0" :max="150" size="small" controls-position="right" style="width:100%"/>
            </el-col>
            <el-col :span="4" :xs="12">
              <div class="score-label">业务课2</div>
              <el-input-number v-model="myScore.course2" :min="0" :max="150" size="small" controls-position="right" style="width:100%"/>
            </el-col>
            <el-col :span="4" :xs="12">
              <div class="score-label">参考专业</div>
              <el-select v-model="targetMajor" placeholder="选择专业" size="small" style="width:100%" clearable>
                <el-option v-for="m in majorOptions" :key="m" :label="m" :value="m"/>
              </el-select>
            </el-col>
            <el-col :span="4" :xs="24">
              <el-button type="primary" size="small" @click="analyzeGap" style="width:100%"><i class="el-icon-search"/> 差距分析</el-button>
            </el-col>
          </el-row>
          <div v-if="gapResult" class="gap-result">
            <el-divider/>
            <div class="gap-summary">
              <div class="gap-card-item">
                <div class="gap-label">你的总分</div>
                <div class="gap-value blue">{{ myTotal }}</div>
              </div>
              <div class="gap-arrow"><i class="el-icon-d-arrow-right"></i></div>
              <div class="gap-card-item">
                <div class="gap-label">{{ gapResult.year }}年 {{ gapResult.major }} 复试线</div>
                <div class="gap-value orange">{{ gapResult.refTotal }}</div>
              </div>
              <div class="gap-arrow"><i class="el-icon-d-arrow-right"></i></div>
              <div class="gap-card-item">
                <div class="gap-label">差距</div>
                <div :class="['gap-value', gapResult.totalGap >= 0 ? 'green' : 'red']">
                  {{ gapResult.totalGap >= 0 ? '+' : '' }}{{ gapResult.totalGap }}
                </div>
              </div>
              <div v-if="gapResult.admitCount" class="gap-ratio-badge">
                报录比 {{ (gapResult.applicantCount / gapResult.admitCount).toFixed(1) }}∶1（{{ gapResult.admitCount }}/{{ gapResult.applicantCount }}）
              </div>
            </div>
          </div>
        </el-card>

        <el-card class="section-card" style="margin-top:16px">
          <div class="filter-row">
            <el-select v-model="scoreYear" @change="fetchScoreLines" clearable placeholder="全部年份" style="width:120px">
              <el-option v-for="y in years" :key="y" :label="y+'年'" :value="y"/>
            </el-select>
            <el-input v-model="scoreMajor" placeholder="搜索专业" clearable style="width:200px" @keyup.enter="fetchScoreLines"/>
            <el-button type="primary" size="small" @click="fetchScoreLines" icon="el-icon-search">查询</el-button>
          </div>
          <el-table :data="scoreLines" stripe border style="margin-top:12px">
            <el-table-column prop="year" label="年份" width="70" align="center"/>
            <el-table-column prop="major" label="专业" min-width="130"/>
            <el-table-column prop="majorCode" label="代码" width="80" align="center"/>
            <el-table-column prop="totalScore" label="总分" width="70" align="center">
              <template slot-scope="s"><strong style="color:#409EFF">{{ s.row.totalScore }}</strong></template>
            </el-table-column>
            <el-table-column prop="politicalScore" label="政治" width="55" align="center"/>
            <el-table-column prop="englishScore" label="英语" width="55" align="center"/>
            <el-table-column prop="course1Score" label="业务1" width="55" align="center"/>
            <el-table-column prop="course2Score" label="业务2" width="55" align="center"/>
            <el-table-column label="录取/报考" width="85" align="center">
              <template slot-scope="s">{{ s.row.admitCount||'-' }}/{{ s.row.applicantCount||'-' }}</template>
            </el-table-column>
            <el-table-column label="报录比" width="75" align="center">
              <template slot-scope="s">
                <el-tag v-if="s.row.applicantCount && s.row.admitCount" size="mini" :type="ratioType(s.row)">{{ ratioText(s.row) }}</el-tag>
                <span v-else>-</span>
              </template>
            </el-table-column>
          </el-table>
          <el-pagination style="margin-top:12px" small @current-change="handleScorePage" :current-page="scorePage" :page-size="scoreSize" :total="scoreTotal" layout="total,prev,pager,next"/>
        </el-card>

        <el-card class="section-card" style="margin-top:16px">
          <h3 class="card-title"><i class="el-icon-trend-charts"></i> 分数线趋势</h3>
          <div ref="chartDom" style="width:100%;height:350px"/>
        </el-card>
      </el-tab-pane>

      <!-- ========== 报录比 ========== -->
      <el-tab-pane label="报录比分析" name="ratio">
        <el-card class="section-card">
          <h3 class="card-title"><i class="el-icon-pie-chart"></i> 各专业历年报录比统计</h3>
          <el-table :data="ratioData" stripe border v-loading="ratioLoading">
            <el-table-column prop="year" label="年份" width="70" align="center"/>
            <el-table-column prop="major" label="专业" min-width="130"/>
            <el-table-column prop="admitCount" label="录取" width="70" align="center"/>
            <el-table-column prop="applicantCount" label="报考" width="70" align="center"/>
            <el-table-column label="报录比" width="160" align="center">
              <template slot-scope="s">
                <el-progress v-if="s.row.applicantCount && s.row.admitCount"
                  :percentage="Math.min(100, Math.round((s.row.admitCount / s.row.applicantCount) * 100))"
                  :color="ratioProgressColor(s.row)"
                  :format="() => (s.row.applicantCount/s.row.admitCount).toFixed(1)+'∶1'"
                  :stroke-width="18"/>
                <span v-else>-</span>
              </template>
            </el-table-column>
            <el-table-column label="录取率" width="75" align="center">
              <template slot-scope="s">
                <span v-if="s.row.applicantCount && s.row.admitCount" :style="{ color: admitRateColor(s.row), fontWeight:'600' }">
                  {{ (s.row.admitCount / s.row.applicantCount * 100).toFixed(1) }}%
                </span>
                <span v-else>-</span>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-if="ratioData.length === 0 && !ratioLoading" description="暂无报录比数据" :image-size="80"/>
        </el-card>
      </el-tab-pane>

      <!-- ========== 招生专业 ========== -->
      <el-tab-pane label="招生专业" name="major">
        <el-card class="section-card">
          <h3 class="card-title"><i class="el-icon-collection-tag"></i> 招生专业一览</h3>
          <el-table :data="majorList" stripe border>
            <el-table-column prop="major" label="专业名称" min-width="150"/>
            <el-table-column prop="majorCode" label="专业代码" width="90" align="center"/>
            <el-table-column prop="degreeType" label="学位类型" width="85" align="center">
              <template slot-scope="s"><el-tag size="mini">{{ s.row.degreeType }}</el-tag></template>
            </el-table-column>
            <el-table-column label="复试线范围" width="120" align="center">
              <template slot-scope="s">
                <span class="score-range">{{ s.row.minScore }} - {{ s.row.maxScore }}</span>
              </template>
            </el-table-column>
            <el-table-column label="报录比范围" width="125" align="center">
              <template slot-scope="s">
                <el-tag size="mini" :type="s.row.maxRatio >= 15 ? 'danger' : s.row.maxRatio >= 8 ? 'warning' : 'success'">
                  {{ s.row.minRatio }}∶1 - {{ s.row.maxRatio }}∶1
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="数据年份" width="75" align="center">
              <template slot-scope="s">{{ s.row.dataYears }}年</template>
            </el-table-column>
          </el-table>
          <el-empty v-if="majorList.length === 0" description="暂无专业数据" :image-size="80"/>
        </el-card>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script>
import * as echarts from 'echarts'
export default {
  name: 'SchoolDetail',
  data() {
    return {
      school: {}, loading: false, activeTab: 'overview',
      scoreLines: [], scoreYear: null, scoreMajor: '', scorePage:1, scoreSize:10, scoreTotal:0,
      years: [2025,2024,2023,2022,2021],
      myScore: { political:60, english:60, course1:110, course2:110 },
      targetMajor: '', gapResult: null, chartInstance: null,
      ratioData: [], ratioLoading: false, majorList: []
    }
  },
  computed: {
    myTotal() { return this.myScore.political + this.myScore.english + this.myScore.course1 + this.myScore.course2 },
    majorOptions() { return [...new Set(this.scoreLines.map(s => s.major))] },
    typeTagColor() {
      const m = { '985': 'danger', '211': 'warning', '双一流': 'success' }
      return m[this.school.type] || 'info'
    },
    disciplineList() {
      return this.school.keyDisciplines ? this.school.keyDisciplines.split(/[,，、]/).filter(Boolean).map(s => s.trim()) : []
    },
    departmentList() {
      return this.school.departments ? this.school.departments.split(/[,，、等]/).filter(Boolean).map(s => s.trim()) : []
    }
  },
  created() { this.fetchSchool(); this.fetchScoreLines() },
  methods: {
    ratioType(r) {
      const v = r.applicantCount/r.admitCount
      return v >= 15 ? 'danger' : v >= 8 ? 'warning' : 'success'
    },
    ratioText(r) { return (r.applicantCount/r.admitCount).toFixed(1) + '∶1' },
    ratioProgressColor(r) {
      const v = r.applicantCount/r.admitCount
      return v >= 15 ? '#F56C6C' : v >= 8 ? '#E6A23C' : '#67C23A'
    },
    admitRateColor(r) {
      const v = r.admitCount/r.applicantCount
      return v >= 0.1 ? '#67C23A' : '#F56C6C'
    },
    async fetchSchool() {
      this.loading = true
      try { const res = await this.$http.get(`/school/detail/${this.$route.params.id}`); this.school = res || {} }
      catch(e) { console.error(e) } finally { this.loading = false }
    },
    async fetchScoreLines() {
      try {
        const res = await this.$http.get(`/school/${this.$route.params.id}/score-lines`, {
          params: { year: this.scoreYear||undefined, major: this.scoreMajor||undefined, page:this.scorePage, size:this.scoreSize }
        })
        this.scoreLines = res?.records || []; this.scoreTotal = res?.total || 0
        this.buildRatioAndMajor()
        this.$nextTick(() => this.renderChart())
      } catch(e) { console.error(e) }
    },
    handleTabClick(tab) {
      if (tab.name === 'score') this.$nextTick(() => this.renderChart())
      if (tab.name === 'ratio') this.buildRatioAndMajor()
      if (tab.name === 'major') this.buildRatioAndMajor()
    },
    buildRatioAndMajor() {
      this.ratioData = [...this.scoreLines].filter(s => s.applicantCount && s.admitCount)
      const majorMap = {}
      this.scoreLines.forEach(s => {
        if (!majorMap[s.major]) majorMap[s.major] = { major: s.major, majorCode: s.majorCode||'', degreeType: s.degreeType||'硕士', scores:[], ratios:[], years:[] }
        majorMap[s.major].scores.push(s.totalScore)
        if (s.applicantCount && s.admitCount) majorMap[s.major].ratios.push(s.applicantCount/s.admitCount)
        majorMap[s.major].years.push(s.year)
      })
      this.majorList = Object.values(majorMap).map(m => ({
        ...m, minScore: Math.min(...m.scores), maxScore: Math.max(...m.scores),
        minRatio: m.ratios.length > 0 ? Math.min(...m.ratios).toFixed(1) : '-',
        maxRatio: m.ratios.length > 0 ? Math.max(...m.ratios).toFixed(1) : '-',
        dataYears: [...new Set(m.years)].length
      }))
    },
    handleScorePage(p) { this.scorePage = p; this.fetchScoreLines() },
    analyzeGap() {
      const major = this.targetMajor || (this.scoreLines.length > 0 ? this.scoreLines[0].major : '')
      if (!major) { this.$message.warning('请先选择参考专业'); return }
      const latestLine = this.scoreLines.filter(s => s.major === major).sort((a,b) => b.year - a.year)[0]
      if (!latestLine) { this.$message.warning('未找到该专业的分数线数据'); return }
      this.gapResult = {
        year: latestLine.year, major: latestLine.major, refTotal: latestLine.totalScore,
        totalGap: this.myTotal - latestLine.totalScore,
        admitCount: latestLine.admitCount, applicantCount: latestLine.applicantCount
      }
    },
    renderChart() {
      if (!this.$refs.chartDom) return
      if (this.chartInstance) { this.chartInstance.dispose(); this.chartInstance = null }
      this.chartInstance = echarts.init(this.$refs.chartDom)
      const majors = [...new Set(this.scoreLines.map(s => s.major))].slice(0, 6)
      const allYears = [...new Set(this.scoreLines.map(s => s.year))].sort()
      if (allYears.length === 0) {
        this.chartInstance.setOption({ title:{ text:'暂无数据', left:'center', top:'middle', textStyle:{ color:'#C0C4CC' } } })
        return
      }
      const colors = ['#5470c6','#91cc75','#fac858','#ee6666','#73c0de','#fc8452']
      this.chartInstance.setOption({
        color: colors,
        tooltip: { trigger: 'axis', backgroundColor: 'rgba(255,255,255,.95)',
          borderColor: '#e0e0e0', textStyle: { color: '#333' },
          formatter: function(params) {
            let html = '<strong>' + params[0].axisValue + '</strong><br/>'
            params.forEach(p => { if (p.value != null) html += '<span style="display:inline-block;width:10px;height:10px;border-radius:50%;background:'+p.color+';margin-right:6px"></span>'+p.seriesName+'：<b>'+p.value+'</b>分<br/>' })
            return html
          }
        },
        legend: { data: majors, bottom: 4, textStyle: { fontSize: 12 }, itemWidth: 16, itemHeight: 8 },
        grid: { left: 50, right: 30, top: 24, bottom: 40 },
        xAxis: { type: 'category', data: allYears.map(y => y+'年'), axisLine: { lineStyle: { color: '#ddd' } },
          axisLabel: { color: '#666' } },
        yAxis: { type: 'value', name: '总分', nameTextStyle: { color: '#999', fontSize: 12 },
          min: function(v) { return Math.floor(v.min / 10) * 10 - 10 },
          axisLabel: { color: '#666' }, splitLine: { lineStyle: { color: '#f0f0f0', type: 'dashed' } } },
        series: majors.map((m, i) => ({
          name: m, type: 'line', smooth: true, symbol: 'circle', symbolSize: 6,
          lineStyle: { width: 2.5 },
          areaStyle: { opacity: 0.05 },
          data: allYears.map(y => { const item = this.scoreLines.find(s => s.major===m && s.year===y); return item ? item.totalScore : null })
        }))
      })
      this._resizeHandler = () => { if (this.chartInstance) this.chartInstance.resize() }
      window.addEventListener('resize', this._resizeHandler)
    }
  },
  beforeDestroy() {
    if (this.chartInstance) this.chartInstance.dispose()
    if (this._resizeHandler) window.removeEventListener('resize', this._resizeHandler)
  }
}
</script>

<style scoped lang="scss">
.school-detail { padding:20px; max-width:1200px; margin:0 auto; }

.school-banner {
  background: linear-gradient(135deg, #1a3a5c 0%, #2d6aa0 100%);
  border-radius: 16px; padding: 28px 36px; margin-bottom: 24px;
  color: #fff; position: relative; overflow: hidden;
  &::after {
    content: ''; position: absolute; right: -40px; top: -40px;
    width: 200px; height: 200px; border-radius: 50%;
    background: rgba(255,255,255,.05); pointer-events: none;
  }
  .back-btn { color: rgba(255,255,255,.8); margin-bottom: 16px; padding: 0;
    &:hover { color: #fff; }
  }
  .banner-content {
    display: flex; justify-content: space-between; align-items: center;
    flex-wrap: wrap; gap: 24px; position: relative; z-index: 1;
  }
  .banner-left {
    display: flex; align-items: center; gap: 20px;
    .school-avatar {
      width: 72px; height: 72px; border-radius: 16px;
      background: rgba(255,255,255,.15);
      display: flex; align-items: center; justify-content: center;
      .school-initial { font-size: 32px; font-weight: bold; color: #fff; }
    }
    .banner-info {
      h1 { font-size: 28px; margin: 0 0 10px; font-weight: 700; }
      .banner-tags { display: flex; gap: 10px; align-items: center; flex-wrap: wrap; }
      .banner-meta { font-size: 13px; color: rgba(255,255,255,.7); i { margin-right: 4px; } }
    }
  }
  .banner-stats {
    display: flex; gap: 0; align-items: center;
    background: rgba(255,255,255,.1); border-radius: 12px; padding: 16px 24px;
    .banner-stat-item {
      text-align: center; padding: 0 20px;
      .stat-value { font-size: 24px; font-weight: 700; }
      .stat-label { font-size: 12px; color: rgba(255,255,255,.6); margin-top: 4px; }
    }
    .banner-stat-divider {
      width: 1px; height: 36px; background: rgba(255,255,255,.15);
    }
  }
}

.detail-tabs {
  background: #fff; border-radius: 12px; padding: 8px 20px 20px;
  box-shadow: 0 2px 12px rgba(0,0,0,.04);
}

.section-card {
  border-radius: 10px; border: 1px solid #ebeef5;
  box-shadow: none;
  .card-title { margin: 0 0 4px; font-size: 16px; i { margin-right: 6px; color: #409EFF; } }
  .card-desc { margin: 4px 0 0; font-size: 13px; color: #909399; }
}

.intro-text { color: #606266; line-height: 1.9; font-size: 14px; text-indent: 2em; }

.tag-cloud {
  display: flex; flex-wrap: wrap; gap: 8px;
  .el-tag { margin: 0; }
}

.score-label { font-size: 12px; color: #606266; margin-bottom: 6px; }

.gap-result { margin-top: 8px; }

.gap-summary {
  display: flex; gap: 16px; align-items: center; flex-wrap: wrap;
  .gap-card-item {
    text-align: center;
    .gap-label { font-size: 12px; color: #909399; margin-bottom: 4px; }
    .gap-value { font-size: 24px; font-weight: 700;
      &.blue { color: #409EFF; } &.orange { color: #E6A23C; }
      &.green { color: #67C23A; } &.red { color: #F56C6C; }
    }
  }
  .gap-arrow { color: #c0c4cc; font-size: 18px; }
  .gap-ratio-badge {
    background: #f5f7fa; padding: 6px 14px; border-radius: 6px;
    font-size: 13px; color: #606266; margin-left: auto;
  }
}

.filter-row { display: flex; gap: 10px; flex-wrap: wrap; align-items: center; }

.score-range { color: #409EFF; font-weight: 600; }

.website-link { color: #409EFF; text-decoration: none; &:hover { text-decoration: underline; } }

@media (max-width:768px) {
  .school-banner { padding: 20px; .banner-content { flex-direction: column; align-items: flex-start; } }
  .banner-stats { width: 100%; justify-content: space-around; .banner-stat-item { padding: 0 10px; } }
}
</style>
