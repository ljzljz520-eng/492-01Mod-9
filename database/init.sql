SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- 创建数据库
CREATE DATABASE IF NOT EXISTS `scaffolding_db` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `scaffolding_db`;

-- 文件信息表
DROP TABLE IF EXISTS `file_info`;
CREATE TABLE `file_info` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `file_name` varchar(255) NOT NULL COMMENT '文件名称',
  `original_name` varchar(255) NOT NULL COMMENT '原始文件名',
  `file_path` varchar(500) NOT NULL COMMENT '文件路径',
  `file_size` bigint(20) DEFAULT '0' COMMENT '文件大小（字节）',
  `file_type` varchar(50) DEFAULT NULL COMMENT '文件类型',
  `file_extension` varchar(20) DEFAULT NULL COMMENT '文件扩展名',
  `upload_user_id` bigint(20) DEFAULT NULL COMMENT '上传人ID',
  `upload_user_name` varchar(50) DEFAULT NULL COMMENT '上传人姓名',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) DEFAULT '0' COMMENT '逻辑删除标识（0-未删除，1-已删除）',
  PRIMARY KEY (`id`),
  KEY `idx_file_type` (`file_type`),
  KEY `idx_upload_user_id` (`upload_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文件信息表';

-- 工作管理表
DROP TABLE IF EXISTS `work`;
CREATE TABLE `work` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `work_name` varchar(100) NOT NULL COMMENT '工作名称',
  `work_content` text COMMENT '工作内容',
  `work_status` varchar(20) DEFAULT 'pending' COMMENT '工作状态（pending-待处理，in_progress-进行中，completed-已完成，cancelled-已取消）',
  `work_time` datetime DEFAULT NULL COMMENT '工作时间',
  `start_time` datetime DEFAULT NULL COMMENT '开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '结束时间',
  `priority` varchar(20) DEFAULT 'normal' COMMENT '优先级（low-低，normal-普通，high-高，urgent-紧急）',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) DEFAULT '0' COMMENT '逻辑删除标识（0-未删除，1-已删除）',
  PRIMARY KEY (`id`),
  KEY `idx_work_status` (`work_status`),
  KEY `idx_work_time` (`work_time`),
  KEY `idx_priority` (`priority`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工作管理表';

-- 用户表
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` varchar(50) NOT NULL COMMENT '用户名（账号）',
  `password` varchar(100) NOT NULL COMMENT '密码（不加密）',
  `nickname` varchar(50) DEFAULT NULL COMMENT '昵称',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) DEFAULT '0' COMMENT '逻辑删除标识（0-未删除，1-已删除）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  KEY `idx_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 插入默认admin账号
INSERT INTO `user` (`username`, `password`, `nickname`) VALUES ('admin', '123456', '管理员');

-- 企业表
DROP TABLE IF EXISTS `enterprise`;
CREATE TABLE `enterprise` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `enterprise_name` varchar(100) NOT NULL COMMENT '企业名称',
  `contact_person` varchar(50) DEFAULT NULL COMMENT '联系人',
  `contact_phone` varchar(20) DEFAULT NULL COMMENT '联系电话',
  `address` varchar(500) DEFAULT NULL COMMENT '地址',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) DEFAULT '0' COMMENT '逻辑删除标识（0-未删除，1-已删除）',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='企业表';

-- 项目表
DROP TABLE IF EXISTS `project`;
CREATE TABLE `project` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `project_name` varchar(100) NOT NULL COMMENT '项目名称',
  `enterprise_id` bigint(20) NOT NULL COMMENT '所属企业ID',
  `project_address` varchar(500) DEFAULT NULL COMMENT '项目地址',
  `project_manager` varchar(50) DEFAULT NULL COMMENT '项目经理',
  `manager_phone` varchar(20) DEFAULT NULL COMMENT '经理电话',
  `start_date` date DEFAULT NULL COMMENT '开工日期',
  `end_date` date DEFAULT NULL COMMENT '竣工日期',
  `project_status` varchar(20) DEFAULT 'active' COMMENT '项目状态（active-进行中，completed-已完成，suspended-暂停）',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) DEFAULT '0' COMMENT '逻辑删除标识（0-未删除，1-已删除）',
  PRIMARY KEY (`id`),
  KEY `idx_enterprise_id` (`enterprise_id`),
  KEY `idx_project_status` (`project_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='项目表';

-- 工人表
DROP TABLE IF EXISTS `worker`;
CREATE TABLE `worker` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `worker_name` varchar(50) NOT NULL COMMENT '工人姓名',
  `id_card` varchar(18) NOT NULL COMMENT '身份证号',
  `phone` varchar(20) NOT NULL COMMENT '手机号码',
  `gender` varchar(10) DEFAULT NULL COMMENT '性别（male-男，female-女）',
  `birth_date` date DEFAULT NULL COMMENT '出生日期',
  `address` varchar(500) DEFAULT NULL COMMENT '家庭住址',
  `emergency_contact` varchar(50) DEFAULT NULL COMMENT '紧急联系人',
  `emergency_phone` varchar(20) DEFAULT NULL COMMENT '紧急联系电话',
  `enterprise_id` bigint(20) DEFAULT NULL COMMENT '所属企业ID',
  `project_id` bigint(20) DEFAULT NULL COMMENT '当前项目ID',
  `worker_status` varchar(20) DEFAULT 'on_job' COMMENT '工人状态（on_job-在岗，off_job-待离职，resigned-已离职，transferred-已转岗）',
  `hire_date` date DEFAULT NULL COMMENT '入职日期',
  `deposit_amount` decimal(10,2) DEFAULT '0.00' COMMENT '押金总额',
  `deposit_paid` tinyint(1) DEFAULT '0' COMMENT '押金是否已缴（0-未缴，1-已缴）',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) DEFAULT '0' COMMENT '逻辑删除标识（0-未删除，1-已删除）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_id_card` (`id_card`),
  KEY `idx_enterprise_id` (`enterprise_id`),
  KEY `idx_project_id` (`project_id`),
  KEY `idx_worker_status` (`worker_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工人表';

-- 装备表
DROP TABLE IF EXISTS `equipment`;
CREATE TABLE `equipment` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `equipment_type` varchar(50) NOT NULL COMMENT '装备类型（badge-工牌，helmet-安全帽，uniform-工服，toolkit-工具包，other-其他）',
  `equipment_name` varchar(100) NOT NULL COMMENT '装备名称',
  `equipment_code` varchar(50) DEFAULT NULL COMMENT '装备编号',
  `specification` varchar(200) DEFAULT NULL COMMENT '规格型号',
  `unit` varchar(20) DEFAULT '件' COMMENT '单位',
  `deposit_amount` decimal(10,2) DEFAULT '0.00' COMMENT '押金金额',
  `total_quantity` int(11) DEFAULT '0' COMMENT '总数量',
  `available_quantity` int(11) DEFAULT '0' COMMENT '可用数量',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) DEFAULT '0' COMMENT '逻辑删除标识（0-未删除，1-已删除）',
  PRIMARY KEY (`id`),
  KEY `idx_equipment_type` (`equipment_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='装备表';

-- 工人装备领用表
DROP TABLE IF EXISTS `worker_equipment`;
CREATE TABLE `worker_equipment` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `worker_id` bigint(20) NOT NULL COMMENT '工人ID',
  `equipment_id` bigint(20) NOT NULL COMMENT '装备ID',
  `equipment_type` varchar(50) NOT NULL COMMENT '装备类型',
  `equipment_name` varchar(100) NOT NULL COMMENT '装备名称',
  `quantity` int(11) DEFAULT '1' COMMENT '领用数量',
  `deposit_amount` decimal(10,2) DEFAULT '0.00' COMMENT '单件押金',
  `receive_date` date DEFAULT NULL COMMENT '领用日期',
  `receive_user_id` bigint(20) DEFAULT NULL COMMENT '发放人ID',
  `receive_user_name` varchar(50) DEFAULT NULL COMMENT '发放人姓名',
  `equipment_status` varchar(20) DEFAULT 'in_use' COMMENT '装备状态（in_use-使用中，returned-已归还，retained-转项目保留，lost-遗失，damaged-损坏）',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) DEFAULT '0' COMMENT '逻辑删除标识（0-未删除，1-已删除）',
  PRIMARY KEY (`id`),
  KEY `idx_worker_id` (`worker_id`),
  KEY `idx_equipment_id` (`equipment_id`),
  KEY `idx_equipment_status` (`equipment_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工人装备领用表';

-- 离职押金归还申请表
DROP TABLE IF EXISTS `resignation_application`;
CREATE TABLE `resignation_application` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `application_no` varchar(50) NOT NULL COMMENT '申请单号',
  `worker_id` bigint(20) NOT NULL COMMENT '工人ID',
  `worker_name` varchar(50) NOT NULL COMMENT '工人姓名',
  `current_project_id` bigint(20) NOT NULL COMMENT '当前项目ID',
  `current_project_name` varchar(100) NOT NULL COMMENT '当前项目名称',
  `enterprise_id` bigint(20) NOT NULL COMMENT '所属企业ID',
  `resignation_type` varchar(20) NOT NULL COMMENT '离职类型（resign-完全离职，transfer-企业内转项目）',
  `target_project_id` bigint(20) DEFAULT NULL COMMENT '目标项目ID（转项目时）',
  `target_project_name` varchar(100) DEFAULT NULL COMMENT '目标项目名称（转项目时）',
  `last_work_date` date DEFAULT NULL COMMENT '最后工作日',
  `total_deposit` decimal(10,2) DEFAULT '0.00' COMMENT '押金总额',
  `deduct_amount` decimal(10,2) DEFAULT '0.00' COMMENT '扣款总额',
  `refund_amount` decimal(10,2) DEFAULT '0.00' COMMENT '应退金额',
  `actual_refund_amount` decimal(10,2) DEFAULT '0.00' COMMENT '实退金额',
  `application_status` varchar(20) DEFAULT 'pending' COMMENT '申请状态（pending-待班组长检查，checked-班组长已检查，pending_finance-待财务审核，refunded-已退款，rejected-已驳回）',
  `team_leader_id` bigint(20) DEFAULT NULL COMMENT '班组长ID',
  `team_leader_name` varchar(50) DEFAULT NULL COMMENT '班组长姓名',
  `check_time` datetime DEFAULT NULL COMMENT '检查时间',
  `check_remark` varchar(1000) DEFAULT NULL COMMENT '检查备注',
  `finance_user_id` bigint(20) DEFAULT NULL COMMENT '财务审核人ID',
  `finance_user_name` varchar(50) DEFAULT NULL COMMENT '财务审核人姓名',
  `finance_audit_time` datetime DEFAULT NULL COMMENT '财务审核时间',
  `finance_remark` varchar(1000) DEFAULT NULL COMMENT '财务备注',
  `refund_time` datetime DEFAULT NULL COMMENT '退款时间',
  `refund_voucher` varchar(500) DEFAULT NULL COMMENT '退款凭证',
  `photo_ids` varchar(1000) DEFAULT NULL COMMENT '照片文件ID列表（逗号分隔）',
  `responsibility_note` text COMMENT '责任说明',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) DEFAULT '0' COMMENT '逻辑删除标识（0-未删除，1-已删除）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_application_no` (`application_no`),
  KEY `idx_worker_id` (`worker_id`),
  KEY `idx_current_project_id` (`current_project_id`),
  KEY `idx_application_status` (`application_status`),
  KEY `idx_resignation_type` (`resignation_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='离职押金归还申请表';

-- 装备归还明细表
DROP TABLE IF EXISTS `equipment_return`;
CREATE TABLE `equipment_return` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `resignation_id` bigint(20) NOT NULL COMMENT '离职申请ID',
  `worker_equipment_id` bigint(20) NOT NULL COMMENT '工人装备领用ID',
  `equipment_id` bigint(20) NOT NULL COMMENT '装备ID',
  `equipment_type` varchar(50) NOT NULL COMMENT '装备类型',
  `equipment_name` varchar(100) NOT NULL COMMENT '装备名称',
  `quantity` int(11) DEFAULT '1' COMMENT '数量',
  `deposit_amount` decimal(10,2) DEFAULT '0.00' COMMENT '押金金额',
  `return_status` varchar(20) NOT NULL COMMENT '归还状态（returned-已归还，retained-转项目保留，lost-遗失，damaged-损坏）',
  `return_condition` varchar(20) DEFAULT NULL COMMENT '归还状况（good-良好，damaged-损坏，serious_damage-严重损坏）',
  `deduct_amount` decimal(10,2) DEFAULT '0.00' COMMENT '扣款金额',
  `photo_ids` varchar(1000) DEFAULT NULL COMMENT '照片文件ID列表（逗号分隔）',
  `responsibility_note` text COMMENT '责任说明',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) DEFAULT '0' COMMENT '逻辑删除标识（0-未删除，1-已删除）',
  PRIMARY KEY (`id`),
  KEY `idx_resignation_id` (`resignation_id`),
  KEY `idx_worker_equipment_id` (`worker_equipment_id`),
  KEY `idx_return_status` (`return_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='装备归还明细表';

-- 插入默认装备数据
INSERT INTO `equipment` (`equipment_type`, `equipment_name`, `equipment_code`, `specification`, `unit`, `deposit_amount`, `total_quantity`, `available_quantity`, `remark`) VALUES
('badge', '工牌', 'EQP-BADGE-001', '标准规格', '个', 50.00, 100, 100, '工人身份标识'),
('helmet', '安全帽', 'EQP-HELMET-001', 'ABS材质，黄色', '顶', 100.00, 100, 100, '安全帽'),
('uniform', '工服', 'EQP-UNIFORM-001', '夏季短袖套装', '套', 200.00, 100, 100, '工作服套装'),
('toolkit', '工具包', 'EQP-TOOLKIT-001', '标准工具包', '个', 150.00, 100, 100, '工具收纳包');

-- 插入默认企业数据
INSERT INTO `enterprise` (`enterprise_name`, `contact_person`, `contact_phone`, `address`, `remark`) VALUES
('建工集团有限公司', '张总', '13800138000', '北京市朝阳区建国路88号', '主要合作企业');

-- 插入默认项目数据
INSERT INTO `project` (`project_name`, `enterprise_id`, `project_address`, `project_manager`, `manager_phone`, `start_date`, `project_status`, `remark`) VALUES
('朝阳商业中心项目', 1, '北京市朝阳区建国路100号', '李经理', '13900139000', '2024-01-01', 'active', '商业综合体项目'),
('海淀科技园项目', 1, '北京市海淀区中关村大街1号', '王经理', '13700137000', '2024-03-01', 'active', '科技园区项目');

SET FOREIGN_KEY_CHECKS = 1;
