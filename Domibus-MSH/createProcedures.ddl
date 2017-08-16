DROP PROCEDURE IF EXISTS domibus.REMOVE_CURRENT_DAY_SIGNAL_MESSAGES;
CREATE PROCEDURE domibus.REMOVE_CURRENT_DAY_SIGNAL_MESSAGES()
  BEGIN
    DECLARE signal_done INT DEFAULT FALSE;
    DECLARE RECEIPT_ID INT;
    DECLARE SIGNAL_MESSAGE_ID INT;
    DECLARE SIGNAL_INFO_ID INT;
    DECLARE BUSINESS_ID VARCHAR(100);

    DECLARE signal_message_of_the_Day CURSOR FOR SELECT
                                                   sm.ID_PK,
                                                   mi.ID_PK,
                                                   sm.receipt_ID_PK,
                                                   mi.MESSAGE_ID
                                                 FROM TB_SIGNAL_MESSAGE sm, TB_MESSAGE_LOG ml, TB_MESSAGE_INFO mi,
                                                   TB_MESSAGING m
                                                 WHERE
                                                   m.SIGNAL_MESSAGE_ID = sm.ID_PK AND sm.messageInfo_ID_PK = mi.ID_PK
                                                   AND ml.MESSAGE_ID = mi.MESSAGE_ID AND DATE(ml.RECEIVED) >= curdate();

    OPEN signal_message_of_the_Day;
    read_loop: LOOP
      FETCH signal_message_of_the_Day
      INTO SIGNAL_MESSAGE_ID, SIGNAL_INFO_ID, RECEIPT_ID, BUSINESS_ID;
      IF signal_done
      THEN
        LEAVE read_loop;
      END IF;
      UPDATE TB_MESSAGING m
      SET m.SIGNAL_MESSAGE_ID = NULL
      WHERE m.SIGNAL_MESSAGE_ID = SIGNAL_MESSAGE_ID;
      DELETE FROM TB_RAWENVELOPE_LOG
      WHERE SIGNAL_MESSAGE_ID = SIGNAL_MESSAGE_ID;
      DELETE FROM TB_SIGNAL_MESSAGE
      WHERE ID_PK = SIGNAL_MESSAGE_ID;
      DELETE FROM TB_MESSAGE_INFO
      WHERE ID_PK = SIGNAL_INFO_ID;
      DELETE FROM TB_RECEIPT_DATA
      WHERE RECEIPT_ID = RECEIPT_ID;
      DELETE FROM TB_RECEIPT
      WHERE ID_PK = RECEIPT_ID;
      DELETE FROM TB_MESSAGE_LOG
      WHERE MESSAGE_ID = BUSINESS_ID;
    END LOOP;
    CLOSE signal_message_of_the_Day;
  END;

DROP PROCEDURE IF EXISTS domibusred.REMOVE_CURRENT_DAY_SIGNAL_MESSAGES;
CREATE PROCEDURE domibusred.REMOVE_CURRENT_DAY_SIGNAL_MESSAGES()
  BEGIN
    DECLARE signal_done INT DEFAULT FALSE;
    DECLARE RECEIPT_ID INT;
    DECLARE SIGNAL_MESSAGE_ID INT;
    DECLARE SIGNAL_INFO_ID INT;
    DECLARE BUSINESS_ID VARCHAR(100);

    DECLARE signal_message_of_the_Day CURSOR FOR SELECT
                                                   sm.ID_PK,
                                                   mi.ID_PK,
                                                   sm.receipt_ID_PK,
                                                   mi.MESSAGE_ID
                                                 FROM TB_SIGNAL_MESSAGE sm, TB_MESSAGE_LOG ml, TB_MESSAGE_INFO mi,
                                                   TB_MESSAGING m
                                                 WHERE
                                                   m.SIGNAL_MESSAGE_ID = sm.ID_PK AND sm.messageInfo_ID_PK = mi.ID_PK
                                                   AND ml.MESSAGE_ID = mi.MESSAGE_ID AND DATE(ml.RECEIVED) >= curdate();

    OPEN signal_message_of_the_Day;
    read_loop: LOOP
      FETCH signal_message_of_the_Day
      INTO SIGNAL_MESSAGE_ID, SIGNAL_INFO_ID, RECEIPT_ID, BUSINESS_ID;
      IF signal_done
      THEN
        LEAVE read_loop;
      END IF;
      UPDATE TB_MESSAGING m
      SET m.SIGNAL_MESSAGE_ID = NULL
      WHERE m.SIGNAL_MESSAGE_ID = SIGNAL_MESSAGE_ID;
      DELETE FROM TB_RAWENVELOPE_LOG
      WHERE SIGNAL_MESSAGE_ID = SIGNAL_MESSAGE_ID;
      DELETE FROM TB_SIGNAL_MESSAGE
      WHERE ID_PK = SIGNAL_MESSAGE_ID;
      DELETE FROM TB_MESSAGE_INFO
      WHERE ID_PK = SIGNAL_INFO_ID;
      DELETE FROM TB_RECEIPT_DATA
      WHERE RECEIPT_ID = RECEIPT_ID;
      DELETE FROM TB_RECEIPT
      WHERE ID_PK = RECEIPT_ID;
      DELETE FROM TB_MESSAGE_LOG
      WHERE MESSAGE_ID = BUSINESS_ID;
    END LOOP;
    CLOSE signal_message_of_the_Day;
  END;

DROP PROCEDURE IF EXISTS domibus.REMOVE_CURRENT_DAY_USER_MESSAGES;
CREATE PROCEDURE domibus.REMOVE_CURRENT_DAY_USER_MESSAGES()
  BEGIN
    DECLARE done INT DEFAULT FALSE;
    DECLARE USER_MESSAGE_ID INT;
    DECLARE BUSINESS_MESSAGE_ID VARCHAR(100);
    DECLARE MESSAGE_INFO_ID INT;
    DECLARE PART_ID INT;

    DECLARE user_message_of_the_day CURSOR FOR SELECT
                                                 um.ID_PK,
                                                 mi.ID_PK,
                                                 mi.MESSAGE_ID,
                                                 pi.ID_PK
                                               FROM TB_USER_MESSAGE um, TB_MESSAGE_LOG ml, TB_MESSAGE_INFO mi,
                                                 TB_MESSAGING m, TB_PART_INFO pi
                                               WHERE
                                                 m.USER_MESSAGE_ID = um.ID_PK AND um.messageInfo_ID_PK = mi.ID_PK AND
                                                 ml.MESSAGE_ID = mi.MESSAGE_ID AND pi.PAYLOADINFO_ID = um.ID_PK AND
                                                 DATE(ml.RECEIVED) >= curdate();
    OPEN user_message_of_the_day;
    read_loop: LOOP
      FETCH user_message_of_the_day
      INTO USER_MESSAGE_ID, MESSAGE_INFO_ID, BUSINESS_MESSAGE_ID, PART_ID;
      IF done
      THEN
        LEAVE read_loop;
      END IF;
      DELETE FROM TB_PROPERTY
      WHERE MESSAGEPROPERTIES_ID = USER_MESSAGE_ID;
      DELETE FROM TB_PROPERTY
      WHERE PARTPROPERTIES_ID = PART_ID;
      DELETE FROM TB_PART_INFO
      WHERE PAYLOADINFO_ID = USER_MESSAGE_ID;
      DELETE FROM TB_PARTY_ID
      WHERE FROM_ID = USER_MESSAGE_ID;
      DELETE FROM TB_PARTY_ID
      WHERE TO_ID = USER_MESSAGE_ID;
      DELETE FROM TB_RAWENVELOPE_LOG
      WHERE USERMESSAGE_ID_FK = USER_MESSAGE_ID;
      DELETE FROM TB_USER_MESSAGE
      WHERE ID_PK = USER_MESSAGE_ID;
      DELETE FROM TB_MESSAGE_INFO
      WHERE ID_PK = MESSAGE_INFO_ID;
      DELETE FROM TB_MESSAGING
      WHERE USER_MESSAGE_ID = USER_MESSAGE_ID;
      DELETE FROM TB_MESSAGE_LOG
      WHERE MESSAGE_ID = BUSINESS_MESSAGE_ID;
    END LOOP;
    CLOSE user_message_of_the_day;
  END;

DROP PROCEDURE IF EXISTS domibusred.REMOVE_CURRENT_DAY_USER_MESSAGES;
CREATE PROCEDURE domibusred.REMOVE_CURRENT_DAY_USER_MESSAGES()
  BEGIN
    DECLARE done INT DEFAULT FALSE;
    DECLARE USER_MESSAGE_ID INT;
    DECLARE BUSINESS_MESSAGE_ID VARCHAR(100);
    DECLARE MESSAGE_INFO_ID INT;
    DECLARE PART_ID INT;

    DECLARE user_message_of_the_day CURSOR FOR SELECT
                                                 um.ID_PK,
                                                 mi.ID_PK,
                                                 mi.MESSAGE_ID,
                                                 pi.ID_PK
                                               FROM TB_USER_MESSAGE um, TB_MESSAGE_LOG ml, TB_MESSAGE_INFO mi,
                                                 TB_MESSAGING m, TB_PART_INFO pi
                                               WHERE
                                                 m.USER_MESSAGE_ID = um.ID_PK AND um.messageInfo_ID_PK = mi.ID_PK AND
                                                 ml.MESSAGE_ID = mi.MESSAGE_ID AND pi.PAYLOADINFO_ID = um.ID_PK AND
                                                 DATE(ml.RECEIVED) >= curdate();
    OPEN user_message_of_the_day;
    read_loop: LOOP
      FETCH user_message_of_the_day
      INTO USER_MESSAGE_ID, MESSAGE_INFO_ID, BUSINESS_MESSAGE_ID, PART_ID;
      IF done
      THEN
        LEAVE read_loop;
      END IF;
      DELETE FROM TB_PROPERTY
      WHERE MESSAGEPROPERTIES_ID = USER_MESSAGE_ID;
      DELETE FROM TB_PROPERTY
      WHERE PARTPROPERTIES_ID = PART_ID;
      DELETE FROM TB_PART_INFO
      WHERE PAYLOADINFO_ID = USER_MESSAGE_ID;
      DELETE FROM TB_PARTY_ID
      WHERE FROM_ID = USER_MESSAGE_ID;
      DELETE FROM TB_PARTY_ID
      WHERE TO_ID = USER_MESSAGE_ID;
      DELETE FROM TB_RAWENVELOPE_LOG
      WHERE USERMESSAGE_ID_FK = USER_MESSAGE_ID;
      DELETE FROM TB_USER_MESSAGE
      WHERE ID_PK = USER_MESSAGE_ID;
      DELETE FROM TB_MESSAGE_INFO
      WHERE ID_PK = MESSAGE_INFO_ID;
      DELETE FROM TB_MESSAGING
      WHERE USER_MESSAGE_ID = USER_MESSAGE_ID;
      DELETE FROM TB_MESSAGE_LOG
      WHERE MESSAGE_ID = BUSINESS_MESSAGE_ID;
    END LOOP;
    CLOSE user_message_of_the_day;
  END;
