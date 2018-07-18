package eu.domibus.core.replication;

import java.util.List;
import java.util.Map;

/**
 * @author Catalin Enache
 * @since 4.0
 */
public interface UIMessageDao {
    UIMessageEntity findUIMessageByMessageId(String messageId);

    int countMessages(Map<String, Object> filters);

    List<UIMessageEntity> findPaged(int from, int max, String column, boolean asc, Map<String, Object> filters);

    void saveOrUpdate(UIMessageEntity uiMessageEntity);

    List<Object[]> findUIMessagesNotSynced();


    /** message id */
    String MESSAGE_ID = "MESSAGE_ID";

    /** oracle query for finding differences */
    String DIFF_QUERY_ORACLE = "SELECT *\n" +
            "FROM\n" +
            "    (\n" +
            "        SELECT\n" +
            "            message_log.message_id,\n" +
            "            message_log.message_status,\n" +
            "            message_log.notification_status,\n" +
            "            message_log.msh_role,\n" +
            "            message_log.message_type,\n" +
            "            message_log.deleted,\n" +
            "            message_log.received,\n" +
            "            message_log.send_attempts,\n" +
            "            message_log.send_attempts_max,\n" +
            "            message_log.next_attempt,\n" +
            "            user_message.coll_info_convers_id AS conversation_id,\n" +
            "            partyid5_.value AS from_id,\n" +
            "            partyid6_.value AS to_id,\n" +
            "            property.value AS from_scheme,\n" +
            "            property2.value AS to_scheme,\n" +
            "            message_info.ref_to_message_id,\n" +
            "            message_log.failed,\n" +
            "            message_log.restored,\n" +
            "            message_log.message_subtype\n" +
            "        FROM\n" +
            "            tb_message_log message_log\n" +
            "            LEFT OUTER JOIN tb_message_info message_info ON message_log.message_id = message_info.message_id,tb_user_message user_message\n" +
            "            LEFT OUTER JOIN tb_property property ON user_message.id_pk = property.messageproperties_id\n" +
            "            LEFT OUTER JOIN tb_property property2 ON user_message.id_pk = property2.messageproperties_id\n" +
            "            LEFT OUTER JOIN tb_party_id partyid5_ ON user_message.id_pk = partyid5_.from_id\n" +
            "            LEFT OUTER JOIN tb_party_id partyid6_ ON user_message.id_pk = partyid6_.to_id\n" +
            "        WHERE\n" +
            "            user_message.messageinfo_id_pk = message_info.id_pk\n" +
            "            AND property.name = 'originalSender'\n" +
            "            AND property2.name = 'finalRecipient'\n" +
            "        UNION\n" +
            "        SELECT\n" +
            "            message_log.message_id,\n" +
            "            message_log.message_status,\n" +
            "            message_log.notification_status,\n" +
            "            message_log.msh_role,\n" +
            "            message_log.message_type,\n" +
            "            message_log.deleted,\n" +
            "            message_log.received,\n" +
            "            message_log.send_attempts,\n" +
            "            message_log.send_attempts_max,\n" +
            "            message_log.next_attempt,\n" +
            "            '' AS conversation_id,\n" +
            "            partyid7_.value AS from_id,\n" +
            "            partyid8_.value AS to_id,\n" +
            "            property.value AS from_scheme,\n" +
            "            property2.value AS to_scheme,\n" +
            "            message_info.ref_to_message_id,\n" +
            "            message_log.failed,\n" +
            "            message_log.restored,\n" +
            "            message_log.message_subtype\n" +
            "        FROM\n" +
            "            tb_message_log message_log\n" +
            "            CROSS JOIN tb_messaging messaging\n" +
            "            INNER JOIN tb_signal_message signalmess2_ ON messaging.signal_message_id = signalmess2_.id_pk\n" +
            "            LEFT OUTER JOIN tb_message_info message_info ON signalmess2_.messageinfo_id_pk = message_info.id_pk\n" +
            "            INNER JOIN tb_user_message user_message ON messaging.user_message_id = user_message.id_pk\n" +
            "            LEFT OUTER JOIN tb_property property ON user_message.id_pk = property.messageproperties_id\n" +
            "            LEFT OUTER JOIN tb_property property2 ON user_message.id_pk = property2.messageproperties_id\n" +
            "            LEFT OUTER JOIN tb_party_id partyid7_ ON user_message.id_pk = partyid7_.from_id\n" +
            "            LEFT OUTER JOIN tb_party_id partyid8_ ON user_message.id_pk = partyid8_.to_id\n" +
            "            CROSS JOIN tb_message_info message_info2\n" +
            "        WHERE\n" +
            "            user_message.messageinfo_id_pk = message_info2.id_pk\n" +
            "            AND message_info.message_id = message_log.message_id\n" +
            "            AND message_info.ref_to_message_id = message_info2.message_id\n" +
            "            AND property.name = 'originalSender'\n" +
            "            AND property2.name = 'finalRecipient'\n" +
            "    ) result\n" +
            "MINUS\n" +
            "SELECT\n" +
            "    message_id,\n" +
            "    message_status,\n" +
            "    notification_status,\n" +
            "    msh_role,\n" +
            "    message_type,\n" +
            "    deleted,\n" +
            "    received,\n" +
            "    send_attempts,\n" +
            "    send_attempts_max,\n" +
            "    next_attempt,\n" +
            "    conversation_id,\n" +
            "    from_id,\n" +
            "    to_id,\n" +
            "    from_scheme,\n" +
            "    to_scheme,\n" +
            "    ref_to_message_id,\n" +
            "    failed,\n" +
            "    restored,\n" +
            "    message_subtype\n" +
            "FROM\n" +
            "    tb_message_ui";
}
