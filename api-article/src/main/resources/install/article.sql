--@xld-modelVersion	: 3593827be99471bcd0960819a1976294
--@xld-versionHint 	: initial

CREATE TABLE `article.article` (
	`articleId` BIGINT,
	`articleName` VARCHAR(100),
	PRIMARY KEY(`articleId`)
)

--@xld-modelVersion	: a599e581a5ee09c3346281b38a096ea9
--@xld-versionHint 	: add description

ALTER TABLE `article.article` ADD  `articleDescription` TEXT NULL



