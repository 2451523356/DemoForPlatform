CREATE DATABASE IF NOT EXISTS kaoyan_platform DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE kaoyan_platform;

SET FOREIGN_KEY_CHECKS = 0;

-- 用户表
DROP TABLE IF EXISTS sys_user;
CREATE TABLE sys_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(100) NOT NULL COMMENT '密码',
    nickname VARCHAR(50) COMMENT '昵称',
    avatar VARCHAR(500) COMMENT '头像',
    email VARCHAR(100) COMMENT '邮箱',
    phone VARCHAR(20) COMMENT '手机号',
    gender TINYINT DEFAULT 0 COMMENT '性别 0-未知 1-男 2-女',
    status TINYINT DEFAULT 1 COMMENT '状态 0-禁用 1-正常',
    role VARCHAR(20) DEFAULT 'USER' COMMENT '角色 USER-用户 ADMIN-管理员',
    points INT DEFAULT 0 COMMENT '积分',
    last_sign_in_time DATETIME DEFAULT NULL COMMENT '上次签到时间',
    target_school VARCHAR(100) COMMENT '目标院校',
    target_major VARCHAR(100) COMMENT '目标专业',
    bio VARCHAR(500) COMMENT '个人简介',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0 COMMENT '是否删除 0-否 1-是',
    INDEX idx_username (username),
    INDEX idx_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 资讯表
DROP TABLE IF EXISTS sys_news;
CREATE TABLE sys_news (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(200) NOT NULL COMMENT '标题',
    content TEXT COMMENT '内容',
    summary VARCHAR(500) COMMENT '摘要',
    cover VARCHAR(500) COMMENT '封面图',
    category VARCHAR(50) COMMENT '分类：国家政策/教育部文件/院校政策/招生简章/专业目录/报名通知/网报公告/考试大纲/初试信息/成绩查询/国家线或复试线/复试通知/复试经验/调剂信息/录取公示/备考指南/经验分享/复习方法/考研常识/院校新闻/学科排名/招生宣讲',
    exam_stage VARCHAR(20) COMMENT '备考阶段：择校评估/基础复习/强化提升/报名报考/冲刺模考/初试/复试准备/调剂录取',
    tags VARCHAR(200) COMMENT '标签',
    author_id BIGINT COMMENT '作者ID',
    view_count INT DEFAULT 0 COMMENT '浏览量',
    like_count INT DEFAULT 0 COMMENT '点赞数',
    is_top TINYINT DEFAULT 0 COMMENT '是否置顶 0-否 1-是',
    status TINYINT DEFAULT 1 COMMENT '状态 0-草稿 1-已发布',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    INDEX idx_category (category),
    INDEX idx_status (status),
    INDEX idx_is_top (is_top)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资讯表';

-- 资源表
DROP TABLE IF EXISTS sys_resource;
CREATE TABLE sys_resource (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(200) NOT NULL COMMENT '标题',
    description TEXT COMMENT '描述',
    file_url VARCHAR(500) COMMENT '文件地址',
    file_type VARCHAR(50) COMMENT '文件类型',
    file_size BIGINT COMMENT '文件大小',
    category VARCHAR(50) COMMENT '资源类型：历年真题/真题解析/模拟试题/复习笔记/课程讲义/学霸笔记/思维导图/公式手册/词汇手册/知识点总结/作文模板/答题模板/背诵提纲',
    subject VARCHAR(50) COMMENT '科目：数学/英语/政治/计算机408/金融学431/法学/教育学/心理学/医学/管理学/经济学/文学/机械/电气/其他',
    tags VARCHAR(200) COMMENT '标签',
    points INT DEFAULT 0 COMMENT '下载所需积分',
    uploader_id BIGINT COMMENT '上传者ID',
    download_count INT DEFAULT 0 COMMENT '下载次数',
    view_count INT DEFAULT 0 COMMENT '浏览次数',
    rating DECIMAL(2,1) DEFAULT 5.0 COMMENT '评分',
    rating_count INT DEFAULT 0 COMMENT '评分人数',
    status TINYINT DEFAULT 1 COMMENT '状态 0-禁用 1-正常',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    INDEX idx_category (category),
    INDEX idx_subject (subject),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资源表';

-- 课程表
DROP TABLE IF EXISTS sys_course;
CREATE TABLE sys_course (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(200) NOT NULL COMMENT '课程标题',
    description TEXT COMMENT '课程描述',
    cover VARCHAR(500) COMMENT '封面图',
    outline TEXT COMMENT '课程大纲(富文本)',
    highlights TEXT COMMENT '课程亮点',
    target_audience TEXT COMMENT '适合人群',
    prerequisites TEXT COMMENT '前置要求',
    teacher_id BIGINT COMMENT '讲师ID关联sys_user',
    category_id VARCHAR(50) COMMENT '分类ID',
    subject VARCHAR(50) COMMENT '学科：数学/英语/政治/计算机408/金融学431/法学/教育学/心理学/医学/管理学/其他',
    stage VARCHAR(20) COMMENT '阶段 基础/强化/冲刺/真题',
    form VARCHAR(20) COMMENT '形式 视频/直播/录播+直播',
    price DECIMAL(10,2) DEFAULT 0.00 COMMENT '价格',
    lesson_count INT DEFAULT 0 COMMENT '课时数',
    duration INT DEFAULT 0 COMMENT '总时长(分钟)',
    student_count INT DEFAULT 0 COMMENT '学员数',
    rating DECIMAL(2,1) DEFAULT 5.0 COMMENT '评分',
    status TINYINT DEFAULT 1 COMMENT '状态 0-下架 1-上架',
    live_url VARCHAR(500) COMMENT '直播流地址/B站链接',
    live_platform VARCHAR(20) DEFAULT 'custom' COMMENT '直播平台 bilibili/custom',
    live_status TINYINT DEFAULT 0 COMMENT '直播状态 0-预告 1-直播中 2-回放',
    live_start_time DATETIME COMMENT '直播开始时间',
    live_end_time DATETIME COMMENT '直播结束时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    INDEX idx_category (category_id),
    INDEX idx_subject (subject),
    INDEX idx_stage (stage),
    INDEX idx_form (form),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程表';

-- 课程章节表
DROP TABLE IF EXISTS sys_course_chapter;
CREATE TABLE sys_course_chapter (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    course_id BIGINT NOT NULL COMMENT '课程ID',
    title VARCHAR(200) NOT NULL COMMENT '章节标题',
    video_url VARCHAR(500) COMMENT '视频地址',
    duration INT DEFAULT 0 COMMENT '时长(分钟)',
    sort INT DEFAULT 0 COMMENT '排序',
    is_free TINYINT DEFAULT 0 COMMENT '是否免费 0-否 1-是',
    file_url VARCHAR(500) COMMENT '配套资料地址',
    file_name VARCHAR(200) COMMENT '配套资料文件名',
    status TINYINT DEFAULT 1 COMMENT '状态',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    INDEX idx_course_id (course_id),
    INDEX idx_sort (sort)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程章节表';

-- 用户课程表
DROP TABLE IF EXISTS sys_user_course;
CREATE TABLE sys_user_course (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    course_id BIGINT NOT NULL COMMENT '课程ID',
    current_chapter_id BIGINT COMMENT '当前学习章节ID',
    progress INT DEFAULT 0 COMMENT '学习进度(%)',
    study_duration INT DEFAULT 0 COMMENT '学习时长(分钟)',
    last_study_time DATETIME COMMENT '最后学习时间',
    status TINYINT DEFAULT 1 COMMENT '状态',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    UNIQUE KEY uk_user_course (user_id, course_id),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户课程表';

-- 学习记录表
DROP TABLE IF EXISTS sys_study_record;
CREATE TABLE sys_study_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    study_date DATE NOT NULL COMMENT '学习日期',
    duration INT DEFAULT 0 COMMENT '学习时长(分钟)',
    subject VARCHAR(50) COMMENT '科目',
    content VARCHAR(500) COMMENT '学习内容',
    type TINYINT DEFAULT 1 COMMENT '类型 1-课程 2-资源 3-资讯',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    INDEX idx_user_id (user_id),
    INDEX idx_study_date (study_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学习记录表';

-- 待办事项表
DROP TABLE IF EXISTS sys_todo;
CREATE TABLE sys_todo (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    title VARCHAR(200) NOT NULL COMMENT '标题',
    description TEXT COMMENT '描述',
    type TINYINT DEFAULT 1 COMMENT '类型 1-长期计划 2-每日待办',
    target_date DATE COMMENT '目标日期',
    start_date DATE COMMENT '开始日期(长期计划)',
    end_date DATE COMMENT '截止日期',
    remind_time DATETIME COMMENT '提醒时间',
    priority TINYINT DEFAULT 1 COMMENT '优先级 1-低 2-中 3-高',
    status TINYINT DEFAULT 0 COMMENT '状态 0-未完成 1-已完成',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    INDEX idx_user_id (user_id),
    INDEX idx_target_date (target_date),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='待办事项表';

-- 圈子表
DROP TABLE IF EXISTS sys_circle;
CREATE TABLE sys_circle (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL COMMENT '圈子名称',
    description TEXT COMMENT '圈子描述',
    icon VARCHAR(500) COMMENT '圈子图标',
    type VARCHAR(50) COMMENT '类型 院校/专业',
    university VARCHAR(100) COMMENT '所属院校',
    major VARCHAR(100) COMMENT '所属专业',
    creator_id BIGINT COMMENT '创建者ID',
    member_count INT DEFAULT 0 COMMENT '成员数',
    post_count INT DEFAULT 0 COMMENT '帖子数',
    sort INT DEFAULT 0 COMMENT '排序',
    status TINYINT DEFAULT 1 COMMENT '状态',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    INDEX idx_type (type),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='圈子表';

-- 帖子表
DROP TABLE IF EXISTS sys_post;
CREATE TABLE sys_post (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    title VARCHAR(200) COMMENT '标题',
    content TEXT NOT NULL COMMENT '内容',
    images TEXT COMMENT '图片列表',
    circle_id BIGINT COMMENT '圈子ID',
    tags VARCHAR(200) COMMENT '标签',
    view_count INT DEFAULT 0 COMMENT '浏览量',
    like_count INT DEFAULT 0 COMMENT '点赞数',
    comment_count INT DEFAULT 0 COMMENT '评论数',
    is_top TINYINT DEFAULT 0 COMMENT '是否置顶',
    status TINYINT DEFAULT 1 COMMENT '状态 0-审核中 1-正常 2-已删除',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    INDEX idx_user_id (user_id),
    INDEX idx_circle_id (circle_id),
    INDEX idx_status (status),
    INDEX idx_is_top (is_top)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='帖子表';

-- 评论表
DROP TABLE IF EXISTS sys_comment;
CREATE TABLE sys_comment (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    post_id BIGINT NOT NULL COMMENT '帖子ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    parent_id BIGINT DEFAULT 0 COMMENT '父评论ID 0-顶级评论',
    content TEXT NOT NULL COMMENT '内容',
    like_count INT DEFAULT 0 COMMENT '点赞数',
    status TINYINT DEFAULT 1 COMMENT '状态',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    INDEX idx_post_id (post_id),
    INDEX idx_user_id (user_id),
    INDEX idx_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评论表';

-- 关注表
DROP TABLE IF EXISTS sys_follow;
CREATE TABLE sys_follow (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    follow_user_id BIGINT NOT NULL COMMENT '被关注用户ID',
    status TINYINT DEFAULT 1 COMMENT '状态',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    UNIQUE KEY uk_user_follow (user_id, follow_user_id),
    INDEX idx_user_id (user_id),
    INDEX idx_follow_user_id (follow_user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='关注表';

-- 消息表
DROP TABLE IF EXISTS sys_message;
CREATE TABLE sys_message (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    from_user_id BIGINT NULL COMMENT '发送者ID，系统通知可为NULL',
    to_user_id BIGINT NOT NULL COMMENT '接收者ID',
    content TEXT NOT NULL COMMENT '内容',
    is_read TINYINT DEFAULT 0 COMMENT '是否已读 0-否 1-是',
    status TINYINT DEFAULT 1 COMMENT '状态',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    INDEX idx_from_user (from_user_id),
    INDEX idx_to_user (to_user_id),
    INDEX idx_is_read (is_read)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息表';

-- 订单表
DROP TABLE IF EXISTS sys_order;
CREATE TABLE sys_order (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_no VARCHAR(50) NOT NULL UNIQUE COMMENT '订单号',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    course_id BIGINT NOT NULL COMMENT '课程ID',
    amount DECIMAL(10,2) NOT NULL COMMENT '金额',
    pay_type TINYINT COMMENT '支付方式 1-支付宝 2-微信',
    pay_time DATETIME COMMENT '支付时间',
    status TINYINT DEFAULT 0 COMMENT '状态 0-未支付 1-已支付 2-已退款',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    INDEX idx_order_no (order_no),
    INDEX idx_user_id (user_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- 订阅表
DROP TABLE IF EXISTS sys_subscription;
CREATE TABLE sys_subscription (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    category VARCHAR(50) COMMENT '订阅分类',
    tags VARCHAR(200) COMMENT '订阅标签',
    status TINYINT DEFAULT 1 COMMENT '状态 0-取消 1-订阅中',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    UNIQUE KEY uk_user_category (user_id, category),
    INDEX idx_user_id (user_id),
    INDEX idx_category (category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订阅表';

-- 通知表
DROP TABLE IF EXISTS sys_notification;
CREATE TABLE sys_notification (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    title VARCHAR(200) NOT NULL COMMENT '标题',
    content TEXT COMMENT '内容',
    type VARCHAR(20) COMMENT '类型 news/resource/course/system',
    related_id BIGINT COMMENT '关联ID',
    is_read TINYINT DEFAULT 0 COMMENT '是否已读 0-未读 1-已读',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    INDEX idx_user_id (user_id),
    INDEX idx_is_read (is_read),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通知表';

-- 资源评论表
DROP TABLE IF EXISTS sys_resource_comment;
CREATE TABLE sys_resource_comment (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    resource_id BIGINT NOT NULL COMMENT '资源ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    parent_id BIGINT DEFAULT 0 COMMENT '父评论ID 0-顶级评论',
    content TEXT NOT NULL COMMENT '评论内容',
    rating TINYINT COMMENT '评分 1-5',
    like_count INT DEFAULT 0 COMMENT '点赞数',
    status TINYINT DEFAULT 1 COMMENT '状态 0-隐藏 1-显示',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    INDEX idx_resource_id (resource_id),
    INDEX idx_user_id (user_id),
    INDEX idx_parent_id (parent_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资源评论表';

-- 圈子成员表
DROP TABLE IF EXISTS sys_circle_member;
CREATE TABLE sys_circle_member (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    circle_id BIGINT NOT NULL COMMENT '圈子ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    join_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
    status TINYINT DEFAULT 1 COMMENT '状态',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    UNIQUE KEY uk_circle_user (circle_id, user_id),
    INDEX idx_circle_id (circle_id),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='圈子成员表';

-- 举报表
DROP TABLE IF EXISTS sys_report;
CREATE TABLE sys_report (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '举报人ID',
    target_type TINYINT NOT NULL COMMENT '举报对象类型 1-帖子 2-评论 3-用户',
    target_id BIGINT NOT NULL COMMENT '举报对象ID',
    reason VARCHAR(500) NOT NULL COMMENT '举报原因',
    status TINYINT DEFAULT 0 COMMENT '状态 0-待处理 1-已处理 2-驳回',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    INDEX idx_user_id (user_id),
    INDEX idx_target_type (target_type),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='举报表';

-- 课程评论表
DROP TABLE IF EXISTS sys_course_comment;
CREATE TABLE sys_course_comment (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    course_id BIGINT NOT NULL COMMENT '课程ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    content TEXT NOT NULL COMMENT '评论内容',
    rating TINYINT COMMENT '评分 1-5',
    parent_id BIGINT COMMENT '父评论ID，用于回复',
    like_count INT DEFAULT 0 COMMENT '点赞数',
    status TINYINT DEFAULT 1 COMMENT '状态 0-隐藏 1-显示',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    INDEX idx_course_id (course_id),
    INDEX idx_user_id (user_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程评论表';

-- 课程分类表
DROP TABLE IF EXISTS sys_course_category;
CREATE TABLE sys_course_category (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL COMMENT '分类名称',
    parent_id BIGINT DEFAULT 0 COMMENT '父分类ID 0-一级分类',
    sort INT DEFAULT 0 COMMENT '排序',
    status TINYINT DEFAULT 1 COMMENT '状态 0-禁用 1-正常',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    INDEX idx_parent_id (parent_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程分类表';

-- 下载历史表
DROP TABLE IF EXISTS sys_download_history;
CREATE TABLE sys_download_history (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    resource_id BIGINT NOT NULL COMMENT '资源ID',
    resource_title VARCHAR(200) NOT NULL COMMENT '资源标题',
    resource_category VARCHAR(50) COMMENT '资源分类',
    download_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '下载时间',
    points_consumed INT DEFAULT 0 COMMENT '消耗积分',
    points_record_id BIGINT COMMENT '积分记录ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    INDEX idx_user_id (user_id),
    INDEX idx_resource_id (resource_id),
    INDEX idx_download_time (download_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='下载历史表';

-- 用户行为日志表
DROP TABLE IF EXISTS sys_user_behavior_log;
CREATE TABLE sys_user_behavior_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    action_type VARCHAR(50) NOT NULL COMMENT '行为类型',
    target_type VARCHAR(50) NOT NULL COMMENT '目标类型',
    target_id BIGINT COMMENT '目标ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    INDEX idx_user_id (user_id),
    INDEX idx_action_type (action_type),
    INDEX idx_target_type (target_type),
    INDEX idx_target_id (target_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户行为日志表';

-- 积分记录表
DROP TABLE IF EXISTS sys_points_record;
CREATE TABLE sys_points_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    points INT NOT NULL COMMENT '积分数量',
    type VARCHAR(20) NOT NULL COMMENT '类型 income-收入 expense-支出',
    description VARCHAR(200) COMMENT '描述',
    related_id BIGINT COMMENT '关联ID',
    related_type VARCHAR(50) COMMENT '关联类型',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    INDEX idx_user_id (user_id),
    INDEX idx_type (type),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='积分记录表';

-- 直播表
DROP TABLE IF EXISTS sys_live_stream;
CREATE TABLE sys_live_stream (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    course_id BIGINT COMMENT '关联课程ID',
    title VARCHAR(200) NOT NULL COMMENT '直播标题',
    description TEXT COMMENT '直播描述',
    cover VARCHAR(500) COMMENT '封面图',
    stream_url VARCHAR(500) COMMENT '推流地址',
    play_url VARCHAR(500) COMMENT '播放地址',
    teacher_name VARCHAR(50) COMMENT '讲师名称',
    start_time DATETIME COMMENT '开始时间',
    end_time DATETIME COMMENT '结束时间',
    viewer_count INT DEFAULT 0 COMMENT '观看人数',
    status TINYINT DEFAULT 0 COMMENT '状态 0-未开始 1-直播中 2-已结束',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    INDEX idx_course_id (course_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='直播表';

-- 弹幕表
DROP TABLE IF EXISTS sys_danmaku;
CREATE TABLE sys_danmaku (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    live_stream_id BIGINT NOT NULL COMMENT '直播ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    content VARCHAR(200) NOT NULL COMMENT '弹幕内容',
    color VARCHAR(20) DEFAULT '#FFFFFF' COMMENT '颜色',
    send_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    INDEX idx_live_stream_id (live_stream_id),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='弹幕表';

-- ===================== 外键约束 =====================
ALTER TABLE sys_news ADD CONSTRAINT fk_news_author FOREIGN KEY (author_id) REFERENCES sys_user(id) ON DELETE SET NULL;
ALTER TABLE sys_resource ADD CONSTRAINT fk_resource_uploader FOREIGN KEY (uploader_id) REFERENCES sys_user(id) ON DELETE SET NULL;
ALTER TABLE sys_course ADD CONSTRAINT fk_course_teacher FOREIGN KEY (teacher_id) REFERENCES sys_user(id) ON DELETE SET NULL;
ALTER TABLE sys_course_chapter ADD CONSTRAINT fk_chapter_course FOREIGN KEY (course_id) REFERENCES sys_course(id) ON DELETE CASCADE;
ALTER TABLE sys_user_course ADD CONSTRAINT fk_uc_user FOREIGN KEY (user_id) REFERENCES sys_user(id) ON DELETE CASCADE;
ALTER TABLE sys_user_course ADD CONSTRAINT fk_uc_course FOREIGN KEY (course_id) REFERENCES sys_course(id) ON DELETE CASCADE;
ALTER TABLE sys_study_record ADD CONSTRAINT fk_study_user FOREIGN KEY (user_id) REFERENCES sys_user(id) ON DELETE CASCADE;
ALTER TABLE sys_todo ADD CONSTRAINT fk_todo_user FOREIGN KEY (user_id) REFERENCES sys_user(id) ON DELETE CASCADE;
ALTER TABLE sys_circle ADD CONSTRAINT fk_circle_creator FOREIGN KEY (creator_id) REFERENCES sys_user(id) ON DELETE SET NULL;
ALTER TABLE sys_post ADD CONSTRAINT fk_post_user FOREIGN KEY (user_id) REFERENCES sys_user(id) ON DELETE CASCADE;
ALTER TABLE sys_post ADD CONSTRAINT fk_post_circle FOREIGN KEY (circle_id) REFERENCES sys_circle(id) ON DELETE SET NULL;
ALTER TABLE sys_comment ADD CONSTRAINT fk_comment_post FOREIGN KEY (post_id) REFERENCES sys_post(id) ON DELETE CASCADE;
ALTER TABLE sys_comment ADD CONSTRAINT fk_comment_user FOREIGN KEY (user_id) REFERENCES sys_user(id) ON DELETE CASCADE;
ALTER TABLE sys_follow ADD CONSTRAINT fk_follow_user FOREIGN KEY (user_id) REFERENCES sys_user(id) ON DELETE CASCADE;
ALTER TABLE sys_follow ADD CONSTRAINT fk_follow_target FOREIGN KEY (follow_user_id) REFERENCES sys_user(id) ON DELETE CASCADE;
ALTER TABLE sys_message ADD CONSTRAINT fk_msg_from FOREIGN KEY (from_user_id) REFERENCES sys_user(id) ON DELETE SET NULL;
ALTER TABLE sys_message ADD CONSTRAINT fk_msg_to FOREIGN KEY (to_user_id) REFERENCES sys_user(id) ON DELETE CASCADE;
ALTER TABLE sys_order ADD CONSTRAINT fk_order_user FOREIGN KEY (user_id) REFERENCES sys_user(id) ON DELETE CASCADE;
ALTER TABLE sys_order ADD CONSTRAINT fk_order_course FOREIGN KEY (course_id) REFERENCES sys_course(id) ON DELETE CASCADE;
ALTER TABLE sys_subscription ADD CONSTRAINT fk_sub_user FOREIGN KEY (user_id) REFERENCES sys_user(id) ON DELETE CASCADE;
ALTER TABLE sys_notification ADD CONSTRAINT fk_notif_user FOREIGN KEY (user_id) REFERENCES sys_user(id) ON DELETE CASCADE;
ALTER TABLE sys_resource_comment ADD CONSTRAINT fk_rc_resource FOREIGN KEY (resource_id) REFERENCES sys_resource(id) ON DELETE CASCADE;
ALTER TABLE sys_resource_comment ADD CONSTRAINT fk_rc_user FOREIGN KEY (user_id) REFERENCES sys_user(id) ON DELETE CASCADE;
ALTER TABLE sys_course_comment ADD CONSTRAINT fk_cc_course FOREIGN KEY (course_id) REFERENCES sys_course(id) ON DELETE CASCADE;
ALTER TABLE sys_course_comment ADD CONSTRAINT fk_cc_user FOREIGN KEY (user_id) REFERENCES sys_user(id) ON DELETE CASCADE;
ALTER TABLE sys_download_history ADD CONSTRAINT fk_dh_user FOREIGN KEY (user_id) REFERENCES sys_user(id) ON DELETE CASCADE;
ALTER TABLE sys_download_history ADD CONSTRAINT fk_dh_resource FOREIGN KEY (resource_id) REFERENCES sys_resource(id) ON DELETE CASCADE;
ALTER TABLE sys_report ADD CONSTRAINT fk_report_user FOREIGN KEY (user_id) REFERENCES sys_user(id) ON DELETE CASCADE;
ALTER TABLE sys_points_record ADD CONSTRAINT fk_pr_user FOREIGN KEY (user_id) REFERENCES sys_user(id) ON DELETE CASCADE;
ALTER TABLE sys_user_behavior_log ADD CONSTRAINT fk_ubl_user FOREIGN KEY (user_id) REFERENCES sys_user(id) ON DELETE CASCADE;
ALTER TABLE sys_live_stream ADD CONSTRAINT fk_ls_course FOREIGN KEY (course_id) REFERENCES sys_course(id) ON DELETE SET NULL;
ALTER TABLE sys_danmaku ADD CONSTRAINT fk_danmaku_live FOREIGN KEY (live_stream_id) REFERENCES sys_live_stream(id) ON DELETE CASCADE;
ALTER TABLE sys_danmaku ADD CONSTRAINT fk_danmaku_user FOREIGN KEY (user_id) REFERENCES sys_user(id) ON DELETE CASCADE;
ALTER TABLE sys_circle_member ADD CONSTRAINT fk_cm_circle FOREIGN KEY (circle_id) REFERENCES sys_circle(id) ON DELETE CASCADE;
ALTER TABLE sys_circle_member ADD CONSTRAINT fk_cm_user FOREIGN KEY (user_id) REFERENCES sys_user(id) ON DELETE CASCADE;

-- ===================== 考研核心新增表 =====================
-- 院校信息表
DROP TABLE IF EXISTS sys_school;
CREATE TABLE sys_school (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL COMMENT '院校名称',
    logo VARCHAR(500) COMMENT '校徽',
    type VARCHAR(50) COMMENT '类型 985/211/双一流/普通',
    province VARCHAR(50) COMMENT '省份',
    city VARCHAR(50) COMMENT '城市',
    website VARCHAR(200) COMMENT '官网',
    introduction TEXT COMMENT '院校简介',
    ranking INT DEFAULT 0 COMMENT '综合排名',
    is_self_drawing TINYINT DEFAULT 0 COMMENT '是否自主划线 0-否 1-是',
    affiliation VARCHAR(100) COMMENT '隶属部门（如：教育部/工业和信息化部/省属）',
    master_points INT DEFAULT 0 COMMENT '硕士点数量',
    doctor_points INT DEFAULT 0 COMMENT '博士点数量',
    key_disciplines TEXT COMMENT '重点学科（JSON或逗号分隔）',
    departments TEXT COMMENT '院系设置',
    established_year INT COMMENT '建校年份',
    status TINYINT DEFAULT 1,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    INDEX idx_type (type),
    INDEX idx_province (province)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='院校信息表';

-- 院校分数线表
DROP TABLE IF EXISTS sys_school_score_line;
CREATE TABLE sys_school_score_line (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    school_id BIGINT NOT NULL COMMENT '院校ID',
    year INT NOT NULL COMMENT '年份',
    major VARCHAR(100) NOT NULL COMMENT '专业',
    major_code VARCHAR(20) COMMENT '专业代码',
    degree_type VARCHAR(20) DEFAULT '硕士' COMMENT '学位类型 硕士/博士',
    total_score INT NOT NULL COMMENT '总分线',
    political_score INT COMMENT '政治线',
    english_score INT COMMENT '英语线',
    course1_score INT COMMENT '业务课1线',
    course2_score INT COMMENT '业务课2线',
    admit_count INT COMMENT '录取人数',
    applicant_count INT COMMENT '报考人数',
    retest_rate DECIMAL(5,2) COMMENT '复试比例',
    source_url VARCHAR(500) COMMENT '来源链接',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    INDEX idx_school_id (school_id),
    INDEX idx_year (year),
    INDEX idx_major (major)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='院校分数线表';

-- 备考计划模板
DROP TABLE IF EXISTS sys_plan_template;
CREATE TABLE sys_plan_template (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(200) NOT NULL COMMENT '模板标题',
    description TEXT COMMENT '模板描述',
    stage VARCHAR(50) COMMENT '阶段 基础/强化/冲刺',
    total_days INT DEFAULT 0 COMMENT '总天数',
    subjects VARCHAR(200) COMMENT '涵盖科目',
    template_data JSON COMMENT '模板详细数据',
    use_count INT DEFAULT 0 COMMENT '使用次数',
    status TINYINT DEFAULT 1,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='备考计划模板';

-- 调剂信息
DROP TABLE IF EXISTS sys_adjustment_info;
CREATE TABLE sys_adjustment_info (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    school_name VARCHAR(100) NOT NULL COMMENT '院校名称',
    major VARCHAR(200) NOT NULL COMMENT '专业',
    requirement TEXT COMMENT '调剂要求',
    contact VARCHAR(500) COMMENT '联系方式',
    source_url VARCHAR(500) COMMENT '来源链接',
    year INT NOT NULL COMMENT '年份',
    publish_date DATE COMMENT '发布日期',
    status TINYINT DEFAULT 1 COMMENT '0-过期 1-有效',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    INDEX idx_year (year),
    INDEX idx_school_name (school_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='调剂信息表';

-- 课程笔记
DROP TABLE IF EXISTS sys_course_note;
CREATE TABLE sys_course_note (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    course_id BIGINT NOT NULL,
    chapter_id BIGINT COMMENT '章节ID',
    video_time INT DEFAULT 0 COMMENT '视频时间戳(秒)',
    content TEXT NOT NULL COMMENT '笔记内容',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    INDEX idx_user_course (user_id, course_id),
    INDEX idx_chapter_id (chapter_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程笔记';

ALTER TABLE sys_school_score_line ADD CONSTRAINT fk_score_school FOREIGN KEY (school_id) REFERENCES sys_school(id) ON DELETE CASCADE;
ALTER TABLE sys_course_note ADD CONSTRAINT fk_cn_user FOREIGN KEY (user_id) REFERENCES sys_user(id) ON DELETE CASCADE;
ALTER TABLE sys_course_note ADD CONSTRAINT fk_cn_course FOREIGN KEY (course_id) REFERENCES sys_course(id) ON DELETE CASCADE;
ALTER TABLE sys_post ADD COLUMN post_type TINYINT DEFAULT 1 COMMENT '帖子类型 1-普通 2-经验 3-答疑 4-资料分享';

-- 国家线表（教育部划定的全国统一最低分数线）
DROP TABLE IF EXISTS sys_national_score_line;
CREATE TABLE sys_national_score_line (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    year INT NOT NULL COMMENT '年份',
    subject_category VARCHAR(50) NOT NULL COMMENT '学科门类（如：哲学、经济学、法学、工学等）',
    degree_type VARCHAR(20) DEFAULT '学术型' COMMENT '学位类型 学术型/专业型',
    total_score INT NOT NULL COMMENT '总分线',
    political_score INT COMMENT '政治线',
    english_score INT COMMENT '英语线',
    course1_score INT COMMENT '业务课1线',
    course2_score INT COMMENT '业务课2线',
    region_type VARCHAR(10) DEFAULT 'A' COMMENT '地区类型 A/B（A类：一般地区，B类：西部地区）',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    INDEX idx_year (year),
    INDEX idx_subject_category (subject_category),
    INDEX idx_region_type (region_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='全国硕士招生考试国家分数线';

-- ===================== 考研核心新增表 V2 =====================

-- 院校对比收藏
DROP TABLE IF EXISTS sys_user_school_compare;
CREATE TABLE sys_user_school_compare (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    school_ids JSON NOT NULL COMMENT '对比院校ID列表',
    note VARCHAR(500) COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='院校对比收藏';

-- 考研专业目录
DROP TABLE IF EXISTS sys_major_catalog;
CREATE TABLE sys_major_catalog (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(20) NOT NULL COMMENT '专业代码',
    name VARCHAR(100) NOT NULL COMMENT '专业名称',
    category VARCHAR(50) NOT NULL COMMENT '学科门类',
    first_level VARCHAR(100) COMMENT '一级学科',
    degree_type VARCHAR(20) DEFAULT '学术型' COMMENT '学位类型',
    description TEXT COMMENT '专业描述',
    exam_subjects JSON COMMENT '考试科目',
    employment TEXT COMMENT '就业方向',
    sort INT DEFAULT 0,
    status TINYINT DEFAULT 1,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    INDEX idx_code (code),
    INDEX idx_category (category),
    INDEX idx_first_level (first_level)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='考研专业目录';

-- 退款申请表
CREATE TABLE IF NOT EXISTS sys_refund_request (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    course_id BIGINT NOT NULL COMMENT '课程ID',
    order_id BIGINT COMMENT '订单ID',
    reason VARCHAR(500) COMMENT '退款理由',
    status TINYINT DEFAULT 0 COMMENT '状态 0-待审核 1-已通过 2-已驳回',
    reject_reason VARCHAR(500) COMMENT '驳回理由',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    INDEX idx_user_id (user_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='退款申请';

ALTER TABLE sys_user_school_compare ADD CONSTRAINT fk_usc_user FOREIGN KEY (user_id) REFERENCES sys_user(id) ON DELETE CASCADE;

SET FOREIGN_KEY_CHECKS = 1;
