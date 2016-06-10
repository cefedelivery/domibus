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

package eu.domibus.plugin.webService;

import eu.domibus.common.model.org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.Messaging;
import eu.domibus.common.model.org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.PartInfo;
import eu.domibus.plugin.Submission;
import eu.domibus.plugin.transformer.MessageSubmissionTransformer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.activation.DataHandler;
import javax.mail.util.ByteArrayDataSource;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.IOException;
import java.util.Locale;

import static org.junit.Assert.*;

/**
 * @author Stefan Mueller
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:eu/domibus/plugin/webService/WSMessageTransformerTest/WSMessageTransformerTest-context.xml")
@DirtiesContext
public class WSMessageTransformerTest {

    private static final String RESOURCE_PATH = "src/test/resources/eu/domibus/plugin/webService/WSMessageTransformerTest/";
    private static final String MESSAGING_NO_LANG_DESCRIPTION = "messaging_noLangForDescriptionAvailable.xml";
    private static final String MESSAGING_EMPTY_LANG_DESCRIPTION = "messaging_LangEmptyForDescriptionAvailable.xml";
    private static final String MESSAGING_DESCRIPTION_AND_LANG = "messaging_DescriptionAndLangAvailable.xml";

    private final MessageSubmissionTransformer<Messaging> messagingMessageSubmissionTransformer = new WSMessageTransformer();

    @Autowired
    private JAXBContext jaxbMessagingContext;

    /**
     * Description element is present
     * Lang attribute is not present
     * Expected result is a default lang attribute with value {@code Locale.getDefault()}
     */
    @Test
    public void testTransformToSubmission_NoLangForDescriptionAvailable_DefaultValueNoError() throws JAXBException, IOException {

        Messaging messaging = createMessagingFromFile(RESOURCE_PATH + MESSAGING_NO_LANG_DESCRIPTION);

        assertNotNull(messaging);


        Submission resultOfTransformation = messagingMessageSubmissionTransformer.transformToSubmission(messaging);

        assertNotNull(resultOfTransformation);

        for (Submission.Payload payload : resultOfTransformation.getPayloads()) {
            assertNotNull(payload.getDescription());
            assertNotNull(payload.getDescription().getLang());
            assertNotEquals("", payload.getDescription().getLang());
            assertEquals(Locale.getDefault(), payload.getDescription().getLang());
        }
    }


    /**
     * Description element is present
     * Lang attribute is present but empty
     * Expected result is a default lang attribute with value {@code Locale.getDefault()}
     */
    @Test
    public void testTransformToSubmission_LangEmptyForDescriptionAvailable_DefaultValueNoError() throws IOException, JAXBException {
        Messaging messaging = createMessagingFromFile(RESOURCE_PATH + MESSAGING_EMPTY_LANG_DESCRIPTION);

        assertNotNull(messaging);


        Submission resultOfTransformation = messagingMessageSubmissionTransformer.transformToSubmission(messaging);

        assertNotNull(resultOfTransformation);

        for (Submission.Payload payload : resultOfTransformation.getPayloads()) {
            assertNotNull(payload.getDescription());
            assertNotNull(payload.getDescription().getLang());
            assertNotEquals("", payload.getDescription().getLang());
            assertEquals(Locale.getDefault(), payload.getDescription().getLang());
        }
    }

    /**
     * Description element is present
     * Lang attribute is present and NOT empty
     * Expected result is a match of description value and lang between input and output
     */
    @Test
    public void testTransformToSubmission_DescriptionAndLangAvailable_ValidTransformationNoError() throws IOException, JAXBException {
        Messaging messaging = createMessagingFromFile(RESOURCE_PATH + MESSAGING_DESCRIPTION_AND_LANG);

        assertNotNull(messaging);


        Submission resultOfTransformation = messagingMessageSubmissionTransformer.transformToSubmission(messaging);

        assertNotNull(resultOfTransformation);

        for (Submission.Payload payload : resultOfTransformation.getPayloads()) {
            assertNotNull(payload.getDescription());
            assertNotNull(payload.getDescription().getLang());
            assertNotEquals("", payload.getDescription().getLang());
            assertEquals("en-US".toLowerCase(), payload.getDescription().getLang().getDisplayName());
        }
    }


    private Messaging createMessagingFromFile(String filename) throws IOException, JAXBException {
        Messaging messaging = ((JAXBElement<Messaging>)jaxbMessagingContext.createUnmarshaller().unmarshal(new File(filename))).getValue();
        for (PartInfo partInfo : messaging.getUserMessage().getPayloadInfo().getPartInfo()) {
            partInfo.setPayloadDatahandler(new DataHandler(new ByteArrayDataSource("TESTDATA", "UTF8")));
        }

        return messaging;
    }

}