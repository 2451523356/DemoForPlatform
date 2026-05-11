<template>
  <div class="course-categories">
    <el-card>
      <div class="toolbar">
        <el-button type="primary" @click="showAddDialog(null)">
          <i class="el-icon-plus"></i> 新增一级分类
        </el-button>
      </div>

      <el-table :data="categoryList" style="width: 100%" row-key="id" border default-expand-all>
        <el-table-column prop="name" label="分类名称" width="300"></el-table-column>
        <el-table-column prop="sort" label="排序" width="100"></el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template slot-scope="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
              {{ scope.row.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160"></el-table-column>
        <el-table-column label="操作" width="300">
          <template slot-scope="scope">
            <el-button
              v-if="scope.row.parentId === 0"
              size="mini"
              type="primary"
              @click="showAddDialog(scope.row)"
            >
              添加子分类
            </el-button>
            <el-button size="mini" @click="editCategory(scope.row)">编辑</el-button>
            <el-button size="mini" type="danger" @click="deleteCategory(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="500px">
      <el-form ref="categoryForm" :model="categoryForm" :rules="categoryRules" label-width="80px">
        <el-form-item label="分类名称" prop="name">
          <el-input v-model="categoryForm.name" placeholder="请输入分类名称"></el-input>
        </el-form-item>
        <el-form-item label="父分类" v-if="parentCategory">
          <el-input :value="parentCategory.name" disabled></el-input>
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="categoryForm.sort" :min="0" style="width: 100%"></el-input-number>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="categoryForm.status">
            <el-radio :label="1">正常</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveCategory">保存</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: 'CourseCategories',
  data() {
    return {
      categoryList: [],
      dialogVisible: false,
      isEdit: false,
      parentCategory: null,
      categoryForm: {
        id: 0,
        name: '',
        parentId: 0,
        sort: 0,
        status: 1
      },
      categoryRules: {
        name: [
          { required: true, message: '请输入分类名称', trigger: 'blur' }
        ]
      }
    }
  },
  computed: {
    dialogTitle() {
      if (this.isEdit) {
        return '编辑分类'
      }
      return this.parentCategory ? '新增子分类' : '新增一级分类'
    }
  },
  created() {
    this.getCategoryList()
  },
  methods: {
    showAddDialog(parent) {
      this.isEdit = false
      this.parentCategory = parent
      this.categoryForm = {
        id: 0,
        name: '',
        parentId: parent ? parent.id : 0,
        sort: 0,
        status: 1
      }
      this.dialogVisible = true
    },
    editCategory(category) {
      this.isEdit = true
      this.parentCategory = null
      this.categoryForm = { ...category }
      this.dialogVisible = true
    },
    async deleteCategory(category) {
      try {
        await this.$confirm('确定要删除该分类吗？', '提示', {
          type: 'warning'
        })
        await this.$http.delete(`/admin/course-category/${category.id}`)
        this.$message.success('删除成功')
        this.getCategoryList()
      } catch (error) {
        if (error !== 'cancel') {
          if (error.response && error.response.data && error.response.data.message) {
            this.$message.error(error.response.data.message)
          } else {
            console.error(error)
          }
        }
      }
    },
    saveCategory() {
      this.$refs.categoryForm.validate(async (valid) => {
        if (valid) {
          try {
            if (this.isEdit) {
              await this.$http.put(`/admin/course-category/${this.categoryForm.id}`, this.categoryForm)
            } else {
              await this.$http.post('/admin/course-category', this.categoryForm)
            }
            this.$message.success('保存成功')
            this.dialogVisible = false
            this.getCategoryList()
          } catch (error) {
            console.error(error)
          }
        }
      })
    },
    async getCategoryList() {
      try {
        const res = await this.$http.get('/admin/course-categories')
        const categories = res.data || res || []
        // 构建树形结构
        this.categoryList = this.buildTree(categories)
      } catch (error) {
        console.error('获取分类列表失败:', error)
        this.$message.error('获取分类列表失败')
      }
    },
    buildTree(categories) {
      const map = {}
      const roots = []

      // 创建映射
      categories.forEach(item => {
        map[item.id] = { ...item, children: [] }
      })

      // 构建树
      categories.forEach(item => {
        if (item.parentId === 0) {
          roots.push(map[item.id])
        } else if (map[item.parentId]) {
          map[item.parentId].children.push(map[item.id])
        }
      })

      // 移除空的children数组
      const cleanChildren = (nodes) => {
        nodes.forEach(node => {
          if (node.children && node.children.length === 0) {
            delete node.children
          } else if (node.children) {
            cleanChildren(node.children)
          }
        })
      }
      cleanChildren(roots)

      return roots
    }
  }
}
</script>

<style scoped lang="scss">
.course-categories {
  .toolbar {
    margin-bottom: 20px;
  }
}
</style>
