<template>
  <div class="resources-manage">
    <el-card>
      <div class="toolbar">
        <el-button type="primary" @click="showAddDialog">
          <i class="el-icon-plus"></i> 新增资源
        </el-button>
      </div>

      <el-table :data="resourceList" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80"></el-table-column>
        <el-table-column prop="title" label="标题"></el-table-column>
        <el-table-column prop="category" label="分类"></el-table-column>
        <el-table-column prop="subject" label="学科"></el-table-column>
        <el-table-column prop="fileSize" label="大小">
          <template slot-scope="scope">
            {{ formatFileSize(scope.row.fileSize) }}
          </template>
        </el-table-column>
        <el-table-column prop="downloadCount" label="下载量"></el-table-column>
        <el-table-column prop="points" label="积分"></el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template slot-scope="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : scope.row.status === 2 ? 'danger' : 'info'">
              {{ scope.row.status === 1 ? '已通过' : scope.row.status === 2 ? '已拒绝' : '待审核' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间"></el-table-column>
        <el-table-column label="操作" width="300">
          <template slot-scope="scope">
            <el-button v-if="scope.row.status === 0" size="mini" type="success" @click="auditResource(scope.row, 1)">通过</el-button>
            <el-button v-if="scope.row.status === 0" size="mini" type="danger" @click="auditResource(scope.row, 2)">拒绝</el-button>
            <el-button size="mini" @click="editResource(scope.row)">编辑</el-button>
            <el-button size="mini" type="danger" @click="deleteResource(scope.row)">删除</el-button>
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

    <el-dialog :title="isEdit ? '编辑资源' : '新增资源'" :visible.sync="dialogVisible" width="600px">
      <el-form ref="resourceForm" :model="resourceForm" :rules="resourceRules" label-width="80px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="resourceForm.title" placeholder="请输入标题"></el-input>
        </el-form-item>
        <el-form-item label="资源类型" prop="category">
          <el-select v-model="resourceForm.category" placeholder="选择类型">
            <el-option-group label="真题资料">
              <el-option label="历年真题" value="历年真题"></el-option>
              <el-option label="真题解析" value="真题解析"></el-option>
              <el-option label="模拟试题" value="模拟试题"></el-option>
            </el-option-group>
            <el-option-group label="学习笔记">
              <el-option label="复习笔记" value="复习笔记"></el-option>
              <el-option label="课程讲义" value="课程讲义"></el-option>
              <el-option label="学霸笔记" value="学霸笔记"></el-option>
            </el-option-group>
            <el-option-group label="总结归纳">
              <el-option label="思维导图" value="思维导图"></el-option>
              <el-option label="公式手册" value="公式手册"></el-option>
              <el-option label="词汇手册" value="词汇手册"></el-option>
              <el-option label="知识点总结" value="知识点总结"></el-option>
            </el-option-group>
            <el-option-group label="备考模板">
              <el-option label="作文模板" value="作文模板"></el-option>
              <el-option label="答题模板" value="答题模板"></el-option>
              <el-option label="背诵提纲" value="背诵提纲"></el-option>
            </el-option-group>
          </el-select>
        </el-form-item>
        <el-form-item label="科目" prop="subject">
          <el-select v-model="resourceForm.subject" placeholder="选择科目">
            <el-option-group label="公共课">
              <el-option label="数学" value="数学"></el-option>
              <el-option label="英语" value="英语"></el-option>
              <el-option label="政治" value="政治"></el-option>
            </el-option-group>
            <el-option-group label="统考专业课">
              <el-option label="计算机（408）" value="计算机408"></el-option>
              <el-option label="金融学（431）" value="金融学431"></el-option>
              <el-option label="法学" value="法学"></el-option>
              <el-option label="教育学" value="教育学"></el-option>
              <el-option label="心理学" value="心理学"></el-option>
              <el-option label="医学" value="医学"></el-option>
              <el-option label="管理学" value="管理学"></el-option>
            </el-option-group>
            <el-option-group label="其他专业课">
              <el-option label="经济学" value="经济学"></el-option>
              <el-option label="文学" value="文学"></el-option>
              <el-option label="机械" value="机械"></el-option>
              <el-option label="电气" value="电气"></el-option>
              <el-option label="其他" value="其他"></el-option>
            </el-option-group>
          </el-select>
        </el-form-item>
        <el-form-item label="上传文件" prop="fileUrl">
          <el-upload
            class="file-upload"
            action="/api/resource/upload"
            :headers="uploadHeaders"
            :on-success="handleFileUploaded"
            :before-upload="beforeFileUpload"
            :file-list="fileList"
            :limit="1"
            :auto-upload="true"
            ref="fileUpload">
            <el-button size="small" type="primary">点击上传文件</el-button>
            <div slot="tip" class="el-upload__tip">支持PDF/Word/Excel/PPT/TXT/ZIP/RAR，不超过50MB</div>
          </el-upload>
        </el-form-item>
        <el-form-item label="当前文件URL" v-if="resourceForm.fileUrl">
          <el-input v-model="resourceForm.fileUrl" disabled></el-input>
        </el-form-item>
        <el-form-item label="文件大小">
          <el-input :value="formatFileSize(resourceForm.fileSize)" disabled></el-input>
        </el-form-item>
        <el-form-item label="积分" prop="points">
          <el-input-number v-model="resourceForm.points" :min="0"></el-input-number>
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="resourceForm.description" type="textarea" :rows="3" placeholder="请输入描述"></el-input>
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveResource">保存</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import Cookies from 'js-cookie'

export default {
  name: 'ResourcesManage',
  computed: {
    uploadHeaders() {
      const token = Cookies.get('token')
      return token ? { Authorization: 'Bearer ' + token } : {}
    }
  },
  data() {
    return {
      resourceList: [],
      currentPage: 1,
      pageSize: 10,
      total: 0,
      dialogVisible: false,
      isEdit: false,
      fileList: [],
      resourceForm: {
        id: 0,
        title: '',
        category: '',
        subject: '',
        fileUrl: '',
        fileSize: 0,
        points: 0,
        description: ''
      },
      resourceRules: {
        title: [
          { required: true, message: '请输入标题', trigger: 'blur' }
        ],
        category: [
          { required: true, message: '请选择分类', trigger: 'change' }
        ],
        subject: [
          { required: true, message: '请选择学科', trigger: 'change' }
        ],
        fileUrl: [
          { required: true, message: '请输入文件URL', trigger: 'blur' }
        ]
      }
    }
  },
  created() {
    this.getResourceList()
  },
  methods: {
    formatFileSize(size) {
      if (size < 1024) return size + ' B'
      if (size < 1024 * 1024) return (size / 1024).toFixed(2) + ' KB'
      return (size / (1024 * 1024)).toFixed(2) + ' MB'
    },
    showAddDialog() {
      this.isEdit = false
      this.resourceForm = {
        id: 0,
        title: '',
        category: '',
        subject: '',
        fileUrl: '',
        fileSize: 0,
        points: 0,
        description: ''
      }
      this.fileList = []
      this.dialogVisible = true
    },
    editResource(resource) {
      this.isEdit = true
      this.resourceForm = { ...resource }
      this.dialogVisible = true
    },
    handleSizeChange(size) {
      this.pageSize = size
      this.getResourceList()
    },
    handleCurrentChange(current) {
      this.currentPage = current
      this.getResourceList()
    },
    async deleteResource(resource) {
      try {
        await this.$confirm('确定要删除该资源吗？', '提示', {
          type: 'warning'
        })
        await this.$http.delete(`/admin/resource/${resource.id}`)
        this.$message.success('删除成功')
        this.getResourceList()
      } catch (error) {
        if (error !== 'cancel') {
          console.error(error)
        }
      }
    },
    async auditResource(resource, status) {
      const action = status === 1 ? '通过' : '拒绝'
      try {
        await this.$confirm(`确定要${action}该资源吗？`, '审核确认', {
          type: 'warning'
        })
        await this.$http.put(`/admin/resource/${resource.id}/status`, null, {
          params: { status }
        })
        this.$message.success(`审核${action}`)
        this.getResourceList()
      } catch (error) {
        if (error !== 'cancel') {
          console.error('审核失败:', error)
          this.$message.error('审核失败，请稍后重试')
        }
      }
    },
    handleFileUploaded(response) {
      const data = response.data || response
      if (data.fileUrl) {
        this.resourceForm.fileUrl = data.fileUrl
        this.resourceForm.fileSize = data.fileSize || 0
        this.$message.success('文件上传成功')
      }
    },
    beforeFileUpload(file) {
      const isValidType = /\.(pdf|doc|docx|xls|xlsx|ppt|pptx|txt|zip|rar)$/i.test(file.name)
      if (!isValidType) {
        this.$message.error('仅支持 PDF/Word/Excel/PPT/TXT/ZIP/RAR 格式')
        return false
      }
      const isLt50M = file.size / 1024 / 1024 < 50
      if (!isLt50M) {
        this.$message.error('文件大小不能超过50MB')
        return false
      }
      return true
    },
    saveResource() {
      this.$refs.resourceForm.validate(async (valid) => {
        if (valid) {
          if (!this.resourceForm.fileUrl) {
            this.$message.warning('请先上传文件')
            return
          }
          try {
            if (this.isEdit) {
              await this.$http.put(`/admin/resource/${this.resourceForm.id}`, this.resourceForm)
            } else {
              await this.$http.post('/admin/resource', this.resourceForm)
            }
            this.$message.success('保存成功')
            this.dialogVisible = false
            this.getResourceList()
          } catch (error) {
            this.$message.error('保存失败：' + (error.message || '请稍后重试'))
          }
        }
      })
    },
    async getResourceList() {
      try {
        const res = await this.$http.get('/admin/resources', {
          params: {
            page: this.currentPage,
            size: this.pageSize
          }
        })
        const responseData = res.data || res
        this.resourceList = responseData?.records || []
        this.total = responseData?.total || 0
      } catch (error) {
        console.error(error)
      }
    }
  }
}
</script>

<style scoped lang="scss">
.resources-manage {
  .toolbar {
    margin-bottom: 20px;
  }

  .pagination {
    margin-top: 20px;
    text-align: center;
  }
}
</style>
