CREATE TABLE IF NOT EXISTS `user_profile` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `nickname` VARCHAR(50) DEFAULT '用户',
  `height_cm` DECIMAL(5,1) COMMENT '身高',
  `weight_kg` DECIMAL(5,1) COMMENT '当前体重',
  `target_weight` DECIMAL(5,1) COMMENT '目标体重',
  `daily_cal_limit` INT DEFAULT 1500 COMMENT '每日热量上限kcal',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
);

CREATE TABLE IF NOT EXISTS `diet_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `record_date` DATE NOT NULL,
  `meal_type` TINYINT COMMENT '1早 2午 3晚 4加餐',
  `photo_url` VARCHAR(255) COMMENT '图片访问路径',
  `total_calories` DECIMAL(7,1) COMMENT '本餐总热量kcal',
  `remark` VARCHAR(200),
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  INDEX `idx_diet_record_user_date` (`user_id`, `record_date`)
);

CREATE TABLE IF NOT EXISTS `diet_item` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `record_id` BIGINT NOT NULL,
  `food_id` BIGINT COMMENT '关联食物库ID',
  `food_name` VARCHAR(100) NOT NULL COMMENT '食物名称',
  `weight_g` DECIMAL(7,1) COMMENT '预估重量g',
  `calories` DECIMAL(7,1) COMMENT '热量kcal',
  `tag_color` TINYINT COMMENT '1绿 2橙 3红',
  `is_confirmed` TINYINT DEFAULT 0 COMMENT '用户是否手动确认',
  PRIMARY KEY (`id`),
  INDEX `idx_diet_item_record_id` (`record_id`)
);

CREATE TABLE IF NOT EXISTS `food_library` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `food_name` VARCHAR(100) NOT NULL COMMENT '食物名（中文）',
  `food_name_en` VARCHAR(100) COMMENT '英文名，用于AI识别结果匹配',
  `alias` VARCHAR(200) COMMENT '别名，逗号分隔',
  `category` VARCHAR(50) COMMENT '分类',
  `calories_kcal` DECIMAL(7,1) NOT NULL COMMENT '热量kcal/100g',
  `tag_color` TINYINT NOT NULL COMMENT '1绿 2橙 3红',
  `tag_reason` VARCHAR(100) COMMENT '简短提示',
  `data_source` VARCHAR(100) COMMENT '数据来源备注',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_food_library_name` (`food_name`)
);

INSERT INTO `user_profile` (`id`, `nickname`, `height_cm`, `weight_kg`, `target_weight`, `daily_cal_limit`)
VALUES (1, '用户', 165.0, 70.0, 65.0, 1500)
ON DUPLICATE KEY UPDATE
  `nickname` = VALUES(`nickname`),
  `height_cm` = VALUES(`height_cm`),
  `weight_kg` = VALUES(`weight_kg`),
  `target_weight` = VALUES(`target_weight`),
  `daily_cal_limit` = VALUES(`daily_cal_limit`);

INSERT INTO `food_library`
(`id`, `food_name`, `food_name_en`, `alias`, `category`, `calories_kcal`, `tag_color`, `tag_reason`, `data_source`)
VALUES
  (1, '鸡蛋', 'egg', '水煮蛋,煎蛋', '蛋类', 144.0, 2, '蛋白质友好，注意烹调油', 'seed'),
  (2, '番茄', 'tomato', '西红柿', '蔬菜', 18.0, 1, '低热量蔬菜', 'seed'),
  (3, '炒饭', 'fried rice', '蛋炒饭,扬州炒饭', '主食', 188.0, 3, '油和主食叠加，热量偏高', 'seed'),
  (4, '鸡胸肉', 'chicken breast', '鸡胸', '肉类', 133.0, 2, '蛋白质较高', 'seed'),
  (5, '米饭', 'rice', '白米饭', '主食', 116.0, 2, '主食适量搭配', 'seed')
ON DUPLICATE KEY UPDATE
  `food_name` = VALUES(`food_name`),
  `food_name_en` = VALUES(`food_name_en`),
  `alias` = VALUES(`alias`),
  `category` = VALUES(`category`),
  `calories_kcal` = VALUES(`calories_kcal`),
  `tag_color` = VALUES(`tag_color`),
  `tag_reason` = VALUES(`tag_reason`),
  `data_source` = VALUES(`data_source`);
