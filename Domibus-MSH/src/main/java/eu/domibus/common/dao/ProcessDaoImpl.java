package eu.domibus.common.dao;

import eu.domibus.common.model.configuration.Party;
import eu.domibus.common.model.configuration.Process;
import eu.domibus.ebms3.common.context.MessageExchangeConfiguration;
import eu.domibus.plugin.BackendConnector;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.util.List;

import static eu.domibus.common.model.configuration.Process.*;

/**
 * @author Thomas Dussart
 * @since 3.3
 * {@inheritDoc}
 */
@Repository
public class ProcessDaoImpl implements ProcessDao{

    private final static String ACTION = "action";
    private final static String SERVICE = "service";
    private final static String AGREEMENT = "agreement";
    private final static String LEG = "leg";
    private final static String INITIATOR_NAME = "initiatorName";
    private final static String RESPONDER_NAME = "responderName";
    private final static String MEP_BINDING = "mepBinding";
    private final static String RESPONDER = "responder";
    private final static String MPC_NAME = "mpcName";
    @PersistenceContext(unitName = "domibusJTA")
    private EntityManager entityManager;

    /**
     *{@inheritDoc}
     */
    @Override
    public List<Process> findProcessByMessageContext(final MessageExchangeConfiguration messageExchangeConfiguration){
        TypedQuery<Process> processQuery= entityManager.createNamedQuery(RETRIEVE_FROM_MESSAGE_CONTEXT,Process.class);
        processQuery.setParameter(ACTION, messageExchangeConfiguration.getAction());
        processQuery.setParameter(SERVICE, messageExchangeConfiguration.getService());
        processQuery.setParameter(AGREEMENT, messageExchangeConfiguration.getAgreementName());
        processQuery.setParameter(LEG, messageExchangeConfiguration.getLeg());
        processQuery.setParameter(INITIATOR_NAME, messageExchangeConfiguration.getSenderParty());
        processQuery.setParameter(RESPONDER_NAME, messageExchangeConfiguration.getReceiverParty());
        return processQuery.getResultList();
    }

    /**
     *{@inheritDoc}
     */
    @Override
    public List<Process> findPullProcessesByResponder(final Party party){
        TypedQuery<Process> processQuery= entityManager.createNamedQuery(FIND_PULL_PROCESS_TO_INITIATE,Process.class);
        processQuery.setParameter(MEP_BINDING,BackendConnector.Mode.PULL.getFileMapping());
        processQuery.setParameter(RESPONDER,party);
        return processQuery.getResultList();
    }


    /**
     *{@inheritDoc}
     */
    @Override
    public List<Process> findPullProcessBytMpc(final String mpc){
        TypedQuery<Process> processQuery= entityManager.createNamedQuery(FIND_PULL_PROCESS_FROM_MPC,Process.class);
        processQuery.setParameter(MEP_BINDING,BackendConnector.Mode.PULL.getFileMapping());
        processQuery.setParameter(MPC_NAME, mpc);
        return processQuery.getResultList();
    }



}
