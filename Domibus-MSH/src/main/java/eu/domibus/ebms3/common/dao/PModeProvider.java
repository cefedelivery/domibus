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

package eu.domibus.ebms3.common.dao;

import eu.domibus.clustering.Command;
import eu.domibus.common.ErrorCode;
import eu.domibus.common.dao.ConfigurationDAO;
import eu.domibus.common.exception.EbMS3Exception;
import eu.domibus.common.model.configuration.*;
import eu.domibus.common.validators.XmlValidationEventHandler;
import eu.domibus.ebms3.common.model.AgreementRef;
import eu.domibus.ebms3.common.model.PartyId;
import eu.domibus.ebms3.common.model.UserMessage;
import eu.domibus.messaging.XmlProcessingException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.core.JmsOperations;
import org.springframework.jms.core.MessageCreator;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.StringReader;
import java.util.Collection;
import java.util.List;

/**
 * @author Christian Koch, Stefan Mueller
 */
public abstract class PModeProvider {

    private static final Log LOG = LogFactory.getLog(PModeProvider.class);

    public static final String SCHEMAS_DIR = "schemas/";
    public static final String DOMIBUS_PMODE_XSD = "domibus-pmode.xsd";
    public static final String DOMIBUS_CONFIG_LOCATION = "domibus.config.location";

    protected static final String OPTIONAL_AND_EMPTY = "OAE";

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

    public abstract void refresh();

    @Transactional(propagation = Propagation.REQUIRED)
    public void updatePModes(final byte[] bytes) throws XmlProcessingException {
        try {
            Schema schema;
            SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            final InputStream xsdStream = getClass().getClassLoader().getResourceAsStream(SCHEMAS_DIR + DOMIBUS_PMODE_XSD);
            if (xsdStream != null) {
                schema = sf.newSchema(new StreamSource(xsdStream));
            } else {
                String filePath = System.getProperty(DOMIBUS_CONFIG_LOCATION) + "/" + SCHEMAS_DIR;
                schema = sf.newSchema(new File(filePath + DOMIBUS_PMODE_XSD));
            }
            Unmarshaller unmarshaller = this.jaxbContext.createUnmarshaller();
            unmarshaller.setSchema(schema);
            unmarshaller.setEventHandler(new XmlValidationEventHandler());

            SAXParserFactory spf = SAXParserFactory.newInstance();
            spf.setFeature("http://xml.org/sax/features/external-general-entities", false);
            spf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            spf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

            Source xmlSource = new SAXSource(spf.newSAXParser().getXMLReader(), new InputSource(new StringReader(new String(bytes))));

            final Configuration configuration = (Configuration) unmarshaller.unmarshal(xmlSource);
            if (configuration != null) {
                this.configurationDAO.updateConfiguration(configuration);
                LOG.info("Configuration successfully updated");
            }
        } catch (JAXBException | SAXException | ParserConfigurationException xmlEx) {
            if (LOG.isDebugEnabled()) {
                LOG.error("Xml not correctly processed: ", xmlEx);
            } else {
                LOG.error("Xml not correctly processed: ", xmlEx.getCause());
            }
            throw new XmlProcessingException(xmlEx.getCause().getMessage());
        }
        // Sends a message into the topic queue in order to refresh all the singleton instances of the PModeProvider.
        jmsOperations.send(new ReloadPmodeMessageCreator());
    }


    @Transactional(propagation = Propagation.REQUIRED, noRollbackFor = IllegalStateException.class)
    public String findPModeKeyForUserMessage(final UserMessage userMessage) throws EbMS3Exception {

        final String agreementName;
        final String senderParty;
        final String receiverParty;
        final String service;
        final String action;
        final String leg;

        try {
            agreementName = this.findAgreement(userMessage.getCollaborationInfo().getAgreementRef());
            senderParty = this.findPartyName(userMessage.getPartyInfo().getFrom().getPartyId());
            receiverParty = this.findPartyName(userMessage.getPartyInfo().getTo().getPartyId());
            service = this.findServiceName(userMessage.getCollaborationInfo().getService());
            action = this.findActionName(userMessage.getCollaborationInfo().getAction());
            leg = this.findLegName(agreementName, senderParty, receiverParty, service, action);

            if ((action.equals(Action.TEST_ACTION) && (!service.equals(Service.TEST_SERVICE)))) {
                throw new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0004, "ebMS3 Test Service: " + Service.TEST_SERVICE + " and ebMS3 Test Action: " + Action.TEST_ACTION + " can only be used together [CORE] 5.2.2.9", null, null);
            }

            return senderParty + ":" + receiverParty + ":" + service + ":" + action + ":" + agreementName + ":" + leg;

        } catch (final EbMS3Exception e) {
            e.setRefToMessageId(userMessage.getMessageInfo().getMessageId());
            throw e;
        }
    }


    class ReloadPmodeMessageCreator implements MessageCreator {
        @Override
        public Message createMessage(Session session) throws JMSException {
            Message m = session.createMessage();
            m.setStringProperty(Command.COMMAND, Command.RELOAD_PMODE);
            return m;
        }
    }

    public abstract List<String> getMpcList();

    public abstract List<String> getMpcURIList();

    protected abstract String findLegName(String agreementRef, String senderParty, String receiverParty, String service, String action) throws EbMS3Exception;

    protected abstract String findActionName(String action) throws EbMS3Exception;

    protected abstract String findServiceName(eu.domibus.ebms3.common.model.Service service) throws EbMS3Exception;

    protected abstract String findPartyName(Collection<PartyId> partyId) throws EbMS3Exception;

    protected abstract String findAgreement(AgreementRef agreementRef) throws EbMS3Exception;

    public abstract Party getSenderParty(String pModeKey);

    public abstract Party getReceiverParty(String pModeKey);

    public abstract Service getService(String pModeKey);

    public abstract Action getAction(String pModeKey);

    public abstract Agreement getAgreement(String pModeKey);

    public abstract LegConfiguration getLegConfiguration(String pModeKey);

    public abstract boolean isMpcExistant(String mpc);

    public abstract int getRetentionDownloadedByMpcName(String mpcName);

    public abstract int getRetentionDownloadedByMpcURI(final String mpcURI);

    public abstract int getRetentionUndownloadedByMpcName(String mpcName);

    public abstract int getRetentionUndownloadedByMpcURI(final String mpcURI);

    public ConfigurationDAO getConfigurationDAO() {
        return configurationDAO;
    }

    public void setConfigurationDAO(final ConfigurationDAO configurationDAO) {
        this.configurationDAO = configurationDAO;
    }

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

}
