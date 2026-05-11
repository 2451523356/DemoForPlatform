<template>
  <div class="comments-manage">
    <el-card>
      <div class="toolbar">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索评论内容"
          style="width: 300px"
          clearable
          @keyup.enter.native="search"
        >
          <i slot="prefix" class="el-icon-search"></i>
        </el-input>
        <el-select v-model="filterStatus" placeholder="评论状态" clearable style="width: 120px" @change="search">
          <el-option label="正常" :value="1"></el-option>
          <el-option label="禁用" :value="0"></el-option>
        </el-select>
        <el-button type="primary" @click="search">搜索</el-button>
      </div>

      <el-table :data="commentList" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80"></el-table-column>
        <el-table-column prop="postId" label="帖子ID" width="100"></el-table-column>
        <el-table-column prop="username" label="用户" width="120">
          <template slot-scope="scope">
            {{ scope.row.username || '用户' + scope.row.userId }}
          </template>
        </el-table-column>
        <el-table-column prop="content" label="评论内容">
          <template slot-scope="scope">
            <span class="content">{{ scope.row.content }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="likeCount" label="点赞数" width="80"></el-table-column>
        <el-table-column prop="status" label="状态" width="80">
          <template slot-scope="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
              {{ scope.row.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160"></el-table-column>
        <el-table-column label="操作" width="200">
          <template slot-scope="scope">
            <el-button
              size="mini"
              :type="scope.row.status === 1 ? 'warning' : 'success'"
              @click="toggleStatus(scope.row)"
            >
              {{ scope.row.status === 1 ? '禁用' : '启用' }}
            </el-button>
            <el-button size="mini" type="danger" @click="deleteComment(scope.row)">删除</el-button>
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
  name: 'CommentsManage',
  data() {
    return {
      searchKeyword: '',
      filterStatus: null,
      commentList: [],
      currentPage: 1,
      pageSize: 10,
      total: 0
    }
  },
  created() {
    this.getCommentList()
  },
  methods: {
    search() {
      this.currentPage = 1
      this.getCommentList()
    },
    handleSizeChange(size) {
      this.pageSize = size
      this.getCommentList()
    },
    handleCurrentChange(current) {
      this.currentPage = current
      this.getCommentList()
    },
    async toggleStatus(comment) {
      const newStatus = comment.status === 1 ? 0 : 1
      const action = newStatus === 0 ? '禁用' : '启用'
      try {
        await this.$confirm(`确定要${action}该评论吗？`, '操作确认', {
          type: 'warning'
        })
        await this.$http.put(`/admin/comment/${comment.id}/status`, null, {
          params: { status: newStatus }
        })
        this.$message.success(`${action}成功`)
        this.getCommentList()
      } catch (error) {
        if (error !== 'cancel') {
          console.error(error)
        }
      }
    },
    async deleteComment(comment) {
      try {
        await this.$confirm('确定要删除该评论吗？', '提示', {
          type: 'warning'
        })
        await this.$http.delete(`/admin/comment/${comment.id}`)
        this.$message.success('删除成功')
        this.getCommentList()
      } catch (error) {
        if (error !== 'cancel') {
          console.error(error)
        }
      }
    },
    async getCommentList() {
      try {
        const params = {
          page: this.currentPage,
          size: this.pageSize
        }
        if (this.searchKeyword) {
          params.keyword = this.searchKeyword
        }
        if (this.filterStatus !== null && this.filterStatus !== '') {
          params.status = this.filterStatus
        }

        const res = await this.$http.get('/admin/comments', { params })
        const responseData = res.data || res
        this.commentList = responseData?.records || []
        this.total = responseData?.total || 0
      } catch (error) {
        console.error('获取评论列表失败:', error)
        this.$message.error('获取评论列表失败')
      }
    }
  }
}
</script>

<style scoped lang="scss">
.comments-manage {
  .toolbar {
    display: flex;
    gap: 10px;
    margin-bottom: 20px;
  }

  .pagination {
    margin-top: 20px;
    text-align: center;
  }

  .content {
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
  }
}
</style>
