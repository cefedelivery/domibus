package eu.domibus.common.dao;

import eu.domibus.common.model.configuration.Party;
import eu.domibus.common.model.configuration.Process;
import eu.domibus.ebms3.common.context.MessageExchangeContext;
import eu.domibus.plugin.BackendConnector;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.util.List;

import static eu.domibus.common.model.configuration.Process.FIND_PULL_PROCESS;
import static eu.domibus.common.model.configuration.Process.RETRIEVE_FROM_MESSAGE_CONTEXT;

/**
 * Created by dussath on 5/18/17.
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
    private final static String INITIATOR = "initiator";
    @PersistenceContext(unitName = "domibusJTA")
    private EntityManager entityManager;

    /**
     *{@inheritDoc}
     */
    @Override
    public List<Process> findProcessForMessageContext(final MessageExchangeContext messageExchangeContext){
        TypedQuery<Process> processQuery= entityManager.createNamedQuery(RETRIEVE_FROM_MESSAGE_CONTEXT,Process.class);
        processQuery.setParameter(ACTION,messageExchangeContext.getAction());
        processQuery.setParameter(SERVICE,messageExchangeContext.getService());
        processQuery.setParameter(AGREEMENT,messageExchangeContext.getAgreementName());
        processQuery.setParameter(LEG,messageExchangeContext.getLeg());
        processQuery.setParameter(INITIATOR_NAME,messageExchangeContext.getSenderParty());
        processQuery.setParameter(RESPONDER_NAME,messageExchangeContext.getReceiverParty());
        return processQuery.getResultList();
    }

    /**
     *{@inheritDoc}
     */
    @Override
    public List<Process> findPullProcessesByIniator(final Party party){
        TypedQuery<Process> processQuery= entityManager.createNamedQuery(FIND_PULL_PROCESS,Process.class);
        processQuery.setParameter(MEP_BINDING,BackendConnector.Mode.PULL.getFileMapping());
        processQuery.setParameter(INITIATOR,party);
        return processQuery.getResultList();
    }



}
