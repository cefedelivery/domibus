package eu.domibus;

import eu.domibus.common.NotificationType;
import eu.domibus.messaging.MessageConstants;
import eu.domibus.messaging.NotifyMessageCreator;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.jms.*;
import javax.sql.DataSource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

/**
 * Created by feriaad on 02/02/2016.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:spring-context.xml")
@DirtiesContext
@Rollback
public abstract class AbstractIT {

    protected static final String WS_NOT_QUEUE = "domibus.notification.webservice";

    protected static final String JMS_NOT_QUEUE_NAME = "domibus.notification.jms";


    private static boolean initialized;

    @Autowired
    protected DataSource dataSource;

    @BeforeClass
    public static void init() throws IOException {
        if (!initialized) {
            FileUtils.deleteDirectory(new File("target/temp"));
            System.setProperty("domibus.config.location", new File("target/test-classes").getAbsolutePath());
            initialized = true;
        }
    }

    /**
     * Execute the given input stream in the given database connection
     *
     * @param conn
     * @param in
     * @throws SQLException
     */
    public static void importSQL(Connection conn, InputStream in) throws SQLException {
        Scanner s = new Scanner(in);
        s.useDelimiter("(;(\r)?\n)|(--\n)");
        Statement st = null;
        try {
            st = conn.createStatement();
            while (s.hasNext()) {
                String line = s.next();
                if (line.startsWith("/*!") && line.endsWith("*/")) {
                    int i = line.indexOf(' ');
                    line = line.substring(i + 1, line.length() - " */".length());
                }

                if (line.trim().length() > 0) {
                    st.execute(line);
                }
            }
        } finally {
            if (st != null) st.close();
        }
    }

    /**
     * Convert the given file to a string
     *
     * @param file
     * @return
     */
    protected String getAS4Response(String file) {
        try {
            DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            InputSource is = new InputSource();
            is.setCharacterStream(new FileReader("target/test-classes/dataset/as4/" + file));
            Document doc = db.parse(is);
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = null;
            transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(doc), new StreamResult(writer));
            return writer.getBuffer().toString().replaceAll("\n|\r", "");
        } catch (Exception exc) {
            Assert.fail(exc.getMessage());
            exc.printStackTrace();
        }
        return null;
    }

    /**
     * Insert the given dataset inside the database
     *
     * @param dataset
     */
    protected void insertDataset(String dataset) {
        try {
            FileInputStream fis = new FileInputStream(new File("target/test-classes/dataset/database/" + dataset).getAbsolutePath());
            importSQL(dataSource.getConnection(), fis);
        } catch (final Exception exc) {
            Assert.fail(exc.getMessage());
            exc.printStackTrace();
        }
    }

    protected void pushQueueMessage(String messageId, javax.jms.Connection connection, String queueName) {

        try {
            connection.start();
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Destination destination = session.createQueue(queueName);
            MessageProducer producer = session.createProducer(destination);
            // Creates the Message using Spring MessageCreator
            NotifyMessageCreator messageCreator = new NotifyMessageCreator(messageId, NotificationType.MESSAGE_RECEIVED);
            Message msg = messageCreator.createMessage(session);
            msg.setStringProperty(MessageConstants.ENDPOINT, "backendInterfaceEndpoint");
            producer.send(msg);
            System.out.println("Message with ID [:" + messageId + "] sent in queue!");
            producer.close();
            session.close();
            connection.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    protected Message popQueueMessage(javax.jms.Connection connection, String queueName) {

        try {
            connection.start();
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Destination destination = session.createQueue(queueName);
            MessageConsumer consumer = session.createConsumer(destination);
            Message message = consumer.receive();
            System.out.println("Message with ID [:" + message.getStringProperty("MESSAGE_ID") + "] consumed from queue [" + message.getJMSDestination() + "]");
            consumer.close();
            session.close();
            connection.close();
            return message;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }
}