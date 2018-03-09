-- *********************************************************************
-- Delete script for Oracle Domibus DB with a time interval
-- Change START_DATE and END_DATE values accordingly - pay attention to that
-- fact that info are stored in DB using timezone agnostic data types
--
-- Important: In order to keep the JMS queues synchronized with the DB data that will be
-- deleted by this script, the Domibus Administrator should remove manually the associated
-- JMS messages FROM the plugin notifications queues
-- *********************************************************************

/* For SQL*Plus client use specific definition like:
   variable START_DATE varchar2(30)
   exec :START_DATE := '2013-10-01';
*/
DEFINE START_DATE = TO_DATE('08-MAR-2018 09:59:00', 'DD-MM-YY HH24:MI:SS');
DEFINE END_DATE = TO_DATE('08-MAR-2018 10:00:00', 'DD-MM-YY HH24:MI:SS');

DELETE FROM tb_messaging WHERE
    (
        signal_message_id IN (
            SELECT
                id_pk
            FROM
                tb_signal_message
            WHERE
                messageinfo_id_pk IN (
                    SELECT
                        id_pk
                    FROM
                        tb_message_info
                    WHERE
                        message_id IN (
                            SELECT
                                message_id
                            FROM
                                tb_message_log
                            WHERE
                                received BETWEEN &START_DATE AND &END_DATE
                        )
                )
        )
    );

DELETE FROM tb_messaging WHERE
    (
        user_message_id IN (
            SELECT
                id_pk
            FROM
                tb_user_message
            WHERE
                messageinfo_id_pk IN (
                    SELECT
                        id_pk
                    FROM
                        tb_message_info
                    WHERE
                        message_id IN (
                            SELECT
                                message_id
                            FROM
                                tb_message_log
                            WHERE
                                received BETWEEN &START_DATE AND &END_DATE
                        )
                )
        )
    );

DELETE FROM tb_error_log WHERE
    (
        error_signal_message_id IN (
            SELECT
                message_id
            FROM
                tb_message_log
            WHERE
                received BETWEEN &START_DATE AND &END_DATE
        )
    );

DELETE FROM tb_error_log WHERE
    (
        message_in_error_id IN (
            SELECT
                message_id
            FROM
                tb_message_log
            WHERE
                received BETWEEN &START_DATE AND &END_DATE
        )
    );

DELETE FROM tb_party_id WHERE
    from_id IN (
        SELECT
            id_pk
        FROM
            tb_user_message
        WHERE
            messageinfo_id_pk IN (
                SELECT
                    id_pk
                FROM
                    tb_message_info
                WHERE
                    message_id IN (
                        SELECT
                            message_id
                        FROM
                            tb_message_log
                        WHERE
                            received BETWEEN &START_DATE AND &END_DATE
                    )
            )
    );

DELETE FROM tb_party_id WHERE
    to_id IN (
        SELECT
            id_pk
        FROM
            tb_user_message
        WHERE
            messageinfo_id_pk IN (
                SELECT
                    id_pk
                FROM
                    tb_message_info
                WHERE
                    message_id IN (
                        SELECT
                            message_id
                        FROM
                            tb_message_log
                        WHERE
                            received BETWEEN &START_DATE AND &END_DATE
                    )
            )
    );

DELETE FROM tb_receipt_data WHERE
    receipt_id IN (
        SELECT
            id_pk
        FROM
            tb_receipt
        WHERE
            id_pk IN (
                SELECT
                    receipt_id_pk
                FROM
                    tb_signal_message
                WHERE
                    messageinfo_id_pk IN (
                        SELECT
                            id_pk
                        FROM
                            tb_message_info
                        WHERE
                            message_id IN (
                                SELECT
                                    message_id
                                FROM
                                    tb_message_log
                                WHERE
                                    received BETWEEN &START_DATE AND &END_DATE
                            )
                    )
            )
    );

DELETE FROM tb_property WHERE
    partproperties_id IN (
        SELECT
            id_pk
        FROM
            tb_part_info
        WHERE
            payloadinfo_id IN (
                SELECT
                    id_pk
                FROM
                    tb_user_message
                WHERE
                    messageinfo_id_pk IN (
                        SELECT
                            id_pk
                        FROM
                            tb_message_info
                        WHERE
                            message_id IN (
                                SELECT
                                    message_id
                                FROM
                                    tb_message_log
                                WHERE
                                    received BETWEEN &START_DATE AND &END_DATE
                            )
                    )
            )
    );

DELETE FROM tb_property WHERE
    messageproperties_id IN (
        SELECT
            id_pk
        FROM
            tb_user_message
        WHERE
            messageinfo_id_pk IN (
                SELECT
                    id_pk
                FROM
                    tb_message_info
                WHERE
                    message_id IN (
                        SELECT
                            message_id
                        FROM
                            tb_message_log
                        WHERE
                            received BETWEEN &START_DATE AND &END_DATE
                    )
            )
    );

DELETE FROM tb_part_info WHERE
    payloadinfo_id IN (
        SELECT
            id_pk
        FROM
            tb_user_message
        WHERE
            messageinfo_id_pk IN (
                SELECT
                    id_pk
                FROM
                    tb_message_info
                WHERE
                    message_id IN (
                        SELECT
                            message_id
                        FROM
                            tb_message_log
                        WHERE
                            received BETWEEN &START_DATE AND &END_DATE
                    )
            )
    );

DELETE FROM tb_rawenvelope_log WHERE
    usermessage_id_fk IN (
        SELECT
            id_pk
        FROM
            tb_user_message
        WHERE
            messageinfo_id_pk IN (
                SELECT
                    id_pk
                FROM
                    tb_message_info
                WHERE
                    message_id IN (
                        SELECT
                            message_id
                        FROM
                            tb_message_log
                        WHERE
                            received BETWEEN &START_DATE AND &END_DATE
                    )
            )
    );

DELETE FROM tb_rawenvelope_log WHERE
    signalmessage_id_fk IN (
        SELECT
            id_pk
        FROM
            tb_signal_message
        WHERE
            messageinfo_id_pk IN (
                SELECT
                    id_pk
                FROM
                    tb_message_info
                WHERE
                    message_id IN (
                        SELECT
                            message_id
                        FROM
                            tb_message_log
                        WHERE
                            received BETWEEN &START_DATE AND &END_DATE
                    )
            )
    );

DELETE FROM tb_error WHERE
    signalmessage_id IN (
        SELECT
            id_pk
        FROM
            tb_signal_message
        WHERE
            messageinfo_id_pk IN (
                SELECT
                    id_pk
                FROM
                    tb_message_info
                WHERE
                    message_id IN (
                        SELECT
                            message_id
                        FROM
                            tb_message_log
                        WHERE
                            received BETWEEN &START_DATE AND &END_DATE
                    )
            )
    );

DELETE FROM tb_user_message WHERE
    messageinfo_id_pk IN (
        SELECT
            id_pk
        FROM
            tb_message_info
        WHERE
            message_id IN (
                SELECT
                    message_id
                FROM
                    tb_message_log
                WHERE
                    received BETWEEN &START_DATE AND &END_DATE
            )
    );

DELETE FROM tb_signal_message WHERE
    messageinfo_id_pk IN (
        SELECT
            id_pk
        FROM
            tb_message_info
        WHERE
            message_id IN (
                SELECT
                    message_id
                FROM
                    tb_message_log
                WHERE
                    received BETWEEN &START_DATE AND &END_DATE
            )
    );

DELETE FROM tb_receipt WHERE
    id_pk IN (
        SELECT
            receipt_id_pk
        FROM
            tb_signal_message
        WHERE
            messageinfo_id_pk IN (
                SELECT
                    id_pk
                FROM
                    tb_message_info
                WHERE
                    message_id IN (
                        SELECT
                            message_id
                        FROM
                            tb_message_log
                        WHERE
                            received BETWEEN &START_DATE AND &END_DATE
                    )
            )
    );

DELETE FROM tb_message_info WHERE
    message_id IN (
        SELECT
            message_id
        FROM
            tb_message_log
        WHERE
            received BETWEEN &START_DATE AND &END_DATE
    );

DELETE FROM tb_message_log WHERE
    received BETWEEN &START_DATE AND &END_DATE;

COMMIT;

