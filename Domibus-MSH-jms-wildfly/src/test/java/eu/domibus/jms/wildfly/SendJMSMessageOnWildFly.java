package eu.domibus.jms.wildfly;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import java.util.Hashtable;
import java.util.Map;

public class SendJMSMessageOnWildFly {

    private static final String PROVIDER_URL = "http-remoting://localhost:8080";
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
            QueueSession qs = qc.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
            QueueSender qsr = qs.createSender(queue);
            final String message = "my custom message";
            TextMessage textMessage = createTextMessage(qs, message, "", null);
            qsr.send(textMessage);
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

    TextMessage createTextMessage(Session session, String message, String messageType, Map<String, String> messageProperties) throws JMSException {
        TextMessage textMessage = session.createTextMessage();
        textMessage.setText(message != null ? message : "");
        if (messageType != null) {
            textMessage.setJMSType(messageType);
        }
        if (messageProperties != null) {
            for (String messageProperty : messageProperties.keySet()) {
                String value = messageProperties.get(messageProperty);
                textMessage.setStringProperty(messageProperty, value);
            }
        }
        return textMessage;
    }

}