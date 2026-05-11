<template>
  <div class="reports-manage">
    <el-card>
      <div class="toolbar">
        <el-select v-model="filterStatus" placeholder="举报状态" clearable style="width: 150px" @change="search">
          <el-option label="待处理" :value="0"></el-option>
          <el-option label="已处理" :value="1"></el-option>
          <el-option label="已驳回" :value="2"></el-option>
        </el-select>
        <el-select v-model="filterTargetType" placeholder="举报类型" clearable style="width: 150px" @change="search">
          <el-option label="帖子" :value="1"></el-option>
          <el-option label="评论" :value="2"></el-option>
          <el-option label="用户" :value="3"></el-option>
        </el-select>
        <el-button type="primary" @click="search">搜索</el-button>
      </div>

      <el-table :data="reportList" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80"></el-table-column>
        <el-table-column prop="userId" label="举报人ID" width="100"></el-table-column>
        <el-table-column prop="targetType" label="举报类型" width="100">
          <template slot-scope="scope">
            <span>{{ getTargetTypeName(scope.row.targetType) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="targetId" label="举报对象ID" width="100"></el-table-column>
        <el-table-column prop="reason" label="举报原因">
          <template slot-scope="scope">
            <span class="reason">{{ scope.row.reason }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template slot-scope="scope">
            <el-tag :type="getStatusTagType(scope.row.status)">
              {{ getStatusName(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="举报时间" width="160"></el-table-column>
        <el-table-column label="操作" width="250">
          <template slot-scope="scope">
            <el-button
              v-if="scope.row.status === 0"
              size="mini"
              type="primary"
              @click="handleReport(scope.row, 1)"
            >
              标记已处理
            </el-button>
            <el-button
              v-if="scope.row.status === 0"
              size="mini"
              type="warning"
              @click="handleReport(scope.row, 2)"
            >
              驳回
            </el-button>
            <el-button size="mini" type="danger" @click="deleteReport(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          :current-page="currentPage"
          :page-sizes="[10, 20, 50]"
          :page-size="pageSize"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
        />
      </div>
    </el-card>
  </div>
</template>

<script>
export default {
  name: 'ReportsManage',
  data() {
    return {
      filterStatus: null,
      filterTargetType: null,
      reportList: [],
      currentPage: 1,
      pageSize: 10,
      total: 0
    }
  },
  created() {
    this.getReportList()
  },
  methods: {
    getTargetTypeName(targetType) {
      const typeMap = {
        1: '帖子',
        2: '评论',
        3: '用户'
      }
      return typeMap[targetType] || '未知'
    },
    getStatusName(status) {
      const statusMap = {
        0: '待处理',
        1: '已处理',
        2: '已驳回'
      }
      return statusMap[status] || '未知'
    },
    getStatusTagType(status) {
      const typeMap = {
        0: 'warning',
        1: 'success',
        2: 'info'
      }
      return typeMap[status] || 'info'
    },
    search() {
      this.currentPage = 1
      this.getReportList()
    },
    handleSizeChange(size) {
      this.pageSize = size
      this.getReportList()
    },
    handleCurrentChange(current) {
      this.currentPage = current
      this.getReportList()
    },
    async handleReport(report, status) {
      const action = status === 1 ? '标记已处理' : '驳回'
      let confirmMsg = `确定要${action}该举报吗？`
      if (status === 1) {
        const targetName = this.getTargetTypeName(report.targetType)
        confirmMsg = `确定要${action}该举报吗？系统将自动下架被举报的${targetName}（ID: ${report.targetId}）。`
      }
      try {
        await this.$confirm(confirmMsg, '操作确认', {
          type: 'warning'
        })
        await this.$http.put(`/admin/report/${report.id}/status`, null, {
          params: { status }
        })
        this.$message.success(`${action}成功`)
        this.getReportList()
      } catch (error) {
        if (error !== 'cancel') {
          console.error(error)
        }
      }
    },
    async deleteReport(report) {
      try {
        await this.$confirm('确定要删除该举报记录吗？', '提示', {
          type: 'warning'
        })
        await this.$http.delete(`/admin/report/${report.id}`)
        this.$message.success('删除成功')
        this.getReportList()
      } catch (error) {
        if (error !== 'cancel') {
          console.error(error)
        }
      }
    },
    async getReportList() {
      try {
        const params = {
          page: this.currentPage,
          size: this.pageSize
        }
        if (this.filterStatus !== null && this.filterStatus !== '') {
          params.status = this.filterStatus
        }
        if (this.filterTargetType !== null && this.filterTargetType !== '') {
          params.targetType = this.filterTargetType
        }

        const res = await this.$http.get('/admin/reports', { params })
        const responseData = res.data || res
        this.reportList = responseData?.records || []
        this.total = responseData?.total || 0
      } catch (error) {
        console.error('获取举报列表失败:', error)
        this.$message.error('获取举报列表失败')
      }
    }
  }
}
</script>

<style scoped lang="scss">
.reports-manage {
  .toolbar {
    display: flex;
    gap: 10px;
    margin-bottom: 20px;
  }

  .pagination {
    margin-top: 20px;
    text-align: center;
  }

  .reason {
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
  }
}
</style>
