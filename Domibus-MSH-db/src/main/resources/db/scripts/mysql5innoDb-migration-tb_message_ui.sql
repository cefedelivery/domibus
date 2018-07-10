-- *********************************************************************
-- Migration script for populate TB_MESSAGE_UI flat table
--
-- For large volume of data is advisable to first create TB_MESSAGE_UI table,
-- migrate the data and then create the indexes
--
-- *********************************************************************
INSERT INTO TB_MESSAGE_UI (
  message_id,
	message_status, 
	notification_status, 
	msh_role, 
	message_type, 
	deleted, 
	received, 
	send_attempts, 
	send_attempts_max, 
	next_attempt,
	CONVERSATION_ID,
	from_id,
	to_id,
	from_scheme,
	to_scheme, 
    ref_to_message_id,
	failed,
	restored,
	message_subtype 
    )

SELECT * FROM (
 SELECT
        message_log.MESSAGE_ID,
        message_log.MESSAGE_STATUS ,
        message_log.NOTIFICATION_STATUS,
        message_log.MSH_ROLE ,
        message_log.MESSAGE_TYPE ,
        message_log.DELETED ,
        message_log.RECEIVED ,
        message_log.SEND_ATTEMPTS ,
        message_log.SEND_ATTEMPTS_MAX,
        message_log.NEXT_ATTEMPT ,
        user_message.COLL_INFO_CONVERS_ID AS CONVERSATION_ID,
        partyid5_.VALUE AS FROM_ID,
        partyid6_.VALUE AS TO_ID,
        property3_.VALUE AS ORIGINAL_SENDER,
        property4_.VALUE AS FINAL_RECIPIENT,
        message_info.REF_TO_MESSAGE_ID,
        message_log.FAILED ,
        message_log.RESTORED ,
        message_log.MESSAGE_SUBTYPE 
    FROM
        TB_MESSAGE_LOG message_log 
    LEFT OUTER JOIN
        TB_MESSAGE_INFO message_info 
            ON message_log.MESSAGE_ID=message_info.MESSAGE_ID CROSS 
    JOIN
        TB_USER_MESSAGE user_message 
    LEFT OUTER JOIN
        TB_PROPERTY property3_ 
            ON user_message.ID_PK=property3_.MESSAGEPROPERTIES_ID 
    LEFT OUTER JOIN
        TB_PROPERTY property4_ 
            ON user_message.ID_PK=property4_.MESSAGEPROPERTIES_ID 
    LEFT OUTER JOIN
        TB_PARTY_ID partyid5_ 
            ON user_message.ID_PK=partyid5_.FROM_ID 
    LEFT OUTER JOIN
        TB_PARTY_ID partyid6_ 
            ON user_message.ID_PK=partyid6_.TO_ID 
    WHERE
        user_message.messageInfo_ID_PK=message_info.ID_PK 
        AND property3_.NAME='originalSender' 
        AND property4_.NAME='finalRecipient' 
UNION
SELECT
        message_log.MESSAGE_ID ,
        message_log.MESSAGE_STATUS ,
        message_log.NOTIFICATION_STATUS ,
        message_log.MSH_ROLE ,
        message_log.MESSAGE_TYPE ,
        message_log.DELETED ,
        message_log.RECEIVED ,
        message_log.SEND_ATTEMPTS ,
        message_log.SEND_ATTEMPTS_MAX ,
        message_log.NEXT_ATTEMPT,
        ''  AS CONVERSATION_ID,
        partyid7_.VALUE AS FROM_ID,
        partyid8_.VALUE AS TO_ID,
        property5_.VALUE AS ORIGINAL_SENDER,
        property6_.VALUE AS FINAL_RECIPIENT,
        messageinf4_.REF_TO_MESSAGE_ID ,
        message_log.FAILED ,
        message_log.RESTORED,
        message_log.MESSAGE_SUBTYPE
    FROM
        TB_MESSAGE_LOG message_log CROSS 
    JOIN
        TB_MESSAGING messaging 
    INNER JOIN
        TB_SIGNAL_MESSAGE signalmess2_ 
            ON messaging.SIGNAL_MESSAGE_ID=signalmess2_.ID_PK 
    LEFT OUTER JOIN
        TB_MESSAGE_INFO messageinf4_ 
            ON signalmess2_.messageInfo_ID_PK=messageinf4_.ID_PK 
    INNER JOIN
        TB_USER_MESSAGE usermessag3_ 
            ON messaging.USER_MESSAGE_ID=usermessag3_.ID_PK 
    LEFT OUTER JOIN
        TB_PROPERTY property5_ 
            ON usermessag3_.ID_PK=property5_.MESSAGEPROPERTIES_ID 
    LEFT OUTER JOIN
        TB_PROPERTY property6_ 
            ON usermessag3_.ID_PK=property6_.MESSAGEPROPERTIES_ID 
    LEFT OUTER JOIN
        TB_PARTY_ID partyid7_ 
            ON usermessag3_.ID_PK=partyid7_.FROM_ID 
    LEFT OUTER JOIN
        TB_PARTY_ID partyid8_ 
            ON usermessag3_.ID_PK=partyid8_.TO_ID CROSS 
    JOIN
        TB_MESSAGE_INFO messageinf11_ 
    WHERE
        usermessag3_.messageInfo_ID_PK=messageinf11_.ID_PK 
        AND messageinf4_.MESSAGE_ID=message_log.MESSAGE_ID 
        AND messageinf4_.REF_TO_MESSAGE_ID=messageinf11_.MESSAGE_ID 
        AND property5_.NAME='originalSender' 
        AND property6_.NAME='finalRecipient' 
	) result
ORDER BY result.received ASC

COMMIT;