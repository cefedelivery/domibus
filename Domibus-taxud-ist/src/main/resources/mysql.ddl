DROP PROCEDURE IF EXISTS get_next;
create procedure get_next(
  in message_type_in VARCHAR(255),
  in initiator_in VARCHAR(255),
  in mpc_in VARCHAR(255),
  out message_id_out VARCHAR(255))
  proc_label:begin

    DECLARE query_timeout INT DEFAULT FALSE;
    DECLARE reccord_id INT DEFAULT NULL;


    DECLARE pull_reccord CURSOR FOR select ID_PK from TB_MESSAGING_LOCK where MESSAGE_STATE = 'READY' and MPC=mpc_in and INITIATOR=initiator_in AND message_type=message_type_in order by ID_PK;
    DECLARE CONTINUE HANDLER FOR 1205
    -- Error: 1205 SQLSTATE: HY000 (ER_LOCK_WAIT_TIMEOUT)
    BEGIN

    END;
    OPEN pull_reccord;
    my_loop:
    LOOP
      FETCH pull_reccord INTO reccord_id;
      SELECT ml.MESSAGE_ID into message_id_out FROM TB_MESSAGING_LOCK ml where ml.ID_PK=reccord_id FOR UPDATE;
      LEAVE proc_label;
    END LOOP;
    CLOSE pull_reccord;
  END;
