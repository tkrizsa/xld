--@xld-modelVersion	: 14a4ef3f31abd6f281d56bb1ab195228
--@xld-versionHint 	: initial

CREATE TABLE `order.detail` (
	`detailId` 			BIGINT NOT NULL,
	`orderId` 			BIGINT NOT NULL,
	`articleId` 		BIGINT NOT NULL,
	`amount` 			DECIMAL(21,3) NOT NULL,
	PRIMARY KEY(`detailId`),
	CONSTRAINT `fk_order_detail_orderId` FOREIGN KEY (`orderId`) REFERENCES `order.order`(`orderId`),
	CONSTRAINT `fk_order_detail_articleId` FOREIGN KEY (`articleId`) REFERENCES `article.article`(`articleId`)
)


--@xld-modelVersion	: 3c2788baa22a0165fbf7d6a5a927fd39
--@xld-versionHint 	: veresion string creation changed

SELECT 'nothing to do'


