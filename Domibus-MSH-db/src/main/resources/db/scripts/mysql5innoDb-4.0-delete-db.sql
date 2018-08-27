-- *********************************************************************
-- Delete script for MySQL Domibus DB with a time interval
-- Change START_DATE and END_DATE values accordingly - please pay attention
-- that the data stored in DB is timezone agnostic.
--
-- Important: In order to keep the JMS queues synchronized with the DB data that will be
-- deleted by this script, the Domibus Administrator should remove manually the associated
-- JMS messages from the plugin notifications queues
-- *********************************************************************
SET @START_DATE=STR_TO_DATE('2017-01-20 10:00:00', '%Y-%m-%d %H:%i:%s');
SET @END_DATE=STR_TO_DATE('2017-12-20 15:00:00', '%Y-%m-%d %H:%i:%s');

SET @OLD_SQL_SAFE_UPDATES = @@SQL_SAFE_UPDATES;
SET SQL_SAFE_UPDATES = 0;


DELETE FROM TB_MESSAGING
WHERE
    (signal_message_id IN (SELECT
        id_pk
    FROM
        TB_SIGNAL_MESSAGE

    WHERE
        messageinfo_id_pk IN (SELECT
            id_pk
        FROM
            TB_MESSAGE_INFO

        WHERE
            message_id IN (SELECT
                message_id
            FROM
                TB_MESSAGE_LOG

            WHERE
                received BETWEEN @START_DATE AND @END_DATE))));

DELETE FROM TB_MESSAGING
WHERE
    (user_message_id IN (SELECT
        id_pk
    FROM
        TB_USER_MESSAGE

    WHERE
        messageinfo_id_pk IN (SELECT
            id_pk
        FROM
            TB_MESSAGE_INFO

        WHERE
            message_id IN (SELECT
                message_id
            FROM
                TB_MESSAGE_LOG

            WHERE
                received BETWEEN @START_DATE AND @END_DATE))));

DELETE FROM TB_ERROR_LOG
WHERE
    (error_signal_message_id IN (SELECT
        message_id
    FROM
        TB_MESSAGE_LOG

    WHERE
        received BETWEEN @START_DATE AND @END_DATE));

DELETE FROM TB_ERROR_LOG
WHERE
    (message_in_error_id IN (SELECT
        message_id
    FROM
        TB_MESSAGE_LOG

    WHERE
        received BETWEEN @START_DATE AND @END_DATE));

DELETE FROM TB_PARTY_ID
WHERE
    from_id IN (SELECT
        id_pk
    FROM
        TB_USER_MESSAGE

    WHERE
        messageinfo_id_pk IN (SELECT
            id_pk
        FROM
            TB_MESSAGE_INFO

        WHERE
            message_id IN (SELECT
                message_id
            FROM
                TB_MESSAGE_LOG

            WHERE
                received BETWEEN @START_DATE AND @END_DATE)));

DELETE FROM TB_PARTY_ID
WHERE
    to_id IN (SELECT
        id_pk
    FROM
        TB_USER_MESSAGE

    WHERE
        messageinfo_id_pk IN (SELECT
            id_pk
        FROM
            TB_MESSAGE_INFO

        WHERE
            message_id IN (SELECT
                message_id
            FROM
                TB_MESSAGE_LOG

            WHERE
                received BETWEEN @START_DATE AND @END_DATE)));

DELETE FROM TB_RECEIPT_DATA
WHERE
    receipt_id IN (SELECT
        id_pk
    FROM
        TB_RECEIPT

    WHERE
        id_pk IN (SELECT
            receipt_id_pk
        FROM
            TB_SIGNAL_MESSAGE

        WHERE
            messageinfo_id_pk IN (SELECT
                id_pk
            FROM
                TB_MESSAGE_INFO

            WHERE
                message_id IN (SELECT
                    message_id
                FROM
                    TB_MESSAGE_LOG

                WHERE
                    received BETWEEN @START_DATE AND @END_DATE))));

DELETE FROM TB_PROPERTY
WHERE
    partproperties_id IN (SELECT
        id_pk
    FROM
        TB_PART_INFO

    WHERE
        payloadinfo_id IN (SELECT
            id_pk
        FROM
            TB_USER_MESSAGE

        WHERE
            messageinfo_id_pk IN (SELECT
                id_pk
            FROM
                TB_MESSAGE_INFO

            WHERE
                message_id IN (SELECT
                    message_id
                FROM
                    TB_MESSAGE_LOG

                WHERE
                    received BETWEEN @START_DATE AND @END_DATE))));

DELETE FROM TB_PROPERTY
WHERE
    messageproperties_id IN (SELECT
        id_pk
    FROM
        TB_USER_MESSAGE

    WHERE
        messageinfo_id_pk IN (SELECT
            id_pk
        FROM
            TB_MESSAGE_INFO

        WHERE
            message_id IN (SELECT
                message_id
            FROM
                TB_MESSAGE_LOG

            WHERE
                received BETWEEN @START_DATE AND @END_DATE)));

DELETE FROM TB_PART_INFO
WHERE
    payloadinfo_id IN (SELECT
        id_pk
    FROM
        TB_USER_MESSAGE

    WHERE
        messageinfo_id_pk IN (SELECT
            id_pk
        FROM
            TB_MESSAGE_INFO

        WHERE
            message_id IN (SELECT
                message_id
            FROM
                TB_MESSAGE_LOG

            WHERE
                received BETWEEN @START_DATE AND @END_DATE)));

DELETE FROM TB_RAWENVELOPE_LOG
WHERE
    usermessage_id_fk IN (SELECT
        id_pk
    FROM
        TB_USER_MESSAGE

    WHERE
        messageinfo_id_pk IN (SELECT
            id_pk
        FROM
            TB_MESSAGE_INFO

        WHERE
            message_id IN (SELECT
                message_id
            FROM
                TB_MESSAGE_LOG

            WHERE
                received BETWEEN @START_DATE AND @END_DATE)));

DELETE FROM TB_RAWENVELOPE_LOG
WHERE
    signalmessage_id_fk IN (SELECT
        id_pk
    FROM
        TB_SIGNAL_MESSAGE

    WHERE
        messageinfo_id_pk IN (SELECT
            id_pk
        FROM
            TB_MESSAGE_INFO

        WHERE
            message_id IN (SELECT
                message_id
            FROM
                TB_MESSAGE_LOG

            WHERE
                received BETWEEN @START_DATE AND @END_DATE)));

DELETE FROM TB_ERROR
WHERE
    signalmessage_id IN (SELECT
        id_pk
    FROM
        TB_SIGNAL_MESSAGE

    WHERE
        messageinfo_id_pk IN (SELECT
            id_pk
        FROM
            TB_MESSAGE_INFO

        WHERE
            message_id IN (SELECT
                message_id
            FROM
                TB_MESSAGE_LOG

            WHERE
                received BETWEEN @START_DATE AND @END_DATE)));

DELETE FROM TB_USER_MESSAGE
WHERE
    messageinfo_id_pk IN (SELECT
        id_pk
    FROM
        TB_MESSAGE_INFO

    WHERE
        message_id IN (SELECT
            message_id
        FROM
            TB_MESSAGE_LOG

        WHERE
            received BETWEEN @START_DATE AND @END_DATE));

DELETE FROM TB_SIGNAL_MESSAGE
WHERE
    messageinfo_id_pk IN (SELECT
        id_pk
    FROM
        TB_MESSAGE_INFO

    WHERE
        message_id IN (SELECT
            message_id
        FROM
            TB_MESSAGE_LOG

        WHERE
            received BETWEEN @START_DATE AND @END_DATE));

DELETE FROM TB_RECEIPT
WHERE
    id_pk IN (SELECT
        receipt_id_pk
    FROM
        TB_SIGNAL_MESSAGE

    WHERE
        messageinfo_id_pk IN (SELECT
            id_pk
        FROM
            TB_MESSAGE_INFO

        WHERE
            message_id IN (SELECT
                message_id
            FROM
                TB_MESSAGE_LOG

            WHERE
                received BETWEEN @START_DATE AND @END_DATE)));

DELETE FROM TB_MESSAGE_INFO
WHERE
    message_id IN (SELECT
        message_id
    FROM
        TB_MESSAGE_LOG

    WHERE
        received BETWEEN @START_DATE AND @END_DATE);

DELETE FROM TB_MESSAGE_LOG
WHERE
    received BETWEEN @START_DATE AND @END_DATE;

DELETE FROM TB_MESSAGE_UI
WHERE
    received BETWEEN @START_DATE AND @END_DATE;


SET SQL_SAFE_UPDATES = @OLD_SQL_SAFE_UPDATES;

COMMIT;