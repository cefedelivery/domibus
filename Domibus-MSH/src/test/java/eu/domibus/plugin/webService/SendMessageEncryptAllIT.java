package eu.domibus.plugin.webService;
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


import com.github.tomakehurst.wiremock.junit.WireMockRule;
import eu.domibus.AbstractSendMessageIT;
import eu.domibus.common.model.org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.Messaging;
import eu.domibus.ebms3.sender.NonRepudiationChecker;
import eu.domibus.ebms3.sender.ReliabilityChecker;
import eu.domibus.plugin.webService.generated.BackendInterface;
import eu.domibus.plugin.webService.generated.SendMessageFault;
import eu.domibus.plugin.webService.generated.SendRequest;
import eu.domibus.plugin.webService.generated.SendResponse;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

/**
 * Created by draguio on 17/02/2016.
 */
public class SendMessageEncryptAllIT extends AbstractSendMessageIT {

    private static boolean initialized;
    @Rule
    public WireMockRule wireMockRule = new WireMockRule(SERVICE_PORT);
    @Autowired
    BackendInterface backendWebService;
    @Mock
    NonRepudiationChecker nonRepudiationChecker;
    @InjectMocks
    @Autowired
    private ReliabilityChecker reliabilityChecker;

    @Before
    public void before() throws IOException {
        if (!initialized) {
            // The dataset is executed only once for each class
            insertDataset("sendMessageEncryptAllDataset.sql");
            initialized = true;
        }
    }

    /**
     * Test for the backend sendMessage service with payload profile enabled
     *
     * @throws SendMessageFault
     * @throws InterruptedException
     */
    @Test
    public void testSendMessageValid() throws SendMessageFault, InterruptedException {
/*
        String payloadHref = "payload";
        SendRequest sendRequest = createSendRequest(payloadHref);
        Messaging ebMSHeaderInfo = createMessageHeader(payloadHref);

        SendResponse response = backendWebService.sendMessage(sendRequest, ebMSHeaderInfo);

        TimeUnit.SECONDS.sleep(4);

        Assert.assertNotNull(response);

        verify(postRequestedFor(urlMatching("/domibus/services/msh"))
                .withRequestBody(containing("EncryptionMethod Algorithm=\"http://www.w3.org/2001/04/xmlenc#rsa-oaep-mgf1p\""))
                .withHeader("Content-Type", notMatching("application/soap+xml")));
                */
    }
}
