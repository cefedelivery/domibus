--Please execute following sql to create the two statistics tables:
--
CREATE TABLE "DOMIBUS_TAXUD"."MESSAGE_RESULT_SPLIT"
   (	"INCOMING_MESS_ID" VARCHAR2(110 BYTE) NOT NULL ENABLE,
	"INCOMING_MESS_RECEIVED_DATE" TIMESTAMP (6) NOT NULL ENABLE,
	"OUTGOING_MESS_ID" VARCHAR2(110 BYTE) NOT NULL ENABLE,
	"OUTGOING_MESS_SAVED_MESS" TIMESTAMP (6) NOT NULL ENABLE,
	"OUTGOING_AK_ID" VARCHAR2(110 BYTE) NOT NULL ENABLE,
	"OUTGOING_AK_RECEIVED_DATE" TIMESTAMP (6) NOT NULL ENABLE,
	"INCOMING_OUTGOING_DIF" FLOAT(126) NOT NULL ENABLE,
	"OUTGOING_ACK_DIF" FLOAT(126) NOT NULL ENABLE,
	"INCOMING_AK_DIF" FLOAT(126) NOT NULL ENABLE
   )

   CREATE TABLE "DOMIBUS_TAXUD"."MESSAGE_RESULT"
   (	"MESSAGE_STAT" VARCHAR2(300 BYTE) NOT NULL);

set serveroutput on size 200000000;
clear screen
set serveroutput on
DECLARE
    RECEIVED_MESSAGE_DATE TIMESTAMP(6);
    REPLY_SEND_DATE TIMESTAMP(6);
    --REPLY_ACK_DATE TIMESTAMP(6);
    ACK_RECEIVE_DATE TIMESTAMP(6);

    INCOMING_MESSAGE_ID  VARCHAR2(255 BYTE);
    REPLY_MESSAGE_ID  VARCHAR2(255 BYTE);
    ACK_MESSAGE_ID VARCHAR2(255 BYTE);
    csv VARCHAR2(3000 BYTE);
    INCOMING_OUTGOING_DIF VARCHAR2(50 BYTE);
    INCOMING_AK_DIF VARCHAR2(50 BYTE);
    OUTGOING_ACK_DIF VARCHAR2(50 BYTE);
    COUNTER NUMBER;
BEGIN
 -- DBMS_OUTPUT.ENABLE(); 
 DELETE  FROM MESSAGE_RESULT;
 DELETE FROM MESSAGE_RESULT_SPLIT;
  csv := 'RECEIVED_MESSAGE_ID,RECEIVED_MESSAGE_DATE,REPLY_MESSAGE_ID,REPLY_SEND_DATE,DELAY1(REPLY_SEND_DATE-RECEIVED_MESSAGE_DATE),REPLY_MESSAGE_ID,REPLY_ACK_DATE,DELAY1(REPLY_ACK_DATE-RECEIVED_MESSAGE_DATE)';
  COUNTER:=0;
  -- csv := csv||chr(13)||chr(10);
  dbms_output.put_line(csv);
    -- loop through all successfully sent messages
  FOR message_rec IN (select * FROM TB_MESSAGE_LOG where MESSAGE_STATUS='DOWNLOADED' AND MESSAGE_TYPE='USER_MESSAGE' AND MSH_ROLE='RECEIVING')
  LOOP

      IF COUNTER >10
      THEN
        EXIT;
      END IF;
      --COUNTER:=COUNTER+1;
        --dbms_output.put_line('');
        --dbms_output.put_line('Received message id: ' || message_rec.MESSAGE_ID);
        INCOMING_MESSAGE_ID:=message_rec.MESSAGE_ID;
        csv := INCOMING_MESSAGE_ID;
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
            into INCOMING_OUTGOING_DIF FROM dual;
            --dbms_output.put_line(diff);
            csv := csv ||  ',' || INCOMING_OUTGOING_DIF;

            select RECEIVED, MESSAGE_ID into ACK_RECEIVE_DATE, ACK_MESSAGE_ID FROM TB_MESSAGE_LOG where MESSAGE_ID IN (select MESSAGE_ID FROM TB_MESSAGE_INFO where REF_TO_MESSAGE_ID=REPLY_MESSAGE_ID) AND MESSAGE_TYPE='SIGNAL_MESSAGE';
            --dbms_output.put_line('Reply ack message id: ' || ACK_MESSAGE_ID);
            --dbms_output.put_line('Reply ack received at: ' || ACK_RECEIVE_DATE);
            --dbms_output.put_line(ACK_RECEIVE_DATE - RECEIVED_MESSAGE_DATE);
            csv := csv || ',' || ACK_MESSAGE_ID || ',' || ACK_RECEIVE_DATE;
            select (extract(day FROM ACK_RECEIVE_DATE - RECEIVED_MESSAGE_DATE)*24*60*60)+
                    (extract(hour FROM ACK_RECEIVE_DATE - RECEIVED_MESSAGE_DATE)*60*60)+
                    (extract(minute FROM ACK_RECEIVE_DATE - RECEIVED_MESSAGE_DATE)*60)+
                    extract(second FROM ACK_RECEIVE_DATE - RECEIVED_MESSAGE_DATE)
            into INCOMING_AK_DIF FROM dual;
            csv := csv ||  ',' || INCOMING_AK_DIF;
            --dbms_output.put_line(diff);
             select (extract(day FROM ACK_RECEIVE_DATE - REPLY_SEND_DATE)*24*60*60)+
                    (extract(hour FROM ACK_RECEIVE_DATE - REPLY_SEND_DATE)*60*60)+
                    (extract(minute FROM ACK_RECEIVE_DATE - REPLY_SEND_DATE)*60)+
                    extract(second FROM ACK_RECEIVE_DATE - REPLY_SEND_DATE)
            into OUTGOING_ACK_DIF FROM dual;
            csv := csv ||  ',' || OUTGOING_ACK_DIF;
        EXCEPTION
            WHEN NO_DATA_FOUND THEN -- there is no reply from C3
            csv := csv ||  ',' || 'NONE' || ',' || 'NULL' || ',' || 'NULL' ||  ',' || 'NULL' || ',' || 'NULL' || ',' || 'NULL';
            --dbms_output.put_line('No reply found.');
            WHEN OTHERS THEN
            csv := csv ||  ',' || 'OTHER' || ',' || 'NULL' || ',' || 'NULL' ||  ',' || 'NULL' || ',' || 'NULL' || ',' || 'NULL';
            --dbms_output.put_line('Others errors');
        END;
        --dbms_output.put_line('');
        insert into MESSAGE_RESULT(MESSAGE_STAT) values(csv);
        insert into MESSAGE_RESULT_SPLIT(
            INCOMING_MESS_ID,
            INCOMING_MESS_RECEIVED_DATE,
            OUTGOING_MESS_ID,
            OUTGOING_MESS_SAVED_MESS,
            OUTGOING_AK_ID,
            OUTGOING_AK_RECEIVED_DATE,
            INCOMING_OUTGOING_DIF,
            OUTGOING_ACK_DIF,
            INCOMING_AK_DIF
        ) VALUES(
            INCOMING_MESSAGE_ID,
            RECEIVED_MESSAGE_DATE,
            REPLY_MESSAGE_ID,
            REPLY_SEND_DATE,
            ACK_MESSAGE_ID,
            ACK_RECEIVE_DATE,
            INCOMING_OUTGOING_DIF,
            OUTGOING_ACK_DIF,
            INCOMING_AK_DIF
            );
        --dbms_output.put_line(csv);
        -- csv := csv||chr(13)||chr(10);
  END LOOP;
END;