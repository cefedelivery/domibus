package eu.domibus.jms.wildfly;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import java.util.Hashtable;
import java.util.UUID;

public class SendJMSMessageOnWildFly {

    private static final String PROVIDER_URL = "http-remoting://localhost:8081";
    //the user has to have the necessary roles; the security configuration is done in the WildFly profile under  <security-settings>
    private static final String USER = "jmssender";
    private static final String PASSWORD = "jmssender";
    private static final String CONNECTION_FACTORY_JNDI = "jms/RemoteConnectionFactory";
    private static final String QUEUE = "jms/domibus.backend.jms.inQueue";

    public static void main(String[] args) throws Exception {
        new SendJMSMessageOnWildFly().run();
    }

    public void run() throws RuntimeException {
        try {
            InitialContext ic = getInitialContext(PROVIDER_URL, USER, PASSWORD);
            Queue queue = (Queue) ic.lookup(QUEUE);
            QueueConnectionFactory cf = (QueueConnectionFactory) ic.lookup(CONNECTION_FACTORY_JNDI);
            QueueConnection qc = cf.createQueueConnection(USER, PASSWORD);
            QueueSession queueSession = qc.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
            QueueSender queueSessionSender = queueSession.createSender(queue);
            MapMessage mapMessage = createMapMessage(queueSession);
            queueSessionSender.send(mapMessage);
            ic.close();
            System.out.println("Successfully sent message");
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    InitialContext getInitialContext(String providerUrl, String userName, String password) throws Exception {
        InitialContext ic = null;
        if (providerUrl != null) {
            Hashtable<String, String> env = new Hashtable<String, String>();
            env.put(Context.PROVIDER_URL, providerUrl);
            env.put(Context.INITIAL_CONTEXT_FACTORY, "org.jboss.naming.remote.client.InitialContextFactory");
            if (userName != null) {
                env.put(Context.SECURITY_PRINCIPAL, userName);
            }
            if (password != null) {
                env.put(Context.SECURITY_CREDENTIALS, password);
            }
            ic = new InitialContext(env);
        } else {
            ic = new InitialContext();
        }
        return ic;
    }

    protected MapMessage createMapMessage(Session session) throws JMSException {
        MapMessage messageMap = session.createMapMessage();

        // Declare message as submit
        messageMap.setStringProperty("messageType", "submitMessage");
        messageMap.setStringProperty("messageId", UUID.randomUUID().toString());
        // Uncomment to test refToMessageId that is too long, i.e. > 255
        // messageMap.setStringProperty("refToMessageId", "0079a47e-ae1a-4c1b-ad62-2b18ee57e39e@domibus.eu0079a47e-ae1a-4c1b-ad62-2b18ee57e39e@domibus.eu0079a47e-ae1a-4c1b-ad62-2b18ee57e39e@domibus.eu0079a47e-ae1a-4c1b-ad62-2b18ee57e39e@domibus.eu0079a47e-ae1a-4c1b-ad62-2b18ee57e39e@domibus.eu0079a47e-0079a47e-aesasa");

        // Set up the Communication properties for the message
        messageMap.setStringProperty("service", "bdx:noprocess");
        //messageMap.setStringProperty("serviceType", "noSecurity");
        //messageMap.setStringProperty("serviceType", "signOnly");
        messageMap.setStringProperty("serviceType", "tc1");

        messageMap.setStringProperty("action", "TC1Leg1");
        messageMap.setStringProperty("conversationId", "123");
        //messageMap.setStringProperty("fromPartyId", "urn:oasis:names:tc:ebcore:partyid-type:unregistered:domibus-blue");
        //messageMap.setStringProperty("fromPartyType", ""); // Mandatory but empty here because it is in the value of the party ID
        messageMap.setStringProperty("fromPartyId", "domibus-blue");
        messageMap.setStringProperty("fromPartyType", "urn:oasis:names:tc:ebcore:partyid-type:unregistered"); // Mandatory

        messageMap.setStringProperty("fromRole", "http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/initiator");

        //messageMap.setStringProperty("toPartyId", "urn:oasis:names:tc:ebcore:partyid-type:unregistered:domibus-red");
        //messageMap.setStringProperty("toPartyType", ""); // Mandatory but empty here because it is in the value of the party ID
        messageMap.setStringProperty("toPartyId", "domibus-red");
        messageMap.setStringProperty("toPartyType", "urn:oasis:names:tc:ebcore:partyid-type:unregistered"); // Mandatory

        messageMap.setStringProperty("toRole", "http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/responder");

        messageMap.setStringProperty("originalSender", "urn:oasis:names:tc:ebcore:partyid-type:unregistered:C1");
        messageMap.setStringProperty("finalRecipient", "urn:oasis:names:tc:ebcore:partyid-type:unregistered:C4");
        messageMap.setStringProperty("protocol", "AS4");

        messageMap.setJMSCorrelationID("12345");
        //Set up the payload properties
        messageMap.setStringProperty("totalNumberOfPayloads", "1");
        messageMap.setStringProperty("payload_1_description", "message");
        messageMap.setStringProperty("payload_1_mimeContentId", "cid:message");
        messageMap.setStringProperty("payload_1_mimeType", "text/xml");

        //messageMap.setStringProperty("p1InBody", "true"); // If true payload_1 will be sent in the body of the AS4 message. Only XML payloads may be sent in the AS4 message body. Optional

        //send the payload in the JMS message as byte array
        String pay1 = "<test>test</test>";
        byte[] payload = pay1.getBytes();
        messageMap.setBytes("payload_1", payload);


        //send the payload as a file system reference
//        messageMap.setStringProperty("payload_1_fileName", "1_2GB.zip");
//        messageMap.setString("payload_1", "file:////C:/DEV/1_2GB.zip");

        return messageMap;
    }

}