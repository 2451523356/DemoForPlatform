<template>
  <div class="resource">
    <el-card>
      <div class="resource-header">
        <h1>学习资源</h1>
      </div>

      <div class="resource-filter">
        <el-form :inline="true" :model="filterForm" @submit.native.prevent="handleSearch">
          <el-form-item>
            <el-input
              v-model="searchKeyword"
              placeholder="搜索资源名称或描述"
              clearable
              class="search-input"
              @keyup.enter.native="handleSearch"
            >
              <i slot="prefix" class="el-icon-search"></i>
            </el-input>
          </el-form-item>
          <el-form-item label="科目">
            <el-select v-model="filterForm.subject" placeholder="选择科目" clearable @change="handleSearch" style="width:150px">
              <el-option label="全部" value=""></el-option>
              <el-option-group label="公共课">
                <el-option label="数学（一/二/三）" value="数学"></el-option>
                <el-option label="英语（一/二）" value="英语"></el-option>
                <el-option label="政治" value="政治"></el-option>
              </el-option-group>
              <el-option-group label="统考专业课">
                <el-option label="计算机（408）" value="计算机408"></el-option>
                <el-option label="金融学（431）" value="金融学431"></el-option>
                <el-option label="法硕联考（398/498）" value="法学"></el-option>
                <el-option label="教育学（311/333）" value="教育学"></el-option>
                <el-option label="心理学（312/347）" value="心理学"></el-option>
                <el-option label="临床医学（306）" value="医学"></el-option>
                <el-option label="管理类联考（199）" value="管理学"></el-option>
                <el-option label="经济学" value="经济学"></el-option>
              </el-option-group>
              <el-option-group label="其他专业课">
                <el-option label="文学/新闻传播" value="文学"></el-option>
                <el-option label="机械工程" value="机械"></el-option>
                <el-option label="电气工程" value="电气"></el-option>
                <el-option label="其他专业" value="其他"></el-option>
              </el-option-group>
            </el-select>
          </el-form-item>
          <el-form-item label="类型">
            <el-select v-model="filterForm.category" placeholder="选择类型" clearable @change="handleSearch" style="width:140px">
              <el-option label="全部" value=""></el-option>
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
          <el-form-item label="排序">
            <el-select v-model="sortBy" @change="handleSearch" style="width:130px">
              <el-option label="最新上传" value="newest"></el-option>
              <el-option label="最多下载" value="downloads"></el-option>
              <el-option label="最高评分" value="rating"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch" icon="el-icon-search">搜索</el-button>
            <el-button @click="handleReset">重置</el-button>
            <el-button type="success" @click="showUploadDialog" style="margin-left: 6px;">
              <i class="el-icon-upload2"></i> 上传资源
            </el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 热门资源 -->
      <div class="recommended-resources" v-if="recommendedResources.length > 0">
        <h2><i class="el-icon-trophy"></i> 热门资源</h2>
        <el-row :gutter="20">
          <el-col :span="8" v-for="item in recommendedResources" :key="'rec'+item.id">
            <el-card class="resource-card recommended" shadow="hover" @click.native="goToDetail(item.id)">
              <div class="card-badge" v-if="item.points === 0">免费</div>
              <div class="card-badge points" v-else>{{ item.points }}积分</div>
              <div class="resource-info">
                <div class="file-icon" :class="getFileIconClass(item)">
                  <i :class="getFileIcon(item)"></i>
                </div>
                <h3 class="resource-title">{{ item.title }}</h3>
                <p class="resource-desc">{{ item.description }}</p>
                <div class="resource-meta">
                  <el-tag size="mini" type="warning">{{ item.category }}</el-tag>
                  <el-tag size="mini" type="info">{{ item.subject }}</el-tag>
                  <span class="file-size">{{ formatFileSize(item.fileSize) }}</span>
                </div>
                <div class="resource-footer">
                  <div class="rating-box">
                    <el-rate v-model="item.rating" disabled show-score text-color="#ff9900"
                      :score-template="item.rating ? item.rating.toFixed(1) : '0.0'" />
                    <span class="rating-count">({{ item.ratingCount }})</span>
                  </div>
                  <span class="download-count">
                    <i class="el-icon-download"></i> {{ item.downloadCount }}
                  </span>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>

      <!-- 全部资源 -->
      <div class="resource-list-section">
        <h2 class="section-title">全部资源</h2>
        <el-row :gutter="20">
          <el-col :span="8" v-for="item in resourceList" :key="item.id">
            <el-card class="resource-card" shadow="hover" @click.native="goToDetail(item.id)">
              <div class="card-badge" v-if="item.points === 0">免费</div>
              <div class="card-badge points" v-else>{{ item.points }}积分</div>
              <div class="resource-info">
                <div class="file-icon" :class="getFileIconClass(item)">
                  <i :class="getFileIcon(item)"></i>
                </div>
                <h3 class="resource-title">{{ item.title }}</h3>
                <p class="resource-desc">{{ item.description }}</p>
                <div class="resource-meta">
                  <el-tag size="mini" type="warning">{{ item.category }}</el-tag>
                  <el-tag size="mini" type="info">{{ item.subject }}</el-tag>
                  <span class="file-size">{{ formatFileSize(item.fileSize) }}</span>
                </div>
                <div class="resource-footer">
                  <div class="rating-box">
                    <el-rate v-model="item.rating" disabled show-score text-color="#ff9900"
                      :score-template="item.rating ? item.rating.toFixed(1) : '0.0'" />
                    <span class="rating-count">({{ item.ratingCount }})</span>
                  </div>
                  <span class="download-count">
                    <i class="el-icon-download"></i> {{ item.downloadCount }}
                  </span>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>
        <div v-if="resourceList.length === 0" class="empty-state">
          <el-empty description="暂无符合条件的资源" />
        </div>
      </div>

      <div class="pagination">
        <el-pagination
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          :current-page="currentPage"
          :page-sizes="[6, 9, 12]"
          :page-size="pageSize"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
        />
      </div>
    </el-card>

    <!-- 上传资源对话框 -->
    <el-dialog title="上传资源" :visible.sync="uploadDialogVisible" width="600px">
      <el-form ref="uploadForm" :model="uploadForm" :rules="uploadRules" label-width="80px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="uploadForm.title" placeholder="请输入标题"></el-input>
        </el-form-item>
        <el-form-item label="资源类型" prop="category">
          <el-select v-model="uploadForm.category" placeholder="选择资源类型">
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
          <el-select v-model="uploadForm.subject" placeholder="选择科目">
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
        <el-form-item label="文件" prop="file">
          <el-upload
            class="upload-demo"
            :action="uploadUrl"
            :headers="uploadHeaders"
            :on-success="handleFileUploadSuccess"
            :on-error="handleFileUploadError"
            :before-upload="beforeFileUpload"
            :auto-upload="false"
            :file-list="fileList"
            ref="fileUpload"
          >
            <el-button size="small" type="primary">选择文件</el-button>
            <div slot="tip" class="el-upload__tip">支持PDF、Word、Excel、PPT等文件，大小不超过50MB</div>
          </el-upload>
        </el-form-item>
        <el-form-item label="积分" prop="points">
          <el-input-number v-model="uploadForm.points" :min="0"></el-input-number>
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="uploadForm.description" type="textarea" :rows="3" placeholder="请输入描述"></el-input>
        </el-form-item>
      </el-form>
      <el-alert type="info" :closable="false" style="margin-top:12px">
        <template slot="title">
          <p style="margin:0;font-size:13px;line-height:1.8">
            <strong>上传须知：</strong><br />
            1. 上传资源需经管理员审核，审核通过后才会公开显示；<br />
            2. 上传优质原创资源可获得 <strong>5-20 积分</strong> 奖励；<br />
            3. 禁止上传侵权、虚假、无效或与考研无关的资源。
          </p>
        </template>
      </el-alert>
      <span slot="footer">
        <el-button @click="uploadDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitUpload">提交上传</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: 'Resource',
  data() {
    return {
      filterForm: {
        category: '',
        subject: ''
      },
      searchKeyword: '',
      sortBy: 'newest',
      resourceList: [],
      currentPage: 1,
      pageSize: 9,
      total: 0,
      fileList: [],
      uploadDialogVisible: false,
      uploadForm: {
        title: '',
        category: '',
        subject: '',
        fileUrl: '',
        fileSize: 0,
        points: 0,
        description: ''
      },
      uploadRules: {
        title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
        category: [{ required: true, message: '请选择分类', trigger: 'change' }],
        subject: [{ required: true, message: '请选择学科', trigger: 'change' }]
      },
      recommendedResources: []
    }
  },
  computed: {
    uploadUrl() {
      return '/api/resource/upload'
    },
    uploadHeaders() {
      const token = this.$store.state.user.token
      return token ? { Authorization: 'Bearer ' + token } : {}
    }
  },
  created() {
    this.getResourceList()
    this.loadRecommended()
  },
  methods: {
    handleSearch() {
      this.currentPage = 1
      this.getResourceList()
    },
    handleReset() {
      this.searchKeyword = ''
      this.filterForm.category = ''
      this.filterForm.subject = ''
      this.sortBy = 'newest'
      this.currentPage = 1
      this.getResourceList()
    },
    handleSizeChange(size) {
      this.pageSize = size
      this.getResourceList()
    },
    handleCurrentChange(current) {
      this.currentPage = current
      this.getResourceList()
    },
    goToDetail(id) {
      this.$router.push(`/resource/${id}`)
    },
    async getResourceList() {
      try {
        const res = await this.$http.get('/resource/list', {
          params: {
            page: this.currentPage,
            size: this.pageSize,
            category: this.filterForm.category,
            subject: this.filterForm.subject,
            keyword: this.searchKeyword,
            sortBy: this.sortBy
          }
        })
        this.resourceList = res?.records || []
        this.total = res?.total || 0
      } catch (error) {
        console.error('获取资源列表失败:', error)
        this.$message.error('获取资源列表失败')
      }
    },
    async loadRecommended() {
      try {
        const res = await this.$http.get('/resource/list', {
          params: { page: 1, size: 10 }
        })
        const records = res?.records || []
        records.sort((a, b) => (b.downloadCount || 0) - (a.downloadCount || 0))
        this.recommendedResources = records.slice(0, 3)
      } catch (error) {
        console.error('获取热门资源失败:', error)
      }
    },
    getFileIcon(item) {
      const ext = this.getFileExtension(item)
      const iconMap = {
        pdf: 'el-icon-document',
        doc: 'el-icon-document',
        docx: 'el-icon-document',
        xls: 'el-icon-s-data',
        xlsx: 'el-icon-s-data',
        ppt: 'el-icon-present',
        pptx: 'el-icon-present',
        zip: 'el-icon-folder-opened',
        rar: 'el-icon-folder-opened',
        mp4: 'el-icon-video-play',
        txt: 'el-icon-tickets'
      }
      return iconMap[ext] || 'el-icon-document'
    },
    getFileIconClass(item) {
      const ext = this.getFileExtension(item)
      if (ext === 'pdf') return 'icon-pdf'
      if (['zip', 'rar'].includes(ext)) return 'icon-archive'
      if (ext === 'mp4') return 'icon-video'
      if (['doc', 'docx', 'xls', 'xlsx', 'ppt', 'pptx'].includes(ext)) return 'icon-office'
      return 'icon-other'
    },
    getFileExtension(item) {
      if (item.fileUrl) {
        const parts = item.fileUrl.split('.')
        if (parts.length > 1) return parts[parts.length - 1].toLowerCase()
      }
      if (item.fileType) return item.fileType.toLowerCase()
      return ''
    },
    formatFileSize(size) {
      if (!size) return '0 B'
      const units = ['B', 'KB', 'MB', 'GB']
      let index = 0
      while (size >= 1024 && index < units.length - 1) {
        size /= 1024
        index++
      }
      return size.toFixed(2) + ' ' + units[index]
    },
    showUploadDialog() {
      this.uploadForm = {
        title: '',
        category: '',
        subject: '',
        fileUrl: '',
        fileSize: 0,
        points: 0,
        description: ''
      }
      this.fileList = []
      this.uploadDialogVisible = true
    },
    handleFileUploadSuccess(response) {
      if (response && response.code === 200 && response.data) {
        this.uploadForm.fileUrl = response.data.fileUrl
        this.uploadForm.fileSize = response.data.fileSize
        this.$message.success('文件上传成功')
      } else if (response && response.fileUrl) {
        this.uploadForm.fileUrl = response.fileUrl
        this.uploadForm.fileSize = response.fileSize || 0
        this.$message.success('文件上传成功')
      } else {
        this.$message.error('文件上传失败：' + (response?.message || '未知错误'))
      }
    },
    handleFileUploadError(err) {
      this.$message.error('文件上传失败，请稍后重试')
      console.error('Upload error:', err)
    },
    beforeFileUpload(file) {
      const allowedTypes = ['application/pdf', 'application/msword',
        'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
        'application/vnd.ms-excel',
        'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
        'application/vnd.ms-powerpoint',
        'application/vnd.openxmlformats-officedocument.presentationml.presentation']
      const isAllowedType = allowedTypes.includes(file.type) || file.name.match(/\.(pdf|doc|docx|xls|xlsx|ppt|pptx|txt|zip|rar)$/i)
      const isLt50M = file.size / 1024 / 1024 < 50

      if (!isAllowedType) {
        this.$message.error('不支持的文件类型，请上传PDF、Word、Excel、PPT等文件')
        return false
      }
      if (!isLt50M) {
        this.$message.error('文件大小不能超过50MB')
        return false
      }
      return true
    },
    async submitUpload() {
      this.$refs.uploadForm.validate(async (valid) => {
        if (!valid) return
        if (!this.$refs.fileUpload.uploadFiles || this.$refs.fileUpload.uploadFiles.length === 0) {
          this.$message.warning('请选择要上传的文件')
          return
        }
        try {
          this.$refs.fileUpload.submit()
          await new Promise((resolve, reject) => {
            const checkInterval = setInterval(() => {
              const uploading = this.$refs.fileUpload.uploadFiles.some(f => f.status === 'uploading' || f.status === 'ready')
              if (!uploading) {
                clearInterval(checkInterval)
                const hasError = this.$refs.fileUpload.uploadFiles.some(f => f.status === 'fail')
                if (hasError) {
                  reject(new Error('文件上传失败'))
                } else if (this.uploadForm.fileUrl) {
                  resolve()
                } else {
                  reject(new Error('文件上传未完成'))
                }
              }
            }, 500)
          })
          await this.$http.post('/resource/add', {
            title: this.uploadForm.title,
            category: this.uploadForm.category,
            subject: this.uploadForm.subject,
            fileUrl: this.uploadForm.fileUrl,
            fileSize: this.uploadForm.fileSize,
            points: this.uploadForm.points,
            description: this.uploadForm.description
          })
          this.$message.success('资源上传成功，等待审核')
          this.uploadDialogVisible = false
          this.getResourceList()
        } catch (error) {
          console.error('上传失败:', error)
          this.$message.error(error.message || '上传失败，请稍后重试')
        }
      })
    }
  }
}
</script>

<style scoped lang="scss">
.resource {
  .resource-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;

    h1 {
      font-size: 24px;
      color: #303133;
      margin: 0;
    }
  }

  .resource-filter {
    margin-bottom: 24px;
    padding: 16px 20px;
    background: #f5f7fa;
    border-radius: 8px;
  }

  .recommended-resources {
    margin-bottom: 32px;

    h2 {
      font-size: 18px;
      color: #303133;
      margin: 0 0 20px 0;
      padding-left: 12px;
      border-left: 4px solid #e6a23c;

      i {
        color: #e6a23c;
        margin-right: 6px;
      }
    }
  }

  .resource-list-section {
    .section-title {
      font-size: 18px;
      color: #303133;
      margin: 0 0 20px 0;
      padding-left: 12px;
      border-left: 4px solid #409EFF;
    }
  }

  .empty-state {
    padding: 40px 0;
  }

  .resource-card {
    cursor: pointer;
    transition: all 0.3s ease;
    margin-bottom: 20px;
    border-radius: 8px;
    overflow: hidden;
    position: relative;

    &:hover {
      transform: translateY(-6px);
      box-shadow: 0 8px 25px rgba(0, 0, 0, 0.12);
    }

    &.recommended {
      border: 2px solid #e6a23c;
      background: linear-gradient(135deg, #fffdf6 0%, #ffffff 100%);
    }

    .card-badge {
      position: absolute;
      top: 12px;
      right: 12px;
      background: #67c23a;
      color: #fff;
      padding: 2px 10px;
      border-radius: 12px;
      font-size: 12px;
      font-weight: 500;
      z-index: 1;

      &.points {
        background: #e6a23c;
      }
    }

    .resource-info {
      padding: 10px 0;

      .file-icon {
        width: 48px;
        height: 48px;
        border-radius: 10px;
        display: flex;
        align-items: center;
        justify-content: center;
        margin-bottom: 12px;
        font-size: 24px;

        &.icon-pdf {
          background: #fef0f0;
          color: #f56c6c;
        }
        &.icon-archive {
          background: #fdf6ec;
          color: #e6a23c;
        }
        &.icon-video {
          background: #ecf5ff;
          color: #409eff;
        }
        &.icon-office {
          background: #f0f9eb;
          color: #67c23a;
        }
        &.icon-other {
          background: #f4f4f5;
          color: #909399;
        }
      }

      .resource-title {
        font-size: 15px;
        margin-bottom: 8px;
        color: #303133;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
        font-weight: 600;
        line-height: 1.4;
      }

      .resource-desc {
        font-size: 13px;
        color: #909399;
        margin-bottom: 12px;
        line-height: 1.5;
        overflow: hidden;
        text-overflow: ellipsis;
        display: -webkit-box;
        -webkit-line-clamp: 2;
        -webkit-box-orient: vertical;
      }

      .resource-meta {
        display: flex;
        flex-wrap: wrap;
        gap: 6px;
        margin-bottom: 12px;
        font-size: 12px;
        color: #909399;
        align-items: center;

        .file-size {
          color: #c0c4cc;
          margin-left: auto;
        }
      }

      .resource-footer {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding-top: 10px;
        border-top: 1px solid #f2f2f2;

        .rating-box {
          display: flex;
          align-items: center;
          gap: 4px;

          .rating-count {
            font-size: 12px;
            color: #c0c4cc;
          }
        }

        .download-count {
          font-size: 12px;
          color: #909399;
          display: flex;
          align-items: center;
          gap: 3px;
        }
      }
    }
  }

  .pagination {
    text-align: center;
    margin-top: 30px;
  }
}
</style>
