DROP TABLE IF EXISTS `t_collection`;

CREATE TABLE `t_collection` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `col_model` json DEFAULT NULL,
  `col_id` varchar(100) GENERATED ALWAYS AS (json_unquote(json_extract(`col_model`,'$.id'))) VIRTUAL,
  `col_restype` varchar(100) GENERATED ALWAYS AS (json_unquote(json_extract(`col_model`,'$.restype'))) VIRTUAL,
  `col_origin` varchar(100) GENERATED ALWAYS AS (json_unquote(json_extract(`col_model`,'$.origin'))) VIRTUAL,
  `is_system` tinyint(1) GENERATED ALWAYS AS (json_unquote(json_extract(`col_model`,'$.isSystem'))) VIRTUAL,
  `name` varchar(100) GENERATED ALWAYS AS (json_unquote(json_extract(`col_model`,'$.name'))) VIRTUAL,
  PRIMARY KEY (`id`),
  KEY `col_id` (`col_id`),
  KEY `col_restype` (`col_restype`),
  KEY `col_origin` (`col_origin`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `t_restype`;

CREATE TABLE `t_restype` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `restype_model` json DEFAULT NULL,
  `restype_id` varchar(100) GENERATED ALWAYS AS (json_unquote(json_extract(`restype_model`,'$.id'))) VIRTUAL,
  `res_parent_id` varchar(100) GENERATED ALWAYS AS (json_unquote(json_extract(`restype_model`,'$.parentid'))) VIRTUAL,
  `isroot` varchar(20) GENERATED ALWAYS AS (json_unquote(json_extract(`restype_model`,'$.isroot'))) VIRTUAL,
  `onlyclassify` varchar(20) GENERATED ALWAYS AS (json_unquote(json_extract(`restype_model`,'$.onlyclassify'))) VIRTUAL,
  `is_system` tinyint(1) GENERATED ALWAYS AS (json_unquote(json_extract(`restype_model`,'$.isSystem'))) VIRTUAL,
  `customerId` varchar(1500) GENERATED ALWAYS AS (json_unquote(json_extract(`restype_model`,'$.customerId'))) VIRTUAL,
  PRIMARY KEY (`id`),
  KEY `restype_id` (`restype_id`),
  KEY `res_parent_id` (`res_parent_id`),
  KEY `isroot` (`isroot`),
  KEY `onlyclassify` (`onlyclassify`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;