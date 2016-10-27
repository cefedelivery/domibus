package eu.domibus.plugin.jms;

import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;
import java.util.UUID;

public class SendJMSMessageOnTomcat {

    public static void main(String[] args) {
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://localhost:61616");
        Connection connection = null;
        MessageProducer producer = null;
        try {
            connection = connectionFactory.createConnection("domibus", "changeit"); //username and password of the default JMS broker

            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Destination destination = session.createQueue("domibus.backend.jms.inQueue");
            producer = session.createProducer(destination);
            producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);

            MapMessage messageMap = session.createMapMessage();

            // Declare message as submit
            messageMap.setStringProperty("messageType", "submitMessage");
            messageMap.setStringProperty("messageId", UUID.randomUUID().toString());
            // Uncomment to test refToMessageId that is too long, i.e. > 255
            // messageMap.setStringProperty("refToMessageId", "0079a47e-ae1a-4c1b-ad62-2b18ee57e39e@domibus.eu0079a47e-ae1a-4c1b-ad62-2b18ee57e39e@domibus.eu0079a47e-ae1a-4c1b-ad62-2b18ee57e39e@domibus.eu0079a47e-ae1a-4c1b-ad62-2b18ee57e39e@domibus.eu0079a47e-ae1a-4c1b-ad62-2b18ee57e39e@domibus.eu0079a47e-0079a47e-aesasa");

            // Set up the Communication properties for the message
            messageMap.setStringProperty("service", "http://ec.europa.eu/e-delivery/services/tobacco-ecig-reporting");
            //messageMap.setStringProperty("serviceType", "noSecurity");
            //messageMap.setStringProperty("serviceType", "signOnly");
            messageMap.setStringProperty("serviceType", "eDelivery");

            messageMap.setStringProperty("action", "SubmitRequest");
            messageMap.setStringProperty("conversationId", "123");
            //messageMap.setStringProperty("fromPartyId", "urn:oasis:names:tc:ebcore:partyid-type:unregistered:domibus-blue");
            //messageMap.setStringProperty("fromPartyType", ""); // Mandatory but empty here because it is in the value of the party ID
            messageMap.setStringProperty("fromPartyId", "EUCEG_EC");
            messageMap.setStringProperty("fromPartyType", "urn:oasis:names:tc:ebcore:partyid-type:unregistered:EUCEG"); // Mandatory

            messageMap.setStringProperty("fromRole", "http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/initiator");

            //messageMap.setStringProperty("toPartyId", "urn:oasis:names:tc:ebcore:partyid-type:unregistered:domibus-red");
            //messageMap.setStringProperty("toPartyType", ""); // Mandatory but empty here because it is in the value of the party ID
            messageMap.setStringProperty("toPartyId", "EUCEG_000001_AS4");
            messageMap.setStringProperty("toPartyType", "urn:oasis:names:tc:ebcore:partyid-type:unregistered:EUCEG"); // Mandatory

            messageMap.setStringProperty("toRole", "http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/responder");

            messageMap.setStringProperty("originalSender", "urn:oasis:names:tc:ebcore:partyid-type:unregistered:C1");
            messageMap.setStringProperty("finalRecipient", "urn:oasis:names:tc:ebcore:partyid-type:unregistered:C4");
            messageMap.setStringProperty("protocol", "AS4");

            messageMap.setJMSCorrelationID("12345");
            //Set up the payload properties
            messageMap.setStringProperty("totalNumberOfPayloads", "1");
            messageMap.setStringProperty("payload-1-description", "message");
            messageMap.setStringProperty("payload-1-mimeContentId", "cid:message");
            messageMap.setStringProperty("payload-1-mimeType", "application/xml");
            //messageMap.setStringProperty("p1InBody", "true"); // If true payload-1 will be sent in the body of the AS4 message. Only XML payloads may be sent in the AS4 message body. Optional

            String pay1 = "<test>FedeMar</test>";
            byte[] payload = pay1.getBytes();
            messageMap.setBytes("payload-1", payload);

            producer.send(messageMap);

            connection.close();
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}