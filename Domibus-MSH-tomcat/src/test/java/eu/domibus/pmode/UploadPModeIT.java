package eu.domibus.pmode;

import eu.domibus.AbstractIT;
import eu.domibus.api.util.xml.UnmarshallerResult;
import eu.domibus.api.util.xml.XMLUtil;
import eu.domibus.common.dao.ConfigurationDAO;
import eu.domibus.common.dao.ConfigurationRawDAO;
import eu.domibus.common.model.configuration.*;
import eu.domibus.ebms3.common.context.MessageExchangeConfiguration;
import eu.domibus.messaging.XmlProcessingException;
import eu.domibus.web.rest.PModeResource;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.Validate;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import static org.junit.Assert.*;


/**
 * This JUNIT implements the Test cases: UploadPMode - 01, UploadPMode - 02, UploadPMode - 03.
 *
 * @author martifp
 */
@DirtiesContext
@Rollback
@Transactional
public class UploadPModeIT extends AbstractIT {

    public static final String SCHEMAS_DIR = "schemas/";
    public static final String DOMIBUS_PMODE_XSD = "domibus-pmode.xsd";

    private static final String BLUE_2_RED_SERVICE1_ACTION1_PMODE_KEY = "blue_gw" + MessageExchangeConfiguration.PMODEKEY_SEPARATOR +
            "red_gw" + MessageExchangeConfiguration.PMODEKEY_SEPARATOR +
            "testService1" + MessageExchangeConfiguration.PMODEKEY_SEPARATOR +
            "tc1Action" + MessageExchangeConfiguration.PMODEKEY_SEPARATOR +
            "agreement1110" + MessageExchangeConfiguration.PMODEKEY_SEPARATOR + "pushTestcase1tc1Action";

    private static final String PREFIX_MPC_URI = "http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/";


    @Autowired
    PModeResource adminGui;

    @Autowired
    ConfigurationDAO configurationDAO;

    @Autowired()
    @Qualifier("jaxbContextConfig")
    private JAXBContext jaxbContext;

    @Autowired
    XMLUtil xmlUtil;

    @Autowired
    ConfigurationRawDAO configurationRawDAO;

    /**
     * Tests that the PMODE is correctly saved in the DB.
     *
     * @throws IOException
     * @throws XmlProcessingException
     */
    @Test
    public void testSavePModeOk() throws Exception {
        InputStream is = getClass().getClassLoader().getResourceAsStream("samplePModes/domibus-configuration-valid.xml");
        pModeProvider.updatePModes(IOUtils.toByteArray(is), "description");
    }

    /**
     * Tests that the PMode is not saved in the DB because there is a wrong configuration.
     */
    @Test
    public void testSavePModeNOk() throws IOException {
        String pmodeName = "domibus-configuration-xsd-not-compliant.xml";
        InputStream is = getClass().getClassLoader().getResourceAsStream("samplePModes/" + pmodeName);

        MultipartFile pModeContent = new MockMultipartFile("wrong-domibus-configuration", pmodeName, "text/xml", IOUtils.toByteArray(is));
        ResponseEntity<String> response = adminGui.uploadPmodes(pModeContent, "description");
        assertTrue(response.getBody().contains("Failed to upload the PMode file due to"));
    }

    private Configuration testUpdatePModes(final byte[] bytes) throws JAXBException {
        final Configuration configuration = (Configuration) this.jaxbContext.createUnmarshaller().unmarshal(new ByteArrayInputStream(bytes));
        configurationDAO.updateConfiguration(configuration);
        return configuration;
    }

    /**
     * Tests that a subset of the PMODE file content (given a fixed pModeKey) is correctly stored in the DB.
     * <p>
     * PMODE Key  = Initiator Party: Responder Party: Service name: Action name: Agreement: Test case name
     */
    @Test
    public void testVerifyPModeContent() throws IOException, JAXBException {
        InputStream is = getClass().getClassLoader().getResourceAsStream("samplePModes/domibus-configuration-valid.xml");
        Configuration configuration = testUpdatePModes(IOUtils.toByteArray(is));
        // Starts to check that the content of the XML file has actually been saved!
        Party receiverParty = pModeProvider.getReceiverParty(BLUE_2_RED_SERVICE1_ACTION1_PMODE_KEY);
        Validate.notNull(receiverParty, "Responder party was not found");
        Party senderParty = pModeProvider.getSenderParty(BLUE_2_RED_SERVICE1_ACTION1_PMODE_KEY);
        Validate.notNull(senderParty, "Initiator party was not found");
        List<String> parties = new ArrayList<>();
        parties.add(receiverParty.getName());
        parties.add(senderParty.getName());

        boolean partyFound = false;
        Iterator<Party> partyIterator = configuration.getBusinessProcesses().getParties().iterator();
        while (!partyFound && partyIterator.hasNext()) {
            Party party = partyIterator.next();
            partyFound = parties.contains(party.getName());
        }
        assertTrue(partyFound);

        Action savedAction = pModeProvider.getAction(BLUE_2_RED_SERVICE1_ACTION1_PMODE_KEY);
        boolean actionFound = false;
        Iterator<Action> actionIterator = configuration.getBusinessProcesses().getActions().iterator();
        while (!actionFound && actionIterator.hasNext()) {
            Action action = actionIterator.next();
            if (action.getName().equals(savedAction.getName())) {
                actionFound = true;
            }
        }
        assertTrue(actionFound);

        Service savedService = pModeProvider.getService(BLUE_2_RED_SERVICE1_ACTION1_PMODE_KEY);
        boolean serviceFound = false;
        Iterator<Service> serviceIterator = configuration.getBusinessProcesses().getServices().iterator();
        while (!serviceFound && serviceIterator.hasNext()) {
            Service service = serviceIterator.next();
            if (service.getName().equals(savedService.getName())) {
                serviceFound = true;
            }
        }
        assertTrue(serviceFound);

        LegConfiguration savedLegConf = pModeProvider.getLegConfiguration(BLUE_2_RED_SERVICE1_ACTION1_PMODE_KEY);
        boolean legConfFound = false;
        Iterator<LegConfiguration> legConfIterator = configuration.getBusinessProcesses().getLegConfigurations().iterator();
        while (!legConfFound && legConfIterator.hasNext()) {
            LegConfiguration legConf = legConfIterator.next();
            if (legConf.getName().equals(savedLegConf.getName())) {
                legConfFound = true;
            }
        }
        assertTrue(legConfFound);

        Agreement savedAgreement = pModeProvider.getAgreement(BLUE_2_RED_SERVICE1_ACTION1_PMODE_KEY);
        boolean agreementFound = false;
        Iterator<Agreement> agreementIterator = configuration.getBusinessProcesses().getAgreements().iterator();
        while (!agreementFound && agreementIterator.hasNext()) {
            Agreement agreement = agreementIterator.next();
            if (agreement.getName().equals(savedAgreement.getName())) {
                agreementFound = true;
            }
        }
        assertTrue(agreementFound);

        List<String> mpcNames = pModeProvider.getMpcList();
        Map<String, Mpc> savedMpcs = new HashMap<>();
        for (String mpcName : mpcNames) {
            Mpc mpc = new Mpc();
            mpc.setName(mpcName);
            mpc.setQualifiedName(PREFIX_MPC_URI + mpcName);
            mpc.setDefault(true);
            mpc.setEnabled(true);
            mpc.setRetentionDownloaded(pModeProvider.getRetentionDownloadedByMpcURI(mpc.getQualifiedName()));
            mpc.setRetentionUndownloaded(pModeProvider.getRetentionUndownloadedByMpcURI(mpc.getQualifiedName()));
            savedMpcs.put(mpcName, mpc);
        }

        for (Mpc mpc : configuration.getMpcs()) {
            Mpc savedMpc = savedMpcs.get(mpc.getName());
            assertNotNull(savedMpc);
            assertEquals(mpc.getName(), savedMpc.getName());
            assertEquals(mpc.getQualifiedName(), savedMpc.getQualifiedName());
            assertEquals(mpc.getRetentionDownloaded(), savedMpc.getRetentionDownloaded());
            assertEquals(mpc.getRetentionUndownloaded(), savedMpc.getRetentionUndownloaded());
        }
    }

    /**
     * Tests that the PMode is not saved in the DB because there is a validation error (maxLength exceeded).
     */
    @Test
    public void testSavePModeValidationError() throws IOException {
        String pmodeName = "domibus-configuration-long-names.xml";
        InputStream is = getClass().getClassLoader().getResourceAsStream("samplePModes/" + pmodeName);
        MultipartFile pModeContent = new MockMultipartFile("domibus-configuration-long-names", pmodeName, "text/xml", IOUtils.toByteArray(is));
        ResponseEntity<String> response = adminGui.uploadPmodes(pModeContent, "description");
        assertTrue(response.getBody().contains("is not facet-valid with respect to maxLength"));
    }

    /**
     * Tests that a PMODE can be serialized/deserialized properly.
     */
    @Test
    public void testVerifyPartyListUpdate() throws Exception {
        InputStream is = getClass().getClassLoader().getResourceAsStream("samplePModes/domibus-configuration-valid.xml");
        byte[] bytes = IOUtils.toByteArray(is);

        InputStream xsdStream = getClass().getClassLoader().getResourceAsStream(SCHEMAS_DIR + DOMIBUS_PMODE_XSD);
        ByteArrayInputStream xmlStream = new ByteArrayInputStream(bytes);

        UnmarshallerResult unmarshallerResult = xmlUtil.unmarshal(true, jaxbContext, xmlStream, xsdStream);
        Configuration configuration = unmarshallerResult.getResult();

        pModeProvider.serializePModeConfiguration(configuration);
    }
}
