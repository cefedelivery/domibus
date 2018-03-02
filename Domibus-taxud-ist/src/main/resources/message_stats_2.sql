
   SELECT
    substr(TO_CHAR(incoming_mess_received_date,'HH24')
    || ':'
    || TO_CHAR(incoming_mess_received_date,'mi'),0,4)
    || '0' AS dvf,
    MIN(incoming_ak_dif),
    MAX(incoming_ak_dif),
    AVG(incoming_ak_dif),
    STDDEV(incoming_ak_dif)
FROM
    message_result_split     where INCOMING_MESS_RECEIVED_DATE> TO_DATE( '03/02/2018 08:37:50','MM/DD/YYYY HH24:MI:SS')
GROUP BY
    substr(TO_CHAR(incoming_mess_received_date,'HH24')
    || ':'
    || TO_CHAR(incoming_mess_received_date,'mi'),0,4)
    || '0' ;
    
    
    SELECT    
    MIN(incoming_ak_dif),
    MAX(incoming_ak_dif),
    AVG(incoming_ak_dif),
    STDDEV(incoming_ak_dif),
    MEAN(incoming_ak_dif)
FROM
    message_result_split     where INCOMING_MESS_RECEIVED_DATE> TO_DATE( '03/02/2018 08:37:50','MM/DD/YYYY HH24:MI:SS');
    
    select * from message_result_split  where incoming_ak_dif>600;



-- number of messages executed per time layer.
SELECT  count(q.dif),q.execution_group FROM(
    select incoming_ak_dif as dif , CASE  
        when incoming_ak_dif <1 THEN  'LOWER THAN ONE SECOND'
        when incoming_ak_dif > 1 and incoming_ak_dif <2 THEN  'BETWEEN 1 AND 2 SECONDS'
        when incoming_ak_dif > 2 and incoming_ak_dif <4 THEN  'BETWEEN 2 AND 4 SECONDS'
        when incoming_ak_dif > 4 and incoming_ak_dif <8 THEN  'BETWEEN 4 AND 8 SECONDS'
        when incoming_ak_dif > 8 and incoming_ak_dif <16 THEN  'BETWEEN 8 AND 16 SECONDS'
        else 'MORE THEN 16'
    END as execution_group
    from 
    message_result_split     where INCOMING_MESS_RECEIVED_DATE> TO_DATE( '03/02/2018 08:37:50','MM/DD/YYYY HH24:MI:SS')) q GROUP BY q.execution_group;
    

    --date of first message received and last aknowlegment.
    SELECT min(INCOMING_MESS_RECEIVED_DATE) as startDate,max(OUTGOING_AK_RECEIVED_DATE)as endDate from
    message_result_split where INCOMING_MESS_RECEIVED_DATE> TO_DATE( '03/02/2018 08:37:50','MM/DD/YYYY HH24:MI:SS');

    -- Full loop per seconds : please change (250000) by the total number of message sent.
    select (250000/diff) from(
     select (extract(day FROM endDate - startDate)*24*60*60)+
                    (extract(hour FROM endDate - startDate)*60*60)+
                    (extract(minute FROM endDate - startDate)*60)+
                    extract(second FROM endDate - startDate) as diff
             FROM (SELECT min(INCOMING_MESS_RECEIVED_DATE) as startDate,max(OUTGOING_AK_RECEIVED_DATE)as endDate from
    message_result_split where INCOMING_MESS_RECEIVED_DATE> TO_DATE( '02/20/2018 10:42:40','MM/DD/YYYY HH24:MI:SS')));
    
    
    
        
     SELECT  *    
    FROM
    message_result_split     where INCOMING_MESS_RECEIVED_DATE> TO_DATE( '02/16/2018 17:14','MM/DD/YYYY HH24:MI');

    SELECT DISTINCT MESSAGE_STATUS,MESSAGE_TYPE,count(MESSAGE_STATUS) FROM TB_MESSAGE_LOG GROUP BY MESSAGE_STATUS,MESSAGE_TYPE;
