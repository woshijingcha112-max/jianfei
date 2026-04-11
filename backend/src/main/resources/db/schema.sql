CREATE TABLE `t_user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `nickname` VARCHAR(50) DEFAULT '用户',
  `height_cm` DECIMAL(5,1) COMMENT '身高',
  `weight_kg` DECIMAL(5,1) COMMENT '当前体重',
  `target_weight` DECIMAL(5,1) COMMENT '目标体重',
  `daily_cal_limit` INT DEFAULT 1500 COMMENT '每日热量上限kcal',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
);

CREATE TABLE `t_diet_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `record_date` DATE NOT NULL,
  `meal_type` TINYINT COMMENT '1早 2午 3晚 4加餐',
  `photo_url` VARCHAR(255) COMMENT '图片访问路径',
  `total_calories` DECIMAL(7,1) COMMENT '本餐总热量kcal',
  `remark` VARCHAR(200),
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  INDEX `idx_user_date` (`user_id`, `record_date`)
);

CREATE TABLE `t_diet_item` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `record_id` BIGINT NOT NULL,
  `food_name` VARCHAR(100) NOT NULL COMMENT '食物名称',
  `weight_g` DECIMAL(7,1) COMMENT '预估重量g',
  `calories` DECIMAL(7,1) COMMENT '热量kcal',
  `tag_color` TINYINT COMMENT '1绿 2橙 3红',
  `is_confirmed` TINYINT DEFAULT 0 COMMENT '用户是否手动确认',
  PRIMARY KEY (`id`),
  INDEX `idx_record_id` (`record_id`)
);

CREATE TABLE `t_food_library` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `food_name` VARCHAR(100) NOT NULL COMMENT '食物名（中文）',
  `food_name_en` VARCHAR(100) COMMENT '英文名，用于AI识别结果匹配',
  `alias` VARCHAR(200) COMMENT '别名，逗号分隔',
  `category` VARCHAR(50) COMMENT '分类',
  `calories_kcal` DECIMAL(7,1) NOT NULL COMMENT '热量kcal/100g',
  `protein_g` DECIMAL(6,2) COMMENT '蛋白质g',
  `fat_g` DECIMAL(6,2) COMMENT '脂肪g',
  `carbs_g` DECIMAL(6,2) COMMENT '碳水g',
  `fiber_g` DECIMAL(6,2) COMMENT '膳食纤维g',
  `tag_color` TINYINT NOT NULL COMMENT '1绿 2橙 3红',
  `tag_reason` VARCHAR(100) COMMENT '简短提示',
  `edible_rate` DECIMAL(5,2) DEFAULT 100.00 COMMENT '可食部比例%',
  `common_unit` VARCHAR(50) COMMENT '常见单位',
  `data_source` VARCHAR(100) COMMENT '数据来源备注',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_name` (`food_name`),
  INDEX `idx_category` (`category`),
  FULLTEXT INDEX `ft_search` (`food_name`, `alias`)
);

CREATE TABLE `t_food_dish` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `dish_name` VARCHAR(100) NOT NULL COMMENT '菜品名',
  `description` VARCHAR(200) COMMENT '简单描述',
  `total_cal_per_portion` DECIMAL(7,1) COMMENT '每份参考热量kcal',
  PRIMARY KEY (`id`)
);

CREATE TABLE `t_dish_ingredient` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `dish_id` BIGINT NOT NULL,
  `food_id` BIGINT NOT NULL COMMENT '关联食物库',
  `food_name` VARCHAR(100) COMMENT '冗余存名称',
  `amount_desc` VARCHAR(50) COMMENT '用量描述',
  `calories` DECIMAL(7,1) COMMENT '该食材贡献热量kcal',
  PRIMARY KEY (`id`),
  INDEX `idx_dish_id` (`dish_id`)
);

CREATE TABLE `t_weight_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `weight_kg` DECIMAL(5,1) NOT NULL,
  `record_date` DATE NOT NULL,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
);

CREATE TABLE `t_period_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `start_date` DATE NOT NULL COMMENT '生理期开始日期',
  `end_date` DATE COMMENT '结束日期',
  `remark` VARCHAR(100),
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
);
