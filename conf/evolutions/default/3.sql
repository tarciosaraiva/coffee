# Setting schema

# --- !Ups

CREATE TABLE `setting` (
  `id`    INT(11) UNSIGNED NOT NULL AUTO_INCREMENT,
  `key`   VARCHAR(255)     NOT NULL,
  `value` LONGTEXT         NOT NULL,
  PRIMARY KEY (`id`)
);

CREATE INDEX `idx_setting_key` ON `setting` (`key`);

# --- !Downs

DROP TABLE IF EXISTS `setting`;
DROP INDEX `idx_setting_key` ON `setting`;
