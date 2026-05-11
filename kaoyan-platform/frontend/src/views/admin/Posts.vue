<template>
  <div class="posts-manage">
    <el-card>
      <el-table :data="postList" style="width: 100%" :row-key="(row) => row.id">
        <el-table-column prop="id" label="ID" width="80"></el-table-column>
        <el-table-column prop="title" label="标题" min-width="150" show-overflow-tooltip></el-table-column>
        <el-table-column prop="authorName" label="作者" width="100"></el-table-column>
        <el-table-column prop="topic" label="话题" width="120"></el-table-column>
        <el-table-column prop="likeCount" label="点赞" width="80"></el-table-column>
        <el-table-column prop="commentCount" label="评论" width="80"></el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template slot-scope="scope">
            <el-tag :type="statusTypeMap[scope.row.status] || 'info'">
              {{ statusTextMap[scope.row.status] || '未知' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="发布时间" width="160"></el-table-column>
        <el-table-column label="操作" width="250" fixed="right">
          <template slot-scope="scope">
            <el-button
              v-if="scope.row.status === 0"
              size="mini"
              type="success"
              @click="auditPost(scope.row, 1)"
            >
              通过
            </el-button>
            <el-button
              v-if="scope.row.status === 0"
              size="mini"
              type="danger"
              @click="auditPost(scope.row, 2)"
            >
              拒绝
            </el-button>
            <el-button size="mini" type="danger" @click="deletePost(scope.row)">删除</el-button>
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
  name: 'PostsManage',
  data() {
    return {
      postList: [],
      currentPage: 1,
      pageSize: 10,
      total: 0,
      statusTypeMap: { 0: 'warning', 1: 'success', 2: 'danger' },
      statusTextMap: { 0: '待审核', 1: '已通过', 2: '已拒绝' }
    }
  },
  created() {
    this.getPostList()
  },
  methods: {
    handleSizeChange(size) {
      this.pageSize = size
      this.getPostList()
    },
    handleCurrentChange(current) {
      this.currentPage = current
      this.getPostList()
    },
    async auditPost(post, status) {
      const action = status === 1 ? '通过' : '拒绝'
      try {
        await this.$confirm(`确定要${action}该帖子吗？`, '审核确认', {
          type: 'warning'
        })
        await this.$http.put(`/admin/post/${post.id}/status`, null, {
          params: { status }
        })
        this.$message.success(`审核${action}`)
        this.getPostList()
      } catch (error) {
        if (error !== 'cancel') {
          console.error(error)
        }
      }
    },
    async deletePost(post) {
      try {
        await this.$confirm('确定要删除该帖子吗？', '提示', {
          type: 'warning'
        })
        await this.$http.delete(`/admin/post/${post.id}`)
        this.$message.success('删除成功')
        this.getPostList()
      } catch (error) {
        if (error !== 'cancel') {
          console.error(error)
        }
      }
    },
    async getPostList() {
      try {
        const res = await this.$http.get('/admin/posts', {
          params: {
            page: this.currentPage,
            size: this.pageSize
          }
        })
        this.postList = res?.records || []
        this.total = res?.total || 0
      } catch (error) {
        console.error(error)
      }
    }
  }
}
</script>

<style scoped lang="scss">
.posts-manage {
  .pagination {
    margin-top: 20px;
    text-align: center;
  }
}
</style>
