<template>
  <div class="school-list-page">
    <div class="page-header">
      <h1>院校信息库</h1>
      <p>收录全国40+院校详细信息，支持按地区、类型、排名精准筛选</p>
    </div>

    <!-- 快捷筛选 -->
    <div class="quick-filter-bar">
      <span class="filter-tag" :class="{ active: filterType === '' }" @click="setFilter('type', '')">全部</span>
      <span class="filter-tag tag-985" :class="{ active: filterType === '985' }" @click="setFilter('type', '985')">985 院校</span>
      <span class="filter-tag tag-211" :class="{ active: filterType === '211' }" @click="setFilter('type', '211')">211 院校</span>
      <span class="filter-tag tag-sl" :class="{ active: filterType === '双一流' }" @click="setFilter('type', '双一流')">双一流</span>
      <span class="filter-tag tag-normal" :class="{ active: filterType === '普通' }" @click="setFilter('type', '普通')">普通院校</span>
      <span class="filter-tag tag-34" :class="{ active: filterIs34 === true }" @click="setFilter('is34', true)">自主划线</span>
    </div>

    <!-- 搜索栏 -->
    <el-card class="search-card">
      <div class="filter-bar">
        <el-input v-model="keyword" placeholder="搜索院校名称" clearable style="width:220px" @keyup.enter.native="search" prefix-icon="el-icon-search"/>
        <el-select v-model="filterProvince" placeholder="所在地区" clearable filterable @change="search" style="width:130px">
          <el-option v-for="p in provinces" :key="p" :label="p" :value="p"/>
        </el-select>
        <el-button type="primary" @click="search" icon="el-icon-search">搜索</el-button>
        <el-button @click="reset">重置</el-button>
        <span class="result-hint" v-if="total > 0">共 <strong>{{ total }}</strong> 所院校</span>
      </div>
    </el-card>

    <!-- 院校卡片网格 -->
    <div class="school-grid" v-loading="loading">
      <div v-for="school in schoolList" :key="school.id" class="school-card" @click="goDetail(school)">
        <div class="card-header">
          <div class="rank-badge" :class="getRankClass(school.ranking)">
            <span class="rank-num">{{ school.ranking }}</span>
            <span class="rank-text">排名</span>
          </div>
          <div class="school-tags">
            <span class="type-tag" :class="'tag-' + school.type">{{ school.type }}</span>
            <span v-if="school.isSelfDrawing" class="tag-34-badge">自主划线</span>
          </div>
        </div>
        <h3 class="school-name">{{ school.name }}</h3>
        <p class="school-city"><i class="el-icon-location-outline"></i> {{ school.province }} · {{ school.city }}</p>
        <p class="school-intro">{{ (school.introduction || '').substring(0, 60) }}...</p>
        <div class="card-footer">
          <span class="footer-item" v-if="school.masterPoints">
            <i class="el-icon-postcard"></i> 硕士点 {{ school.masterPoints }}
          </span>
          <span class="footer-item" v-if="school.establishedYear">
            <i class="el-icon-time"></i> 建校 {{ school.establishedYear }}
          </span>
          <span class="footer-link">查看详情 <i class="el-icon-arrow-right"></i></span>
        </div>
      </div>

      <div v-if="schoolList.length === 0 && !loading" class="empty-state">
        <el-empty description="未找到匹配的院校">
          <el-button type="primary" @click="reset">清除筛选条件</el-button>
        </el-empty>
      </div>
    </div>

    <div class="pagination" v-if="total > 0">
      <el-pagination
        @current-change="handlePage"
        :current-page="page"
        :page-size="size"
        :total="total"
        layout="total, prev, pager, next"
      />
    </div>
  </div>
</template>

<script>
export default {
  name: 'SchoolList',
  data() {
    return {
      keyword: '',
      filterType: '',
      filterProvince: '',
      filterIs34: false,
      schoolList: [],
      loading: false,
      page: 1,
      size: 12,
      total: 0,
      provinces: ['北京','上海','江苏','浙江','湖北','湖南','广东','四川','陕西','天津','山东','辽宁','吉林','黑龙江','安徽','福建','重庆','甘肃','河北','河南','江西','广西','云南','贵州','山西','内蒙古','海南','新疆','西藏','青海','宁夏']
    }
  },
  created() { this.fetch() },
  methods: {
    getRankClass(rank) {
      if (!rank) return ''
      if (rank <= 5) return 'rank-top5'
      if (rank <= 15) return 'rank-top15'
      if (rank <= 35) return 'rank-top35'
      return 'rank-other'
    },
    setFilter(field, value) {
      if (field === 'type') {
        this.filterType = this.filterType === value ? '' : value
      } else if (field === 'is34') {
        this.filterIs34 = !this.filterIs34
      }
      this.page = 1
      this.fetch()
    },
    async fetch() {
      this.loading = true
      try {
        const res = await this.$http.get('/school/list', {
          params: {
            page: this.page, size: this.size,
            keyword: this.keyword || undefined,
            type: this.filterType || undefined,
            province: this.filterProvince || undefined
          }
        })
        this.schoolList = res?.records || []
        this.total = res?.total || 0
        // self-drawing filter on frontend
        if (this.filterIs34 && this.schoolList.length > 0) {
          this.schoolList = this.schoolList.filter(s => s.isSelfDrawing === 1)
          this.total = this.schoolList.length
        }
      } catch(e) { console.error(e) } finally { this.loading = false }
    },
    search() { this.page = 1; this.fetch() },
    reset() {
      this.keyword = ''; this.filterType = ''; this.filterProvince = ''; this.filterIs34 = false
      this.search()
    },
    handlePage(p) { this.page = p; this.fetch() },
    goDetail(row) { this.$router.push(`/schools/${row.id}`) }
  }
}
</script>

<style scoped lang="scss">
.school-list-page { padding: 20px; max-width: 1200px; margin: 0 auto; }
.page-header { margin-bottom: 20px; h1 { font-size: 24px; margin-bottom: 8px; color:#303133; } p { color:#909399; font-size:14px; }}

.quick-filter-bar {
  display: flex; gap: 10px; flex-wrap: wrap; margin-bottom: 16px;
  .filter-tag {
    cursor: pointer; padding: 6px 16px; border-radius: 20px; font-size: 13px;
    background: #f5f7fa; color: #606266; transition: all 0.3s;
    &:hover { background: #ecf5ff; color: #409EFF; }
    &.active { background: #409EFF; color: #fff; }
    &.tag-985.active { background: #E4393C; }
    &.tag-211.active { background: #E6A23C; }
    &.tag-sl.active { background: #67C23A; }
    &.tag-normal.active { background: #909399; }
    &.tag-34.active { background: #B37FEB; }
  }
}

.search-card { margin-bottom: 20px; }
.filter-bar { display: flex; gap: 12px; flex-wrap: wrap; align-items: center;
  .result-hint { margin-left: auto; font-size: 13px; color: #909399; strong { color:#409EFF; } }
}

.school-grid {
  display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px;
  .school-card {
    background: #fff; border-radius: 12px; padding: 20px; cursor: pointer;
    border: 1px solid #EBEEF5; transition: all 0.3s;
    &:hover { border-color: #409EFF; box-shadow: 0 4px 16px rgba(0,0,0,0.08); transform: translateY(-4px); }
    .card-header { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 12px; }
    .rank-badge {
      width: 44px; height: 44px; border-radius: 10px; display: flex;
      flex-direction: column; align-items: center; justify-content: center;
      background: #f5f7fa; color: #909399;
      .rank-num { font-size: 18px; font-weight: 700; line-height: 1; }
      .rank-text { font-size: 10px; }
      &.rank-top5 { background: #FFF0F0; color: #E4393C; }
      &.rank-top15 { background: #FFF7E6; color: #E6A23C; }
      &.rank-top35 { background: #F0F9EB; color: #67C23A; }
    }
    .school-tags { display: flex; gap: 6px; align-items: center; }
    .type-tag { font-size: 11px; padding: 2px 8px; border-radius: 4px; font-weight: 600;
      &.tag-985 { background: #FFF0F0; color: #E4393C; }
      &.tag-211 { background: #FFF7E6; color: #E6A23C; }
      &.tag-双一流 { background: #F0F9EB; color: #67C23A; }
      &.tag-普通 { background: #F5F7FA; color: #909399; }
    }
    .tag-34-badge { font-size: 10px; padding: 2px 6px; border-radius: 4px; background: #F5E6FF; color: #B37FEB; }
    .school-name { font-size: 17px; font-weight: 700; color: #303133; margin: 0 0 6px; }
    .school-city { font-size: 12px; color: #909399; margin: 0 0 8px; i { margin-right: 2px; } }
    .school-intro { font-size: 12px; color: #606266; line-height: 1.6; margin: 0 0 12px; }
    .card-footer { display: flex; justify-content: space-between; align-items: center; font-size: 11px; color: #C0C4CC; flex-wrap: wrap; gap: 4px;
      .footer-item { i { margin-right: 2px; } }
      .footer-link { color: #409EFF; font-weight: 500; i { font-size: 10px; } }
    }
  }
  .empty-state { grid-column: 1 / -1; padding: 60px 0; }
}

.pagination { margin-top: 24px; text-align: center; }

@media (max-width: 1200px) { .school-grid { grid-template-columns: repeat(3, 1fr); } }
@media (max-width: 900px) { .school-grid { grid-template-columns: repeat(2, 1fr); } }
@media (max-width: 600px) { .school-grid { grid-template-columns: 1fr; } }
</style>
