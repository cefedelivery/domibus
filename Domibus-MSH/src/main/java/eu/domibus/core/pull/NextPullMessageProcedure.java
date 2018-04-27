package eu.domibus.core.pull;

import org.hibernate.Session;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * /**
 *
 * @author Dussart Thomas.
 * @since 3.3.4
 * <p>
 * Proxy class to call the get_next stored procedure. The get_next procedure is in charge of looping through
 * messages of a given type until an unlocked message is found. When the message is found, it is locked at database level
 * and its id is returned for further processing.
 */
@Component
public class NextPullMessageProcedure {

    @PersistenceContext(unitName = "domibusJTA")
    private EntityManager entityManager;

    /**
     * Call the procedure with the given parameters.
     *
     * @param messageType the type of message to search for. For the moment only pull is supported.
     * @param initiator   the initiator of the pull request.
     * @param mpc         the mpc contained in the pull request.
     * @return the id of the locked message or null if nothing to be processed.
     */
    public String callProcedure(final String messageType, final String initiator, final String mpc) {
        NextPullMessageProcedureWork storedProc = new NextPullMessageProcedureWork(messageType, initiator, mpc);
        final Session unwrap = entityManager.unwrap(Session.class);
        unwrap.doWork(storedProc);
        return storedProc.getMessageId();
    }

}


