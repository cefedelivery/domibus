-- C2 sends a message to C3. C3 sends a message with a RefToMessageId set to first messageId. Both are USER_MESSAGES
-- Scenario:
--  T1. C3 receives a message (message1) from C2
--  T2. C3 responds with a signal ACK
--  T3. C3 sends a reply (message2) to C2 with refToMessageId=message1
--  T4. C3 gets the ACK from C2
--  DELAY1 = T3-T1
--  DELAY2 = T4-T1

clear screen
set serveroutput on
DECLARE
    RECEIVED_MESSAGE_DATE TIMESTAMP(6);
    REPLY_SEND_DATE TIMESTAMP(6);
    REPLY_ACK_DATE TIMESTAMP(6);
    ACK_RECEIVE_DATE TIMESTAMP(6);

    REPLY_MESSAGE_ID  VARCHAR2(255 BYTE);
    ACK_MESSAGE_ID VARCHAR2(255 BYTE);
    csv VARCHAR2(3000 BYTE);
    diff VARCHAR2(50 BYTE);
BEGIN
  csv := 'RECEIVED_MESSAGE_ID,RECEIVED_MESSAGE_DATE,REPLY_MESSAGE_ID,REPLY_SEND_DATE,DELAY1(REPLY_SEND_DATE-RECEIVED_MESSAGE_DATE),REPLY_MESSAGE_ID,REPLY_ACK_DATE,DELAY1(REPLY_ACK_DATE-RECEIVED_MESSAGE_DATE)';
  -- csv := csv||chr(13)||chr(10);
  dbms_output.put_line(csv);
    -- loop through all successfully sent messages
  FOR message_rec IN (select * FROM TB_MESSAGE_LOG where MESSAGE_STATUS='RECEIVED' AND MESSAGE_TYPE='USER_MESSAGE' AND MSH_ROLE='RECEIVING')
  LOOP
        --dbms_output.put_line('');
        --dbms_output.put_line('Received message id: ' || message_rec.MESSAGE_ID);
        csv := message_rec.MESSAGE_ID;
        -- retrieve sent date
        select RECEIVED into RECEIVED_MESSAGE_DATE FROM TB_MESSAGE_LOG where MESSAGE_ID=message_rec.MESSAGE_ID;
        --dbms_output.put_line('Received at: ' || RECEIVED_MESSAGE_DATE);
        csv := csv || ',' || RECEIVED_MESSAGE_DATE;
        BEGIN
            -- if there is a reply with refToMessageId, retrieve the date it was received
            select RECEIVED, MESSAGE_ID into REPLY_SEND_DATE, REPLY_MESSAGE_ID FROM TB_MESSAGE_LOG where MESSAGE_ID IN (select MESSAGE_ID FROM TB_MESSAGE_INFO where REF_TO_MESSAGE_ID=message_rec.MESSAGE_ID) AND MESSAGE_TYPE='USER_MESSAGE';
            --dbms_output.put_line('Reply message id: ' || REPLY_MESSAGE_ID);
            --dbms_output.put_line('Reply sent at: ' || REPLY_SEND_DATE);
            --dbms_output.put_line(REPLY_SEND_DATE - RECEIVED_MESSAGE_DATE);

            csv := csv || ',' || REPLY_MESSAGE_ID || ',' || REPLY_SEND_DATE;
            select (extract(day FROM REPLY_SEND_DATE - RECEIVED_MESSAGE_DATE)*24*60*60)+
                    (extract(hour FROM REPLY_SEND_DATE - RECEIVED_MESSAGE_DATE)*60*60)+
                    (extract(minute FROM REPLY_SEND_DATE - RECEIVED_MESSAGE_DATE)*60)+
                    extract(second FROM REPLY_SEND_DATE - RECEIVED_MESSAGE_DATE)
            into diff FROM dual;
            --dbms_output.put_line(diff);
            csv := csv ||  ',' || diff;

            select RECEIVED, MESSAGE_ID into ACK_RECEIVE_DATE, ACK_MESSAGE_ID FROM TB_MESSAGE_LOG where MESSAGE_ID IN (select MESSAGE_ID FROM TB_MESSAGE_INFO where REF_TO_MESSAGE_ID=REPLY_MESSAGE_ID) AND MESSAGE_TYPE='SIGNAL_MESSAGE';
            --dbms_output.put_line('Reply ack message id: ' || ACK_MESSAGE_ID);
            --dbms_output.put_line('Reply ack received at: ' || ACK_RECEIVE_DATE);
            --dbms_output.put_line(ACK_RECEIVE_DATE - RECEIVED_MESSAGE_DATE);
            csv := csv || ',' || ACK_MESSAGE_ID || ',' || ACK_RECEIVE_DATE;
            select (extract(day FROM ACK_RECEIVE_DATE - RECEIVED_MESSAGE_DATE)*24*60*60)+
                    (extract(hour FROM ACK_RECEIVE_DATE - RECEIVED_MESSAGE_DATE)*60*60)+
                    (extract(minute FROM ACK_RECEIVE_DATE - RECEIVED_MESSAGE_DATE)*60)+
                    extract(second FROM ACK_RECEIVE_DATE - RECEIVED_MESSAGE_DATE)
            into diff FROM dual;
            --dbms_output.put_line(diff);
            csv := csv ||  ',' || diff;
        EXCEPTION
            WHEN NO_DATA_FOUND THEN -- there is no reply from C3
            csv := csv ||  ',' || 'NONE' || ',' || 'NULL' || ',' || 'NULL' ||  ',' || 'NULL' || ',' || 'NULL' || ',' || 'NULL';
            --dbms_output.put_line('No reply found.');
            WHEN OTHERS THEN
            csv := csv ||  ',' || 'OTHER' || ',' || 'NULL' || ',' || 'NULL' ||  ',' || 'NULL' || ',' || 'NULL' || ',' || 'NULL';
            --dbms_output.put_line('Others errors');
        END;
        --dbms_output.put_line('');
        dbms_output.put_line(csv);
        -- csv := csv||chr(13)||chr(10);
  END LOOP;
END;
/
