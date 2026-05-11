<template>
  <div class="adjustment-page">
    <div class="page-header">
      <h1>调剂信息中心</h1>
      <p>各院校官方发布的调剂信息实时汇总，助力把握上岸机会。数据持续更新中</p>
    </div>

    <!-- 筛选栏 -->
    <el-card>
      <div class="filter-bar">
        <el-input v-model="keyword" placeholder="搜索院校或专业" clearable style="width:220px" @keyup.enter.native="search" prefix-icon="el-icon-search"/>
        <el-select v-model="filterYear" placeholder="年份" clearable @change="search" style="width:110px">
          <el-option v-for="y in years" :key="y" :label="y+'年'" :value="y"/>
        </el-select>
        <el-button type="primary" @click="search" icon="el-icon-search">搜索</el-button>
        <el-button @click="reset">重置</el-button>
        <span class="result-count" v-if="total > 0">共找到 <strong>{{ total }}</strong> 条调剂信息</span>
      </div>
    </el-card>

    <!-- 调剂列表 -->
    <div class="adjustment-list" v-loading="loading">
      <el-card v-for="item in list" :key="item.id" class="adjustment-card" shadow="hover">
        <div class="card-top">
          <div class="card-left">
            <h3 class="school-name">{{ item.schoolName }}</h3>
            <div class="major-row">
              <el-tag type="primary" size="small">{{ item.major }}</el-tag>
              <el-tag :type="item.status === 1 ? 'success' : 'info'" size="small">
                {{ item.status === 1 ? '调剂中' : '已过期' }}
              </el-tag>
              <span class="year-badge">{{ item.year }}年</span>
            </div>
          </div>
          <div class="card-right">
            <span class="publish-date">发布于 {{ formatDate(item.publishDate) }}</span>
            <el-button type="primary" size="small" @click="showDetail(item)">
              <i class="el-icon-document"></i> 查看详情
            </el-button>
          </div>
        </div>
        <!-- 摘要信息 -->
        <div class="card-summary">
          <div class="summary-item">
            <i class="el-icon-s-flag"></i>
            <span>{{ item.requirement ? item.requirement.substring(0, 80) + '...' : '暂无详细信息' }}</span>
          </div>
        </div>
      </el-card>

      <div v-if="list.length === 0 && !loading" class="empty-state">
        <el-empty description="暂无符合条件的调剂信息">
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

    <!-- 调剂详情对话框 -->
    <el-dialog
      :title="detailItem ? detailItem.schoolName + ' — ' + detailItem.major : ''"
      :visible.sync="detailVisible"
      width="650px"
      top="5vh"
    >
      <div class="detail-content" v-if="detailItem">
        <div class="detail-header-info">
          <div class="detail-tags">
            <el-tag type="primary">{{ detailItem.major }}</el-tag>
            <el-tag :type="detailItem.status === 1 ? 'success' : 'info'">
              {{ detailItem.status === 1 ? '调剂进行中' : '已结束' }}
            </el-tag>
            <el-tag type="warning">{{ detailItem.year }}年</el-tag>
          </div>
          <p class="detail-date">发布日期：{{ formatDate(detailItem.publishDate) }}</p>
        </div>

        <el-divider content-position="left">调剂要求</el-divider>
        <div class="requirement-box">
          <pre class="requirement-text">{{ detailItem.requirement || '暂无详细要求信息' }}</pre>
        </div>

        <el-divider content-position="left">联系方式</el-divider>
        <div class="contact-box" v-if="detailItem.contact">
          <p><i class="el-icon-phone"></i> {{ detailItem.contact }}</p>
        </div>
        <el-empty v-else description="暂无联系方式" :image-size="60" />

        <el-divider content-position="left">来源信息</el-divider>
        <div class="source-box">
          <p v-if="detailItem.sourceUrl">
            <i class="el-icon-link"></i>
            <a :href="detailItem.sourceUrl" target="_blank" rel="noopener">查看原始通知</a>
          </p>
          <p v-else><i class="el-icon-warning-outline"></i> 请以院校研究生院官网发布的信息为准</p>
        </div>

        <el-divider />
        <el-alert
          title="调剂重要提示"
          type="warning"
          :closable="false"
          show-icon
          description="1. 调剂信息具有时效性，请以院校官网最新通知为准。2. 调剂系统开放后，每次最多填写3个平行志愿。3. 确认调剂录取后不可再参加其他院校复试。4. A区考生可申请B区院校调剂，反之不可。"
        />
      </div>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: 'Adjustment',
  data() {
    return {
      list: [],
      loading: false,
      page: 1,
      size: 10,
      total: 0,
      keyword: '',
      filterYear: null,
      years: [2026, 2025, 2024, 2023],
      detailVisible: false,
      detailItem: null
    }
  },
  created() { this.fetch() },
  methods: {
    formatDate(dateStr) {
      if (!dateStr) return ''
      const d = new Date(dateStr)
      if (isNaN(d.getTime())) return dateStr
      return d.toLocaleDateString('zh-CN')
    },
    async fetch() {
      this.loading = true
      try {
        const res = await this.$http.get('/adjustment/list', {
          params: {
            page: this.page,
            size: this.size,
            schoolName: this.keyword || undefined,
            major: this.keyword || undefined,
            year: this.filterYear || undefined,
          }
        })
        this.list = res?.records || []
        this.total = res?.total || 0
      } catch (e) {
        console.error('获取调剂信息失败:', e)
      } finally {
        this.loading = false
      }
    },
    search() { this.page = 1; this.fetch() },
    reset() { this.keyword = ''; this.filterYear = null; this.search() },
    handlePage(p) { this.page = p; this.fetch() },
    showDetail(row) {
      this.detailItem = row
      this.detailVisible = true
    }
  }
}
</script>

<style scoped lang="scss">
.adjustment-page {
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
  .result-count {
    margin-left: auto;
    font-size: 13px;
    color: #909399;
    strong { color: #409EFF; }
  }
}

.adjustment-list {
  margin-top: 16px;
  .adjustment-card {
    margin-bottom: 14px;
    border-radius: 10px;
    transition: all 0.3s;
    border-left: 4px solid #409EFF;
    &:hover { box-shadow: 0 4px 16px rgba(0,0,0,0.1); transform: translateX(4px); }
    .card-top {
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      flex-wrap: wrap;
      gap: 12px;
      margin-bottom: 12px;
    }
    .card-left {
      .school-name { margin: 0 0 8px; font-size: 18px; color: #303133; }
      .major-row { display: flex; gap: 8px; align-items: center; flex-wrap: wrap; }
      .year-badge { font-size: 12px; color: #909399; background: #f5f7fa; padding: 2px 8px; border-radius: 4px; }
    }
    .card-right {
      display: flex;
      align-items: center;
      gap: 12px;
      .publish-date { font-size: 12px; color: #C0C4CC; }
    }
    .card-summary {
      .summary-item {
        display: flex;
        align-items: flex-start;
        gap: 8px;
        font-size: 13px;
        color: #606266;
        line-height: 1.6;
        background: #f9fafc;
        padding: 10px 14px;
        border-radius: 6px;
        i { margin-top: 2px; color: #909399; }
      }
    }
  }
  .empty-state { padding: 60px 0; }
}

.pagination { margin-top: 20px; text-align: center; }

.detail-content {
  .detail-header-info {
    margin-bottom: 8px;
    .detail-tags { display: flex; gap: 8px; margin-bottom: 8px; }
    .detail-date { font-size: 13px; color: #909399; margin: 0; }
  }
  .requirement-box {
    background: #f5f7fa;
    border-radius: 8px;
    padding: 16px;
    .requirement-text {
      margin: 0;
      white-space: pre-wrap;
      font-family: inherit;
      font-size: 14px;
      color: #303133;
      line-height: 1.8;
      word-break: break-all;
    }
  }
  .contact-box {
    p { font-size: 14px; color: #303133; i { color: #409EFF; margin-right: 6px; } }
  }
  .source-box {
    font-size: 13px;
    color: #606266;
    a { color: #409EFF; }
    i { margin-right: 4px; }
  }
}

@media (max-width: 768px) {
  .adjustment-card {
    .card-top { flex-direction: column; }
    .card-right { width: 100%; justify-content: space-between; }
  }
  .filter-bar .result-count { margin-left: 0; width: 100%; }
}
</style>
