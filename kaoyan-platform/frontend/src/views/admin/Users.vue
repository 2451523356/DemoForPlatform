<template>
  <div class="users">
    <el-card>
      <div class="toolbar">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索用户名"
          style="width: 200px"
          clearable
          @keyup.enter.native="search"
        >
          <i slot="prefix" class="el-icon-search"></i>
        </el-input>
        <el-select v-model="filterStatus" placeholder="用户状态" clearable style="width: 120px" @change="search">
          <el-option label="正常" :value="1"></el-option>
          <el-option label="禁用" :value="0"></el-option>
        </el-select>
        <el-select v-model="filterRole" placeholder="用户角色" clearable style="width: 150px" @change="search">
          <el-option label="普通用户" value="USER"></el-option>
          <el-option label="管理员" value="ADMIN"></el-option>
          <el-option label="内容审核员" value="CONTENT_AUDITOR"></el-option>
          <el-option label="课程管理员" value="COURSE_ADMIN"></el-option>
        </el-select>
        <el-button type="primary" @click="search">搜索</el-button>
      </div>

      <el-table :data="userList" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80"></el-table-column>
        <el-table-column prop="username" label="用户名"></el-table-column>
        <el-table-column prop="nickname" label="昵称"></el-table-column>
        <el-table-column prop="email" label="邮箱"></el-table-column>
        <el-table-column prop="role" label="角色" width="120">
          <template slot-scope="scope">
            <el-tag :type="getRoleTagType(scope.row.role)">
              {{ getRoleName(scope.row.role) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80">
          <template slot-scope="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
              {{ scope.row.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="points" label="积分" width="80"></el-table-column>
        <el-table-column prop="createTime" label="注册时间" width="160"></el-table-column>
        <el-table-column label="操作" width="200">
          <template slot-scope="scope">
            <el-button size="mini" @click="editUser(scope.row)">编辑</el-button>
            <el-button
              size="mini"
              :type="scope.row.status === 1 ? 'danger' : 'success'"
              @click="toggleStatus(scope.row)"
            >
              {{ scope.row.status === 1 ? '禁用' : '启用' }}
            </el-button>
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

    <el-dialog title="编辑用户" :visible.sync="editDialogVisible" width="500px">
      <el-form ref="editForm" :model="editForm" :rules="editRules" label-width="80px">
        <el-form-item label="用户名">
          <el-input v-model="editForm.username" disabled></el-input>
        </el-form-item>
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="editForm.nickname"></el-input>
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="editForm.email"></el-input>
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="editForm.role" style="width: 100%">
            <el-option label="普通用户" value="USER"></el-option>
            <el-option label="管理员" value="ADMIN"></el-option>
            <el-option label="内容审核员" value="CONTENT_AUDITOR"></el-option>
            <el-option label="课程管理员" value="COURSE_ADMIN"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="积分" prop="points">
          <el-input-number v-model="editForm.points" :min="0" style="width: 100%"></el-input-number>
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveUser">保存</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { mapGetters } from 'vuex'

export default {
  name: 'Users',
  computed: {
    ...mapGetters('user', ['userInfo']),
    currentUserId() { return this.userInfo ? this.userInfo.id : null }
  },
  data() {
    return {
      searchKeyword: '',
      filterStatus: null,
      filterRole: '',
      userList: [],
      currentPage: 1,
      pageSize: 10,
      total: 0,
      editDialogVisible: false,
      editForm: {
        id: 0,
        username: '',
        nickname: '',
        email: '',
        role: '',
        points: 0
      },
      editRules: {
        nickname: [
          { required: true, message: '请输入昵称', trigger: 'blur' }
        ],
        email: [
          { required: true, message: '请输入邮箱', trigger: 'blur' },
          { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
        ],
        role: [
          { required: true, message: '请选择角色', trigger: 'change' }
        ]
      }
    }
  },
  created() {
    this.getUserList()
  },
  methods: {
    getRoleName(role) {
      const roleMap = {
        'USER': '普通用户',
        'ADMIN': '管理员',
        'CONTENT_AUDITOR': '内容审核员',
        'COURSE_ADMIN': '课程管理员'
      }
      return roleMap[role] || role
    },
    getRoleTagType(role) {
      const typeMap = {
        'USER': 'success',
        'ADMIN': 'danger',
        'CONTENT_AUDITOR': 'warning',
        'COURSE_ADMIN': 'primary'
      }
      return typeMap[role] || 'info'
    },
    search() {
      this.currentPage = 1
      this.getUserList()
    },
    handleSizeChange(size) {
      this.pageSize = size
      this.getUserList()
    },
    handleCurrentChange(current) {
      this.currentPage = current
      this.getUserList()
    },
    editUser(user) {
      if (user.id === this.currentUserId) { this.$message.warning('不能修改自己的账号'); return }
      this.editForm = { ...user }
      this.editDialogVisible = true
    },
    async toggleStatus(user) {
      if (user.id === this.currentUserId) { this.$message.warning('不能禁用自己'); return }
      try {
        const action = user.status === 1 ? '禁用' : '启用'
        await this.$confirm(`确定要${action}该用户吗？`, '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        await this.$http.put(`/admin/user/${user.id}/status`, null, {
          params: { status: user.status === 1 ? 0 : 1 }
        })
        this.$message.success('操作成功')
        this.getUserList()
      } catch (error) {
        if (error !== 'cancel') {
          console.error(error)
        }
      }
    },
    saveUser() {
      this.$refs.editForm.validate(async (valid) => {
        if (valid) {
          try {
            await this.$http.put(`/admin/user/${this.editForm.id}`, this.editForm)
            this.$message.success('保存成功')
            this.editDialogVisible = false
            this.getUserList()
          } catch (error) {
            console.error(error)
          }
        }
      })
    },
    async getUserList() {
      try {
        const params = {
          page: this.currentPage,
          size: this.pageSize
        }
        if (this.searchKeyword) {
          params.username = this.searchKeyword
        }
        if (this.filterStatus !== null && this.filterStatus !== '') {
          params.status = this.filterStatus
        }
        if (this.filterRole) {
          params.role = this.filterRole
        }

        const res = await this.$http.get('/admin/users', { params })
        console.log('用户列表响应数据:', res)
        const responseData = res.data || res
        this.userList = responseData?.records || []
        this.total = responseData?.total || 0
      } catch (error) {
        console.error('获取用户列表失败:', error)
        this.$message.error('获取用户列表失败')
      }
    }
  }
}
</script>

<style scoped lang="scss">
.users {
  .toolbar {
    display: flex;
    gap: 10px;
    margin-bottom: 20px;
  }

  .pagination {
    margin-top: 20px;
    text-align: center;
  }
}
</style>
