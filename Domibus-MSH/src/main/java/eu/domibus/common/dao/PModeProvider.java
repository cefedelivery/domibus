/*
 * Copyright 2015 e-CODEX Project
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they
 * will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the
 * Licence.
 * You may obtain a copy of the Licence at:
 * http://ec.europa.eu/idabc/eupl5
 * Unless required by applicable law or agreed to in
 * writing, software distributed under the Licence is
 * distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied.
 * See the Licence for the specific language governing
 * permissions and limitations under the Licence.
 */

package eu.domibus.common.dao;

import eu.domibus.clustering.Command;
import eu.domibus.common.ErrorCode;
import eu.domibus.common.exception.EbMS3Exception;
import eu.domibus.common.model.configuration.*;
import eu.domibus.common.model.org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.AgreementRef;
import eu.domibus.common.model.org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.PartyId;
import eu.domibus.common.model.org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.UserMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.core.JmsOperations;
import org.springframework.jms.core.MessageCreator;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.io.ByteArrayInputStream;
import java.util.Collection;
import java.util.List;

/**
 * @author Christian Koch, Stefan Mueller
 */
public abstract class PModeProvider {
    private static final String EBMS3_TEST_ACTION = "http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/test";
    private static final String EBMS3_TEST_SERVICE = "http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/service"; //TODO: move to appropiate classes
    @Autowired
    protected ConfigurationDAO configurationDAO;
    @PersistenceContext
    protected EntityManager entityManager;
    @Autowired()
    @Qualifier("jaxbContextConfig")
    private JAXBContext jaxbContext;

    @Qualifier("jmsTemplateCommand")
    @Autowired
    private JmsOperations jmsOperations;


    public abstract void init();

    public ConfigurationDAO getConfigurationDAO() {
        return configurationDAO;
    }

    public void setConfigurationDAO(final ConfigurationDAO configurationDAO) {
        this.configurationDAO = configurationDAO;
    }

    @Transactional
    public String findPModeKeyForUserMesssage(final UserMessage userMessage) throws EbMS3Exception {
        final String agreementRef;
        final String senderParty;
        final String receiverParty;
        final String service;
        final String action;
        final String leg;


        try {
            agreementRef = this.findAgreementRef(userMessage.getCollaborationInfo().getAgreementRef());
            senderParty = this.findPartyName(userMessage.getPartyInfo().getFrom().getPartyId());
            receiverParty = this.findPartyName(userMessage.getPartyInfo().getTo().getPartyId());
            service = this.findServiceName(userMessage.getCollaborationInfo().getService());
            action = this.findActionName(userMessage.getCollaborationInfo().getAction());
            leg = this.findLegName(agreementRef, senderParty, receiverParty, service, action);


            if ((action.equals(PModeProvider.EBMS3_TEST_ACTION) && (!service.equals(PModeProvider.EBMS3_TEST_SERVICE)))) {
                throw new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0004, "ebMS3 Test Service: " + PModeProvider.EBMS3_TEST_SERVICE + " and ebMS3 Test Action: " + PModeProvider.EBMS3_TEST_ACTION + " can only be used together [CORE] 5.2.2.9", null, null, null);
            }

            return senderParty + ":" + receiverParty + ":" + service + ":" + action + ":" + agreementRef + ":" + leg;
        } catch (final EbMS3Exception e) {
            e.setRefToMessageId(userMessage.getMessageInfo().getMessageId());
            throw e;
        }
    }

    protected abstract String findLegName(String agreementRef, String senderParty, String receiverParty, String service, String action) throws EbMS3Exception;

    protected abstract String findActionName(String action) throws EbMS3Exception;

    protected abstract String findServiceName(eu.domibus.common.model.org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.Service service) throws EbMS3Exception;

    protected abstract String findPartyName(Collection<PartyId> partyId) throws EbMS3Exception;

    protected abstract String findAgreementRef(AgreementRef agreementRef) throws EbMS3Exception;

    public abstract Party getSenderParty(String pModeKey);

    public abstract Party getReceiverParty(String pModeKey);

    public abstract Service getService(String pModeKey);

    public abstract Action getAction(String pModeKey);

    public abstract Agreement getAgreement(String pModeKey);

    public abstract LegConfiguration getLegConfiguration(String pModeKey);

    public abstract boolean isMpcExistant(String mpc);

    public abstract int getRetentionDownloadedByMpcName(String mpcName);

    public abstract int getRetentionUndownloadedByMpcName(String mpcName);

    protected String getSenderPartyNameFromPModeKey(final String pModeKey) {
        return pModeKey.split(":")[0];
    }

    protected String getReceiverPartyNameFromPModeKey(final String pModeKey) {
        return pModeKey.split(":")[1];
    }

    protected String getServiceNameFromPModeKey(final String pModeKey) {
        return pModeKey.split(":")[2];
    }

    protected String getActionNameFromPModeKey(final String pModeKey) {
        return pModeKey.split(":")[3];
    }

    protected String getAgreementRefNameFromPModeKey(final String pModeKey) {
        return pModeKey.split(":")[4];
    }

    protected String getLegConfigurationNameFromPModeKey(final String pModeKey) {
        return pModeKey.split(":")[5];
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void updatePModes(final byte[] bytes) throws JAXBException {
        final Configuration configuration = (Configuration) this.jaxbContext.createUnmarshaller().unmarshal(new ByteArrayInputStream(bytes));
        this.configurationDAO.updateConfiguration(configuration);
        jmsOperations.send(new ReloadPmodeMessageCreator());

    }


    public abstract List<String> getMpcList();

    public abstract void refresh();

    class ReloadPmodeMessageCreator implements MessageCreator {
        @Override
        public Message createMessage(Session session) throws JMSException {
            Message m = session.createMessage();
            m.setStringProperty(Command.COMMAND, Command.RELOAD_PMODE);
            return m;
        }
    }
}
