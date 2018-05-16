DELIMITER $$
  CREATE PROCEDURE get_next(IN  message_type_in VARCHAR(255), IN initiator_in VARCHAR(255), IN mpc_in VARCHAR(255),
                          OUT message_id_out  VARCHAR(255))
  BEGIN
    DECLARE no_data_found INT DEFAULT 0;
    DECLARE loop_count INT DEFAULT 0;
    DECLARE retry_count INT DEFAULT 0;
    DECLARE message_locked BOOLEAN;
    DECLARE reccord_id INT DEFAULT NULL;
    DECLARE pull_reccord CURSOR FOR select ID_PK from TB_MESSAGING_LOCK where MESSAGE_STATE = 'READY' and MPC=mpc_in and INITIATOR=initiator_in AND message_type=message_type_in order by ID_PK;
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET no_data_found = 1;
    DECLARE CONTINUE HANDLER FOR 1205 BEGIN END;
    DECLARE CONTINUE HANDLER FOR 1213 BEGIN END;
    OPEN pull_reccord;
    my_loop:LOOP
      set loop_count=loop_count+1;
      set message_locked=FALSE ;
      set no_data_found=0;
      set reccord_id=null;
      FETCH pull_reccord INTO reccord_id;
      IF NOT no_data_found THEN
        UPDATE TB_MESSAGING_LOCK ml set ml.MESSAGE_STATE=ml.MESSAGE_STATE where ml.ID_PK=reccord_id;
        SELECT ml.MESSAGE_ID into message_id_out FROM TB_MESSAGING_LOCK ml where ml.ID_PK=reccord_id FOR UPDATE;
        IF message_id_out is NOT NULL
        THEN
          LEAVE my_loop;
        END IF;
      END IF;
      IF no_data_found and reccord_id is NULL THEN
        LEAVE my_loop;
      END IF;
  END LOOP;
CLOSE pull_reccord;
END$$
DELIMITER ;