<template>
  <div class="news-manage">
    <el-card>
      <div class="toolbar">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索资讯标题"
          style="width: 250px"
          clearable
          @keyup.enter.native="search"
        >
          <i slot="prefix" class="el-icon-search"></i>
        </el-input>
        <el-button type="primary" @click="search">搜索</el-button>
        <el-button type="primary" @click="showAddDialog">
          <i class="el-icon-plus"></i> 新增资讯
        </el-button>
      </div>

      <el-table :data="newsList" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80"></el-table-column>
        <el-table-column prop="title" label="标题"></el-table-column>
        <el-table-column prop="category" label="分类"></el-table-column>
        <el-table-column prop="viewCount" label="浏览量"></el-table-column>
        <el-table-column prop="isTop" label="置顶">
          <template slot-scope="scope">
            <el-tag :type="scope.row.isTop ? 'danger' : 'info'">
              {{ scope.row.isTop ? '是' : '否' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态">
          <template slot-scope="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'info'">
              {{ scope.row.status === 1 ? '已发布' : '草稿' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间"></el-table-column>
        <el-table-column label="操作" width="250">
          <template slot-scope="scope">
            <el-button size="mini" @click="editNews(scope.row)">编辑</el-button>
            <el-button
              size="mini"
              :type="scope.row.isTop ? 'warning' : 'success'"
              @click="toggleTop(scope.row)"
            >
              {{ scope.row.isTop ? '取消置顶' : '置顶' }}
            </el-button>
            <el-button size="mini" type="danger" @click="deleteNews(scope.row)">删除</el-button>
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

    <el-dialog :title="isEdit ? '编辑资讯' : '新增资讯'" :visible.sync="dialogVisible" width="800px">
      <el-form ref="newsForm" :model="newsForm" :rules="newsRules" label-width="80px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="newsForm.title" placeholder="请输入标题"></el-input>
        </el-form-item>
        <el-form-item label="分类" prop="category">
          <el-select v-model="newsForm.category" placeholder="选择分类">
            <el-option-group label="政策公告">
              <el-option label="国家政策" value="国家政策"></el-option>
              <el-option label="教育部文件" value="教育部文件"></el-option>
              <el-option label="院校政策" value="院校政策"></el-option>
            </el-option-group>
            <el-option-group label="招生信息">
              <el-option label="招生简章" value="招生简章"></el-option>
              <el-option label="专业目录" value="专业目录"></el-option>
              <el-option label="报名通知" value="报名通知"></el-option>
              <el-option label="网报公告" value="网报公告"></el-option>
            </el-option-group>
            <el-option-group label="考试资讯">
              <el-option label="考试大纲" value="考试大纲"></el-option>
              <el-option label="初试信息" value="初试信息"></el-option>
              <el-option label="成绩查询" value="成绩查询"></el-option>
              <el-option label="国家线/复试线" value="国家线/复试线"></el-option>
            </el-option-group>
            <el-option-group label="复试调剂">
              <el-option label="复试通知" value="复试通知"></el-option>
              <el-option label="复试经验" value="复试经验"></el-option>
              <el-option label="调剂信息" value="调剂信息"></el-option>
              <el-option label="录取公示" value="录取公示"></el-option>
            </el-option-group>
            <el-option-group label="备考指导">
              <el-option label="备考指南" value="备考指南"></el-option>
              <el-option label="经验分享" value="经验分享"></el-option>
              <el-option label="复习方法" value="复习方法"></el-option>
              <el-option label="考研常识" value="考研常识"></el-option>
            </el-option-group>
            <el-option-group label="院校动态">
              <el-option label="院校新闻" value="院校新闻"></el-option>
              <el-option label="学科排名" value="学科排名"></el-option>
              <el-option label="招生宣讲" value="招生宣讲"></el-option>
            </el-option-group>
          </el-select>
        </el-form-item>
        <el-form-item label="备考阶段">
          <el-select v-model="newsForm.examStage" placeholder="选择备考阶段" clearable>
            <el-option label="择校评估" value="择校评估"></el-option>
            <el-option label="基础复习" value="基础复习"></el-option>
            <el-option label="强化提升" value="强化提升"></el-option>
            <el-option label="报名报考" value="报名报考"></el-option>
            <el-option label="冲刺模考" value="冲刺模考"></el-option>
            <el-option label="复试准备" value="复试准备"></el-option>
            <el-option label="调剂录取" value="调剂录取"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="摘要" prop="summary">
          <el-input v-model="newsForm.summary" type="textarea" :rows="2" placeholder="请输入摘要"></el-input>
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <quill-editor ref="newsEditorRef" v-model="newsForm.content" :options="editorOption"></quill-editor>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="newsForm.status">
            <el-radio :label="1">发布</el-radio>
            <el-radio :label="0">草稿</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveNews">保存</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { quillEditor } from 'vue-quill-editor'
import 'quill/dist/quill.core.css'
import 'quill/dist/quill.snow.css'
import 'quill/dist/quill.bubble.css'

export default {
  name: 'NewsManage',
  components: { quillEditor },
  data() {
    return {
      searchKeyword: '',
      newsList: [],
      currentPage: 1,
      pageSize: 10,
      total: 0,
      dialogVisible: false,
      isEdit: false,
      newsForm: {
        id: 0,
        title: '',
        category: '',
        examStage: '',
        summary: '',
        content: '',
        status: 1
      },
      newsRules: {
        title: [
          { required: true, message: '请输入标题', trigger: 'blur' }
        ],
        category: [
          { required: true, message: '请选择分类', trigger: 'change' }
        ],
        content: [
          { required: true, message: '请输入内容', trigger: 'blur' }
        ]
      },
      editorOption: {
        placeholder: '请输入资讯内容...',
        modules: {
          toolbar: {
            container: [
              ['bold', 'italic', 'underline', 'strike'],
              ['blockquote', 'code-block'],
              [{ 'header': 1 }, { 'header': 2 }],
              [{ 'list': 'ordered' }, { 'list': 'bullet' }],
              [{ 'script': 'sub' }, { 'script': 'super' }],
              [{ 'indent': '-1' }, { 'indent': '+1' }],
              [{ 'direction': 'rtl' }],
              [{ 'size': ['small', false, 'large', 'huge'] }],
              [{ 'header': [1, 2, 3, 4, 5, 6, false] }],
              [{ 'color': [] }, { 'background': [] }],
              [{ 'font': [] }],
              [{ 'align': [] }],
              ['link', 'image', 'video'],
              ['clean']
            ],
            handlers: {
              image: () => {
                const input = document.createElement('input')
                input.setAttribute('type', 'file')
                input.setAttribute('accept', 'image/*')
                input.click()
                input.onchange = async () => {
                  const file = input.files[0]
                  if (!file) return
                  const formData = new FormData()
                  formData.append('file', file)
                  try {
                    const res = await this.$http.post('/resource/upload', formData, {
                      headers: { 'Content-Type': 'multipart/form-data' }
                    })
                    const url = (res && res.fileUrl) ? res.fileUrl : ''
                    if (url) {
                      const quill = this.$refs.newsEditorRef ? this.$refs.newsEditorRef.quill : null
                      if (!quill) return
                      const range = quill.getSelection()
                      const index = range ? range.index : quill.getLength()
                      quill.insertEmbed(index, 'image', url)
                      quill.setSelection(index + 1)
                    }
                  } catch (e) {
                    this.$message.error('图片上传失败')
                  }
                }
              }
            }
          }
        }
      }
    }
  },
  created() {
    this.getNewsList()
  },
  methods: {
    showAddDialog() {
      this.isEdit = false
      this.newsForm = { id: 0, title: '', category: '', examStage: '', summary: '', content: '', status: 1 }
      this.dialogVisible = true
    },
    editNews(news) {
      this.isEdit = true
      this.newsForm = { ...news }
      this.dialogVisible = true
    },
    handleSizeChange(size) {
      this.pageSize = size
      this.getNewsList()
    },
    handleCurrentChange(current) {
      this.currentPage = current
      this.getNewsList()
    },
    async toggleTop(news) {
      const action = news.isTop ? '取消置顶' : '置顶'
      try {
        await this.$confirm(`确定要${action}该资讯吗？`, '提示', { type: 'warning' })
        await this.$http.put(`/admin/news/${news.id}`, { ...news, isTop: news.isTop ? 0 : 1 })
        this.$message.success(`${action}成功`)
        this.getNewsList()
      } catch (error) {
        if (error !== 'cancel') console.error(error)
      }
    },
    async deleteNews(news) {
      try {
        await this.$confirm('确定要删除该资讯吗？', '提示', {
          type: 'warning'
        })
        await this.$http.delete(`/admin/news/${news.id}`)
        this.$message.success('删除成功')
        this.getNewsList()
      } catch (error) {
        if (error !== 'cancel') {
          console.error(error)
        }
      }
    },
    async saveNews() {
      this.$refs.newsForm.validate(async (valid) => {
        if (!valid) return
        try {
          if (this.isEdit) {
            await this.$http.put(`/admin/news/${this.newsForm.id}`, this.newsForm)
          } else {
            await this.$http.post('/admin/news', this.newsForm)
          }
          this.$message.success('保存成功')
          this.dialogVisible = false
          this.getNewsList()
        } catch (error) {
          console.error(error)
        }
      })
    },
    search() {
      this.currentPage = 1
      this.getNewsList()
    },
    async getNewsList() {
      try {
        const params = {
          page: this.currentPage,
          size: this.pageSize
        }
        if (this.searchKeyword) {
          params.keyword = this.searchKeyword
        }
        const res = await this.$http.get('/admin/news', { params })
        this.newsList = res?.records || []
        this.total = res?.total || 0
      } catch (error) {
        console.error('获取资讯列表失败:', error)
        this.$message.error('获取资讯列表失败')
      }
    }
  }
}
</script>

<style scoped lang="scss">
.news-manage {
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
