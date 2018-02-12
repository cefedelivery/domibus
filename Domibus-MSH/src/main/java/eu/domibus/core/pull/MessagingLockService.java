package eu.domibus.core.pull;

import eu.domibus.common.model.configuration.Party;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
public interface MessagingLockService {

    String getPullMessageToProcess(String initiator, String mpc);

    void addLockingInformation(Party initiator, String messageId, String mpc);


    void delete(String messageId);


    void rollback(String messageId);
}
