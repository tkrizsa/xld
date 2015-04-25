--@xld-modelVersion	: 9604b4de8d8f94c7a69a431d09bbaa1a
--@xld-versionHint 	: initial

CREATE TABLE `homebudget.expense` (
	`expenseId` 		BIGINT,
	`expenseKindId`		BIGINT,
	`expenseName` 		VARCHAR(200),
	PRIMARY KEY(`expenseId`),
	CONSTRAINT `fk_expense_expenseKindId` FOREIGN KEY (`expenseKindId`) REFERENCES `homebudget.expensekind`(`expenseKindId`)
)


--@xld-modelVersion	: 9c116ab2b4fff7d921bca4b64deeab09
--@xld-versionHint 	: rename to expenseDescription

ALTER TABLE `homebudget.expense` 
	CHANGE `expenseName` `expenseDescription` TEXT
	
	


--@xld-modelVersion	: 0c0076a670246d0bf4602520e8582912
--@xld-versionHint 	: set fields to not null

ALTER TABLE `homebudget.expense` 
	CHANGE `expenseId` 			`expenseId`		BIGINT NOT NULL,
	CHANGE `expenseKindId` 		`expenseKindId`	BIGINT NOT NULL
	
	

--@xld-modelVersion	: 11446ba8d196747c7fab485d5d08485f
--@xld-versionHint 	: add money field

ALTER TABLE `homebudget.expense` 
	ADD `amount`		NUMERIC(24,4) NOT NULL
