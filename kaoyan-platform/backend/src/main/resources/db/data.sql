-- ============================================
-- 考研学习与社区平台 完整测试数据
-- 所有用户密码：123456 (BCrypt)
-- ============================================
USE kaoyan_platform;
SET FOREIGN_KEY_CHECKS = 0;

DELETE FROM sys_comment WHERE 1=1;
DELETE FROM sys_post WHERE 1=1;
DELETE FROM sys_circle_member WHERE 1=1;
DELETE FROM sys_circle WHERE 1=1;
DELETE FROM sys_course_chapter WHERE 1=1;
DELETE FROM sys_course_comment WHERE 1=1;
DELETE FROM sys_course WHERE 1=1;
DELETE FROM sys_resource_comment WHERE 1=1;
DELETE FROM sys_resource WHERE 1=1;
DELETE FROM sys_news WHERE 1=1;
DELETE FROM sys_follow WHERE 1=1;
DELETE FROM sys_message WHERE 1=1;
DELETE FROM sys_order WHERE 1=1;
DELETE FROM sys_subscription WHERE 1=1;
DELETE FROM sys_notification WHERE 1=1;
DELETE FROM sys_study_record WHERE 1=1;
DELETE FROM sys_todo WHERE 1=1;
DELETE FROM sys_report WHERE 1=1;
DELETE FROM sys_user_course WHERE 1=1;
DELETE FROM sys_user WHERE 1=1;

ALTER TABLE sys_user AUTO_INCREMENT = 1;
ALTER TABLE sys_news AUTO_INCREMENT = 1;
ALTER TABLE sys_resource AUTO_INCREMENT = 1;
ALTER TABLE sys_course AUTO_INCREMENT = 1;
ALTER TABLE sys_course_chapter AUTO_INCREMENT = 1;
ALTER TABLE sys_post AUTO_INCREMENT = 1;
ALTER TABLE sys_comment AUTO_INCREMENT = 1;
ALTER TABLE sys_circle AUTO_INCREMENT = 1;

-- ============================================
-- 1. 用户
-- ============================================
INSERT INTO sys_user (username, password, nickname, avatar, email, phone, gender, status, role, points, target_school, target_major, bio, create_time) VALUES
('admin', '$2a$10$63HGCacEMGfnbzyqGVQU0u0wIiV2IzK6qL45CvgDOLU4VcQV3dWQi', '管理员', null, 'admin@kaoyan.com', null, 0, 1, 'ADMIN', 9999, null, null, null, NOW()),
('zhangsan', '$2a$10$63HGCacEMGfnbzyqGVQU0u0wIiV2IzK6qL45CvgDOLU4VcQV3dWQi', '张三', 'https://api.dicebear.com/7.x/avataaars/svg?seed=zhangsan', 'zhangsan@qq.com', '13800138001', 1, 1, 'USER', 100, '清华大学', '计算机科学与技术', '2026考研党，目标清华计算机，加油！', DATE_SUB(NOW(), INTERVAL 30 DAY)),
('lisi', '$2a$10$63HGCacEMGfnbzyqGVQU0u0wIiV2IzK6qL45CvgDOLU4VcQV3dWQi', '李四', 'https://api.dicebear.com/7.x/avataaars/svg?seed=lisi', 'lisi@qq.com', '13800138002', 2, 1, 'USER', 150, '北京大学', '金融学', '北大金融我来啦！', DATE_SUB(NOW(), INTERVAL 25 DAY)),
('wangwu', '$2a$10$63HGCacEMGfnbzyqGVQU0u0wIiV2IzK6qL45CvgDOLU4VcQV3dWQi', '王五', 'https://api.dicebear.com/7.x/avataaars/svg?seed=wangwu', 'wangwu@qq.com', '13800138003', 1, 1, 'USER', 80, '复旦大学', '新闻传播学', '新闻传播是我的梦想', DATE_SUB(NOW(), INTERVAL 20 DAY)),
('zhaoliu', '$2a$10$63HGCacEMGfnbzyqGVQU0u0wIiV2IzK6qL45CvgDOLU4VcQV3dWQi', '赵六', 'https://api.dicebear.com/7.x/avataaars/svg?seed=zhaoliu', 'zhaoliu@qq.com', '13800138004', 2, 1, 'USER', 200, '浙江大学', '软件工程', '浙大软工冲鸭！', DATE_SUB(NOW(), INTERVAL 15 DAY)),
('sunqi', '$2a$10$63HGCacEMGfnbzyqGVQU0u0wIiV2IzK6qL45CvgDOLU4VcQV3dWQi', '孙七', 'https://api.dicebear.com/7.x/avataaars/svg?seed=sunqi', 'sunqi@qq.com', '13800138005', 1, 1, 'USER', 50, '南京大学', '法学', '法律人，法律魂', DATE_SUB(NOW(), INTERVAL 10 DAY)),
('zhouba', '$2a$10$63HGCacEMGfnbzyqGVQU0u0wIiV2IzK6qL45CvgDOLU4VcQV3dWQi', '周八', 'https://api.dicebear.com/7.x/avataaars/svg?seed=zhouba', 'zhouba@qq.com', '13800138006', 2, 1, 'USER', 120, '上海交通大学', '电子信息', '上交电信，未来可期', DATE_SUB(NOW(), INTERVAL 8 DAY)),
('wujiu', '$2a$10$63HGCacEMGfnbzyqGVQU0u0wIiV2IzK6qL45CvgDOLU4VcQV3dWQi', '吴九', 'https://api.dicebear.com/7.x/avataaars/svg?seed=wujiu', 'wujiu@qq.com', '13800138007', 1, 1, 'USER', 90, '中国科学技术大学', '物理学', '物理系学生一枚', DATE_SUB(NOW(), INTERVAL 5 DAY)),
('zhengshi', '$2a$10$63HGCacEMGfnbzyqGVQU0u0wIiV2IzK6qL45CvgDOLU4VcQV3dWQi', '郑十', 'https://api.dicebear.com/7.x/avataaars/svg?seed=zhengshi', 'zhengshi@qq.com', '13800138008', 2, 1, 'USER', 180, '华中科技大学', '机械工程', '机械工程考研党', DATE_SUB(NOW(), INTERVAL 3 DAY));

-- ============================================
-- 2. 资讯
-- ============================================
INSERT INTO sys_news (title, content, summary, cover, category, exam_stage, tags, author_id, view_count, like_count, is_top, status, create_time) VALUES
('教育部：2027年全国硕士研究生招生考试报名公告正式发布', '教育部正式发布《2027年全国硕士研究生招生工作管理规定》，2027年研招工作全面启动。网上报名时间为2026年10月15日至10月28日，每天9:00-22:00。预报名时间为2026年9月24日至9月27日，每天9:00-22:00。报名流程包括注册账号、填写考生信息、填写报考信息、上传证件照、网上缴费、网上确认等环节。应届生原则上应选择就读学校所在地省级教育招生考试机构指定的报考点。往届生应选择工作所在地或户籍所在地的报考点。一位考生只能保留一条有效报名信息，逾期不再补报。', '教育部正式公布2027年考研报名安排：10月15日-28日网上报名，9月24日-27日预报名', null, '报名通知', '报名报考', '考研报名,2027考研,教育部公告', 1, 3250, 189, 1, 1, DATE_SUB(NOW(), INTERVAL 2 DAY)),
('清华大学2027年计算机科学与技术专业硕士研究生招生简章', '清华大学计算机科学与技术系2027年硕士研究生招生工作已全面启动。学术型硕士（081200）：计算机系统结构、计算机软件与理论、计算机应用技术、计算机网络与信息安全。专业型硕士（085400）：人工智能、大数据工程、网络与信息安全、智能软件工程。学硕统考35人（推免85人），专硕统考40人（推免40人）。初试科目：政治、英语一、数学一、408计算机学科专业基础综合。复试含机试（20%）、专业综合面试（60%）、英语口语面试（20%）。近年报录比：学硕报考890人/录取35人≈25:1，专硕报考720人/录取40人=18:1。', '清华大学计算机系2027年招生简章发布：学硕35人+专硕40人，含研究方向/考试科目/报录比', null, '招生简章', '择校评估', '清华大学,计算机,招生简章,报录比', 1, 4300, 256, 1, 1, DATE_SUB(NOW(), INTERVAL 1 DAY)),
('考研数学基础阶段复习规划：从零基础到120+', '数学是拉开考研分数差距的关键科目。教材选择：高等数学（同济第七版）、线性代数（同济第六版）、概率论（浙大第四版）。四轮复习法：第一轮教材精读30天、第二轮视频+辅导书40天、第三轮专项突破30天、第四轮真题入门20天。常见误区：只看视频不做题、死磕偏题怪题、缺乏错题整理、基础阶段拖太久。', '从教材选择到四轮复习法，全面规划基础阶段数学备考', null, '备考指南', '基础复习', '数学,复习方法,备考攻略', 2, 2890, 267, 1, 1, DATE_SUB(NOW(), INTERVAL 3 DAY)),
('2027年考研政治大纲变动详解', '2027年考研政治大纲已发布。马原新增"习近平新时代中国特色社会主义思想的世界观和方法论"考点，毛中特更新"中国式现代化"相关理论，史纲增加"新时代十年伟大变革"内容。推荐资料：肖秀荣精讲精练+1000题+8套卷+4套卷，徐涛核心考案，腿姐冲刺背诵手册。', '2027考研政治大纲发布，马原新增考点，毛中特更新中国式现代化', null, '考试大纲', '强化提升', '政治大纲,考研政治,2027考研', 1, 3560, 312, 1, 1, DATE_SUB(NOW(), INTERVAL 4 DAY)),
('考研英语80分学姐经验：阅读+作文双突破方法', '2026年考研英语一85分经验分享。阅读理解：精读阶段每日1篇逐句翻译（3-6月）、强化阶段每日2篇限时18分钟（7-9月）、冲刺阶段整套模拟（10-12月）。作文：小作文整理10种书信模板，大作文整理三大主题框架。词汇：3月前完成第一轮每日100词，3-6月第二轮每日50词，7-12月第三轮每日30词。', '英语一85分学姐分享阅读理解+作文双突破方法', null, '经验分享', '基础复习', '英语,高分经验,阅读技巧,作文模板', 3, 5200, 489, 0, 1, DATE_SUB(NOW(), INTERVAL 5 DAY)),
('2024年全国硕士研究生招生考试国家线分析', '2024年考研国家线公布。哲学270（A类），经济学360，法学320，教育学340，文学370，工学285，管理学340。整体稳中有升。考研报名人数虽小幅下降但高分考生比例上升，专硕报考热度持续增长。', '2024年考研国家线分析：哲学270、经济学360、工学285', null, '国家线/复试线', '复试调剂', '国家线,分数线,考研趋势', 1, 4800, 398, 0, 1, DATE_SUB(NOW(), INTERVAL 6 DAY)),
('考研择校五步法：科学选择目标院校和专业', '科学择校五步法：第一步明确自身定位（本科层次、专业基础、学习能力、经济条件）；第二步确定目标区域（A区21省市/B区10省区）；第三步筛选目标院校（冲稳保策略）；第四步查报录比和分数线；第五步评估专业课难度（统考vs自命题）。', '从明确定位到评估专业课难度，科学择校五步法', null, '备考指南', '择校评估', '择校,报录比,院校选择', 2, 6100, 567, 0, 1, DATE_SUB(NOW(), INTERVAL 7 DAY)),
('考研复试全流程指南：从初试结束到拟录取', '复试时间线：2月中下旬出成绩→3月中旬国家线→3月下旬-4月复试→4-5月调剂。复试内容：专业课笔试（30-40%）、综合面试（40-50%）、英语口试（10-20%）。准备清单：个人简历、中英文自我介绍、本科成绩单、联系导师邮件模板、常见面试问题准备。', '详解复试时间线、内容权重、准备清单', null, '复试经验', '复试准备', '复试,面试技巧,联系导师', 4, 3400, 298, 0, 1, DATE_SUB(NOW(), INTERVAL 9 DAY)),
('考研调剂全攻略：抓住上岸的最后机会', '调剂条件：初试过国家线、调入专业与第一志愿相同或相近、初试科目相同或相近。时间节点：3月下旬调剂意向采集系统→4月上旬调剂服务系统→4-5月分批复试。策略：提前收集信息、A区考生可调B区、学硕可调专硕、关注新增硕士点。', '从调剂条件到策略技巧，全面掌握考研调剂', null, '调剂信息', '调剂录取', '调剂,考研调剂,上岸机会', 2, 2800, 245, 0, 1, DATE_SUB(NOW(), INTERVAL 10 DAY));

-- ============================================
-- 3. 资源
-- ============================================
INSERT INTO sys_resource (title, description, file_url, file_type, file_size, category, subject, tags, points, uploader_id, download_count, view_count, rating, rating_count, status) VALUES
('考研数学一历年真题及解析（2015-2026年合集）', '包含2015年至2026年共12年考研数学一真题，每道题附带详细答案解析、解题思路和考点分析。', 'https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf', 'PDF', 15240000, '历年真题', '数学', '真题,数学一,历年真题', 10, 1, 1328, 4250, 4.8, 156, 1),
('考研英语大纲词汇5500词（带音标+例句+艾宾浩斯复习表）', '严格按2027考研英语大纲整理5500高频词汇。每个单词标注国际音标、词性、中文释义、考研真题例句。附赠艾宾浩斯记忆曲线复习计划表。', 'https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf', 'PDF', 8560000, '复习笔记', '英语', '词汇,英语单词,高频词汇', 5, 2, 2512, 6100, 4.9, 289, 1),
('考研政治思维导图全科合集', '系统梳理考研政治五大部分知识框架，以思维导图形式呈现核心考点和逻辑关系。含马原原理图、毛中特时间轴、史纲大事件年表、思修法基体系图、时政热点汇总。', 'https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf', 'ZIP', 23100000, '思维导图', '政治', '思维导图,政治,知识点', 8, 1, 1256, 3890, 4.7, 142, 1),
('计算机408统考专业课复习笔记（四合一完整版）', '上岸清华学长整理的408四门课完整笔记。数据结构含算法代码实现，计组含CPU/存储器图解，操作系统含进程/内存管理思维导图，计算机网络含各层协议对比表。', 'https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf', 'PDF', 18900000, '复习笔记', '计算机408', '408,专业课,复习笔记', 15, 3, 1189, 2670, 4.9, 135, 1),
('考研数学公式速查手册（高数+线代+概率论便携版）', '汇总高等数学、线性代数、概率论所有必考公式，按章节分类，双栏排版方便查阅。含常用积分表、泰勒展开式、分布律速查。建议打印随身携带。', 'https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf', 'PDF', 5200000, '公式手册', '数学', '公式,数学,速查手册', 3, 2, 2423, 5560, 4.6, 278, 1),
('考研英语阅读理解真题精讲（2015-2026）', '精选近12年考研英语阅读理解真题，逐篇精讲。每篇文章含原文+全文翻译+题目解析+选项分析+长难句拆解+核心词汇标注。', 'https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf', 'PDF', 11200000, '历年真题', '英语', '真题,阅读理解,英语,精讲', 8, 3, 1876, 4200, 4.8, 167, 1),
('考研政治1000题错题本模板（Excel+PDF双版）', '专为肖秀荣《考研政治1000题》设计的系统化错题管理工具。Excel自动统计正确率、按章节分类、错题归档、薄弱分析。PDF打印版A4排版，左右分栏，单页可记录6道错题。', 'https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf', 'ZIP', 3800000, '学霸笔记', '政治', '错题本,1000题,政治', 5, 1, 5680, 9200, 4.9, 428, 1),
('考研数据结构算法代码手册（C++/Java双版本）', '408数据结构要求的所有算法代码实现，含C++和Java两种语言版本。涵盖线性表、栈、队列、树、图、查找、排序七大类算法，代码均经过编译验证。', 'https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf', 'PDF', 7200000, '复习笔记', '计算机408', '数据结构,算法,代码实现', 12, 3, 980, 2450, 4.9, 156, 1);

-- ============================================
-- 4. 课程
-- ============================================
INSERT INTO sys_course (title, description, cover, outline, highlights, target_audience, prerequisites, teacher_id, category_id, subject, stage, form, price, lesson_count, duration, student_count, rating, status) VALUES
('2026考研数学全程班（数一/数二/数三）', '本课程覆盖考研数学全部内容，从基础到强化再到冲刺，系统讲解高等数学、线性代数、概率统计。120课时系统讲解，名校数学博士亲授，8年考研辅导经验。', 'https://placehold.co/400x240/409EFF/ffffff?text=C', '函数极限与连续→一元函数微分学→一元函数积分学→多元函数微积分→无穷级数→常微分方程→线性代数→概率论与数理统计', '名校博士授课/120课时系统覆盖/习题+真题+模拟全阶段训练/专属答疑群24小时回复', '备战2027考研数学一/二/三的考生，基础薄弱需系统学习的同学', '具备大学本科高等数学、线性代数、概率论基础知识', 1, 'math', '数学', '基础', '视频', 299.00, 120, 3600, 1250, 4.9, 1),
('考研英语词汇+阅读+写作系统班', '词汇、阅读、写作三大模块系统讲解，采用科学的记忆方法和解题技巧。配套每日打卡和作业批改，全面提升英语能力。', 'https://placehold.co/400x240/409EFF/ffffff?text=C', '模块一：词汇突破（30课时）/模块二：阅读理解精讲（40课时）/模块三：完形填空技巧/模块四：新题型解析/模块五：翻译专项/模块六：写作高分模板', '英语专八名师授课/独创词根词缀记忆法/阅读三步解题法/作文模板+批改服务', '备战2027考研英语一/二的考生，词汇量不足、阅读速度慢的同学', '已通过CET-4，具备基本英语阅读能力', 2, 'english', '英语', '强化', '视频', 199.00, 120, 2880, 890, 4.8, 1),
('考研政治冲刺押题班', '考研政治名师团队精心打造的冲刺押题课程，全面覆盖马原、毛中特、史纲、思修法基、时政五大模块共15课时。前2课时免费试听。名师连续5年精准命中分析题方向。', 'https://placehold.co/400x240/409EFF/ffffff?text=C', '第一部分 马原（第1-5讲）/第二部分 毛中特（第6-8讲）/第三部分 史纲（第9-11讲）/第四部分 思修法基（第12-13讲）/第五部分 时政（第14讲）/第六部分 终极押题（第15讲）', '名师连续5年命中分析题/15课时完整覆盖政治全科/每课配套PDF讲义下载/时政专题深度解读/终极押题+万能模板', '已进入冲刺阶段（10-12月）的考生，目标分数65-75分', '已完成一轮政治基础知识学习', 1, 'political', '政治', '冲刺', '直播', 159.00, 15, 855, 2100, 4.7, 1),
('计算机408专业课全程班', '针对计算机408统考专业课，涵盖数据结构、计组、操作系统、网络四门课程。由清华计算机博士授课，180课时全面覆盖所有考点。', 'https://placehold.co/400x240/409EFF/ffffff?text=C', '数据结构（45课时）/计算机组成原理（40课时）/操作系统（35课时）/计算机网络（30课时）/真题精讲（20课时）/模拟冲刺（10课时）', '清华计算机博士亲授/180课时全面覆盖/配套代码实现+图解/近10年真题逐题精讲/6次全真模拟+成绩分析', '报考计算机408统考院校的考生，目标分数120+', '具备C/C++或Java编程基础，了解基本数据结构概念', 3, 'computer', '计算机408', '基础', '视频', 399.00, 180, 5400, 650, 4.9, 1),
('金融学考研专业课精讲', '系统讲解货币银行学、国际金融、投资学、公司理财等金融学核心课程。北大金融硕士+CFA持证人授课。', 'https://placehold.co/400x240/409EFF/ffffff?text=C', '货币银行学精讲/国际金融理论与实务/投资学核心概念/公司理财与估值/金融热点专题/名校真题解析', '北大金融硕士授课/100课时覆盖全部核心课程/金融热点专题讲解/计算题专项训练', '报考金融专硕或金融学学硕的考生，目标985/211院校', '具备基本经济学常识和数学基础', 4, 'finance', '金融学431', '强化', '视频', 349.00, 100, 3000, 420, 4.8, 1);

-- ============================================
-- 5. 课程章节（课程3含15章完整数据 + 配套文件）
-- ============================================
INSERT INTO sys_course_chapter (course_id, title, video_url, duration, sort, is_free, file_url, file_name, status) VALUES
-- 数学课程章节
(1, '第1讲：函数的概念与性质', 'https://media.w3.org/2010/05/sintel/trailer.mp4', 45, 1, 1, null, null, 1),
(1, '第2讲：极限的定义与性质', 'https://media.w3.org/2010/05/sintel/trailer.mp4', 50, 2, 1, null, null, 1),
(1, '第3讲：极限的计算方法', 'https://media.w3.org/2010/05/sintel/trailer.mp4', 55, 3, 0, null, null, 1),
(1, '第4讲：连续性及其应用', 'https://media.w3.org/2010/05/sintel/trailer.mp4', 48, 4, 0, null, null, 1),
(1, '第5讲：导数的概念与计算', 'https://media.w3.org/2010/05/sintel/trailer.mp4', 52, 5, 0, null, null, 1),
-- 英语课程章节
(2, '第1讲：考研英语词汇记忆法', 'https://media.w3.org/2010/05/sintel/trailer.mp4', 40, 1, 1, null, null, 1),
(2, '第2讲：高频词汇精讲（A-D）', 'https://media.w3.org/2010/05/sintel/trailer.mp4', 45, 2, 1, null, null, 1),
(2, '第3讲：阅读理解题型分类', 'https://media.w3.org/2010/05/sintel/trailer.mp4', 50, 3, 0, null, null, 1),
(2, '第4讲：阅读长难句解析', 'https://media.w3.org/2010/05/sintel/trailer.mp4', 48, 4, 0, null, null, 1),
-- 政治冲刺押题班 15章（data_checkpoint.sql 会重新插入完整15章，这里先放基础数据）
(3, '第1讲：马原核心考点串讲', 'https://media.w3.org/2010/05/sintel/trailer.mp4', 60, 1, 1, 'https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf', '马原哲学核心原理思维导图.pdf', 1),
(3, '第2讲：辩证法三大规律精讲', 'https://media.w3.org/2010/05/sintel/trailer.mp4', 65, 2, 1, 'https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf', '辩证法三大规律对比表.pdf', 1),
(3, '第3讲：认识论：实践与真理', 'https://media.w3.org/2010/05/sintel/trailer.mp4', 55, 3, 0, 'https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf', '认识论核心考点速记.pdf', 1),
(3, '第4讲：历史唯物主义原理', 'https://media.w3.org/2010/05/sintel/trailer.mp4', 50, 4, 0, 'https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf', '历史唯物主义框架图.pdf', 1),
(3, '第5讲：政治经济学核心考点', 'https://media.w3.org/2010/05/sintel/trailer.mp4', 55, 5, 0, 'https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf', '政经计算公式汇总.pdf', 1),
(3, '第6讲：时政热点分析', 'https://media.w3.org/2010/05/sintel/trailer.mp4', 50, 6, 0, null, null, 1),
-- 计算机课程章节
(4, '第1讲：数据结构基础概念', 'https://media.w3.org/2010/05/sintel/trailer.mp4', 45, 1, 1, null, null, 1),
(4, '第2讲：线性表与链表', 'https://media.w3.org/2010/05/sintel/trailer.mp4', 50, 2, 1, null, null, 1),
(4, '第3讲：栈和队列', 'https://media.w3.org/2010/05/sintel/trailer.mp4', 48, 3, 0, null, null, 1),
(4, '第4讲：树与二叉树', 'https://media.w3.org/2010/05/sintel/trailer.mp4', 55, 4, 0, null, null, 1),
-- 金融课程章节
(5, '第1讲：货币与货币制度', 'https://media.w3.org/2010/05/sintel/trailer.mp4', 40, 1, 1, null, null, 1),
(5, '第2讲：信用与利率', 'https://media.w3.org/2010/05/sintel/trailer.mp4', 45, 2, 0, null, null, 1);

-- ============================================
-- 6. 圈子
-- ============================================
INSERT INTO sys_circle (name, description, type, university, major, creator_id, member_count, post_count, sort, status) VALUES
('清华大学考研圈', '清华大学考研交流圈，分享备考经验、资料和信息。', '院校', '清华大学', NULL, 1, 2560, 890, 1, 1),
('北京大学考研圈', '北京大学考研专属圈子，交流学习心得，共享备考资源。', '院校', '北京大学', NULL, 2, 1890, 650, 2, 1),
('计算机考研圈', '计算机专业考研交流圈，涵盖408统考和自命题院校。', '专业', NULL, '计算机', 3, 3200, 1250, 3, 1),
('金融专硕圈', '金融专硕（MF）考研交流圈，分享院校信息和专业课资料。', '专业', NULL, '金融', 4, 1580, 520, 4, 1),
('数学一交流圈', '考研数学一专项交流圈，讨论难题、分享解题技巧。', '专业', NULL, '数学', 1, 2100, 780, 5, 1),
('复旦考研圈', '复旦大学考研交流圈，欢迎报考复旦的研友加入讨论。', '院校', '复旦大学', NULL, 3, 980, 340, 6, 1);

-- ============================================
-- 7. 帖子
-- ============================================
INSERT INTO sys_post (user_id, title, content, circle_id, tags, view_count, like_count, comment_count, is_top, status, post_type) VALUES
(2, '清华计算机考研经验分享：从双非到清华的逆袭之路', '基本情况：本科双非院校计算机专业，初试385分上岸清华计算机系。复习规划：3-6月基础阶段复习数学和英语，7-9月强化阶段专业课和数学同步，10-12月冲刺阶段真题训练。数学一定要重视基础，把教材概念理解透彻。', 1, '经验分享,清华计算机,上岸', 2560, 189, 45, 1, 1, 2),
(3, '【资料分享】我整理的408复习笔记，免费分享给大家', '备考期间整理了详细的408四门课笔记：数据结构重点算法代码实现、计组CPU/存储器核心概念图解、操作系统进程管理/内存管理思维导图、网络各层协议详解。需要的朋友私信我免费分享！', 3, '资料分享,408,笔记', 1890, 156, 32, 0, 1, 4),
(1, '考研数学每日一题：这道极限题你会做吗？', '求极限：lim(x→0) (sinx - x) / x³，这道题考察泰勒展开的应用，sinx的泰勒展开式为 x - x³/3! + x⁵/5! - ...，大家可以先尝试做一下。', 5, '数学,每日一题,极限', 1230, 89, 28, 0, 1, 3),
(4, '金融专硕择校建议：清北复交人难度分析', '金融专硕择校难度分析：第一梯队（最难）清华五道口、北大光华；第二梯队复旦经院、上交高金、人大财金；第三梯队央财、上财、对外经贸。建议根据自身实力和目标合理选择，不要盲目追求名校。', 4, '择校,金融专硕,院校分析', 1560, 112, 38, 1, 1, 1),
(5, '2026考研英语大纲词汇表（完整版）', '整理了2026考研英语大纲要求的全部词汇，按字母顺序排列，包含音标、词性、中文释义。完整版PDF可以私信我获取！', 5, '英语,词汇,大纲', 890, 67, 15, 0, 1, 4),
(3, '考研英语二和英语一的区别，选错可能浪费一年', '英语一>英语二难度。学硕一般考英语一，专硕很多考英语二（有例外！）。翻译：英语一句子翻译10分，英语二段落翻译15分。大作文：英语一图画作文20分，英语二图表作文15分。', 2, '英语一,英语二,区别', 2890, 189, 35, 0, 1, 3),
(6, '考研政治分析题答题模板（马原篇）', '马原分析题答题模板：1.辩证法类"上述材料体现了XX原理……XX原理告诉我们……材料中……因此我们应该……" 2.认识论类"实践是认识的基础……认识具有反复性无限性……" 3.历史唯物主义类"社会存在决定社会意识……人民群众是历史创造者……"', 5, '政治,分析题,答题模板', 4200, 398, 62, 0, 1, 4),
(2, '考研调剂上岸经验：从第一志愿失利到上岸211', '第一志愿某985差2分进复试，调剂上岸某211。经验：调剂系统开放前就筛选可能有名额的院校做成Excel表格、系统一开放就填报、电话联系目标院校研招办、多手准备、不要放弃。调剂比一志愿更需要主动出击。', 6, '调剂,上岸经验,211', 3700, 289, 56, 0, 1, 2);

-- ============================================
-- 8. 评论
-- ============================================
INSERT INTO sys_comment (post_id, user_id, parent_id, content, like_count, status) VALUES
(1, 3, 0, '这篇经验分享非常详细，对我帮助很大！', 25, 1),
(1, 4, 0, '请问专业课是怎么复习的？', 18, 1),
(1, 2, 2, '专业课我是从3月份开始的，主要看教材和视频课', 12, 1),
(2, 5, 0, '感谢分享资料，已经下载了', 15, 1),
(3, 1, 0, '这道题很经典，考察泰勒展开', 20, 1),
(3, 2, 0, '用洛必达法则也可以做', 10, 1),
(4, 3, 0, '择校分析很到位', 17, 1),
(5, 4, 0, '词汇表整理得很清晰', 14, 1),
(6, 6, 0, '终于搞清楚英一英二的区别了', 22, 1),
(7, 5, 0, '模板很实用！收藏了', 25, 1),
(8, 6, 0, '去年我也调剂了，深有同感', 18, 1),
(8, 3, 0, '请问调剂系统可以同时填几个学校？', 5, 1),
(8, 5, 3, '可以同时填3个平行志愿', 12, 1);

-- ============================================
-- 9. 关注
-- ============================================
INSERT INTO sys_follow (user_id, follow_user_id, status) VALUES
(2, 1, 1), (3, 2, 1), (4, 3, 1), (5, 4, 1), (6, 5, 1), (2, 3, 1), (3, 4, 1);

-- ============================================
-- 10. 消息
-- ============================================
INSERT INTO sys_message (from_user_id, to_user_id, content, is_read, status) VALUES
(1, 2, '恭喜你发布的帖子被设为精华帖！', 1, 1),
(2, 3, '你好，我对您分享的408笔记很感兴趣，能发给我一份吗？', 1, 1),
(3, 2, '好的，已经发送到你的邮箱了，请查收。', 1, 1),
(4, 5, '关于金融专硕择校，我还有一些问题想咨询你。', 0, 1),
(5, 1, '管理员您好，我想申请加入清华大学考研圈。', 0, 1);

-- ============================================
-- 11. 订单
-- ============================================
INSERT INTO sys_order (order_no, user_id, course_id, amount, pay_type, pay_time, status) VALUES
('ORD202601010001', 2, 1, 299.00, 1, DATE_SUB(NOW(), INTERVAL 10 DAY), 1),
('ORD202601010002', 3, 2, 199.00, 2, DATE_SUB(NOW(), INTERVAL 8 DAY), 1),
('ORD202601010003', 4, 4, 399.00, 1, DATE_SUB(NOW(), INTERVAL 5 DAY), 1),
('ORD202601010004', 5, 5, 349.00, 2, DATE_SUB(NOW(), INTERVAL 3 DAY), 1),
('ORD202601010005', 6, 3, 159.00, 1, DATE_SUB(NOW(), INTERVAL 1 DAY), 1);

-- ============================================
-- 12. 用户课程
-- ============================================
INSERT INTO sys_user_course (user_id, course_id, current_chapter_id, progress, study_duration, last_study_time, status) VALUES
(2, 1, 3, 30, 1200, DATE_SUB(NOW(), INTERVAL 1 DAY), 1),
(3, 2, 3, 25, 900, DATE_SUB(NOW(), INTERVAL 1 DAY), 1),
(4, 4, 4, 40, 1500, DATE_SUB(NOW(), INTERVAL 1 DAY), 1),
(5, 5, 2, 20, 800, DATE_SUB(NOW(), INTERVAL 1 DAY), 1),
(6, 3, 2, 15, 600, DATE_SUB(NOW(), INTERVAL 1 DAY), 1);

-- ============================================
-- 13. 学习记录
-- ============================================
INSERT INTO sys_study_record (user_id, study_date, duration, subject, content, type) VALUES
(2, DATE_SUB(CURDATE(), INTERVAL 1 DAY), 120, '数学', '复习高等数学极限部分', 1),
(2, DATE_SUB(CURDATE(), INTERVAL 2 DAY), 90, '英语', '背诵考研词汇', 2),
(2, DATE_SUB(CURDATE(), INTERVAL 3 DAY), 150, '数学', '练习线性代数题目', 1),
(3, DATE_SUB(CURDATE(), INTERVAL 1 DAY), 150, '计算机408', '学习数据结构链表部分', 1),
(3, DATE_SUB(CURDATE(), INTERVAL 2 DAY), 60, '政治', '学习马原基本原理', 2),
(3, DATE_SUB(CURDATE(), INTERVAL 3 DAY), 120, '计算机408', '操作系统进程管理复习', 1),
(4, DATE_SUB(CURDATE(), INTERVAL 1 DAY), 100, '金融学', '复习货币银行学', 1),
(4, DATE_SUB(CURDATE(), INTERVAL 2 DAY), 130, '金融学', '国际金融汇率理论学习', 1),
(5, DATE_SUB(CURDATE(), INTERVAL 1 DAY), 80, '英语', '做阅读理解真题', 2),
(5, DATE_SUB(CURDATE(), INTERVAL 2 DAY), 140, '政治', '学习史纲第四章', 3),
(6, DATE_SUB(CURDATE(), INTERVAL 1 DAY), 180, '计算机408', '数据结构算法题训练', 1),
(6, DATE_SUB(CURDATE(), INTERVAL 2 DAY), 120, '计算机408', '操作系统内存管理', 1),
(6, DATE_SUB(CURDATE(), INTERVAL 3 DAY), 100, '数学', '高数级数章节复习', 1),
(1, DATE_SUB(CURDATE(), INTERVAL 1 DAY), 120, '数学', '复习高等数学', 1),
(1, DATE_SUB(CURDATE(), INTERVAL 2 DAY), 90, '英语', '背诵词汇', 2),
(1, DATE_SUB(CURDATE(), INTERVAL 3 DAY), 150, '政治', '学习马原', 3),
(1, DATE_SUB(CURDATE(), INTERVAL 4 DAY), 100, '计算机408', '复习数据结构', 1),
(1, DATE_SUB(CURDATE(), INTERVAL 5 DAY), 80, '英语', '做阅读真题', 2),
(1, DATE_SUB(CURDATE(), INTERVAL 6 DAY), 110, '数学', '练习线代', 1),
(1, DATE_SUB(CURDATE(), INTERVAL 7 DAY), 95, '政治', '学习毛中特', 3);

-- ============================================
-- 14. 待办事项
-- ============================================
INSERT INTO sys_todo (user_id, title, description, type, target_date, priority, status) VALUES
(2, '完成数学基础阶段复习', '复习高等数学、线性代数、概率论基础知识点', 1, DATE_ADD(NOW(), INTERVAL 30 DAY), 2, 0),
(2, '每天背诵100个英语单词', '使用墨墨背单词APP', 2, DATE_ADD(NOW(), INTERVAL 1 DAY), 1, 1),
(2, '政治第一轮复习', '完成马原、毛中特、史纲、思修的第一轮复习', 1, DATE_ADD(NOW(), INTERVAL 45 DAY), 3, 0),
(3, '408专业课一轮复习', '完成数据结构、计组、操作系统、网络的第一轮复习', 1, DATE_ADD(NOW(), INTERVAL 45 DAY), 2, 0),
(3, '做一套数学真题', '模拟考试环境，限时完成', 2, DATE_ADD(NOW(), INTERVAL 1 DAY), 2, 0),
(4, '复习金融学专业课', '重点复习货币银行学和国际金融', 2, DATE_ADD(NOW(), INTERVAL 1 DAY), 1, 0),
(1, '完成数学基础阶段复习', '系统复习基础知识', 1, DATE_ADD(NOW(), INTERVAL 30 DAY), 2, 0),
(1, '每天背诵100个英语单词', '坚持每日打卡', 2, DATE_ADD(NOW(), INTERVAL 1 DAY), 1, 1),
(1, '做一套数学真题', '限时模拟', 2, DATE_ADD(NOW(), INTERVAL 1 DAY), 2, 0);

-- ============================================
-- 15. 订阅+通知+举报
-- ============================================
INSERT INTO sys_subscription (user_id, category, tags, status) VALUES
(2, '资讯', '考研报名,数学,英语', 1),
(3, '资源', '408,计算机,专业课', 1),
(4, '课程', '金融学,经济学', 1);

INSERT INTO sys_notification (user_id, title, content, type, related_id, is_read) VALUES
(2, '帖子被点赞', '您的帖子"清华计算机考研经验分享"被用户李四点赞了', 'news', 1, 1),
(3, '资源下载', '您上传的资源"408复习笔记"被下载了10次', 'resource', 4, 1),
(4, '课程更新', '您购买的课程"金融学考研专业课精讲"有新的章节更新', 'course', 5, 0);

INSERT INTO sys_report (user_id, target_type, target_id, reason, status) VALUES
(2, 1, 1, '帖子内容包含广告信息', 1),
(3, 2, 1, '评论内容不友善', 2);

SET FOREIGN_KEY_CHECKS = 1;
