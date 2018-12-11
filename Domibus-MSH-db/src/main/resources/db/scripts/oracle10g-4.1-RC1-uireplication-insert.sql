-- *********************************************************************
-- This script should be run if UIReplication feature is enabled
-- ( before setting domibus.ui.replication.enabled=true).
--
--
-- *********************************************************************
INSERT /*+ append*/ INTO tb_message_ui (
  id_pk,
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
  conversation_id,
  from_id,
  to_id,
  from_scheme,
  to_scheme,
  ref_to_message_id,
  failed,
  restored,
  message_subtype
)
SELECT
  hibernate_sequence.NEXTVAL,
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
  conversation_id,
  from_id,
  to_id,
  original_sender,
  final_recipient,
  ref_to_message_id,
  failed,
  restored,
  message_subtype
FROM
  (
    SELECT
      *
    FROM
      (
        SELECT
          message_log.message_id,
          message_log.message_status,
          message_log.notification_status,
          message_log.msh_role,
          message_log.message_type,
          message_log.deleted,
          message_log.received,
          message_log.send_attempts,
          message_log.send_attempts_max,
          message_log.next_attempt,
          user_message.coll_info_convers_id AS conversation_id,
          partyid5_.value AS from_id,
          partyid6_.value AS to_id,
          property.value AS original_sender,
          property2.value AS final_recipient,
          message_info.ref_to_message_id,
          message_log.failed,
          message_log.restored,
          message_log.message_subtype
        FROM
          tb_message_log message_log
            LEFT OUTER JOIN tb_message_info message_info ON message_log.message_id = message_info.message_id,tb_user_message user_message
                                                                                                               LEFT OUTER JOIN tb_property property ON user_message.id_pk = property.messageproperties_id
                                                                                                               LEFT OUTER JOIN tb_property property2 ON user_message.id_pk = property2.messageproperties_id
                                                                                                               LEFT OUTER JOIN tb_party_id partyid5_ ON user_message.id_pk = partyid5_.from_id
                                                                                                               LEFT OUTER JOIN tb_party_id partyid6_ ON user_message.id_pk = partyid6_.to_id
        WHERE
            user_message.messageinfo_id_pk = message_info.id_pk
          AND property.name = 'originalSender'
          AND property2.name = 'finalRecipient'
        UNION
        SELECT
          message_log.message_id,
          message_log.message_status,
          message_log.notification_status,
          message_log.msh_role,
          message_log.message_type,
          message_log.deleted,
          message_log.received,
          message_log.send_attempts,
          message_log.send_attempts_max,
          message_log.next_attempt,
          '' AS conversation_id,
          partyid7_.value AS from_id,
          partyid8_.value AS to_id,
          property.value AS original_sender,
          property2.value AS final_recipient,
          message_info.ref_to_message_id,
          message_log.failed,
          message_log.restored,
          message_log.message_subtype
        FROM
          tb_message_log message_log
            CROSS JOIN tb_messaging messaging
            INNER JOIN tb_signal_message signalmess2_ ON messaging.signal_message_id = signalmess2_.id_pk
            LEFT OUTER JOIN tb_message_info message_info ON signalmess2_.messageinfo_id_pk = message_info.id_pk
            INNER JOIN tb_user_message user_message ON messaging.user_message_id = user_message.id_pk
            LEFT OUTER JOIN tb_property property ON user_message.id_pk = property.messageproperties_id
            LEFT OUTER JOIN tb_property property2 ON user_message.id_pk = property2.messageproperties_id
            LEFT OUTER JOIN tb_party_id partyid7_ ON user_message.id_pk = partyid7_.from_id
            LEFT OUTER JOIN tb_party_id partyid8_ ON user_message.id_pk = partyid8_.to_id
            CROSS JOIN tb_message_info message_info2
        WHERE
            user_message.messageinfo_id_pk = message_info2.id_pk
          AND message_info.message_id = message_log.message_id
          AND message_info.ref_to_message_id = message_info2.message_id
          AND property.name = 'originalSender'
          AND property2.name = 'finalRecipient'
      ) result
    ORDER BY
      result.received ASC
  );
/
COMMIT;
/