--@xld-modelVersion	: f69e50d364352fac4132316f215d4ef6
--@xld-versionHint 	: initial

CREATE TABLE partner (
	`partnerId` INT AUTO_INCREMENT,
	`partnerName` VARCHAR(100),
	PRIMARY KEY(`partnerId`)
)
	
	
--@xld-modelVersion	: 1e9f1d6d3549d260208e951331cae38c
--@xld-versionHint 	: add column partnerStatus

ALTER TABLE `partner`
	ADD  `partnerStatus` ENUM('programmer','customer','stranger')

--@xld-modelVersion 	: ec3e752f3ee7f6d1afecb294ea711b1e
--@xld-versionHint 		: add column address1',


ALTER TABLE `partner`
	ADD  `address1` VARCHAR(200)
		
--@xld-modelVersion 	: e44a439a6c6c8430941028272b9178c2
--@xld-versionHint 		: 'set fields to not null,

ALTER TABLE `partner`
	MODIFY `partnerId` INT NOT NULL AUTO_INCREMENT,
	MODIFY `partnerName` VARCHAR(100) NOT NULL ,
	MODIFY `partnerStatus` ENUM('programmer','customer','stranger') NOT NULL 


--@xld-modelVersion : d551cf3e3cabbf48edfa6eae2b9dfa76
--@xld-versionHint 	: add field address2

ALTER TABLE `partner`
	ADD `address2` VARCHAR(200) NOT NULL
	
	
