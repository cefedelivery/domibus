package eu.domibus.jms.weblogic;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import weblogic.security.Security;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.security.auth.Subject;
import java.io.InputStream;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Hashtable;
import java.util.Map;

public class SendJMSMessageOnWeblogic {

    private static final String PROVIDER_URL = "t3://localhost:7001";
    private static final String USER = "admin";
    private static final String PASSWORD = "Europa0005";
    private static final String CONNECTION_FACTORY_JNDI = "jms/ConnectionFactory";
    private static final String QUEUE = "jms/domibus.backend.etrustex.inQueue";

    public static void main(String[] args) throws Exception {
        try {
            Security.runAs(new Subject(), new PrivilegedExceptionAction<Object>() {
                @Override
                public Object run()  {
                    new SendJMSMessageOnWeblogic().run();
                    return null;
                }
            });
        } catch (PrivilegedActionException e) {
            throw e;
        }


    }

    @Test
    public void run() throws RuntimeException {
        try {
            InitialContext ic = getInitialContext(PROVIDER_URL, USER, PASSWORD);
            Queue queue = (Queue) ic.lookup(QUEUE);
            QueueConnectionFactory cf = (QueueConnectionFactory) ic.lookup(CONNECTION_FACTORY_JNDI);
            QueueConnection qc = cf.createQueueConnection();
            QueueSession qs = qc.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
            QueueSender qsr = qs.createSender(queue);
            InputStream resourceAsStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("jms/etrustexJmsMessage.xml");
            final String message = IOUtils.toString(resourceAsStream);
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
            env.put(Context.INITIAL_CONTEXT_FACTORY, "weblogic.jndi.WLInitialContextFactory");
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