--@xld-modelVersion	: aed1befbebe0dc1bb32ee0fcc57e1add
--@xld-versionHint 	: initial

CREATE TABLE `homebudget.expensekind` (
	`expensekindId` 	BIGINT,
	`expenseName` 		VARCHAR(200),
	PRIMARY KEY(`expensekindId`)
)


--@xld-modelVersion	: 4b6b74cb9c675acfb738600ddaa20386
--@xld-versionHint 	: alter to camelcase and not null

ALTER TABLE `homebudget.expensekind` 
	CHANGE `expensekindId`		`expenseKindId` 	BIGINT NOT NULL,
	CHANGE `expenseName` 		`expenseName`		VARCHAR(200) NOT NULL
	