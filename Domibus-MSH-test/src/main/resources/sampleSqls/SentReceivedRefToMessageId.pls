-- C2 sends a message to C3. C3 sends a message with a RefToMessageId set to first messageId. Both are USER_MESSAGES
-- The script retrieves for each pair the date of the first message, the date when the reply arrived to C2 (if any)

clear screen
set serveroutput on
DECLARE 
    SENT_DATE TIMESTAMP(6);
    RECEIVED_DATE TIMESTAMP(6);
    message_id  VARCHAR2(255 BYTE);
BEGIN
    -- loop through all successfully sent messages
  FOR message_rec IN (select * FROM TB_MESSAGE_LOG where MESSAGE_STATUS='ACKNOWLEDGED' AND MESSAGE_TYPE='USER_MESSAGE' AND MSH_ROLE='SENDING')
  LOOP
        dbms_output.put_line('');
        dbms_output.put_line('Message_id: ' || message_rec.MESSAGE_ID);
        -- retrieve sent date
        select RECEIVED into SENT_DATE FROM TB_MESSAGE_LOG where MESSAGE_ID=message_rec.MESSAGE_ID;
        dbms_output.put_line('Sent at: ' || SENT_DATE);
        BEGIN
            -- if there is a reply with refToMessageId, retrieve the date it was received
            select RECEIVED into RECEIVED_DATE FROM TB_MESSAGE_LOG where MESSAGE_ID IN (select MESSAGE_ID FROM TB_MESSAGE_INFO where REF_TO_MESSAGE_ID=message_rec.MESSAGE_ID) AND MESSAGE_TYPE='USER_MESSAGE';
            dbms_output.put_line('Received at: ' || RECEIVED_DATE);
            dbms_output.put_line(RECEIVED_DATE - SENT_DATE);
        EXCEPTION
            WHEN NO_DATA_FOUND THEN -- there is no reply from C3
            dbms_output.put_line('No reply found.');    
        END;    
        dbms_output.put_line('');
  END LOOP;

END;
/
