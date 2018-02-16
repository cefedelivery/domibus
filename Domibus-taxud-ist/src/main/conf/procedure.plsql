create or replace PROCEDURE REMOVE_CURRENT_DAY_SIGNAL_MESSAGES AS
  signal_done       INT ;
  signal_done       INT ;
  RECEIPT_ID        INT;
  SIGNAL_MESSAGE_ID INT;
  SIGNAL_INFO_ID    INT;
  BUSINESS_ID       VARCHAR(100);

  BEGIN
    FOR X IN (
    SELECT
      sm.ID_PK         AS SIGNAL_MESSAGE_ID,
      mi.ID_PK         AS SIGNAL_INFO_ID,
      sm.receipt_ID_PK AS RECEIPT_ID,
      mi.MESSAGE_ID    AS BUSINESS_ID
    FROM TB_SIGNAL_MESSAGE sm, TB_MESSAGE_LOG ml, TB_MESSAGE_INFO mi,
      TB_MESSAGING m
    WHERE
      m.SIGNAL_MESSAGE_ID = sm.ID_PK AND sm.messageInfo_ID_PK = mi.ID_PK
      AND ml.MESSAGE_ID = mi.MESSAGE_ID
      AND CAST(ml.RECEIVED AS DATE) >= trunc(sysdate))
    LOOP
      SIGNAL_MESSAGE_ID := X.SIGNAL_MESSAGE_ID;
      SIGNAL_INFO_ID := X.SIGNAL_INFO_ID;
      RECEIPT_ID := X.RECEIPT_ID;
      BUSINESS_ID := X.BUSINESS_ID;

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

  END REMOVE_CURRENT_DAY_SIGNAL_MESSAGES;

  CREATE OR REPLACE PROCEDURE remove_current_day_user_messages AS
      done                  INT;
      user_message_id       INT;
      business_message_id   VARCHAR(100);
      message_info_id       INT;
      part_id               INT;
      property_part_id               INT;
  BEGIN
      FOR x IN (
          SELECT
              um.id_pk user_message_id,
              mi.id_pk business_message_id,
              mi.message_id message_info_id,
              pi.id_pk part_id

          FROM
              tb_user_message um,
              tb_message_log ml,
              tb_message_info mi,
              tb_messaging m,
              tb_part_info pi
          WHERE
              m.user_message_id = um.id_pk
              AND   um.messageinfo_id_pk = mi.id_pk
              AND   ml.message_id = mi.message_id
              AND   pi.payloadinfo_id = um.id_pk
              AND   CAST(ml.received AS DATE) >= trunc(SYSDATE)
      ) LOOP
          user_message_id := x.user_message_id;
          business_message_id := x.business_message_id;
          message_info_id := message_info_id;
          part_id := part_id;



          DELETE FROM tb_property WHERE
              messageproperties_id = user_message_id;

          DELETE FROM tb_part_info WHERE
              ID_PK IN(SELECT po.PARTPROPERTIES_ID FROM tb_property po WHERE messageproperties_id = user_message_id)  ;

          DELETE FROM tb_property WHERE
              partproperties_id = part_id;

              DELETE FROM tb_part_info WHERE
              payloadinfo_id = user_message_id;

          DELETE FROM tb_party_id WHERE
              from_id = user_message_id;

          DELETE FROM tb_party_id WHERE
              to_id = user_message_id;

          DELETE FROM tb_rawenvelope_log WHERE
              usermessage_id_fk = user_message_id;

          DELETE FROM tb_user_message WHERE
              id_pk = user_message_id;

          DELETE FROM tb_message_info WHERE
              id_pk = message_info_id;

          DELETE FROM tb_messaging WHERE
              user_message_id = user_message_id;

          DELETE FROM tb_message_log WHERE
              message_id = business_message_id;

      END LOOP;
  END;