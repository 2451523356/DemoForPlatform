<template>
  <div class="courses-manage">
    <el-card>
      <div class="toolbar">
        <el-button type="primary" @click="showAddDialog">
          <i class="el-icon-plus"></i> 新增课程
        </el-button>
      </div>

      <el-table :data="courseList" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80"></el-table-column>
        <el-table-column prop="title" label="标题"></el-table-column>
        <el-table-column prop="stage" label="阶段" width="80"></el-table-column>
        <el-table-column prop="form" label="形式" width="80"></el-table-column>
        <el-table-column prop="price" label="价格" width="100">
          <template slot-scope="scope">
            {{ scope.row.price > 0 ? '¥' + scope.row.price : '免费' }}
          </template>
        </el-table-column>
        <el-table-column prop="studentCount" label="学员数" width="80"></el-table-column>
        <el-table-column prop="status" label="状态" width="80">
          <template slot-scope="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'info'">
              {{ scope.row.status === 1 ? '上架' : '下架' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160"></el-table-column>
        <el-table-column label="操作" width="280">
          <template slot-scope="scope">
            <el-button size="mini" type="primary" @click="manageChapters(scope.row)">章节管理</el-button>
            <el-button size="mini" @click="editCourse(scope.row)">编辑</el-button>
            <el-button
              size="mini"
              :type="scope.row.status === 1 ? 'warning' : 'success'"
              @click="toggleStatus(scope.row)"
            >
              {{ scope.row.status === 1 ? '下架' : '上架' }}
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

    <el-dialog :title="isEdit ? '编辑课程' : '新增课程'" :visible.sync="dialogVisible" width="800px">
      <el-form ref="courseForm" :model="courseForm" :rules="courseRules" label-width="80px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="courseForm.title" placeholder="请输入标题"></el-input>
        </el-form-item>
        <el-form-item label="封面" prop="cover">
          <div style="display:flex;align-items:center;gap:10px">
            <el-upload action="/api/resource/upload" :headers="uploadHeaders"
              :show-file-list="false" :on-success="onCoverUploadSuccess"
              :before-upload="beforeCoverUpload" accept="image/*">
              <el-button size="small" type="primary">上传封面图片</el-button>
            </el-upload>
            <el-input v-model="courseForm.cover" placeholder="或输入封面URL" style="flex:1" />
          </div>
          <img v-if="courseForm.cover" :src="courseForm.cover" style="width:200px;height:120px;object-fit:cover;margin-top:8px;border-radius:4px" />
        </el-form-item>
        <el-form-item label="分类" prop="categoryId">
          <el-cascader
            v-model="selectedCategory"
            :options="categoryOptions"
            :props="{ value: 'id', label: 'name', children: 'children', checkStrictly: false }"
            placeholder="选择课程分类"
            style="width: 100%"
            clearable
            @change="onCategoryChange"
          ></el-cascader>
        </el-form-item>
        <el-form-item label="学科" prop="subject">
          <el-select v-model="courseForm.subject" placeholder="选择学科" style="width: 100%" clearable>
            <el-option label="数学" value="数学"></el-option>
            <el-option label="英语" value="英语"></el-option>
            <el-option label="政治" value="政治"></el-option>
            <el-option label="计算机（408）" value="计算机408"></el-option>
            <el-option label="金融学（431）" value="金融学431"></el-option>
            <el-option label="法学" value="法学"></el-option>
            <el-option label="教育学" value="教育学"></el-option>
            <el-option label="心理学" value="心理学"></el-option>
            <el-option label="医学" value="医学"></el-option>
            <el-option label="管理学" value="管理学"></el-option>
            <el-option label="经济学" value="经济学"></el-option>
            <el-option label="文学" value="文学"></el-option>
            <el-option label="其他" value="其他"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="阶段" prop="stage">
          <el-select v-model="courseForm.stage" placeholder="选择阶段" style="width: 100%">
            <el-option label="基础" value="基础"></el-option>
            <el-option label="强化" value="强化"></el-option>
            <el-option label="冲刺" value="冲刺"></el-option>
            <el-option label="真题" value="真题"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="形式" prop="form">
          <el-select v-model="courseForm.form" placeholder="选择形式" style="width: 100%">
            <el-option label="视频" value="视频"></el-option>
            <el-option label="直播" value="直播"></el-option>
            <el-option label="录播+直播" value="录播+直播"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="价格" prop="price">
          <el-input-number v-model="courseForm.price" :min="0" :precision="2" style="width: 100%"></el-input-number>
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="courseForm.description" type="textarea" :rows="3" placeholder="请输入描述"></el-input>
        </el-form-item>
        <el-form-item label="大纲" prop="outline">
          <el-input v-model="courseForm.outline" type="textarea" :rows="5" placeholder="请输入课程大纲"></el-input>
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveCourse">保存</el-button>
      </span>
    </el-dialog>

    <el-dialog :title="'章节管理 - ' + currentCourse.title" :visible.sync="chapterDialogVisible" width="900px">
      <div class="chapter-toolbar">
        <el-button type="primary" size="small" @click="showAddChapterDialog">
          <i class="el-icon-plus"></i> 新增章节
        </el-button>
      </div>

      <el-table :data="chapterList" style="width: 100%">
        <el-table-column prop="sort" label="排序" width="80"></el-table-column>
        <el-table-column prop="title" label="章节标题"></el-table-column>
        <el-table-column prop="duration" label="时长(分钟)" width="100"></el-table-column>
        <el-table-column prop="isFree" label="免费" width="80">
          <template slot-scope="scope">
            <el-tag :type="scope.row.isFree === 1 ? 'success' : 'info'">
              {{ scope.row.isFree === 1 ? '是' : '否' }}
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
        <el-table-column label="操作" width="150">
          <template slot-scope="scope">
            <el-button size="mini" @click="editChapter(scope.row)">编辑</el-button>
            <el-button size="mini" type="danger" @click="deleteChapter(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <el-dialog :title="isEditChapter ? '编辑章节' : '新增章节'" :visible.sync="chapterFormDialogVisible" width="600px">
      <el-form ref="chapterForm" :model="chapterForm" :rules="chapterRules" label-width="80px">
        <el-form-item label="章节标题" prop="title">
          <el-input v-model="chapterForm.title" placeholder="请输入章节标题"></el-input>
        </el-form-item>
        <el-form-item label="视频地址" prop="videoUrl">
          <div style="display:flex;align-items:center;gap:10px">
            <el-upload action="/api/resource/upload" :headers="uploadHeaders"
              :show-file-list="false" :on-success="onVideoUploadSuccess"
              :before-upload="beforeVideoUpload" accept="video/*">
              <el-button size="small" type="danger">上传视频</el-button>
            </el-upload>
            <el-input v-model="chapterForm.videoUrl" placeholder="或输入视频URL" style="flex:1" />
          </div>
        </el-form-item>
        <el-form-item label="时长(分钟)" prop="duration">
          <el-input-number v-model="chapterForm.duration" :min="0" style="width: 100%" placeholder="上传视频后自动获取"></el-input-number>
          <div class="el-form-item__tip" style="color:#909399;font-size:12px;line-height:1.5">上传视频后可自动读取时长，也可手动修改</div>
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="chapterForm.sort" :min="0" style="width: 100%"></el-input-number>
        </el-form-item>
        <el-form-item label="是否免费" prop="isFree">
          <el-radio-group v-model="chapterForm.isFree">
            <el-radio :label="1">是</el-radio>
            <el-radio :label="0">否</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="chapterForm.status">
            <el-radio :label="1">正常</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="chapterFormDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveChapter">保存</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: 'CoursesManage',
  data() {
    return {
      courseList: [],
      currentPage: 1,
      pageSize: 10,
      total: 0,
      dialogVisible: false,
      isEdit: false,
      selectedCategory: [],
      categoryOptions: [],
      courseForm: {
        id: 0,
        title: '',
        cover: '',
        categoryId: '',
        subject: '',
        stage: '',
        form: '',
        price: 0,
        description: '',
        outline: ''
      },
      courseRules: {
        title: [
          { required: true, message: '请输入标题', trigger: 'blur' }
        ],
        stage: [
          { required: true, message: '请选择阶段', trigger: 'change' }
        ],
        form: [
          { required: true, message: '请选择形式', trigger: 'change' }
        ]
      },
      chapterDialogVisible: false,
      chapterFormDialogVisible: false,
      isEditChapter: false,
      currentCourse: {},
      chapterList: [],
      chapterForm: {
        id: 0,
        courseId: 0,
        title: '',
        videoUrl: '',
        duration: 0,
        sort: 0,
        isFree: 0,
        status: 1
      },
      chapterRules: {
        title: [
          { required: true, message: '请输入章节标题', trigger: 'blur' }
        ]
      }
    }
  },
  computed: {
    uploadHeaders() {
      const token = this.$store.state.user.token
      return token ? { Authorization: 'Bearer ' + token } : {}
    }
  },
  created() {
    this.getCourseList()
    this.fetchCategories()
  },
  methods: {
    onCoverUploadSuccess(response) {
      if (response && response.data && response.data.fileUrl) {
        this.courseForm.cover = response.data.fileUrl
        this.$message.success('封面上传成功')
      } else if (response && response.fileUrl) {
        this.courseForm.cover = response.fileUrl
        this.$message.success('封面上传成功')
      }
    },
    onVideoUploadSuccess(response) {
      if (response && response.data && response.data.fileUrl) {
        this.chapterForm.videoUrl = response.data.fileUrl
        this.$message.success('视频上传成功')
      } else if (response && response.fileUrl) {
        this.chapterForm.videoUrl = response.fileUrl
        this.$message.success('视频上传成功')
      }
    },
    beforeCoverUpload(file) {
      const isImage = file.type.startsWith('image/')
      if (!isImage) { this.$message.error('只能上传图片文件'); return false }
      return true
    },
    beforeVideoUpload(file) {
      const isVideo = file.type.startsWith('video/')
      const isLt500M = file.size / 1024 / 1024 < 500
      if (!isVideo) { this.$message.error('只能上传视频文件'); return false }
      if (!isLt500M) { this.$message.error('视频大小不能超过500MB'); return false }
      // 自动读取视频时长
      const url = URL.createObjectURL(file)
      const video = document.createElement('video')
      video.preload = 'metadata'
      video.onloadedmetadata = () => {
        URL.revokeObjectURL(url)
        const durationMinutes = Math.round(video.duration / 60)
        this.chapterForm.duration = durationMinutes > 0 ? durationMinutes : 0
      }
      video.onerror = () => {
        URL.revokeObjectURL(url)
      }
      video.src = url
      return true
    },
    async fetchCategories() {
      try {
        const res = await this.$http.get('/admin/course-categories')
        this.categoryOptions = res || []
      } catch (e) {
        console.error('获取分类失败', e)
      }
    },
    onCategoryChange(val) {
      if (val && val.length > 0) {
        this.courseForm.categoryId = String(val[val.length - 1])
      } else {
        this.courseForm.categoryId = ''
      }
    },
    showAddDialog() {
      this.isEdit = false
      this.selectedCategory = []
      this.courseForm = {
        id: 0,
        title: '',
        cover: '',
        categoryId: '',
        subject: '',
        stage: '',
        form: '',
        price: 0,
        description: '',
        outline: ''
      }
      this.dialogVisible = true
    },
    editCourse(course) {
      this.isEdit = true
      this.courseForm = { ...course }
      // 回显分类级联
      if (course.categoryId) {
        this.selectedCategory = this.resolveCategoryPath(this.categoryOptions, course.categoryId)
      } else {
        this.selectedCategory = []
      }
      this.dialogVisible = true
    },
    resolveCategoryPath(categories, targetId) {
      for (const cat of categories) {
        if (String(cat.id) === String(targetId)) return [cat.id]
        if (cat.children) {
          for (const child of cat.children) {
            if (String(child.id) === String(targetId)) return [cat.id, child.id]
          }
        }
      }
      return []
    },
    handleSizeChange(size) {
      this.pageSize = size
      this.getCourseList()
    },
    handleCurrentChange(current) {
      this.currentPage = current
      this.getCourseList()
    },
    async toggleStatus(course) {
      const action = course.status === 1 ? '下架' : '上架'
      try {
        await this.$confirm(`确定要${action}该课程吗？`, '操作确认', {
          type: 'warning'
        })
        await this.$http.put(`/admin/course/${course.id}`, { ...course, status: course.status === 1 ? 0 : 1 })
        this.$message.success(`${action}成功`)
        this.getCourseList()
      } catch (error) {
        if (error !== 'cancel') {
          console.error(error)
        }
      }
    },
    saveCourse() {
      this.$refs.courseForm.validate(async (valid) => {
        if (valid) {
          try {
            if (this.isEdit) {
              await this.$http.put(`/admin/course/${this.courseForm.id}`, this.courseForm)
            } else {
              await this.$http.post('/admin/course', this.courseForm)
            }
            this.$message.success('保存成功')
            this.dialogVisible = false
            this.getCourseList()
          } catch (error) {
            console.error(error)
          }
        }
      })
    },
    async getCourseList() {
      try {
        const res = await this.$http.get('/admin/courses', {
          params: {
            page: this.currentPage,
            size: this.pageSize
          }
        })
        const responseData = res.data || res
        this.courseList = responseData?.records || []
        this.total = responseData?.total || 0
      } catch (error) {
        console.error(error)
      }
    },
    async manageChapters(course) {
      this.currentCourse = course
      this.chapterDialogVisible = true
      await this.getChapterList()
    },
    showAddChapterDialog() {
      this.isEditChapter = false
      this.chapterForm = {
        id: 0,
        courseId: this.currentCourse.id,
        title: '',
        videoUrl: '',
        duration: 0,
        sort: this.chapterList.length,
        isFree: 0,
        status: 1
      }
      this.chapterFormDialogVisible = true
    },
    editChapter(chapter) {
      this.isEditChapter = true
      this.chapterForm = { ...chapter }
      this.chapterFormDialogVisible = true
    },
    async deleteChapter(chapter) {
      try {
        await this.$confirm('确定要删除该章节吗？', '提示', {
          type: 'warning'
        })
        await this.$http.delete(`/admin/course-chapter/${chapter.id}`)
        this.$message.success('删除成功')
        await this.getChapterList()
      } catch (error) {
        if (error !== 'cancel') {
          console.error(error)
        }
      }
    },
    saveChapter() {
      this.$refs.chapterForm.validate(async (valid) => {
        if (valid) {
          try {
            if (this.isEditChapter) {
              await this.$http.put(`/admin/course-chapter/${this.chapterForm.id}`, this.chapterForm)
            } else {
              await this.$http.post('/admin/course-chapter', this.chapterForm)
            }
            this.$message.success('保存成功')
            this.chapterFormDialogVisible = false
            await this.getChapterList()
          } catch (error) {
            console.error(error)
          }
        }
      })
    },
    async getChapterList() {
      try {
        const res = await this.$http.get(`/admin/course/${this.currentCourse.id}/chapters`)
        this.chapterList = Array.isArray(res) ? res : (res?.data || [])
      } catch (error) {
        console.error(error)
      }
    }
  }
}
</script>

<style scoped lang="scss">
.courses-manage {
  .toolbar {
    margin-bottom: 20px;
  }

  .pagination {
    margin-top: 20px;
    text-align: center;
  }

  .chapter-toolbar {
    margin-bottom: 15px;
  }
}
</style>
