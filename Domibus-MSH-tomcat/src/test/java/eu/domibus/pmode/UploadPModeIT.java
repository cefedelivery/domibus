package eu.domibus.pmode;

import eu.domibus.AbstractIT;
import eu.domibus.common.model.configuration.*;
import eu.domibus.ebms3.common.dao.PModeProvider;
import eu.domibus.messaging.XmlProcessingException;
import eu.domibus.web.rest.PModeResource;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.Validate;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;


/**
 * This JUNIT implements the Test cases: UploadPMode - 01, UploadPMode - 02, UploadPMode - 03.
 *
 * @author martifp
 */
@Transactional
public class UploadPModeIT extends AbstractIT {

    private static final String BLUE_2_RED_SERVICE1_ACTION1_PMODE_KEY = "blue_gw:red_gw:testService1:tc1Action:agreement1110:pushTestcase1tc1Action";

    private static final String PREFIX_MPC_URI = "http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/";

    private static boolean initialized;
    @Autowired
    PModeResource adminGui;
    @Autowired
    PModeProvider pModeProvider;
    /*@Autowired
    PModeProvider pModeDao;*/
    @Autowired()
    @Qualifier("jaxbContextConfig")
    private JAXBContext jaxbContext;

    @Before
    public void before() {
        if (!initialized) {
            initialized = true;
        }
    }


    /**
     * Tests that the PMODE is correctly saved in the DB.
     *
     * @throws IOException
     * @throws XmlProcessingException
     */
    @Test
    public void testSavePModeOk() throws IOException, XmlProcessingException {

        try {
            InputStream is = getClass().getClassLoader().getResourceAsStream("samplePModes/domibus-configuration-valid.xml");
            //MultipartFile pModeContent = new MockMultipartFile("domibus-configuration-blue_gw", pModeFile.getName(), "text/xml", IOUtils.toByteArray(fis));
            //String response = adminGui.uploadFileHandler(pModeContent);
            pModeProvider.updatePModes(IOUtils.toByteArray(is));
            //Assert.assertEquals("You successfully uploaded the PMode file.", response);
        } catch (IOException ioEx) {
            System.out.println("File reading error: " + ioEx.getMessage());
            throw ioEx;
        } catch (XmlProcessingException xpEx) {
            System.out.println("XML error: " + xpEx.getMessage());
            throw xpEx;
        }
    }

    /**
     * Tests that the PMode is not saved in the DB because there is a wrong configuration.
     */
    @Test
    public void testSavePModeNOk() throws IOException {


        try {
            String pmodeName = "domibus-configuration-xsd-not-compliant.xml";
            InputStream is = getClass().getClassLoader().getResourceAsStream("samplePModes/" + pmodeName);

            MultipartFile pModeContent = new MockMultipartFile("wrong-domibus-configuration", pmodeName, "text/xml", IOUtils.toByteArray(is));
            ResponseEntity<String> response = adminGui.uploadPmodes(pModeContent);
            Assert.assertTrue(response.getBody().contains("Failed to upload the PMode file due to"));
        } catch (IOException ioEx) {
            System.out.println("Error: " + ioEx.getMessage());
            throw ioEx;
        }
    }

    @Transactional(propagation = Propagation.REQUIRED)
    private Configuration testUpdatePModes(final byte[] bytes) throws JAXBException {
        final Configuration configuration = (Configuration) this.jaxbContext.createUnmarshaller().unmarshal(new ByteArrayInputStream(bytes));
        pModeProvider.getConfigurationDAO().updateConfiguration(configuration);
        return configuration;
    }

    /**
     * Tests that a subset of the PMODE file content (given a fixed pModeKey) is correctly stored in the DB.
     *
     * PMODE Key  = Initiator Party: Responder Party: Service name: Action name: Agreement: Test case name
     */
    @Test
    public void testVerifyPModeContent() throws IOException, JAXBException {

        try {
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
            Assert.assertTrue(partyFound);

            Action savedAction = pModeProvider.getAction(BLUE_2_RED_SERVICE1_ACTION1_PMODE_KEY);
            boolean actionFound = false;
            Iterator<Action> actionIterator = configuration.getBusinessProcesses().getActions().iterator();
            while (!actionFound && actionIterator.hasNext()) {
                Action action = actionIterator.next();
                if (action.getName().equals(savedAction.getName())) {
                    actionFound = true;
                }
            }
            Assert.assertTrue(actionFound);

            Service savedService = pModeProvider.getService(BLUE_2_RED_SERVICE1_ACTION1_PMODE_KEY);
            boolean serviceFound = false;
            Iterator<Service> serviceIterator = configuration.getBusinessProcesses().getServices().iterator();
            while (!serviceFound && serviceIterator.hasNext()) {
                Service service = serviceIterator.next();
                if (service.getName().equals(savedService.getName())) {
                    serviceFound = true;
                }
            }
            Assert.assertTrue(serviceFound);

            LegConfiguration savedLegConf = pModeProvider.getLegConfiguration(BLUE_2_RED_SERVICE1_ACTION1_PMODE_KEY);
            boolean legConfFound = false;
            Iterator<LegConfiguration> legConfIterator = configuration.getBusinessProcesses().getLegConfigurations().iterator();
            while (!legConfFound && legConfIterator.hasNext()) {
                LegConfiguration legConf = legConfIterator.next();
                if (legConf.getName().equals(savedLegConf.getName())) {
                    legConfFound = true;
                }
            }
            Assert.assertTrue(legConfFound);

            Agreement savedAgreement = pModeProvider.getAgreement(BLUE_2_RED_SERVICE1_ACTION1_PMODE_KEY);
            boolean agreementFound = false;
            Iterator<Agreement> agreementIterator = configuration.getBusinessProcesses().getAgreements().iterator();
            while (!agreementFound && agreementIterator.hasNext()) {
                Agreement agreement = agreementIterator.next();
                if (agreement.getName().equals(savedAgreement.getName())) {
                    agreementFound = true;
                }
            }
            Assert.assertTrue(agreementFound);

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
                Assert.assertNotNull(savedMpc);
                // Assert.assertEquals(mpc, savedMpc); This strangely seeems to work only with Strings!
                //Assert.assertTrue(mpc.equals(savedMpc)); equals and hashcode are based on hibernate Ids !
                Assert.assertEquals(mpc.getName(), savedMpc.getName());
                Assert.assertEquals(mpc.getQualifiedName(), savedMpc.getQualifiedName());
                Assert.assertEquals(mpc.getRetentionDownloaded(), savedMpc.getRetentionDownloaded());
                Assert.assertEquals(mpc.getRetentionUndownloaded(), savedMpc.getRetentionUndownloaded());
            }

        } catch (IOException ioEx) {
            System.out.println("Error: " + ioEx.getMessage());
            throw ioEx;
        } catch (JAXBException jEx) {
            System.out.println("JAXB error: " + jEx.getMessage());
            throw jEx;
        }
    }


    /**
     * Tests that the PMode is not saved in the DB because there is a validation error (maxLength exceeded).
     */
    @Test
    public void testSavePModeValidationError() throws IOException {

        try {
            String pmodeName = "domibus-configuration-long-names.xml";
            InputStream is = getClass().getClassLoader().getResourceAsStream("samplePModes/" + pmodeName);
            MultipartFile pModeContent = new MockMultipartFile("domibus-configuration-long-names", pmodeName, "text/xml", IOUtils.toByteArray(is));
            ResponseEntity<String> response = adminGui.uploadPmodes(pModeContent);
            Assert.assertTrue(response.getBody().contains("is not facet-valid with respect to maxLength"));
        } catch (IOException ioEx) {
            System.out.println("Error: " + ioEx.getMessage());
            throw ioEx;
        }
    }
}
