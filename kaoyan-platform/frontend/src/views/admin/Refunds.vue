<template>
  <div class="refunds-manage">
    <el-card>
      <div class="toolbar">
        <el-select v-model="filterStatus" placeholder="审核状态" clearable style="width: 150px" @change="search">
          <el-option label="待审核" :value="0"></el-option>
          <el-option label="已通过" :value="1"></el-option>
          <el-option label="已驳回" :value="2"></el-option>
        </el-select>
        <el-button type="primary" @click="search">搜索</el-button>
      </div>

      <el-table :data="refundList" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80"></el-table-column>
        <el-table-column prop="userName" label="用户" width="120">
          <template slot-scope="scope">
            {{ scope.row.userName || '用户' + scope.row.userId }}
          </template>
        </el-table-column>
        <el-table-column prop="courseTitle" label="课程" min-width="150" show-overflow-tooltip>
          <template slot-scope="scope">
            {{ scope.row.courseTitle || '课程' + scope.row.courseId }}
          </template>
        </el-table-column>
        <el-table-column prop="reason" label="退款理由" min-width="200">
          <template slot-scope="scope">
            <span class="reason">{{ scope.row.reason || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template slot-scope="scope">
            <el-tag :type="getStatusTagType(scope.row.status)">
              {{ getStatusName(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="rejectReason" label="驳回理由" min-width="150">
          <template slot-scope="scope">
            <span>{{ scope.row.rejectReason || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="申请时间" width="160"></el-table-column>
        <el-table-column label="操作" width="200">
          <template slot-scope="scope">
            <el-button
              v-if="scope.row.status === 0"
              size="mini"
              type="success"
              @click="approveRefund(scope.row)"
            >
              通过
            </el-button>
            <el-button
              v-if="scope.row.status === 0"
              size="mini"
              type="danger"
              @click="showRejectDialog(scope.row)"
            >
              驳回
            </el-button>
            <span v-if="scope.row.status !== 0" class="text-muted">已处理</span>
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

    <!-- 驳回理由弹窗 -->
    <el-dialog title="驳回退款申请" :visible.sync="rejectDialogVisible" width="420px">
      <el-form label-width="80px">
        <el-form-item label="驳回理由">
          <el-input
            v-model="rejectReason"
            type="textarea"
            :rows="3"
            placeholder="请输入驳回理由（选填）"
            maxlength="200"
            show-word-limit
          ></el-input>
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="rejectDialogVisible = false">取消</el-button>
        <el-button type="danger" @click="confirmReject">确定驳回</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: 'RefundsManage',
  data() {
    return {
      filterStatus: null,
      refundList: [],
      currentPage: 1,
      pageSize: 10,
      total: 0,
      rejectDialogVisible: false,
      rejectReason: '',
      currentRefund: null
    }
  },
  created() {
    this.getRefundList()
  },
  methods: {
    getStatusName(status) {
      const statusMap = {
        0: '待审核',
        1: '已通过',
        2: '已驳回'
      }
      return statusMap[status] || '未知'
    },
    getStatusTagType(status) {
      const typeMap = {
        0: 'warning',
        1: 'success',
        2: 'danger'
      }
      return typeMap[status] || 'info'
    },
    search() {
      this.currentPage = 1
      this.getRefundList()
    },
    handleSizeChange(size) {
      this.pageSize = size
      this.getRefundList()
    },
    handleCurrentChange(current) {
      this.currentPage = current
      this.getRefundList()
    },
    async approveRefund(row) {
      try {
        await this.$confirm('确定要通过该退款申请吗？通过后将自动退款并通知用户。', '审核确认', {
          type: 'warning'
        })
        await this.$http.put(`/admin/refund/${row.id}/approve`)
        this.$message.success('退款申请已通过')
        this.getRefundList()
      } catch (error) {
        if (error !== 'cancel') {
          this.$message.error('操作失败，请稍后重试')
        }
      }
    },
    showRejectDialog(row) {
      this.currentRefund = row
      this.rejectReason = ''
      this.rejectDialogVisible = true
    },
    async confirmReject() {
      if (!this.currentRefund) return
      try {
        await this.$http.put(`/admin/refund/${this.currentRefund.id}/reject`, {
          reason: this.rejectReason || '暂无理由'
        })
        this.$message.success('已驳回退款申请')
        this.rejectDialogVisible = false
        this.getRefundList()
      } catch (error) {
        this.$message.error('驳回失败，请稍后重试')
      }
    },
    async getRefundList() {
      try {
        const params = {
          page: this.currentPage,
          size: this.pageSize
        }
        if (this.filterStatus !== null && this.filterStatus !== '') {
          params.status = this.filterStatus
        }
        const res = await this.$http.get('/admin/refunds', { params })
        this.refundList = res?.records || []
        this.total = res?.total || 0
      } catch (error) {
        this.refundList = []
        this.total = 0
        console.error('获取退款列表失败:', error)
        this.$message.error('获取退款列表失败：' + (error.message || '请检查服务是否正常'))
      }
    }
  }
}
</script>

<style scoped lang="scss">
.refunds-manage {
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

  .text-muted {
    color: #909399;
  }
}
</style>
