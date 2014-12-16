--@xld-modelVersion	: c0361a06fcd0671e757e8be524d27159
--@xld-versionHint 	: initial

CREATE TABLE `order.order` (
	`orderId` 			BIGINT,
	`orderName` 		VARCHAR(100),
	`orderDescription` 	TEXT,
	PRIMARY KEY(`orderId`)
)

--@xld-modelVersion	: 2a7a6f55283bda20f8ce5210db727350
--@xld-versionHint 	: add reference for actor

ALTER TABLE `order.order` 
	ADD `actorId` BIGINT NULL,
	ADD FOREIGN KEY `fk_order_actorId`(`actorId`) REFERENCES `actor.actor`(`actorId`)

