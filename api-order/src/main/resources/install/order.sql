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
	ADD CONSTRAINT `fk_order_actorId` FOREIGN KEY (`actorId`) REFERENCES `actor.actor`(`actorId`)


--@xld-modelVersion	: a08c472ae096f1895620d232b6ace006
--@xld-versionHint 	: actor changes to delivery and invoice partner

ALTER TABLE `order.order`
	DROP FOREIGN KEY `fk_order_actorId`

--@xld-go	

ALTER TABLE `order.order`
	CHANGE `actorId` `deliveryActorId` BIGINT NULL,
	ADD `invoiceActorId` BIGINT NULL,
	ADD CONSTRAINT `fk_order_deliveryActorId` FOREIGN KEY (`deliveryActorId`) REFERENCES `actor.actor`(`actorId`),
	ADD CONSTRAINT `fk_order_invoiceActorId`  FOREIGN KEY (`invoiceActorId`) REFERENCES `actor.actor`(`actorId`)
	
	
--@xld-modelVersion	: 42f290a8eb214e04651b06b8082d2576
--@xld-versionHint 	: version string creating changed

SELECT 'nothing to do'


