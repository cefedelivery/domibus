DELIMITER $$
            create procedure get_next(
            in message_type_in VARCHAR(255),
            in initiator_in VARCHAR(255),
            in mpc_in VARCHAR(255),
            out message_id_out VARCHAR(255))
            BEGIN
            DECLARE no_data_found INT DEFAULT 0;
            DECLARE message_locked INT DEFAULT 0;
            DECLARE reccord_id INT DEFAULT NULL;
            DECLARE pull_reccord CURSOR FOR select ID_PK from TB_MESSAGING_LOCK where MESSAGE_STATE = 'READY' and MPC=mpc_in and INITIATOR=initiator_in AND message_type=message_type_in order by ID_PK;
            DECLARE CONTINUE HANDLER FOR NOT FOUND SET no_data_found = 1;
            DECLARE CONTINUE HANDLER FOR 1205 SET message_locked = 1;
            OPEN pull_reccord;
            my_loop:LOOP
            FETCH pull_reccord INTO reccord_id;
            IF NOT no_data_found THEN
            SELECT ml.MESSAGE_ID into message_id_out FROM TB_MESSAGING_LOCK ml where ml.ID_PK=reccord_id FOR UPDATE;
            IF NOT message_locked THEN
            LEAVE my_loop;
            END IF;
            IF NOT message_locked THEN
            ITERATE my_loop;
            END IF;
            END IF;
            IF no_data_found THEN
            LEAVE my_loop;
            END IF;
            END LOOP;
            CLOSE pull_reccord;
            END$$
            DELIMITER ;