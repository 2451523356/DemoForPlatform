<template>
  <div class="study">
    <div class="study-header">
      <h2>学习中心</h2>
      <p class="study-subtitle">记录学习进度，管理学习计划</p>
    </div>

    <!-- 智能提醒弹窗 -->
    <div v-if="urgentTasks.length > 0" class="reminder-container">
      <el-alert
        :title="`您有 ${urgentTasks.length} 个即将到期或已过期的任务`"
        type="warning"
        show-icon
        :closable="false"
      >
        <div class="reminder-list">
          <div v-for="task in urgentTasks" :key="task.id" class="reminder-item">
            <span class="reminder-title">
              <el-tag :type="task.isOverdue ? 'danger' : 'warning'" size="mini">
                {{ task.isOverdue ? '已过期' : task.daysLeft === 0 ? '今天到期' : task.daysLeft + '天后到期' }}
              </el-tag>
              {{ task.title }}
            </span>
            <span class="reminder-deadline">截止: {{ task.deadline }}</span>
          </div>
        </div>
      </el-alert>
    </div>

    <el-tabs v-model="activeTab" @tab-click="handleTabClick" class="study-tabs">
      <!-- ======================== 备考日历 ======================== -->
      <el-tab-pane label="备考日历" name="calendar">
        <div class="calendar-container">
          <div class="calendar-header">
            <h3>备考日历</h3>
            <div class="calendar-actions">
              <el-button size="small" type="primary" @click="openAddPlanDialog">添加计划</el-button>
              <el-button size="small" type="success" @click="openAddTodoDialog">添加待办</el-button>
              <el-button size="small" @click="prevMonth">上月</el-button>
              <el-button size="small" type="primary" plain @click="today">今天</el-button>
              <el-button size="small" @click="nextMonth">下月</el-button>
            </div>
          </div>

          <div class="calendar-layout">
            <div class="calendar-main">
              <el-calendar v-model="currentDate" class="custom-calendar" :hide-header="true">
                <template slot="dateCell" slot-scope="{ date, data }">
                  <div
                    class="calendar-cell"
                    :class="{
                      'has-record': getCalendarStudyTime(date) > 0,
                      'high-study': getCalendarStudyTime(date) > 3,
                      'medium-study': getCalendarStudyTime(date) > 1 && getCalendarStudyTime(date) <= 3,
                      'low-study': getCalendarStudyTime(date) > 0 && getCalendarStudyTime(date) <= 1,
                      'today': isToday(date)
                    }"
                    @click="showCalendarDetail(date)"
                  >
                    <span class="date">{{ data.day.split('-')[2] }}</span>
                    <span class="study-time" v-if="getCalendarStudyTime(date) > 0">
                      {{ getCalendarStudyTime(date) }}h
                    </span>
                    <span class="indicator-dots">
                      <span v-if="hasPlanOnDate(date)" class="dot dot-plan" title="有学习计划">●</span>
                      <span v-if="hasTodoOnDate(date)" class="dot dot-todo" title="有待办事项">●</span>
                    </span>
                  </div>
                </template>
              </el-calendar>
            </div>
            <!-- 今日任务面板 -->
            <div class="today-panel">
              <div class="today-header">
                <h4><i class="el-icon-s-flag"></i> 今日任务</h4>
                <span class="today-date">{{ formatToday() }}</span>
              </div>
              <div class="today-section">
                <h5><i class="el-icon-document"></i> 学习计划</h5>
                <div v-if="todayPlans.length > 0">
                  <div v-for="p in todayPlans" :key="p.id" class="today-item plan-item">
                    <el-tag size="mini" :type="getPriorityType(p.priority)">{{ getPriorityText(p.priority) }}</el-tag>
                    <span>{{ p.title }}</span>
                  </div>
                </div>
                <p v-else class="today-empty">今日暂无计划</p>
              </div>
              <div class="today-section">
                <h5><i class="el-icon-check"></i> 待办事项</h5>
                <div v-if="todayTodos.length > 0">
                  <div v-for="t in todayTodos" :key="t.id" class="today-item todo-item"
                    :class="{ 'completed-text': t.status === 'completed' }">
                    <el-tag size="mini" :type="t.status === 'completed' ? 'success' : getPriorityType(t.priority)">
                      {{ t.status === 'completed' ? '完成' : getPriorityText(t.priority) }}
                    </el-tag>
                    <span>{{ t.content || t.title }}</span>
                  </div>
                </div>
                <p v-else class="today-empty">今日暂无待办</p>
              </div>
              <div class="today-section">
                <h5><i class="el-icon-time"></i> 今日学习</h5>
                <div v-if="todayStudyTime > 0">
                  <div class="today-study-info">
                    <span class="study-big-num">{{ todayStudyTime }}</span> 分钟
                  </div>
                </div>
                <p v-else class="today-empty">今日暂无学习记录</p>
                <el-button size="small" type="primary" plain @click="showAddStudyDialog" style="width:100%;margin-top:8px">
                  记录学习
                </el-button>
              </div>
            </div>
          </div>

          <!-- 日历详情对话框 -->
          <el-dialog :title="'日历详情 - ' + selectedDate" :visible.sync="calendarDetailVisible" width="550px">
            <div class="calendar-detail">
              <div class="detail-section">
                <h4><i class="el-icon-time"></i> 学习记录</h4>
                <div v-if="calendarStudyRecords.length > 0">
                  <div v-for="r in calendarStudyRecords" :key="r.id" class="detail-item">
                    <span>{{ r.subject || '学习' }}</span>
                    <span>{{ r.content }}</span>
                    <el-tag size="mini">{{ r.duration }}分钟</el-tag>
                  </div>
                </div>
                <p v-else class="no-data">暂无学习记录</p>
                <el-button type="primary" size="small" @click="showAddStudyDialog" style="margin-top:10px">
                  记录学习
                </el-button>
              </div>
              <div class="detail-section">
                <h4><i class="el-icon-document"></i> 学习计划</h4>
                <div v-if="calendarPlans.length > 0">
                  <div v-for="p in calendarPlans" :key="p.id" class="detail-item">
                    <el-tag :type="getPriorityType(p.priority)" size="mini">{{ getPriorityText(p.priority) }}</el-tag>
                    <span>{{ p.title }}</span>
                    <span class="detail-date">{{ p.startDate }} ~ {{ p.endDate }}</span>
                  </div>
                </div>
                <p v-else class="no-data">暂无计划</p>
              </div>
              <div class="detail-section">
                <h4><i class="el-icon-check"></i> 待办事项</h4>
                <div v-if="calendarTodos.length > 0">
                  <div v-for="t in calendarTodos" :key="t.id" class="detail-item">
                    <el-tag :type="getPriorityType(t.priority)" size="mini">{{ getPriorityText(t.priority) }}</el-tag>
                    <span :class="{ 'completed-text': t.status === 1 }">{{ t.title }}</span>
                    <el-tag v-if="t.status === 1" type="success" size="mini">已完成</el-tag>
                  </div>
                </div>
                <p v-else class="no-data">暂无待办</p>
              </div>
            </div>
          </el-dialog>

          <!-- 记录学习对话框 -->
          <el-dialog title="记录学习" :visible.sync="addStudyDialogVisible" width="400px" append-to-body>
            <el-form :model="studyForm" label-width="80px">
              <el-form-item label="科目">
                <el-select v-model="studyForm.subject" placeholder="选择科目" style="width:100%">
                  <el-option label="数学" value="数学"></el-option>
                  <el-option label="英语" value="英语"></el-option>
                  <el-option label="政治" value="政治"></el-option>
                  <el-option label="专业课" value="专业课"></el-option>
                  <el-option label="其他" value="其他"></el-option>
                </el-select>
              </el-form-item>
              <el-form-item label="学习内容">
                <el-input v-model="studyForm.content" placeholder="学习内容描述"></el-input>
              </el-form-item>
              <el-form-item label="学习时长">
                <el-input-number v-model="studyForm.duration" :min="10" :max="600" :step="10" style="width:100%"></el-input-number>
                <span style="margin-left:8px;color:#909399">分钟</span>
              </el-form-item>
            </el-form>
            <span slot="footer">
              <el-button @click="addStudyDialogVisible = false">取消</el-button>
              <el-button type="primary" @click="addStudyRecord">保存</el-button>
            </span>
          </el-dialog>
        </div>
      </el-tab-pane>

      <!-- ======================== 学习计划 ======================== -->
      <el-tab-pane label="学习计划" name="plan">
        <div class="plan-container">
          <div class="plan-header">
            <h3>学习计划</h3>
            <div class="plan-header-actions">
              <el-button @click="openTemplateDialog" class="template-btn">
                <i class="el-icon-s-operation"></i> 使用模板
              </el-button>
              <el-button type="primary" @click="openAddPlanDialog" class="add-plan-btn">
                <i class="el-icon-plus"></i> 添加计划
              </el-button>
            </div>
          </div>

          <div v-if="planList.length === 0" class="empty-state">
            <el-empty description="暂无学习计划" :image-size="120" />
            <p class="empty-hint">添加你的第一个学习计划，开始高效学习之旅</p>
          </div>

          <el-card v-for="plan in planList" :key="plan.id" class="plan-card"
            :class="{ 'completed': plan.status === 'completed', 'overdue-card': isPlanOverdue(plan) && plan.status !== 'completed' }">
            <div class="plan-card-header">
              <div class="plan-title-row">
                <h4>{{ plan.title }}</h4>
                <div class="plan-tags">
                  <el-tag size="small" :type="getPriorityType(plan.priority)">{{ getPriorityText(plan.priority) }}优先级</el-tag>
                  <el-tag size="small" :type="getUrgencyType(plan)">{{ getUrgencyText(plan) }}</el-tag>
                  <el-tag size="small" :type="plan.status === 'completed' ? 'success' : 'warning'">
                    {{ plan.status === 'completed' ? '已完成' : '进行中' }}
                  </el-tag>
                </div>
              </div>
            </div>
            <p class="plan-description">{{ plan.description || '无描述' }}</p>
            <div class="plan-meta">
              <span class="meta-item"><i class="el-icon-date"></i> {{ plan.startDate || '未设置' }} ~ {{ plan.endDate || '未设置' }}</span>
              <span class="meta-item" v-if="plan.status !== 'completed' && plan.endDate">
                <i class="el-icon-time"></i>
                <span :class="{ 'text-danger': getDaysLeft(plan) <= 3 && getDaysLeft(plan) >= 0, 'text-overdue': getDaysLeft(plan) < 0 }">
                  {{ getDaysLeftText(plan) }}
                </span>
              </span>
            </div>
            <div class="plan-progress" v-if="plan.status !== 'completed'">
              <el-progress :percentage="getPlanProgress(plan)" :stroke-width="10"
                :color="getProgressColor(plan)" />
              <div class="progress-controls">
                <span class="progress-text">{{ getPlanProgress(plan) }}%</span>
              </div>
              <div class="progress-study-days" v-if="getPlanStudyDays(plan).total > 0">
                {{ getPlanStudyDays(plan).studied }}/{{ getPlanStudyDays(plan).total }} 天有学习记录
              </div>
            </div>
            <div class="plan-actions">
              <el-button type="primary" size="small" @click="togglePlanStatus(plan)">
                {{ plan.status === 'completed' ? '标记为进行中' : '标记为完成' }}
              </el-button>
              <el-button type="danger" size="small" @click="deletePlan(plan.id)">删除</el-button>
            </div>
          </el-card>
        </div>
      </el-tab-pane>

      <!-- ======================== 每日待办 ======================== -->
      <el-tab-pane label="每日待办" name="todo">
        <div class="todo-container">
          <div class="todo-header">
            <h3>每日待办</h3>
            <div class="todo-actions">
              <el-button type="primary" @click="openAddTodoDialog" class="add-todo-btn">
                <i class="el-icon-plus"></i> 添加待办
              </el-button>
              <el-select v-model="todoFilter" placeholder="筛选" size="small" class="todo-filter">
                <el-option label="全部" value="all"></el-option>
                <el-option label="未完成" value="pending"></el-option>
                <el-option label="已完成" value="completed"></el-option>
              </el-select>
            </div>
          </div>

          <div v-if="todoList.length === 0" class="empty-state">
            <el-empty description="暂无待办事项" :image-size="120" />
            <p class="empty-hint">添加今日待办，提高学习效率</p>
          </div>

          <div v-else class="todo-groups">
            <div v-if="groupedTodoList.overdue.length > 0" class="todo-group">
              <div class="todo-group-header overdue-header">
                <i class="el-icon-warning"></i> 已过期 ({{ groupedTodoList.overdue.length }})
              </div>
              <el-card class="todo-card" v-for="todo in groupedTodoList.overdue" :key="todo.id">
                <div class="todo-item">
                  <el-checkbox
                    :value="todo.status === 'completed'"
                    @change="handleTodoChange(todo)"
                    class="todo-checkbox"
                  >
                    <div class="todo-content">
                      <span :class="{ 'completed-text': todo.status === 'completed' }">{{ todo.content }}</span>
                      <div class="todo-badges">
                        <el-tag size="mini" :type="getPriorityType(todo.priority)" class="todo-priority">
                          {{ getPriorityText(todo.priority) }}
                        </el-tag>
                        <el-tag size="mini" type="danger" class="todo-deadline-tag">
                          {{ getTodoDeadlineText(todo) }}
                        </el-tag>
                        <el-tag v-if="todo.planTitle" size="mini" type="success" class="todo-plan-tag">
                          {{ todo.planTitle }}
                        </el-tag>
                      </div>
                    </div>
                    <div class="todo-meta">
                      <span class="todo-time">{{ formatDate(todo.deadline) || formatDate(todo.createTime) }}</span>
                      <span v-if="todo.isDueToday && todayStudyMinutes > 0" class="todo-today-study">
                        今日已学习 {{ todayStudyMinutes }} 分钟
                      </span>
                      <el-button type="text" size="small" @click.stop="deleteTodo(todo.id)" class="delete-todo">
                        <i class="el-icon-delete"></i>
                      </el-button>
                    </div>
                  </el-checkbox>
                </div>
              </el-card>
            </div>
            <div v-if="groupedTodoList.today.length > 0" class="todo-group">
              <div class="todo-group-header today-header">
                <i class="el-icon-s-flag"></i> 今天到期 ({{ groupedTodoList.today.length }})
              </div>
              <el-card class="todo-card" v-for="todo in groupedTodoList.today" :key="todo.id">
                <div class="todo-item">
                  <el-checkbox
                    :value="todo.status === 'completed'"
                    @change="handleTodoChange(todo)"
                    class="todo-checkbox"
                  >
                    <div class="todo-content">
                      <span :class="{ 'completed-text': todo.status === 'completed' }">{{ todo.content }}</span>
                      <div class="todo-badges">
                        <el-tag size="mini" :type="getPriorityType(todo.priority)" class="todo-priority">
                          {{ getPriorityText(todo.priority) }}
                        </el-tag>
                        <el-tag size="mini" type="warning" class="todo-deadline-tag">
                          {{ getTodoDeadlineText(todo) }}
                        </el-tag>
                        <el-tag v-if="todo.planTitle" size="mini" type="success" class="todo-plan-tag">
                          {{ todo.planTitle }}
                        </el-tag>
                      </div>
                    </div>
                    <div class="todo-meta">
                      <span class="todo-time">{{ formatDate(todo.deadline) || formatDate(todo.createTime) }}</span>
                      <span v-if="todo.isDueToday && todayStudyMinutes > 0" class="todo-today-study">
                        今日已学习 {{ todayStudyMinutes }} 分钟
                      </span>
                      <el-button type="text" size="small" @click.stop="deleteTodo(todo.id)" class="delete-todo">
                        <i class="el-icon-delete"></i>
                      </el-button>
                    </div>
                  </el-checkbox>
                </div>
              </el-card>
            </div>
            <div v-if="groupedTodoList.upcoming.length > 0" class="todo-group">
              <div class="todo-group-header upcoming-header">
                <i class="el-icon-date"></i> 即将到来 ({{ groupedTodoList.upcoming.length }})
              </div>
              <el-card class="todo-card" v-for="todo in groupedTodoList.upcoming" :key="todo.id">
                <div class="todo-item">
                  <el-checkbox
                    :value="todo.status === 'completed'"
                    @change="handleTodoChange(todo)"
                    class="todo-checkbox"
                  >
                    <div class="todo-content">
                      <span :class="{ 'completed-text': todo.status === 'completed' }">{{ todo.content }}</span>
                      <div class="todo-badges">
                        <el-tag size="mini" :type="getPriorityType(todo.priority)" class="todo-priority">
                          {{ getPriorityText(todo.priority) }}
                        </el-tag>
                        <el-tag v-if="todo.status !== 'completed' && todo.deadline" size="mini"
                          :type="getTodoDeadlineType(todo)" class="todo-deadline-tag">
                          {{ getTodoDeadlineText(todo) }}
                        </el-tag>
                        <el-tag v-if="todo.planTitle" size="mini" type="success" class="todo-plan-tag">
                          {{ todo.planTitle }}
                        </el-tag>
                      </div>
                    </div>
                    <div class="todo-meta">
                      <span class="todo-time">{{ formatDate(todo.deadline) || formatDate(todo.createTime) }}</span>
                      <span v-if="todo.isDueToday && todayStudyMinutes > 0" class="todo-today-study">
                        今日已学习 {{ todayStudyMinutes }} 分钟
                      </span>
                      <el-button type="text" size="small" @click.stop="deleteTodo(todo.id)" class="delete-todo">
                        <i class="el-icon-delete"></i>
                      </el-button>
                    </div>
                  </el-checkbox>
                </div>
              </el-card>
            </div>
            <div v-if="groupedTodoList.completed.length > 0" class="todo-group">
              <div class="todo-group-header completed-header">
                <i class="el-icon-check"></i> 已完成 ({{ groupedTodoList.completed.length }})
              </div>
              <el-card class="todo-card completed-todo-card" v-for="todo in groupedTodoList.completed" :key="todo.id">
                <div class="todo-item">
                  <el-checkbox
                    :value="true"
                    @change="handleTodoChange(todo)"
                    class="todo-checkbox"
                  >
                    <div class="todo-content">
                      <span class="completed-text">{{ todo.content }}</span>
                      <div class="todo-badges">
                        <el-tag size="mini" :type="getPriorityType(todo.priority)" class="todo-priority">
                          {{ getPriorityText(todo.priority) }}
                        </el-tag>
                      </div>
                    </div>
                    <div class="todo-meta">
                      <span class="todo-time">{{ formatDate(todo.deadline) || formatDate(todo.createTime) }}</span>
                      <el-button type="text" size="small" @click.stop="deleteTodo(todo.id)" class="delete-todo">
                        <i class="el-icon-delete"></i>
                      </el-button>
                    </div>
                  </el-checkbox>
                </div>
              </el-card>
            </div>
          </div>
        </div>
      </el-tab-pane>

      <!-- ======================== 学习轨迹 ======================== -->
      <el-tab-pane label="学习轨迹" name="track">
        <div class="track-container">
          <h3>学习统计</h3>
          <div class="stats">
            <el-card class="stat-card" hover>
              <div class="stat-content">
                <div class="stat-icon"><i class="el-icon-time"></i></div>
                <div class="stat-value">{{ totalStudyTime || 0 }}</div>
                <div class="stat-label">总学习时长(h)</div>
              </div>
            </el-card>
            <el-card class="stat-card" hover>
              <div class="stat-content">
                <div class="stat-icon"><i class="el-icon-video-camera"></i></div>
                <div class="stat-value">{{ completedCourses || 0 }}</div>
                <div class="stat-label">已学课程(门)</div>
              </div>
            </el-card>
            <el-card class="stat-card" hover>
              <div class="stat-content">
                <div class="stat-icon"><i class="el-icon-download"></i></div>
                <div class="stat-value">{{ downloadedResources || 0 }}</div>
                <div class="stat-label">下载资源(个)</div>
              </div>
            </el-card>
            <el-card class="stat-card" hover>
              <div class="stat-content">
                <div class="stat-icon"><i class="el-icon-document"></i></div>
                <div class="stat-value">{{ readNews || 0 }}</div>
                <div class="stat-label">阅读资讯(篇)</div>
              </div>
            </el-card>
          </div>

          <div class="charts-container">
            <div class="chart-item">
              <h3>学习时长趋势</h3>
              <div v-if="chartDataEmpty" class="empty-chart">
                <el-empty description="暂无学习数据" :image-size="100" />
                <p class="empty-hint">开始学习后，这里会显示你的学习时长趋势</p>
              </div>
              <div v-else ref="studyChart" class="study-chart"></div>
            </div>
            <div class="chart-item">
              <h3>学习科目分布</h3>
              <div v-if="chartDataEmpty" class="empty-chart">
                <el-empty description="暂无学习数据" :image-size="100" />
                <p class="empty-hint">开始学习后，这里会显示你的学习科目分布</p>
              </div>
              <div v-else ref="typeChart" class="type-chart"></div>
            </div>
          </div>

          <div class="learning-trajectory">
            <h3>最近学习轨迹</h3>
            <div class="trajectory-filters">
              <el-radio-group v-model="trajectoryFilter" size="small">
                <el-radio-button label="all">全部</el-radio-button>
                <el-radio-button label="study">学习</el-radio-button>
                <el-radio-button label="download">下载</el-radio-button>
                <el-radio-button label="news">资讯</el-radio-button>
              </el-radio-group>
            </div>
            <div v-if="weekGroupedTrajectory.length > 0" class="trajectory-grid">
              <div v-for="(week, wIdx) in displayedWeeks" :key="'w'+wIdx" class="trajectory-week">
                <div class="week-header">第{{ week.weekNum }}周 ({{ week.dateRange }})</div>
                <el-row :gutter="16">
                  <el-col :span="12" v-for="(item, iIdx) in week.items" :key="'i'+iIdx">
                    <el-card class="trajectory-card" :class="'trajectory-' + item.filterType" shadow="hover">
                      <div class="trajectory-card-inner">
                        <div class="trajectory-card-icon">
                          <i :class="item.icon"></i>
                          <span class="trajectory-card-type">{{ item.typeLabel }}</span>
                        </div>
                        <div class="trajectory-card-body">
                          <p class="trajectory-card-content">{{ item.content }}</p>
                          <span class="trajectory-card-time">{{ formatTimeShort(item.time) }}</span>
                          <span class="trajectory-card-date">{{ item.date }}</span>
                        </div>
                      </div>
                    </el-card>
                  </el-col>
                </el-row>
              </div>
              <div v-if="trajectoryShowWeeks < weekGroupedTrajectory.length" class="expand-more">
                <el-button type="text" @click="trajectoryShowWeeks = weekGroupedTrajectory.length">
                  展开更多 (剩余 {{ weekGroupedTrajectory.length - trajectoryShowWeeks }} 周)
                </el-button>
              </div>
            </div>
            <div v-else class="empty-trajectory">
              <el-empty description="暂无学习轨迹" :image-size="100" />
              <p class="empty-hint">开始学习后，这里会显示你的学习轨迹</p>
            </div>
          </div>
        </div>
      </el-tab-pane>

      <!-- ======================== 周报 ======================== -->
      <el-tab-pane label="周报" name="weekly">
        <div class="weekly-container">
          <div class="weekly-header">
            <h3>周报</h3>
            <div class="week-selector">
              <el-button size="small" icon="el-icon-arrow-left" @click="goToPrevWeek">上一周</el-button>
              <span class="current-week-label">{{ currentWeekLabel }}</span>
              <el-button size="small" icon="el-icon-arrow-right" @click="goToNextWeek">下一周</el-button>
            </div>
          </div>

          <div class="weekly-stats">
            <el-card class="weekly-stat-card" hover>
              <div class="stat-content">
                <div class="stat-icon"><i class="el-icon-time"></i></div>
                <div class="stat-value">
                  <span class="count-up-number">{{ formatMinutes(weekStatDisplay) }}</span>
                </div>
                <div class="stat-label">本周学习总时长</div>
                <div v-if="prevWeekComparison !== null" class="stat-compare">
                  较上周
                  <span :class="prevWeekComparison >= 0 ? 'compare-up' : 'compare-down'">
                    {{ prevWeekComparison >= 0 ? '+' : '' }}{{ prevWeekComparison }}%
                  </span>
                </div>
              </div>
            </el-card>
            <el-card class="weekly-stat-card" hover>
              <div class="stat-content">
                <div class="stat-icon"><i class="el-icon-document"></i></div>
                <div class="stat-value">{{ weeklyReport.completedChapters || 0 }}</div>
                <div class="stat-label">完成章节数</div>
              </div>
            </el-card>
            <el-card class="weekly-stat-card" hover>
              <div class="stat-content">
                <div class="stat-icon"><i class="el-icon-check"></i></div>
                <div class="stat-value">{{ weeklyReport.completedTasks || 0 }}</div>
                <div class="stat-label">完成任务数</div>
              </div>
            </el-card>
          </div>

          <div class="weekly-charts">
            <div class="chart-item">
              <h4>每日学习时长（点击查看详情）</h4>
              <div ref="weekBarChart" class="week-chart"></div>
            </div>
            <div class="chart-item">
              <h4>科目分布</h4>
              <div ref="weekPieChart" class="week-chart"></div>
            </div>
          </div>
          <el-dialog :title="'学习详情 - ' + dayDetailDate" :visible.sync="dayDetailVisible" width="480px" append-to-body>
            <div v-if="dayDetailItems.length > 0" class="day-detail-list">
              <div v-for="(d, idx) in dayDetailItems" :key="idx" class="day-detail-item">
                <el-tag size="small" :type="d.type === 'study' ? 'primary' : d.type === 'download' ? 'success' : 'info'">
                  {{ d.typeLabel }}
                </el-tag>
                <span class="day-detail-content">{{ d.content }}</span>
                <el-tag size="mini">{{ d.duration }}分钟</el-tag>
              </div>
            </div>
            <div v-else class="day-detail-empty">
              <p>该日暂无详细学习记录</p>
            </div>
            <span slot="footer">
              <el-button type="primary" @click="dayDetailVisible = false">关闭</el-button>
            </span>
          </el-dialog>
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- ======================== 添加计划对话框 ======================== -->
    <el-dialog title="添加学习计划" :visible.sync="planDialogVisible" width="500px">
      <el-form ref="planForm" :model="planForm" :rules="planRules" label-width="80px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="planForm.title" placeholder="如：完成数学一轮复习"></el-input>
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="planForm.description" type="textarea" :rows="3" placeholder="计划详细描述（选填）"></el-input>
        </el-form-item>
        <el-form-item label="开始日期" prop="startDate">
          <el-date-picker v-model="planForm.startDate" type="date" placeholder="选择开始日期" style="width:100%"></el-date-picker>
        </el-form-item>
        <el-form-item label="结束日期" prop="endDate">
          <el-date-picker v-model="planForm.endDate" type="date" placeholder="选择结束日期" style="width:100%"></el-date-picker>
        </el-form-item>
        <el-form-item label="优先级">
          <el-select v-model="planForm.priority" placeholder="选择优先级" style="width:100%">
            <el-option label="低" value="1"></el-option>
            <el-option label="中" value="2"></el-option>
            <el-option label="高" value="3"></el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="planDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="addPlan">确定</el-button>
      </span>
    </el-dialog>

    <!-- ======================== 添加待办对话框 ======================== -->
    <el-dialog title="添加待办事项" :visible.sync="todoDialogVisible" width="500px">
      <el-form ref="todoForm" :model="todoForm" :rules="todoRules" label-width="80px">
        <el-form-item label="内容" prop="content">
          <el-input v-model="todoForm.content" placeholder="如：做完第三章练习题"></el-input>
        </el-form-item>
        <el-form-item label="优先级">
          <el-select v-model="todoForm.priority" placeholder="选择优先级" style="width:100%">
            <el-option label="低" value="1"></el-option>
            <el-option label="中" value="2"></el-option>
            <el-option label="高" value="3"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="截止日期">
          <el-date-picker v-model="todoForm.deadline" type="date" placeholder="选择截止日期" style="width:100%"></el-date-picker>
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="todoDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="addTodo">确定</el-button>
      </span>
    </el-dialog>

    <!-- ======================== 到期提醒弹窗 ======================== -->
    <el-dialog :title="'任务提醒'" :visible.sync="reminderDialogVisible" width="480px" :close-on-click-modal="false">
      <div class="reminder-dialog">
        <p class="reminder-dialog-subtitle">以下任务即将到期或已过期，请及时处理：</p>
        <div v-for="task in allUrgentTasks" :key="task.id" class="reminder-dialog-item">
          <div class="reminder-dialog-left">
            <el-tag :type="task.isOverdue ? 'danger' : 'warning'" size="small">
              {{ task.isOverdue ? '已过期' : task.daysLeft === 0 ? '今天到期' : task.daysLeft + '天后' }}
            </el-tag>
            <span class="reminder-dialog-title">{{ task.title }}</span>
          </div>
          <span class="reminder-dialog-deadline">{{ task.deadline }}</span>
        </div>
      </div>
      <span slot="footer">
        <el-button type="primary" @click="reminderDialogVisible = false">知道了</el-button>
      </span>
    </el-dialog>

    <!-- ======================== 计划模板对话框 ======================== -->
    <el-dialog title="选择计划模板" :visible.sync="templateDialogVisible" width="700px">
      <div v-if="planTemplates.length === 0" class="template-loading">
        <el-empty description="暂无可用模板" :image-size="80"></el-empty>
      </div>
      <div v-else class="template-grid">
        <el-card v-for="tpl in planTemplates" :key="tpl.id" class="template-card" shadow="hover" @click.native="applyTemplate(tpl)">
          <h4>{{ tpl.title }}</h4>
          <p class="template-desc">{{ tpl.description }}</p>
          <div class="template-meta">
            <el-tag size="small" :type="getCategoryType(tpl.stage)">{{ tpl.stage }}</el-tag>
            <span class="template-duration">预计 {{ tpl.totalDays }} 天</span>
          </div>
        </el-card>
      </div>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: 'Study',
  data() {
    return {
      activeTab: 'calendar',
      currentDate: new Date(),
      planDialogVisible: false,
      todoDialogVisible: false,
      calendarDetailVisible: false,
      addStudyDialogVisible: false,
      reminderDialogVisible: false,
      planList: [],
      todoList: [],
      checkedTodos: [],
      studyRecords: {},
      rawStudyRecords: {},
      calendarData: { plansByDate: {}, todosByDate: {}, studyRecords: {} },
      totalStudyTime: 0,
      completedCourses: 0,
      downloadedResources: 0,
      readNews: 0,
      selectedDate: '',
      selectedStudyTime: 0,
      selectedStudyContent: '',
      todoFilter: 'all',
      recentActivities: [],
      urgentTasks: [],
      allUrgentTasks: [],
      chartDataEmpty: true,
      activityPageSize: 20,
      activityShowCount: 20,
      trajectoryFilter: 'all',
      trajectoryShowWeeks: 2,
      subjectData: {},
      subjectDistributionData: [],
      calendarPlans: [],
      calendarTodos: [],
      calendarStudyRecords: [],
      weekOffset: 0,
      prevWeekReport: null,
      weekStatDisplay: 0,
      weekStatAnimating: false,
      dayDetailVisible: false,
      dayDetailDate: '',
      dayDetailItems: [],
      weeklyReport: { weekTotal: 0, dailyData: {}, subjectDistribution: {}, completedChapters: 0, completedTasks: 0 },
      templateDialogVisible: false,
      planTemplates: [],
      studyForm: { subject: '数学', content: '', duration: 60 },
      planForm: { title: '', description: '', startDate: '', endDate: '', priority: '2' },
      planRules: {
        title: [{ required: true, message: '请输入计划标题', trigger: 'blur' }],
        startDate: [{ required: true, message: '请选择开始日期', trigger: 'change' }],
        endDate: [{ required: true, message: '请选择结束日期', trigger: 'change' }]
      },
      todoForm: { content: '', priority: '2', deadline: '', planId: '' },
      todoRules: {
        content: [{ required: true, message: '请输入待办内容', trigger: 'blur' }]
      }
    }
  },
  computed: {
    filteredTodoList() {
      if (this.todoFilter === 'all') return this.todoList
      if (this.todoFilter === 'completed') return this.todoList.filter(t => t.status === 'completed')
      return this.todoList.filter(t => t.status === 'pending')
    },
    todayKey() {
      return new Date().toISOString().split('T')[0]
    },
    todayPlans() {
      const plans = this.calendarData.plansByDate || {}
      return plans[this.todayKey] || []
    },
    todayTodos() {
      const todos = this.calendarData.todosByDate || {}
      return (todos[this.todayKey] || []).map(t => ({
        ...t,
        content: t.title || t.content,
        status: t.status === 1 ? 'completed' : 'pending'
      }))
    },
    todayStudyTime() {
      const records = this.calendarData.studyRecords || {}
      return records[this.todayKey] || 0
    },
    activePlanOptions() {
      return this.planList.filter(p => p.status !== 'completed')
    },
    groupedTodoList() {
      const today = new Date(new Date().toDateString())
      const overdue = []
      const todayItems = []
      const upcoming = []
      const completed = []
      // Respect the todoFilter
      let source = this.todoList
      if (this.todoFilter === 'completed') {
        source = this.todoList.filter(t => t.status === 'completed')
      } else if (this.todoFilter === 'pending') {
        source = this.todoList.filter(t => t.status !== 'completed')
      }
      source.forEach(todo => {
        const enriched = { ...todo, isDueToday: false, planTitle: '' }
        // Extract plan title from description convention [plan:Title]
        if (todo.description || todo.content) {
          const match = (todo.description || todo.content).match(/\[plan:(.+?)\]/)
          if (match) enriched.planTitle = match[1]
        }
        if (todo.status === 'completed') {
          completed.push(enriched)
        } else if (todo.deadline) {
          const dl = new Date(todo.deadline)
          dl.setHours(0, 0, 0, 0)
          if (dl < today) {
            overdue.push(enriched)
          } else if (dl.getTime() === today.getTime()) {
            enriched.isDueToday = true
            todayItems.push(enriched)
          } else {
            upcoming.push(enriched)
          }
        } else {
          upcoming.push(enriched)
        }
      })
      return { overdue, today: todayItems, upcoming, completed }
    },
    todayStudyMinutes() {
      return this.rawStudyRecords[this.todayKey] || 0
    },
    filteredTrajectory() {
      if (this.trajectoryFilter === 'all') return this.recentActivities
      return this.recentActivities.filter(a => a.filterType === this.trajectoryFilter)
    },
    weekGroupedTrajectory() {
      const items = this.filteredTrajectory
      if (items.length === 0) return []
      // Sort by time descending
      const sorted = [...items].sort((a, b) => new Date(b.time) - new Date(a.time))
      // Get first item's date to determine "current" week reference
      const firstDate = new Date(sorted[0].time)
      // Group by ISO week
      const weekMap = new Map()
      sorted.forEach(item => {
        const d = new Date(item.time)
        const weekKey = this.getISOWeekKey(d)
        if (!weekMap.has(weekKey)) {
          weekMap.set(weekKey, [])
        }
        weekMap.get(weekKey).push(item)
      })
      // Convert to array, sorted by week descending
      const weeks = []
      weekMap.forEach((items, key) => {
        const [year, weekNum] = key.split('-W')
        // Calculate date range for this week
        const weekStart = this.getDateOfISOWeek(parseInt(year), parseInt(weekNum))
        const weekEnd = new Date(weekStart)
        weekEnd.setDate(weekEnd.getDate() + 6)
        const fmt = (d) => (d.getMonth() + 1) + '/' + d.getDate()
        weeks.push({
          weekNum: parseInt(weekNum),
          dateRange: fmt(weekStart) + ' - ' + fmt(weekEnd),
          items,
          sortKey: key
        })
      })
      weeks.sort((a, b) => b.sortKey.localeCompare(a.sortKey))
      return weeks
    },
    displayedWeeks() {
      return this.weekGroupedTrajectory.slice(0, this.trajectoryShowWeeks)
    },
    prevWeekComparison() {
      if (!this.prevWeekReport || !this.prevWeekReport.weekTotal) return null
      const prev = this.prevWeekReport.weekTotal
      const curr = this.weeklyReport.weekTotal
      if (prev === 0) return curr > 0 ? 100 : 0
      return Math.round((curr / prev - 1) * 100)
    },
    currentWeekStart() {
      const now = new Date()
      const dayOfWeek = now.getDay()
      const daysFromMonday = dayOfWeek === 0 ? 6 : dayOfWeek - 1
      const monday = new Date(now)
      monday.setDate(now.getDate() - daysFromMonday + this.weekOffset * 7)
      monday.setHours(0, 0, 0, 0)
      return monday
    },
    weekDays() {
      const days = []
      for (let i = 0; i < 7; i++) {
        const d = new Date(this.currentWeekStart)
        d.setDate(d.getDate() + i)
        days.push(d)
      }
      return days
    },
    currentWeekLabel() {
      const start = this.currentWeekStart
      const end = new Date(start)
      end.setDate(end.getDate() + 6)
      const fmt = (d) => d.getMonth() + 1 + '/' + d.getDate()
      return fmt(start) + ' - ' + fmt(end)
    }
  },
  mounted() {
    this.getStudyData()
    this.fetchCalendarData()
    setInterval(() => { this.checkUpcomingTasks() }, 5 * 60 * 1000)
  },
  methods: {
    // ==================== 日历相关 ====================
    getCalendarStudyTime(date) {
      const key = this.formatDateKey(date)
      const records = this.calendarData.studyRecords || {}
      const mins = records[key] || 0
      return parseFloat((mins / 60).toFixed(1))
    },
    hasPlanOnDate(date) {
      const key = this.formatDateKey(date)
      const plans = this.calendarData.plansByDate || {}
      return !!(plans[key] && plans[key].length > 0)
    },
    hasTodoOnDate(date) {
      const key = this.formatDateKey(date)
      const todos = this.calendarData.todosByDate || {}
      return !!(todos[key] && todos[key].length > 0)
    },
    showCalendarDetail(date) {
      const key = this.formatDateKey(date)
      this.selectedDate = key
      const plans = this.calendarData.plansByDate || {}
      const todos = this.calendarData.todosByDate || {}
      this.calendarPlans = plans[key] || []
      this.calendarTodos = todos[key] || []
      this.calendarStudyRecords = (this.recentActivities || []).filter(r => {
        if (!r.time) return false
        return this.formatDateKey(new Date(r.time)) === key
      })
      this.calendarDetailVisible = true
    },
    showAddStudyDialog() {
      this.studyForm = { subject: '数学', content: '', duration: 60 }
      this.addStudyDialogVisible = true
    },
    async addStudyRecord() {
      try {
        await this.$http.post('/study/record', null, {
          params: {
            duration: this.studyForm.duration,
            subject: this.studyForm.subject,
            content: this.studyForm.content,
            type: 1
          }
        })
        this.$message.success('学习记录添加成功')
        this.addStudyDialogVisible = false
        this.calendarDetailVisible = false
        this.getStudyData()
        this.fetchCalendarData()
      } catch (e) {
        this.$message.error('添加失败')
      }
    },
    async fetchCalendarData() {
      try {
        const now = new Date(this.currentDate)
        const startDate = new Date(now.getFullYear(), now.getMonth() - 1, 1).toISOString().split('T')[0]
        const endDate = new Date(now.getFullYear(), now.getMonth() + 2, 0).toISOString().split('T')[0]
        const res = await this.$http.get('/study/calendar', { params: { startDate, endDate } })
        this.calendarData = res || { plansByDate: {}, todosByDate: {}, studyRecords: {} }
      } catch (e) {
        console.error('获取日历数据失败:', e)
      }
    },
    formatDateKey(date) {
      if (typeof date === 'string') return date
      return date.toISOString().split('T')[0]
    },

    // ==================== 计划相关 ====================
    isPlanOverdue(plan) {
      if (!plan.endDate) return false
      return new Date(plan.endDate) < new Date(new Date().toDateString())
    },
    getDaysLeft(plan) {
      if (!plan.endDate) return null
      const end = new Date(plan.endDate)
      const now = new Date(new Date().toDateString())
      return Math.ceil((end - now) / (1000 * 60 * 60 * 24))
    },
    getDaysLeftText(plan) {
      const days = this.getDaysLeft(plan)
      if (days === null) return ''
      if (days < 0) return '已过期' + Math.abs(days) + '天'
      if (days === 0) return '今天截止'
      return '剩余' + days + '天'
    },
    getUrgencyType(plan) {
      const days = this.getDaysLeft(plan)
      if (days === null) return 'info'
      if (days < 0) return 'danger'
      if (days <= 3) return 'danger'
      if (days <= 7) return 'warning'
      return 'success'
    },
    getUrgencyText(plan) {
      const days = this.getDaysLeft(plan)
      if (days === null) return ''
      if (days < 0) return '已过期'
      if (days <= 7) return '即将到期'
      return '时间充裕'
    },
    getPlanProgress(plan) {
      if (plan.status === 'completed') return 100
      if (plan.progress !== undefined && plan.progress !== null) return plan.progress
      if (!plan.startDate || !plan.endDate) return 0
      // Count days with actual study records during plan period
      const start = new Date(plan.startDate)
      const end = new Date(plan.endDate)
      const now = new Date()
      if (now < start) return 0
      const effectiveEnd = now > end ? end : now
      let studiedDays = 0
      let totalDays = 0
      const cursor = new Date(start)
      while (cursor <= effectiveEnd) {
        totalDays++
        const key = cursor.toISOString().split('T')[0]
        if ((this.rawStudyRecords[key] || 0) > 0) {
          studiedDays++
        }
        cursor.setDate(cursor.getDate() + 1)
      }
      if (totalDays === 0) return 0
      const progress = Math.round((studiedDays / totalDays) * 100)
      // Fall back to time-based if no study records exist at all
      if (studiedDays === 0) {
        const totalMs = end - start
        const elapsedMs = now - start
        return Math.min(100, Math.max(0, Math.round((elapsedMs / totalMs) * 100)))
      }
      return Math.min(100, Math.max(0, progress))
    },
    getPlanStudyDays(plan) {
      if (!plan.startDate || !plan.endDate) return { studied: 0, total: 0 }
      const start = new Date(plan.startDate)
      const end = new Date(plan.endDate)
      const now = new Date()
      const effectiveEnd = now > end ? end : now
      if (effectiveEnd < start) return { studied: 0, total: 0 }
      let studied = 0
      let total = 0
      const cursor = new Date(start)
      while (cursor <= effectiveEnd) {
        total++
        const key = cursor.toISOString().split('T')[0]
        if ((this.rawStudyRecords[key] || 0) > 0) {
          studied++
        }
        cursor.setDate(cursor.getDate() + 1)
      }
      return { studied, total }
    },
    getProgressColor(plan) {
      const days = this.getDaysLeft(plan)
      if (days !== null && days < 0) return '#F56C6C'
      return '#409EFF'
    },
    openAddPlanDialog() {
      this.planForm = { title: '', description: '', startDate: '', endDate: '', priority: '2' }
      this.planDialogVisible = true
    },
    async addPlan() {
      try {
        await this.$refs.planForm.validate()
        const fmtDate = (d) => d ? new Date(d).toISOString().split('T')[0] : ''
        const startStr = fmtDate(this.planForm.startDate)
        const endStr = fmtDate(this.planForm.endDate)
        await this.$http.post('/study/todo', null, {
          params: {
            title: this.planForm.title,
            description: this.planForm.description,
            type: 1,
            targetDate: startStr,
            startDate: startStr,
            endDate: endStr,
            remindTime: endStr + ' 09:00:00',
            priority: parseInt(this.planForm.priority)
          }
        })
        this.$message.success('学习计划添加成功')
        this.planDialogVisible = false
        this.getStudyData()
        this.fetchCalendarData()
      } catch (error) {
        if (error && error.message) this.$message.error('添加失败')
      }
    },
    async togglePlanStatus(plan) {
      const targetStatus = plan.status === 'completed' ? 0 : 1
      const statusText = targetStatus === 1 ? '已完成' : '进行中'
      try {
        await this.$confirm(`确定要将计划"${plan.title}"标记为${statusText}吗？`, '确认操作', {
          confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning'
        })
        await this.$http.put(`/study/todo/${plan.id}/status`, null, { params: { status: targetStatus } })
        this.$message.success(`已将计划标记为${statusText}`)
        this.getStudyData()
        this.fetchCalendarData()
      } catch (error) {
        if (error !== 'cancel') this.$message.error('操作失败')
      }
    },
    async deletePlan(id) {
      try {
        await this.$confirm('确定要删除这个学习计划吗？', '删除确认', {
          confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning'
        })
        await this.$http.delete(`/study/todo/${id}`)
        this.$message.success('删除成功')
        this.getStudyData()
        this.fetchCalendarData()
      } catch (error) {
        if (error !== 'cancel') this.$message.error('删除失败')
      }
    },

    // ==================== 待办相关 ====================
    getTodoDeadlineType(todo) {
      if (!todo.deadline) return 'info'
      const days = this.getDaysLeftFromStr(todo.deadline)
      if (days < 0) return 'danger'
      if (days === 0) return 'warning'
      if (days <= 1) return 'warning'
      return ''
    },
    getTodoDeadlineText(todo) {
      if (!todo.deadline) return ''
      const days = this.getDaysLeftFromStr(todo.deadline)
      if (days < 0) return '过期' + Math.abs(days) + '天'
      if (days === 0) return '今天到期'
      if (days === 1) return '明天到期'
      return days + '天后到期'
    },
    getDaysLeftFromStr(dateStr) {
      const end = new Date(dateStr)
      const now = new Date(new Date().toDateString())
      return Math.ceil((end - now) / (1000 * 60 * 60 * 24))
    },
    openAddTodoDialog() {
      this.todoForm = { content: '', priority: '2', deadline: '' }
      this.todoDialogVisible = true
    },
    async addTodo() {
      try {
        await this.$refs.todoForm.validate()
        const deadlineStr = this.todoForm.deadline ? new Date(this.todoForm.deadline).toISOString().split('T')[0] : ''
        // Build params with optional plan reference via description
        const params = {
          title: this.todoForm.content,
          type: 2,
          targetDate: deadlineStr,
          endDate: deadlineStr,
          priority: parseInt(this.todoForm.priority)
        }
        if (this.todoForm.planId) {
          const plan = this.planList.find(p => p.id === this.todoForm.planId)
          if (plan) {
            params.description = '[plan:' + plan.title + ']'
          }
        }
        await this.$http.post('/study/todo', null, { params })
        this.$message.success('待办事项添加成功')
        this.todoDialogVisible = false
        this.getStudyData()
        this.fetchCalendarData()
      } catch (error) {
        if (error && error.message) this.$message.error('添加失败')
      }
    },
    async deleteTodo(id) {
      try {
        await this.$confirm('确定要删除这个待办事项吗？', '删除确认', {
          confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning'
        })
        await this.$http.delete(`/study/todo/${id}`)
        this.$message.success('删除成功')
        this.getStudyData()
        this.fetchCalendarData()
      } catch (error) {
        if (error !== 'cancel') this.$message.error('删除失败')
      }
    },
    async handleTodoChange(todo) {
      const targetStatus = todo.status === 'completed' ? 0 : 1
      const statusText = targetStatus === 1 ? '已完成' : '未完成'
      try {
        await this.$confirm(`确定要将待办"${todo.content}"标记为${statusText}吗？`, '确认操作', {
          confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning'
        })
        await this.$http.put(`/study/todo/${todo.id}/status`, null, { params: { status: targetStatus } })
        // When marking as completed, auto-create a study record
        if (targetStatus === 1) {
          try {
            await this.$http.post('/study/record', null, {
              params: {
                duration: 10,
                subject: '综合',
                content: todo.content || '',
                type: 2
              }
            })
          } catch (e) {
            console.error('自动创建学习记录失败:', e)
          }
        }
        this.$message.success(`已将待办标记为${statusText}`)
        this.getStudyData()
        this.fetchCalendarData()
      } catch (error) {
        if (error !== 'cancel') this.$message.error('操作失败')
      }
    },

    // ==================== 通用 ====================
    formatDate(dateStr) {
      if (!dateStr) return ''
      try {
        const d = new Date(dateStr)
        if (isNaN(d.getTime())) return dateStr
        return d.toLocaleDateString('zh-CN')
      } catch (e) {
        return dateStr
      }
    },
    handleTabClick(tab) {
      if (tab.name === 'calendar') {
        this.fetchCalendarData()
      }
      if (tab.name === 'track') {
        this.trajectoryShowWeeks = 2
        this.fetchSubjectDistribution()
        this.$nextTick(() => {
          this.initChart()
          this.initTypeChart()
        })
      }
      if (tab.name === 'weekly') {
        if (!this.weeklyReport || !this.weeklyReport.weekTotal) {
          this.fetchWeeklyReport()
        }
      }
    },
    formatToday() {
      const now = new Date()
      const weekDays = ['日', '一', '二', '三', '四', '五', '六']
      return `${now.getFullYear()}/${now.getMonth() + 1}/${now.getDate()} 星期${weekDays[now.getDay()]}`
    },
    formatTimeShort(timeStr) {
      if (!timeStr) return ''
      try {
        const d = new Date(timeStr)
        if (isNaN(d.getTime())) return timeStr.split(' ')[1] || timeStr
        return d.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
      } catch (e) { return '' }
    },
    extractDate(timeStr) {
      if (!timeStr) return '未知日期'
      try {
        const d = new Date(timeStr)
        if (isNaN(d.getTime())) return timeStr.split(' ')[0] || timeStr
        const now = new Date()
        const diff = Math.floor((now - d) / 86400000)
        if (diff === 0) return '今天'
        if (diff === 1) return '昨天'
        if (diff < 7) return diff + '天前'
        return d.toLocaleDateString('zh-CN')
      } catch (e) { return timeStr }
    },
    loadMoreActivities() {
      this.activityShowCount = Math.min(this.activityShowCount + 20, this.recentActivities.length)
    },
    getISOWeekKey(date) {
      const d = new Date(date)
      const dayNum = d.getUTCDay() || 7
      d.setUTCDate(d.getUTCDate() + 4 - dayNum)
      const yearStart = new Date(Date.UTC(d.getUTCFullYear(), 0, 1))
      const weekNo = Math.ceil((((d - yearStart) / 86400000) + 1) / 7)
      return d.getUTCFullYear() + '-W' + String(weekNo).padStart(2, '0')
    },
    getDateOfISOWeek(year, week) {
      const simple = new Date(year, 0, 1 + (week - 1) * 7)
      const dayOfWeek = simple.getDay()
      const diff = dayOfWeek <= 4 ? simple.getDate() - dayOfWeek + 1 : simple.getDate() + 8 - dayOfWeek
      const monday = new Date(year, 0, diff)
      return monday
    },
    isToday(date) {
      const today = new Date()
      return today.toDateString() === new Date(date).toDateString()
    },
    prevMonth() {
      const d = new Date(this.currentDate)
      d.setMonth(d.getMonth() - 1)
      this.currentDate = d
      this.fetchCalendarData()
    },
    nextMonth() {
      const d = new Date(this.currentDate)
      d.setMonth(d.getMonth() + 1)
      this.currentDate = d
      this.fetchCalendarData()
    },
    today() {
      this.currentDate = new Date()
      this.fetchCalendarData()
    },
    getPriorityType(priority) {
      const p = parseInt(priority)
      if (p === 3) return 'danger'
      if (p === 2) return 'warning'
      return 'info'
    },
    getPriorityText(priority) {
      const p = parseInt(priority)
      if (p === 3) return '高'
      if (p === 2) return '中'
      return '低'
    },

    // ==================== 图表 ====================
    initChart() {
      if (!this.$refs.studyChart) return
      const chart = this.$echarts.init(this.$refs.studyChart)
      const data = []
      const today = new Date()
      for (let i = 6; i >= 0; i--) {
        const date = new Date(today)
        date.setDate(date.getDate() - i)
        const key = date.toISOString().split('T')[0]
        data.push((this.studyRecords[key] || 0))
      }
      const option = {
        tooltip: { trigger: 'axis', formatter: '{b}: {c}小时' },
        grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
        xAxis: { type: 'category', data: (() => {
          const labels = []
          for (let i = 6; i >= 0; i--) {
            const date = new Date(today)
            date.setDate(date.getDate() - i)
            labels.push((date.getMonth() + 1) + '/' + date.getDate())
          }
          return labels
        })() },
        yAxis: { type: 'value', name: '学习时长(h)' },
        series: [{ data, type: 'bar', itemStyle: { color: '#409EFF' } }]
      }
      chart.setOption(option)
      window.addEventListener('resize', () => chart.resize())
    },
    initTypeChart() {
      if (!this.$refs.typeChart) return
      const chart = this.$echarts.init(this.$refs.typeChart)
      const distData = this.subjectDistributionData || []
      let chartData
      if (distData.length > 0) {
        chartData = distData.map(d => ({ name: d.subject, value: d.duration }))
      } else {
        chartData = [{ name: '暂无数据', value: 1, itemStyle: { color: '#e0e0e0' } }]
      }
      const option = {
        tooltip: { trigger: 'item', formatter: '{b}: {c}分钟 ({d}%)' },
        legend: { orient: 'vertical', left: 'left' },
        series: [{
          name: '学习科目', type: 'pie', radius: '60%',
          data: chartData,
          emphasis: { shadowBlur: 10, shadowColor: 'rgba(0, 0, 0, 0.5)' }
        }]
      }
      chart.setOption(option)
      window.addEventListener('resize', () => chart.resize())
    },

    // ==================== 科目分布 ====================
    async fetchSubjectDistribution() {
      try {
        const res = await this.$http.get('/study/subject-distribution')
        this.subjectDistributionData = res || []
      } catch (e) {
        console.error('获取科目分布失败:', e)
        this.subjectDistributionData = []
      }
    },

    // ==================== 周报 ====================
    async fetchWeeklyReport() {
      try {
        const weekStart = this.currentWeekStart.toISOString().split('T')[0]
        const res = await this.$http.get('/study/weekly-report', { params: { weekStart } })
        this.weeklyReport = res || { weekTotal: 0, dailyData: {}, subjectDistribution: {}, completedChapters: 0, completedTasks: 0 }
        // Fetch previous week for comparison
        const prevStart = new Date(this.currentWeekStart)
        prevStart.setDate(prevStart.getDate() - 7)
        try {
          const prevRes = await this.$http.get('/study/weekly-report', { params: { weekStart: prevStart.toISOString().split('T')[0] } })
          this.prevWeekReport = prevRes || null
        } catch (e) {
          this.prevWeekReport = null
        }
        this.animateWeekCount()
        this.$nextTick(() => {
          this.initWeekBarChart()
          this.initWeekPieChart()
        })
      } catch (e) {
        console.error('获取周报失败:', e)
        this.weeklyReport = { weekTotal: 0, dailyData: {}, subjectDistribution: {}, completedChapters: 0, completedTasks: 0 }
        this.prevWeekReport = null
      }
    },
    animateWeekCount() {
      const target = this.weeklyReport.weekTotal || 0
      if (target === 0) {
        this.weekStatDisplay = 0
        return
      }
      this.weekStatAnimating = true
      this.weekStatDisplay = 0
      const duration = 1000
      const startTime = Date.now()
      const step = () => {
        const elapsed = Date.now() - startTime
        const progress = Math.min(elapsed / duration, 1)
        // Ease-out
        const eased = 1 - Math.pow(1 - progress, 3)
        this.weekStatDisplay = Math.round(target * eased)
        if (progress < 1) {
          requestAnimationFrame(step)
        } else {
          this.weekStatDisplay = target
          this.weekStatAnimating = false
        }
      }
      requestAnimationFrame(step)
    },

    initWeekBarChart() {
      if (!this.$refs.weekBarChart) return
      const chart = this.$echarts.init(this.$refs.weekBarChart)
      const dailyData = this.weeklyReport.dailyData || {}
      const weekDayNames = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
      const data = this.weekDays.map(d => {
        const key = d.toISOString().split('T')[0]
        return Math.round((dailyData[key] || 0) / 60 * 10) / 10
      })
      const labels = this.weekDays.map(d => {
        return weekDayNames[d.getDay()] + '\n' + (d.getMonth() + 1) + '/' + d.getDate()
      })
      const option = {
        tooltip: { trigger: 'axis', formatter: '{b}: {c}小时' },
        grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
        xAxis: { type: 'category', data: labels },
        yAxis: { type: 'value', name: '学习时长(h)' },
        series: [{
          data, type: 'bar',
          itemStyle: { color: '#409EFF' }
        }]
      }
      chart.setOption(option)
      // Click handler for day detail
      const that = this
      chart.off('click')
      chart.on('click', function(params) {
        if (params.componentType === 'series') {
          const dayIdx = params.dataIndex
          if (dayIdx >= 0 && dayIdx < that.weekDays.length) {
            const key = that.weekDays[dayIdx].toISOString().split('T')[0]
            that.showDayDetail(key)
          }
        }
      })
      window.addEventListener('resize', () => chart.resize())
    },
    showDayDetail(dateKey) {
      this.dayDetailDate = dateKey
      this.dayDetailItems = []
      // Collect activities from recentActivities matching this date
      const items = (this.recentActivities || []).filter(a => a.date === dateKey)
      this.dayDetailItems = items.map(a => ({
        type: a.filterType || 'study',
        typeLabel: a.typeLabel || '学习',
        content: a.content || '',
        duration: a.duration || 0
      }))
      this.dayDetailVisible = true
    },

    initWeekPieChart() {
      if (!this.$refs.weekPieChart) return
      const chart = this.$echarts.init(this.$refs.weekPieChart)
      const subjectDist = this.weeklyReport.subjectDistribution || {}
      const keys = Object.keys(subjectDist)
      let chartData
      if (keys.length > 0) {
        chartData = keys.map(k => ({ name: k, value: subjectDist[k] }))
      } else {
        chartData = [{ name: '暂无数据', value: 1, itemStyle: { color: '#e0e0e0' } }]
      }
      const option = {
        tooltip: { trigger: 'item', formatter: '{b}: {c}分钟 ({d}%)' },
        legend: { orient: 'vertical', left: 'left' },
        series: [{
          name: '科目分布', type: 'pie', radius: '60%',
          data: chartData,
          emphasis: { shadowBlur: 10, shadowColor: 'rgba(0, 0, 0, 0.5)' }
        }]
      }
      chart.setOption(option)
      window.addEventListener('resize', () => chart.resize())
    },

    goToPrevWeek() {
      this.weekOffset--
      this.fetchWeeklyReport()
    },

    goToNextWeek() {
      this.weekOffset++
      this.fetchWeeklyReport()
    },

    formatMinutes(minutes) {
      if (!minutes || minutes === 0) return '0分钟'
      const h = Math.floor(minutes / 60)
      const m = minutes % 60
      if (h > 0 && m > 0) return h + '小时' + m + '分钟'
      if (h > 0) return h + '小时'
      return m + '分钟'
    },

    // ==================== 计划模板 ====================
    async fetchPlanTemplates() {
      try {
        const res = await this.$http.get('/plan-template/list')
        this.planTemplates = res || []
      } catch (e) {
        console.error('获取计划模板失败:', e)
        this.planTemplates = []
      }
    },

    openTemplateDialog() {
      this.templateDialogVisible = true
      if (this.planTemplates.length === 0) {
        this.fetchPlanTemplates()
      }
    },

    applyTemplate(tpl) {
      this.planForm.title = tpl.title || ''
      this.planForm.description = tpl.description || ''
      this.planForm.priority = '2'
      this.planForm.startDate = new Date()
      const endDate = new Date()
      endDate.setDate(endDate.getDate() + (tpl.totalDays || 7))
      this.planForm.endDate = endDate
      this.templateDialogVisible = false
      this.planDialogVisible = true
    },

    getCategoryType(category) {
      const types = { '数学': '', '英语': 'success', '政治': 'warning', '专业课': 'danger' }
      return types[category] || 'info'
    },

    // ==================== 提醒 ====================
    checkUpcomingTasks() {
      const now = new Date()
      const threeDaysLater = new Date(now.getTime() + 3 * 24 * 60 * 60 * 1000)
      const urgent = []
      const allUrgent = []

      // 检查计划
      this.planList.forEach(plan => {
        if (plan.status !== 'completed' && plan.endDate) {
          const endDate = new Date(plan.endDate)
          const daysLeft = this.getDaysLeft(plan)
          if (daysLeft !== null && daysLeft <= 3) {
            const task = {
              id: plan.id, title: plan.title, deadline: plan.endDate,
              type: 'plan', isOverdue: daysLeft < 0, daysLeft: daysLeft
            }
            allUrgent.push(task)
            if (endDate >= now && endDate <= threeDaysLater) {
              urgent.push(task)
            }
          }
        }
      })

      // 检查待办
      this.todoList.forEach(todo => {
        if (todo.status !== 'completed' && todo.deadline) {
          const daysLeft = this.getDaysLeftFromStr(todo.deadline)
          if (daysLeft <= 3) {
            const task = {
              id: todo.id, title: todo.content, deadline: todo.deadline,
              type: 'todo', isOverdue: daysLeft < 0, daysLeft: daysLeft
            }
            allUrgent.push(task)
            const deadline = new Date(todo.deadline)
            if (deadline >= now && deadline <= threeDaysLater) {
              urgent.push(task)
            }
          }
        }
      })

      this.urgentTasks = urgent
      this.allUrgentTasks = allUrgent

      // 弹出提醒对话框
      if (allUrgent.length > 0 && !this.reminderDialogVisible) {
        this.reminderDialogVisible = true
      }

      if (urgent.length > 0) {
        this.$notify({
          title: '任务提醒',
          message: `您有 ${urgent.length} 个即将到期的任务`,
          type: 'warning',
          duration: 5000
        })
      }
    },

    // ==================== 数据获取 ====================
    async getStudyData() {
      try {
        const res = await this.$http.get('/study/stats')
        const data = res || {}

        this.totalStudyTime = data.totalStudyTime || 0
        this.completedCourses = data.completedCourses || 0
        this.downloadedResources = data.downloadedResources || 0
        this.readNews = data.readNews || 0

        const studyRecords = data.studyRecords || {}
        // Save raw minute values for plan progress calculation
        this.rawStudyRecords = { ...studyRecords }
        const newRecords = {}
        Object.keys(studyRecords).forEach(date => {
          newRecords[date] = studyRecords[date] / 60
        })
        this.studyRecords = newRecords
        this.chartDataEmpty = Object.keys(studyRecords).length === 0

        // 处理计划列表
        this.planList = (data.planList || []).map(item => ({
          id: item.id,
          title: item.title,
          description: item.description,
          startDate: item.startDate || item.targetDate || '',
          endDate: item.endDate || item.targetDate || '',
          status: item.status === 1 ? 'completed' : 'pending',
          priority: item.priority,
          progress: item.progress || 0
        }))

        // 处理待办列表
        this.todoList = (data.todoList || []).map(item => ({
          id: item.id,
          content: item.title,
          description: item.description || '',
          status: item.status === 1 ? 'completed' : 'pending',
          priority: item.priority,
          deadline: item.endDate || item.targetDate || '',
          createTime: item.createTime || ''
        }))

        // 科目数据（从专门API获取全量聚合数据）
        this.fetchSubjectDistribution()

        // 构建活动时间线 with filterType for trajectory filtering
        const recentRecords = data.recentRecords || []
        this.recentActivities = []
        recentRecords.forEach(record => {
          const timeStr = record.createTime ? new Date(record.createTime).toLocaleString('zh-CN') : ''
          this.recentActivities.push({
            time: timeStr,
            type: 'primary', icon: 'el-icon-time',
            filterType: 'study', typeLabel: '学习',
            content: `${record.subject || '学习'} - ${record.content || ''} (${record.duration}分钟)`,
            duration: record.duration || 0,
            date: record.createTime ? record.createTime.split('T')[0] : ''
          })
        })
        const readNewsList = data.readNewsList || []
        readNewsList.forEach(news => {
          const timeStr = news.time ? new Date(news.time).toLocaleString('zh-CN') : ''
          this.recentActivities.push({
            time: timeStr,
            type: 'info', icon: 'el-icon-document',
            filterType: 'news', typeLabel: '资讯',
            content: `阅读资讯 - ${news.title || ''}`,
            duration: 0,
            date: news.time ? news.time.split('T')[0] : ''
          })
        })
        const downloadedList = data.downloadedResourcesList || []
        downloadedList.forEach(resource => {
          const timeStr = resource.time ? new Date(resource.time).toLocaleString('zh-CN') : ''
          this.recentActivities.push({
            time: timeStr,
            type: 'success', icon: 'el-icon-download',
            filterType: 'download', typeLabel: '下载',
            content: `下载资源 - ${resource.name || ''}`,
            duration: 0,
            date: resource.time ? resource.time.split('T')[0] : ''
          })
        })
        const studiedList = data.studiedCoursesList || []
        studiedList.forEach(course => {
          const timeStr = course.time ? new Date(course.time).toLocaleString('zh-CN') : ''
          this.recentActivities.push({
            time: timeStr,
            type: 'warning', icon: 'el-icon-video-camera',
            filterType: 'study', typeLabel: '学习',
            content: `学习课程 - ${course.name || ''} (${course.duration || 0}分钟)`,
            duration: course.duration || 0,
            date: course.time ? course.time.split('T')[0] : ''
          })
        })
        this.recentActivities.sort((a, b) => new Date(b.time) - new Date(a.time))

        this.checkUpcomingTasks()
      } catch (error) {
        console.error('获取学习数据失败:', error)
        this.$message.error('获取学习数据失败')
      }
    }
  }
}
</script>

<style scoped lang="scss">
.study {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;

  .study-header {
    margin-bottom: 30px;
    text-align: center;
    h2 { font-size: 28px; font-weight: 600; color: #303133; margin-bottom: 8px; }
    .study-subtitle { font-size: 16px; color: #909399; margin: 0; }
  }

  .reminder-container {
    margin-bottom: 20px;
    .reminder-list { margin-top: 10px; }
    .reminder-item {
      display: flex; justify-content: space-between; align-items: center;
      padding: 8px 0; border-bottom: 1px solid #f0f0f0;
      &:last-child { border-bottom: none; }
      .reminder-title { font-size: 14px; color: #303133; display: flex; align-items: center; gap: 8px; }
      .reminder-deadline { font-size: 12px; color: #909399; white-space: nowrap; }
    }
  }

  .study-tabs {
    background: #fff; border-radius: 8px;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1); overflow: hidden;
  }
}

// ==================== 日历 ====================
.calendar-container {
  padding: 20px;
  .calendar-header {
    display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px;
    h3 { font-size: 18px; font-weight: 600; margin: 0; }
    .calendar-actions { display: flex; gap: 8px; flex-wrap: wrap; }
  }
  .calendar-layout {
    display: flex; gap: 20px;
    .calendar-main { flex: 1; min-width: 0; }
    .today-panel {
      width: 260px; flex-shrink: 0; background: #fafbfc; border-radius: 8px;
      padding: 16px; border: 1px solid #ebeef5;
      .today-header {
        display: flex; justify-content: space-between; align-items: center;
        margin-bottom: 16px; padding-bottom: 12px; border-bottom: 2px solid #409eff;
        h4 { margin: 0; font-size: 16px; color: #409eff; i { margin-right: 4px; } }
        .today-date { font-size: 12px; color: #909399; }
      }
      .today-section {
        margin-bottom: 16px;
        h5 { font-size: 13px; color: #606266; margin: 0 0 8px; i { margin-right: 4px; } }
        .today-item {
          display: flex; align-items: center; gap: 6px; padding: 6px 0;
          font-size: 13px; border-bottom: 1px dashed #ebeef5;
          &:last-child { border-bottom: none; }
        }
        .today-empty { font-size: 12px; color: #c0c4cc; margin: 0; }
        .today-study-info { font-size: 24px; color: #409eff; font-weight: 700;
          .study-big-num { font-size: 32px; }
        }
      }
    }
  }
  .custom-calendar {
    background: #f9f9f9; border-radius: 8px; padding: 10px;
    ::v-deep .el-calendar__header { display: none !important; }
    ::v-deep .el-calendar__button-group { display: none !important; }
  }
  .calendar-cell {
    height: 100%; padding: 8px 4px; cursor: pointer; transition: all 0.3s;
    text-align: center; border-radius: 4px; position: relative;
    &:hover { background: #F5F7FA; }
    &.has-record { background: #ECF5FF; }
    &.high-study { background: #FDE2E2; }
    &.medium-study { background: #FEF3CD; }
    &.low-study { background: #E3F2FD; }
    &.today { border: 2px solid #409EFF; font-weight: bold; }
    .date { font-size: 14px; display: block; margin-bottom: 2px; }
    .study-time { font-size: 11px; color: #409EFF; display: block; font-weight: 500; }
    .indicator-dots {
      position: absolute; bottom: 4px; left: 50%; transform: translateX(-50%);
      display: flex; gap: 3px;
      .dot { font-size: 10px; line-height: 1; }
      .dot-plan { color: #67C23A; }
      .dot-todo { color: #E6A23C; }
    }
  }
}

@media (max-width: 900px) {
  .calendar-layout { flex-direction: column; .today-panel { width: 100%; } }
}

.calendar-detail {
  .detail-section {
    margin-bottom: 20px;
    h4 { font-size: 15px; color: #303133; margin: 0 0 10px 0; i { margin-right: 4px; } }
    .detail-item {
      display: flex; align-items: center; gap: 10px; padding: 8px 0;
      border-bottom: 1px solid #f0f0f0; font-size: 14px;
      .detail-date { color: #909399; font-size: 12px; margin-left: auto; }
    }
    .no-data { color: #C0C4CC; font-size: 13px; }
  }
}

// ==================== 计划 ====================
.plan-container {
  padding: 20px;
  .plan-header {
    display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px;
    h3 { font-size: 18px; font-weight: 600; margin: 0; }
  }
  .empty-state { margin-top: 40px; text-align: center; .empty-hint { margin-top: 16px; color: #909399; font-size: 14px; } }
  .plan-card {
    margin-top: 15px; transition: all 0.3s; border-radius: 8px;
    &:hover { box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15); transform: translateY(-2px); }
    &.completed { opacity: 0.7; }
    &.overdue-card { border-left: 4px solid #F56C6C; }
    .plan-card-header {
      margin-bottom: 12px;
      .plan-title-row {
        display: flex; justify-content: space-between; align-items: flex-start; flex-wrap: wrap; gap: 8px;
        h4 { margin: 0; font-size: 16px; font-weight: 600; }
        .plan-tags { display: flex; gap: 6px; flex-wrap: wrap; }
      }
    }
    .plan-description { color: #606266; margin-bottom: 12px; line-height: 1.6; }
    .plan-meta {
      display: flex; gap: 24px; color: #909399; font-size: 14px; margin-bottom: 16px; flex-wrap: wrap;
      .meta-item { display: flex; align-items: center; gap: 4px; i { font-size: 14px; } }
      .text-danger { color: #E6A23C; font-weight: 600; }
      .text-overdue { color: #F56C6C; font-weight: 600; }
    }
    .plan-progress { margin-bottom: 16px; .progress-controls { margin-top: 4px; .progress-text { font-size: 12px; color: #909399; } } .progress-study-days { font-size: 12px; color: #67C23A; margin-top: 4px; } }
    .plan-actions { display: flex; gap: 10px; justify-content: flex-end; }
  }
}

// ==================== 待办 ====================
.todo-container {
  padding: 20px;
  .todo-header {
    display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px;
    h3 { font-size: 18px; font-weight: 600; margin: 0; }
    .todo-actions { display: flex; gap: 10px; align-items: center; .todo-filter { min-width: 120px; } }
  }
  .empty-state { margin-top: 40px; text-align: center; .empty-hint { margin-top: 16px; color: #909399; font-size: 14px; } }
  .todo-groups {
    .todo-group {
      margin-bottom: 20px;
      .todo-group-header {
        font-size: 14px; font-weight: 600; padding: 8px 12px; border-radius: 6px; margin-bottom: 8px;
        i { margin-right: 4px; }
        &.overdue-header { background: #FEF0F0; color: #F56C6C; }
        &.today-header { background: #FDF6EC; color: #E6A23C; }
        &.upcoming-header { background: #ECF5FF; color: #409EFF; }
        &.completed-header { background: #F0F9EB; color: #67C23A; }
      }
    }
  }
  .todo-card {
    margin-bottom: 8px; border-radius: 8px;
    &.completed-todo-card { opacity: 0.6; }
  }
  .todo-checkbox { display: flex; width: 100%; }
  .todo-item {
    display: flex; justify-content: space-between; align-items: center;
    width: 100%; padding: 4px 0;
    .todo-content {
      display: flex; align-items: center; gap: 10px; flex: 1;
      span { font-size: 14px; line-height: 1.6; }
      .completed-text { text-decoration: line-through; color: #C0C4CC; }
      .todo-badges { display: flex; gap: 6px; }
      .todo-plan-tag { margin-left: 4px; }
    }
    .todo-meta {
      display: flex; align-items: center; gap: 12px; margin-left: 12px;
      .todo-time { color: #909399; font-size: 12px; }
      .todo-today-study { color: #409EFF; font-size: 12px; font-weight: 500; }
      .delete-todo { color: #F56C6C; &:hover { color: #F78989; transform: scale(1.1); } }
    }
  }
}

// ==================== 轨迹 ====================
.track-container {
  padding: 20px;
  h3 { font-size: 18px; font-weight: 600; margin: 0 0 20px 0; }
  .stats {
    display: grid; grid-template-columns: repeat(4, 1fr); gap: 20px; margin-bottom: 30px;
    .stat-card {
      text-align: center; transition: all 0.3s; border-radius: 8px;
      &:hover { transform: translateY(-5px); box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15); }
      .stat-content {
        .stat-icon { font-size: 28px; color: #409EFF; margin-bottom: 12px; }
        .stat-value { font-size: 36px; font-weight: bold; color: #409EFF; margin-bottom: 6px; }
        .stat-label { font-size: 14px; color: #909399; }
      }
    }
  }
  .charts-container {
    display: grid; grid-template-columns: 1fr 1fr; gap: 30px; margin-bottom: 30px;
    .chart-item {
      background: #fff; padding: 20px; border-radius: 8px; box-shadow: 0 1px 4px rgba(0, 0, 0, 0.1);
      h3 { margin-top: 0; margin-bottom: 20px; font-size: 16px; font-weight: 600; }
      .study-chart, .type-chart { height: 300px; }
      .empty-chart {
        height: 300px; display: flex; flex-direction: column; align-items: center;
        justify-content: center; background: #f9f9f9; border-radius: 8px; padding: 20px;
        .empty-hint { margin-top: 16px; color: #909399; font-size: 14px; text-align: center; }
      }
    }
  }
  .learning-trajectory {
    background: #fff; padding: 20px; border-radius: 8px; box-shadow: 0 1px 4px rgba(0, 0, 0, 0.1);
    h3 { margin-top: 0; margin-bottom: 16px; font-size: 16px; font-weight: 600; }
    .trajectory-filters { margin-bottom: 20px; text-align: center; }
    .trajectory-grid {
      .trajectory-week {
        margin-bottom: 24px;
        .week-header {
          font-size: 14px; font-weight: 600; color: #303133; margin-bottom: 12px;
          padding: 6px 12px; background: #F5F7FA; border-radius: 6px;
        }
      }
      .trajectory-card {
        margin-bottom: 12px; border-radius: 8px; max-height: 80px; overflow: hidden;
        transition: all 0.3s;
        &:hover { transform: translateY(-2px); box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1); }
        &.trajectory-study { border-left: 3px solid #409EFF; }
        &.trajectory-download { border-left: 3px solid #67C23A; }
        &.trajectory-news { border-left: 3px solid #909399; }
        ::v-deep .el-card__body { padding: 10px 14px; }
        .trajectory-card-inner {
          display: flex; gap: 10px; align-items: flex-start;
          .trajectory-card-icon {
            flex-shrink: 0; text-align: center;
            i { font-size: 18px; color: #409EFF; display: block; }
            .trajectory-card-type { font-size: 11px; color: #909399; margin-top: 2px; display: block; }
          }
          .trajectory-card-body {
            flex: 1; min-width: 0;
            .trajectory-card-content {
              margin: 0; font-size: 13px; color: #606266;
              white-space: nowrap; overflow: hidden; text-overflow: ellipsis;
            }
            .trajectory-card-time { font-size: 11px; color: #909399; margin-right: 8px; }
            .trajectory-card-date { font-size: 11px; color: #C0C4CC; }
          }
        }
      }
    }
    .expand-more { text-align: center; padding: 10px 0; }
    .empty-trajectory { padding: 40px 0; text-align: center; background: #f9f9f9; border-radius: 8px; .empty-hint { margin-top: 16px; color: #909399; font-size: 14px; } }
  }
}

// ==================== 提醒弹窗 ====================
.reminder-dialog {
  .reminder-dialog-subtitle { color: #909399; font-size: 14px; margin-bottom: 16px; }
  .reminder-dialog-item {
    display: flex; justify-content: space-between; align-items: center;
    padding: 12px 0; border-bottom: 1px solid #F0F2F5;
    &:last-child { border-bottom: none; }
    .reminder-dialog-left { display: flex; align-items: center; gap: 10px; }
    .reminder-dialog-title { font-size: 14px; color: #303133; }
    .reminder-dialog-deadline { font-size: 12px; color: #909399; white-space: nowrap; }
  }
}

// ==================== 周报 ====================
.weekly-container {
  padding: 20px;
  .weekly-header {
    display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px;
    h3 { font-size: 18px; font-weight: 600; margin: 0; }
    .week-selector {
      display: flex; align-items: center; gap: 12px;
      .current-week-label { font-size: 14px; font-weight: 500; color: #303133; min-width: 140px; text-align: center; }
    }
  }
  .weekly-stats {
    display: grid; grid-template-columns: repeat(3, 1fr); gap: 20px; margin-bottom: 30px;
    .weekly-stat-card {
      text-align: center; transition: all 0.3s; border-radius: 8px;
      &:hover { transform: translateY(-5px); box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15); }
      .stat-content {
        .stat-icon { font-size: 28px; color: #409EFF; margin-bottom: 12px; }
        .stat-value { font-size: 28px; font-weight: bold; color: #409EFF; margin-bottom: 6px; }
        .stat-label { font-size: 14px; color: #909399; }
        .stat-compare { font-size: 13px; margin-top: 4px;
          .compare-up { color: #F56C6C; font-weight: 600; }
          .compare-down { color: #67C23A; font-weight: 600; }
        }
      }
    }
  }
  .weekly-charts {
    display: grid; grid-template-columns: 1fr 1fr; gap: 30px;
    .chart-item {
      background: #fff; padding: 20px; border-radius: 8px; box-shadow: 0 1px 4px rgba(0, 0, 0, 0.1);
      h4 { font-size: 16px; font-weight: 600; color: #303133; margin: 0 0 20px 0; }
      .week-chart { height: 300px; }
    }
  }
}

// ==================== 周报日详情弹窗 ====================
.day-detail-list {
  .day-detail-item {
    display: flex; align-items: center; gap: 10px; padding: 10px 0;
    border-bottom: 1px solid #F0F2F5;
    &:last-child { border-bottom: none; }
    .day-detail-content { flex: 1; font-size: 14px; color: #606266; }
  }
}
.day-detail-empty { text-align: center; padding: 20px 0; color: #909399; font-size: 14px; }

// ==================== 计划模板 ====================
.plan-header-actions {
  display: flex; gap: 8px; align-items: center;
}

.template-loading {
  padding: 40px 0; text-align: center;
}

.template-grid {
  display: grid; grid-template-columns: repeat(2, 1fr); gap: 16px;
  .template-card {
    cursor: pointer; transition: all 0.3s; border-radius: 8px;
    &:hover { transform: translateY(-3px); box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15); border-color: #409EFF; }
    h4 { font-size: 15px; font-weight: 600; color: #303133; margin: 0 0 8px 0; }
    .template-desc {
      font-size: 13px; color: #606266; margin: 0 0 12px 0;
      line-height: 1.6; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden;
    }
    .template-meta {
      display: flex; justify-content: space-between; align-items: center;
      .template-duration { font-size: 12px; color: #909399; }
    }
  }
}

// ==================== 响应式 ====================
@media (max-width: 1024px) {
  .track-container { .stats { grid-template-columns: repeat(2, 1fr); } .charts-container { grid-template-columns: 1fr; } }
  .weekly-stats { grid-template-columns: repeat(2, 1fr); }
  .weekly-charts { grid-template-columns: 1fr; }
}
@media (max-width: 768px) {
  .study { padding: 10px;
    .study-header h2 { font-size: 24px; }
    .calendar-header { flex-direction: column; align-items: flex-start; gap: 10px; }
  }
  .plan-container, .todo-container, .calendar-container, .track-container { padding: 15px; }
  .track-container .stats { grid-template-columns: 1fr; }
  .weekly-stats { grid-template-columns: 1fr; }
  .weekly-header { flex-direction: column; gap: 12px; align-items: flex-start; }
  .template-grid { grid-template-columns: 1fr; }
}
</style>
