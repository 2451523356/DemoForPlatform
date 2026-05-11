<template>
  <div class="orders-manage">
    <el-card>
      <div class="toolbar">
        <el-alert
          title="退款操作请前往左侧菜单「退款审核」页面处理，支持审核通过/驳回并自动通知用户"
          type="info"
          show-icon
          :closable="false"
          style="margin-bottom:12px"
        />
        <div style="display:flex;gap:10px;align-items:center">
          <el-input
            v-model="searchOrderNo"
            placeholder="搜索订单号"
            style="width: 250px"
            clearable
            @keyup.enter.native="search"
          >
            <i slot="prefix" class="el-icon-search"></i>
          </el-input>
          <el-select v-model="filterStatus" placeholder="订单状态" clearable style="width: 150px" @change="search">
            <el-option label="未支付" :value="0"></el-option>
            <el-option label="已支付" :value="1"></el-option>
            <el-option label="已退款" :value="2"></el-option>
          </el-select>
          <el-button type="primary" @click="search">搜索</el-button>
        </div>
      </div>

      <el-table :data="orderList" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80"></el-table-column>
        <el-table-column prop="orderNo" label="订单号" width="200"></el-table-column>
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
        <el-table-column prop="amount" label="金额" width="100">
          <template slot-scope="scope">
            ¥{{ scope.row.amount }}
          </template>
        </el-table-column>
        <el-table-column prop="payType" label="支付方式" width="100">
          <template slot-scope="scope">
            <span v-if="scope.row.payType === 1">支付宝</span>
            <span v-else-if="scope.row.payType === 2">微信</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template slot-scope="scope">
            <el-tag :type="getStatusTagType(scope.row.status)">
              {{ getStatusName(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="payTime" label="支付时间" width="160">
          <template slot-scope="scope">
            {{ scope.row.payTime || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160"></el-table-column>
        <el-table-column label="操作" width="160">
          <template>
            <span class="text-muted">请前往退款审核处理</span>
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
  name: 'OrdersManage',
  data() {
    return {
      searchOrderNo: '',
      filterStatus: null,
      orderList: [],
      currentPage: 1,
      pageSize: 10,
      total: 0
    }
  },
  created() {
    this.getOrderList()
  },
  methods: {
    getStatusName(status) {
      const statusMap = {
        0: '未支付',
        1: '已支付',
        2: '已退款'
      }
      return statusMap[status] || '未知'
    },
    getStatusTagType(status) {
      const typeMap = {
        0: 'info',
        1: 'success',
        2: 'warning'
      }
      return typeMap[status] || 'info'
    },
    search() {
      this.currentPage = 1
      this.getOrderList()
    },
    handleSizeChange(size) {
      this.pageSize = size
      this.getOrderList()
    },
    handleCurrentChange(current) {
      this.currentPage = current
      this.getOrderList()
    },
    async getOrderList() {
      try {
        const params = {
          page: this.currentPage,
          size: this.pageSize
        }
        if (this.searchOrderNo) {
          params.orderNo = this.searchOrderNo
        }
        if (this.filterStatus !== null && this.filterStatus !== '') {
          params.status = this.filterStatus
        }

        const res = await this.$http.get('/admin/orders', { params })
        const responseData = res.data || res
        this.orderList = responseData?.records || []
        this.total = responseData?.total || 0
      } catch (error) {
        console.error('获取订单列表失败:', error)
        this.$message.error('获取订单列表失败')
      }
    }
  }
}
</script>

<style scoped lang="scss">
.orders-manage {
  .toolbar {
    display: flex;
    gap: 10px;
    margin-bottom: 20px;
  }

  .pagination {
    margin-top: 20px;
    text-align: center;
  }

  .text-muted {
    color: #909399;
  }
}
</style>
