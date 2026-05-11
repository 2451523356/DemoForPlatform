<template>
  <div class="course-learning">
    <el-backtop :right="40" :bottom="40" />

    <div class="learning-header">
      <el-button type="primary" @click="$router.back()" plain>
        <i class="el-icon-arrow-left"></i> 返回课程
      </el-button>
      <h1>{{ course.title }}</h1>
    </div>

    <div class="learning-content">
      <div class="video-section">
        <div class="video-player">
          <div v-if="currentChapter && currentChapter.videoUrl" class="custom-video-player">
            <div v-if="videoError" class="video-error-overlay">
              <i class="el-icon-video-camera-solid"></i>
              <p>视频加载失败，请检查网络连接后重试</p>
              <el-button size="small" @click="reloadVideo">重新加载</el-button>
            </div>
            <video
              :src="getVideoUrl(currentChapter.videoUrl)"
              @timeupdate="handleTimeUpdate"
              @ended="handleVideoEnded"
              @error="handleVideoError"
              @loadedmetadata="handleLoadedMetadata"
              @canplay="handleCanPlay"
              ref="videoPlayer"
              preload="metadata"
            >
              您的浏览器不支持视频播放
            </video>
            <div class="video-controls">
              <div class="control-left">
                <button class="control-btn" @click="togglePlay">
                  <i :class="isPlaying ? 'el-icon-video-pause' : 'el-icon-video-play'"></i>
                </button>
                <span class="time-display">{{ formatTime(currentTime) }} / {{ formatTime(videoDuration) }}</span>
                <div class="progress-container">
                  <el-slider
                    v-model="currentTime"
                    :min="0"
                    :max="videoDuration"
                    :step="0.1"
                    @change="seekTo"
                    class="video-progress"
                  />
                </div>
              </div>
              <div class="control-right">
                <button class="control-btn" @click="toggleNotes">
                  <i class="el-icon-edit"></i>
                </button>
                <button class="control-btn" @click="toggleComments">
                  <i class="el-icon-chat-line-round"></i>
                </button>
                <button class="control-btn" @click="toggleDanmaku">
                  <i class="el-icon-chat-dot-round"></i>
                </button>
                <div class="speed-control">
                  <el-dropdown @command="changeSpeed">
                    <button class="control-btn">
                      <span>{{ playbackSpeed }}x</span>
                      <i class="el-icon-arrow-down"></i>
                    </button>
                    <el-dropdown-menu slot="dropdown">
                      <el-dropdown-item command="0.5">0.5x</el-dropdown-item>
                      <el-dropdown-item command="0.75">0.75x</el-dropdown-item>
                      <el-dropdown-item command="1">1x</el-dropdown-item>
                      <el-dropdown-item command="1.25">1.25x</el-dropdown-item>
                      <el-dropdown-item command="1.5">1.5x</el-dropdown-item>
                      <el-dropdown-item command="2">2x</el-dropdown-item>
                    </el-dropdown-menu>
                  </el-dropdown>
                </div>
                <div class="quality-control">
                  <el-dropdown @command="changeQuality">
                    <button class="control-btn">
                      <span>{{ videoQuality }}</span>
                      <i class="el-icon-arrow-down"></i>
                    </button>
                    <el-dropdown-menu slot="dropdown">
                      <el-dropdown-item command="1080p">1080p</el-dropdown-item>
                      <el-dropdown-item command="720p">720p</el-dropdown-item>
                      <el-dropdown-item command="480p">480p</el-dropdown-item>
                      <el-dropdown-item command="360p">360p</el-dropdown-item>
                    </el-dropdown-menu>
                  </el-dropdown>
                </div>
                <button class="control-btn" @click="toggleFullscreen">
                  <i class="el-icon-full-screen"></i>
                </button>
              </div>
            </div>
          </div>
          <div v-else class="no-video">
            <el-empty description="请选择要学习的章节" />
          </div>
        </div>

        <div class="chapter-info" v-if="currentChapter">
          <h3>{{ currentChapter.title }}</h3>
          <div class="chapter-meta">
            <span class="duration"><i class="el-icon-time"></i> {{ currentChapter.duration }}分钟</span>
            <span class="progress">学习进度: {{ currentProgress }}%</span>
          </div>
        </div>
      </div>

      <div class="notes-panel" v-if="showNotes && currentChapter">
        <div class="notes-header"><h4>课程笔记</h4><el-button type="primary" size="mini" icon="el-icon-plus" @click="addNote">添加笔记</el-button></div>
        <div class="notes-input" v-if="showNoteInput">
          <el-input v-model="newNoteContent" type="textarea" :rows="3" :placeholder="'在此记录笔记... (当前视频时间: ' + formatTime(currentTime) + ')'" />
          <div class="notes-input-actions"><el-button size="mini" @click="showNoteInput=false">取消</el-button><el-button size="mini" type="primary" @click="saveNote">保存</el-button></div>
        </div>
        <div class="notes-list"><div v-for="note in notes" :key="note.id" class="note-item" @click="seekToNote(note)"><div class="note-time">{{ formatTime(note.videoTime) }}</div><div class="note-content">{{ note.content }}</div><el-button type="text" size="mini" style="color:#F56C6C" @click.stop="deleteNote(note.id)"><i class="el-icon-delete"/></el-button></div></div>
      </div>

      <div class="chapter-list">
        <div class="chapter-header">
          <h3>课程章节</h3>
          <span class="total-progress">总进度: {{ totalProgress }}%</span>
        </div>

        <div class="chapters">
          <div
            v-for="chapter in chapters"
            :key="chapter.id"
            class="chapter-item"
            :class="{
              'active': currentChapter && currentChapter.id === chapter.id,
              'completed': isChapterCompleted(chapter.id),
              'locked': !isChapterUnlocked(chapter)
            }"
            @click="selectChapter(chapter)"
          >
            <div class="chapter-index">
              <i v-if="isChapterCompleted(chapter.id)" class="el-icon-circle-check"></i>
              <i v-else-if="currentChapter && currentChapter.id === chapter.id" class="el-icon-video-play"></i>
              <i v-else class="el-icon-video-camera"></i>
            </div>
            <div class="chapter-details">
              <h4>{{ chapter.title }}</h4>
              <div class="chapter-meta">
                <span class="duration">{{ chapter.duration }}分钟</span>
                <el-tag v-if="chapter.isFree" size="mini" type="success">试看</el-tag>
                <el-tag v-else size="mini" type="warning">付费</el-tag>
                <el-button v-if="chapter.fileUrl" size="mini" type="primary" plain icon="el-icon-download"
                  @click.stop="downloadChapterFile(chapter)">{{ chapter.fileName || '配套资料' }}</el-button>
              </div>
            </div>
            <div class="chapter-status">
              <el-progress
                v-if="getChapterProgress(chapter.id) > 0"
                :percentage="getChapterProgress(chapter.id)"
                :stroke-width="6"
              />
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'CourseLearning',
  data() {
    return {
      courseId: null,
      isPurchased: false,
      course: {
        id: 0,
        title: ''
      },
      chapters: [],
      currentChapter: null,
      currentProgress: 0,
      totalProgress: 0,
      completedChapters: [],
      chapterProgress: {},
      videoDuration: 0,
      currentTime: 0,
      lastSaveTime: 0,
      videoError: false,
      isPlaying: false,
      playbackSpeed: 1,
      videoQuality: '720p',
      lastSavedPosition: 0,
      showNotes: false,
      showComments: false,
      showDanmaku: false,
      notes: [],
      showNoteInput: false,
      newNoteContent: '',
      comments: []
    }
  },
  created() {
    this.courseId = this.$route.params.id
    this.getCourseDetail()
    this.loadCourseData()
  },
  methods: {
    async checkPurchaseStatus() {
      try {
        const res = await this.$http.get(`/course/check-purchase/${this.courseId}`)
        this.isPurchased = res || false
      } catch (e) {
        this.isPurchased = false
      }
    },
    async loadCourseData() {
      await this.checkPurchaseStatus()
      await this.getCourseChapters()
      await this.getUserCourseProgress()
    },
    async getCourseDetail() {
      try {
        const res = await this.$http.get(`/course/detail/${this.courseId}`)
        this.course = res || {}
      } catch (error) {
        console.error('获取课程详情失败:', error)
      }
    },
    async getCourseChapters() {
      try {
        const res = await this.$http.get(`/course/chapters/${this.courseId}`)
        // 确保是数组
        this.chapters = Array.isArray(res) ? res : (res.data || [])

        if (this.chapters.length > 0) {
          // 检查URL中是否带有chapterId参数
          const chapterId = this.$route.query.chapterId
          if (chapterId) {
            const chapter = this.chapters.find(c => c.id === parseInt(chapterId))
            if (chapter) {
              this.selectChapter(chapter)
              return
            }
          }
          // 如果没有chapterId参数或未找到对应章节，选择第一个章节
          this.selectChapter(this.chapters[0])
        }
      } catch (error) {
        console.error('获取课程章节失败:', error)
      }
    },
    async getUserCourseProgress() {
      try {
        const res = await this.$http.get(`/course/user-progress/${this.courseId}`)
        const progress = res || {}
        this.completedChapters = progress.completedChapters || []
        this.chapterProgress = progress.chapterProgress || {}
        this.calculateTotalProgress()
        // 如果当前有选中的章节，更新当前进度
        if (this.currentChapter) {
          this.currentProgress = this.getChapterProgress(this.currentChapter.id)
        }
      } catch (error) {
        console.error('获取学习进度失败:', error)
      }
    },
    selectChapter(chapter) {
      if (!this.isChapterUnlocked(chapter)) {
        if (chapter.isFree) {
          this.$message.warning('请先完成前面的章节')
        } else {
          this.$message.warning('该章节需要购买课程后才能学习')
        }
        return
      }

      // 先暂停当前视频
      if (this.$refs.videoPlayer) {
        this.$refs.videoPlayer.pause()
        this.isPlaying = false
      }

      this.currentChapter = chapter
      this.currentProgress = this.getChapterProgress(chapter.id)
      this.videoError = false
      this.isPlaying = false
      this.currentTime = 0
      this.videoDuration = 0
      this.lastSavedPosition = 0

      // 使用 nextTick 确保 DOM 更新后再加载视频
      this.$nextTick(() => {
        if (this.$refs.videoPlayer) {
          this.$refs.videoPlayer.load()
        }
      })
    },
    handleTimeUpdate(event) {
      const video = event.target
      this.videoDuration = video.duration || 0
      this.currentTime = video.currentTime || 0

      if (this.videoDuration > 0) {
        this.currentProgress = Math.round((this.currentTime / this.videoDuration) * 100)
      }

      const now = Date.now()
      if (now - this.lastSaveTime > 5000) {
        // 计算本次实际学习时长（当前播放位置 - 上次保存的播放位置，上限5秒）
        const elapsed = Math.max(0, Math.round(this.currentTime - this.lastSavedPosition))
        this.saveProgress(this.currentTime, Math.min(elapsed, 10))
        this.lastSavedPosition = this.currentTime
        this.lastSaveTime = now
      }
    },
    togglePlay() {
      const video = this.$refs.videoPlayer
      if (video) {
        if (video.paused) {
          video.play()
          this.isPlaying = true
        } else {
          video.pause()
          this.isPlaying = false
        }
      }
    },
    seekTo(time) {
      const video = this.$refs.videoPlayer
      if (video) {
        video.currentTime = time
        this.currentTime = time
      }
    },
    formatTime(seconds) {
      if (!seconds) return '00:00'
      const mins = Math.floor(seconds / 60)
      const secs = Math.floor(seconds % 60)
      return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`
    },
    changeSpeed(speed) {
      const video = this.$refs.videoPlayer
      if (video) {
        video.playbackRate = parseFloat(speed)
        this.playbackSpeed = parseFloat(speed)
      }
    },
    changeQuality(quality) {
      this.videoQuality = quality
      // 在实际应用中，这里应该切换视频源
      this.$message.info(`已切换到${quality}清晰度`)
    },
    toggleFullscreen() {
      const video = this.$refs.videoPlayer
      if (video) {
        if (!document.fullscreenElement) {
          video.requestFullscreen().catch(err => {
            this.$message.error(`全屏请求失败: ${err.message}`)
          })
        } else {
          if (document.exitFullscreen) {
            document.exitFullscreen()
          }
        }
      }
    },
    toggleNotes() {
      this.showNotes = !this.showNotes
      if (this.showNotes) this.fetchNotes()
    },
    async fetchNotes() {
      try { const res = await this.$http.get('/course-note/list', { params: { chapterId: this.currentChapter?.id } }); this.notes = res?.records || res || [] } catch(e) {}
    },
    addNote() { this.showNoteInput = true; this.newNoteContent = '' },
    async saveNote() {
      if (!this.newNoteContent.trim() || !this.currentChapter) return
      try { await this.$http.post('/course-note/add', { courseId: this.courseId, chapterId: this.currentChapter.id, videoTime: Math.floor(this.currentTime), content: this.newNoteContent }); this.showNoteInput = false; this.fetchNotes(); this.$message.success('笔记已保存') } catch(e) { this.$message.error('保存失败') }
    },
    async deleteNote(id) { try { await this.$http.delete(`/course-note/${id}`); this.fetchNotes() } catch(e) {} },
    seekToNote(note) { if (this.$refs.videoPlayer) { this.$refs.videoPlayer.currentTime = note.videoTime; this.currentTime = note.videoTime } },
    toggleComments() {
      this.showComments = !this.showComments
    },
    toggleDanmaku() {
      this.showDanmaku = !this.showDanmaku
    },
    handleVideoEnded() {
      this.currentProgress = 100
      this.markChapterCompleted()
    },
    handleVideoError() {
      this.videoError = true
      this.isPlaying = false
    },
    reloadVideo() {
      this.videoError = false
      this.$nextTick(() => {
        const video = this.$refs.videoPlayer
        if (video) {
          video.load()
          video.play().catch(() => {})
        }
      })
    },
    handleLoadedMetadata(event) {
      const video = event.target
      this.videoDuration = video.duration || 0
      console.log('视频元数据加载完成，时长:', this.videoDuration)
    },
    handleCanPlay(event) {
      console.log('视频可以播放了')
      // 自动播放（如果用户之前点击过播放）
      if (this.isPlaying && event.target.paused) {
        event.target.play().catch(err => {
          console.log('自动播放被阻止:', err)
        })
      }
    },
    async saveProgress(currentTime, elapsedSeconds) {
      if (!this.currentChapter) return

      // 已完成的章节回看时进度不降低
      const cid = this.currentChapter.id
      const saved = this.getChapterProgress(cid)
      const progress = this.isChapterCompleted(cid) ? Math.max(this.currentProgress, saved) : this.currentProgress

      try {
        await this.$http.post('/course/update-progress', {
          courseId: this.courseId,
          chapterId: cid,
          progress: progress,
          currentTime: currentTime,
          elapsedSeconds: elapsedSeconds || 0
        })

        this.chapterProgress[cid] = progress
        this.calculateTotalProgress()
      } catch (error) {
        console.error('保存学习进度失败:', error)
      }
    },
    async markChapterCompleted() {
      if (!this.currentChapter) return

      try {
        await this.$http.post('/course/complete-chapter', {
          courseId: this.courseId,
          chapterId: this.currentChapter.id
        })

        if (!this.completedChapters.includes(this.currentChapter.id)) {
          this.completedChapters.push(this.currentChapter.id)
        }

        this.chapterProgress[this.currentChapter.id] = 100
        this.calculateTotalProgress()

        this.$message.success('恭喜完成本章节！')

        const currentIndex = this.chapters.findIndex(c => c.id === this.currentChapter.id)
        if (currentIndex < this.chapters.length - 1) {
          setTimeout(() => {
            this.selectChapter(this.chapters[currentIndex + 1])
          }, 1500)
        }
      } catch (error) {
        console.error('标记章节完成失败:', error)
      }
    },
    async downloadChapterFile(chapter) {
      if (!chapter.id) return
      try {
        const token = this.$store.state.user.token
        const resp = await fetch(`/api/course/chapter/${chapter.id}/download`, {
          headers: { 'Authorization': `Bearer ${token}` }
        })
        if (!resp.ok) { this.$message.error('下载失败'); return }
        const blob = await resp.blob()
        const url = window.URL.createObjectURL(blob)
        const a = document.createElement('a')
        a.href = url
        a.download = chapter.fileName || '资料.txt'
        document.body.appendChild(a)
        a.click()
        window.URL.revokeObjectURL(url)
        document.body.removeChild(a)
        this.$message.success('开始下载')
      } catch (e) {
        this.$message.error('下载失败')
      }
    },
    isChapterCompleted(chapterId) {
      return this.completedChapters.includes(chapterId)
    },
    isChapterUnlocked(chapter) {
      if (chapter.isFree) return true
      // 必须已购买课程才能学习付费章节
      return this.isPurchased
    },
    getChapterProgress(chapterId) {
      return this.chapterProgress[chapterId] || 0
    },
    calculateTotalProgress() {
      if (this.chapters.length === 0) {
        this.totalProgress = 0
        return
      }

      const totalProgress = this.chapters.reduce((sum, chapter) => {
        return sum + this.getChapterProgress(chapter.id)
      }, 0)

      this.totalProgress = Math.round(totalProgress / this.chapters.length)
    },
    getVideoUrl(videoUrl) {
      if (!videoUrl) return ''
      return videoUrl
    }
  }
}
</script>

<style scoped lang="scss">
.course-learning {
  padding: 20px;

  .learning-header {
    display: flex;
    align-items: center;
    gap: 20px;
    margin-bottom: 30px;

    h1 {
      font-size: 24px;
      color: #303133;
    }
  }

  .learning-content {
    display: grid;
    grid-template-columns: 2fr 1fr;
    gap: 20px;

    .video-section {
      .video-player {
          background: #000;
          border-radius: 8px;
          overflow: hidden;
          aspect-ratio: 16 / 9;
          margin-bottom: 20px;

          .custom-video-player {
            position: relative;
            width: 100%;
            height: 100%;

            .video-error-overlay {
              position: absolute; top: 0; left: 0; right: 0; bottom: 0;
              display: flex; flex-direction: column; align-items: center;
              justify-content: center; z-index: 10;
              background: rgba(0,0,0,0.85); color: #fff;
              i { font-size: 48px; margin-bottom: 12px; color: #909399; }
              p { font-size: 16px; margin-bottom: 16px; color: #C0C4CC; }
            }

            video {
              width: 100%;
              height: 100%;
              object-fit: contain;
            }

            .video-controls {
              position: absolute;
              bottom: 0;
              left: 0;
              right: 0;
              background: linear-gradient(transparent, rgba(0,0,0,0.8));
              color: #fff;
              padding: 15px;
              display: flex;
              justify-content: space-between;
              align-items: center;
              gap: 10px;

              .control-left {
                display: flex;
                align-items: center;
                gap: 10px;
                flex: 1;

                .control-btn {
                  background: transparent;
                  border: none;
                  color: #fff;
                  font-size: 18px;
                  cursor: pointer;
                  padding: 5px;

                  &:hover {
                    color: #409EFF;
                  }
                }

                .time-display {
                  font-size: 14px;
                  min-width: 100px;
                }

                .progress-container {
                  flex: 1;

                  .video-progress {
                    .el-slider__runway {
                      height: 4px;
                      background: rgba(255,255,255,0.3);
                    }

                    .el-slider__bar {
                      background: #409EFF;
                    }

                    .el-slider__button {
                      border-color: #409EFF;
                      background: #409EFF;
                    }
                  }
                }
              }

              .control-right {
                display: flex;
                align-items: center;
                gap: 5px;

                .control-btn {
                  background: transparent;
                  border: none;
                  color: #fff;
                  font-size: 18px;
                  cursor: pointer;
                  padding: 5px;

                  &:hover {
                    color: #409EFF;
                  }
                }

                .speed-control,
                .quality-control {
                  .control-btn {
                    font-size: 14px;
                    display: flex;
                    align-items: center;
                    gap: 2px;
                  }
                }
              }
            }
          }

          .no-video {
            display: flex;
            align-items: center;
            justify-content: center;
            height: 100%;
            color: #fff;
          }
        }

      .chapter-info {
        background: #fff;
        padding: 20px;
        border-radius: 8px;
        box-shadow: 0 2px 12px rgba(0,0,0,0.1);

        h3 {
          font-size: 18px;
          margin-bottom: 10px;
          color: #303133;
        }

        .chapter-meta {
          display: flex;
          gap: 20px;
          font-size: 14px;
          color: #606266;

          .duration {
            display: flex;
            align-items: center;
            gap: 5px;
          }

          .progress {
            font-weight: bold;
            color: #409EFF;
          }
        }
      }
    }

    .chapter-list {
      background: #fff;
      border-radius: 8px;
      box-shadow: 0 2px 12px rgba(0,0,0,0.1);
      padding: 20px;
      max-height: calc(100vh - 200px);
      overflow-y: auto;

      .chapter-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 20px;
        padding-bottom: 15px;
        border-bottom: 1px solid #EBEEF5;

        h3 {
          font-size: 16px;
          margin: 0;
          color: #303133;
        }

        .total-progress {
          font-size: 14px;
          color: #409EFF;
          font-weight: bold;
        }
      }

      .chapters {
        .chapter-item {
          display: flex;
          gap: 12px;
          padding: 15px;
          border-radius: 8px;
          cursor: pointer;
          transition: all 0.3s;
          margin-bottom: 10px;
          border: 1px solid transparent;

          &:hover {
            background: #F5F7FA;
          }

          &.active {
            background: #ECF5FF;
            border-color: #409EFF;
          }

          &.completed {
            .chapter-index {
              color: #67C23A;
            }
          }

          &.locked {
            opacity: 0.6;
            cursor: not-allowed;

            .chapter-index {
              color: #C0C4CC;
            }
          }

          .chapter-index {
            display: flex;
            align-items: center;
            justify-content: center;
            width: 40px;
            height: 40px;
            border-radius: 50%;
            background: #F5F7FA;
            color: #909399;
            font-size: 18px;
            flex-shrink: 0;
          }

          .chapter-details {
            flex: 1;
            min-width: 0;

            h4 {
              font-size: 14px;
              margin: 0 0 8px 0;
              color: #303133;
              overflow: hidden;
              text-overflow: ellipsis;
              white-space: nowrap;
            }

            .chapter-meta {
              display: flex;
              gap: 10px;
              font-size: 12px;
              color: #909399;

              .duration {
                display: flex;
                align-items: center;
                gap: 4px;
              }
            }
          }

          .chapter-status {
            display: flex;
            align-items: center;
            width: 60px;
            flex-shrink: 0;
          }
        }
      }
    }
  }
  .notes-panel { background:#fff; border:1px solid #e4e7ed; border-radius:8px; padding:16px; margin-bottom:20px; max-height:300px; overflow-y:auto;
    .notes-header { display:flex; justify-content:space-between; align-items:center; margin-bottom:12px; }
    .notes-input { margin-bottom:12px; }
    .notes-input-actions { display:flex; justify-content:flex-end; gap:8px; margin-top:8px; }
    .note-item { display:flex; align-items:flex-start; gap:8px; padding:8px 0; border-bottom:1px solid #f0f0f0; cursor:pointer;
      &:hover { background:#f5f7fa; }
    }
    .note-time { color:#409EFF; font-size:12px; white-space:nowrap; min-width:50px; }
    .note-content { flex:1; font-size:13px; word-break:break-all; }
  }
}
</style>
