<template>
  <div class="exam-countdown" v-if="countdown">
    <div class="countdown-inner">
      <div class="countdown-label">距2027考研初试还有</div>
      <div class="countdown-digits">
        <div class="digit-block">
          <span class="digit-num">{{ countdown.days }}</span>
          <span class="digit-label">天</span>
        </div>
        <span class="digit-sep">:</span>
        <div class="digit-block">
          <span class="digit-num">{{ countdown.hours }}</span>
          <span class="digit-label">时</span>
        </div>
        <span class="digit-sep">:</span>
        <div class="digit-block">
          <span class="digit-num">{{ countdown.minutes }}</span>
          <span class="digit-label">分</span>
        </div>
        <span class="digit-sep">:</span>
        <div class="digit-block">
          <span class="digit-num">{{ countdown.seconds }}</span>
          <span class="digit-label">秒</span>
        </div>
      </div>
      <div class="next-milestone" v-if="nextMilestone">
        <i class="el-icon-bell"></i>
        {{ nextMilestone.label }}：{{ nextMilestone.date }}
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'ExamCountdown',
  data() {
    return {
      countdown: null,
      timer: null,
      milestones: [
        { label: '考研大纲发布', date: '2026-09-15' },
        { label: '预报名开始', date: '2026-09-24' },
        { label: '正式报名开始', date: '2026-10-05' },
        { label: '网上确认', date: '2026-11-01' },
        { label: '准考证打印', date: '2026-12-10' },
        { label: '考研初试', date: '2026-12-26' },
        { label: '初试成绩公布', date: '2027-02-15' },
        { label: '国家线公布', date: '2027-03-15' }
      ]
    }
  },
  computed: {
    nextMilestone() {
      const now = new Date()
      for (const m of this.milestones) {
        if (new Date(m.date) > now) return m
      }
      return null
    }
  },
  mounted() {
    this.tick()
    this.timer = setInterval(this.tick, 1000)
  },
  beforeDestroy() {
    if (this.timer) clearInterval(this.timer)
  },
  methods: {
    tick() {
      const examDate = new Date('2026-12-26T08:30:00')
      const now = new Date()
      let diff = examDate - now
      if (diff < 0) { this.countdown = { days: 0, hours: 0, minutes: 0, seconds: 0 }; return }
      const days = Math.floor(diff / 86400000)
      diff -= days * 86400000
      const hours = Math.floor(diff / 3600000)
      diff -= hours * 3600000
      const minutes = Math.floor(diff / 60000)
      diff -= minutes * 60000
      const seconds = Math.floor(diff / 1000)
      this.countdown = { days, hours, minutes, seconds }
    }
  }
}
</script>

<style scoped>
.exam-countdown {
  width: 100%;
}
.countdown-inner {
  text-align: center;
  padding: 8px 0;
}
.countdown-label {
  font-size: 14px;
  color: #606266;
  margin-bottom: 10px;
  font-weight: 500;
}
.countdown-digits {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  margin-bottom: 10px;
}
.digit-block {
  text-align: center;
  background: linear-gradient(135deg, #409EFF, #66B1FF);
  border-radius: 8px;
  padding: 6px 12px;
  min-width: 54px;
}
.digit-num {
  font-size: 26px;
  font-weight: 700;
  font-family: 'Courier New', monospace;
  display: block;
  line-height: 1.2;
  color: #fff;
}
.digit-label {
  font-size: 11px;
  color: rgba(255,255,255,0.85);
}
.digit-sep {
  font-size: 22px;
  font-weight: 700;
  color: #409EFF;
  padding: 0 2px;
}
.next-milestone {
  font-size: 13px;
  color: #E6A23C;
  background: #FDF6EC;
  padding: 6px 14px;
  border-radius: 20px;
  display: inline-block;
}
.next-milestone i {
  margin-right: 4px;
}
@media (max-width: 768px) {
  .digit-block { min-width: 44px; padding: 4px 8px; }
  .digit-num { font-size: 20px; }
}
</style>
