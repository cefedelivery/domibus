package utils.soap_client;

import eu.domibus.common.model.org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.Messaging;
import eu.domibus.plugin.webService.generated.*;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import javax.xml.soap.AttachmentPart;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.Dispatch;
import javax.xml.ws.soap.SOAPBinding;
import java.io.File;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.UUID;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

/**
 * @author Catalin Comanici
 * @description:
 * @since 4.1
 */
public class MyMessageSender {

	private static final String TEST_SUBMIT_MESSAGE_SUBMITREQUEST = "src\\main\\resources\\eu\\domibus\\example\\ws\\submitMessage_submitRequest.xml";
	private static final String TEST_SUBMIT_MESSAGE_MESSAGING = "src\\main\\resources\\eu\\domibus\\example\\ws\\submitMessage_messaging.xml";
	private static final String SAMPLE_MSH_MESSAGE = "src\\main\\resources\\eu\\domibus\\example\\ws\\sampleMSHMessage.xml";
	private static String mshWSLoc;

	public SubmitResponse sendMessage(String pluginU, String password) throws Exception{
		WebserviceExample webserviceExample = new WebserviceExample();
        BackendInterface backendInterface = webserviceExample.getPort(pluginU, password);


        SubmitRequest submitRequest = Helper.parseSendRequestXML(TEST_SUBMIT_MESSAGE_SUBMITREQUEST,SubmitRequest.class);
        Messaging messaging = Helper.parseMessagingXML(TEST_SUBMIT_MESSAGE_MESSAGING);

        SubmitResponse result = backendInterface.submitMessage(submitRequest, messaging);

        return result;
	}

	private static class Helper {
        private static JAXBContext jaxbMessagingContext;
        private static JAXBContext jaxbWebserviceContext;
        private static MessageFactory messageFactory = new com.sun.xml.internal.messaging.saaj.soap.ver1_2.SOAPMessageFactory1_2Impl();
        private static final String LINE_SEPARATOR = System.getProperty("line.separator");

        static {
            try {
                jaxbMessagingContext = JAXBContext.newInstance("eu.domibus.common.model.org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704");
                jaxbWebserviceContext = JAXBContext.newInstance("eu.domibus.plugin.webService.generated");
            } catch (JAXBException e) {
                throw new RuntimeException("Initialization of Helper class failed.");
            }

        }

        private static <E> E parseSendRequestXML(final String uriSendRequestXML,Class<E> requestType) throws Exception {
            return (E) jaxbWebserviceContext.createUnmarshaller().unmarshal(new File(uriSendRequestXML));
        }

        private static Messaging parseMessagingXML(String uriMessagingXML) throws Exception {
            return ((JAXBElement<Messaging>) jaxbMessagingContext.createUnmarshaller().unmarshal(new File(uriMessagingXML))).getValue();
        }

        private static SOAPMessage dispatchMessage(Messaging messaging) throws Exception {
            final QName serviceName = new QName("http://domibus.eu", "msh-dispatch-service");
            final QName portName = new QName("http://domibus.eu", "msh-dispatch");
            final javax.xml.ws.Service service = javax.xml.ws.Service.create(serviceName);
            service.addPort(portName, SOAPBinding.SOAP12HTTP_BINDING, mshWSLoc);
            final Dispatch<SOAPMessage> dispatch = service.createDispatch(portName, SOAPMessage.class, javax.xml.ws.Service.Mode.MESSAGE);

            SOAPMessage soapMessage = messageFactory.createMessage();
            jaxbMessagingContext.createMarshaller().marshal(new JAXBElement(new QName("http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/", "Messaging"), Messaging.class, messaging), soapMessage.getSOAPHeader());

            AttachmentPart attachment=soapMessage.createAttachmentPart();
            attachment.setContent("PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz4KPGhlbGxvPndvcmxkPC9oZWxsbz4=", "text/xml");
            attachment.setContentId("payload");
            soapMessage.addAttachmentPart(attachment);
            soapMessage.saveChanges();
            return dispatch.invoke(soapMessage);
        }

        private static XMLGregorianCalendar getCurrentDate() throws Exception {
            GregorianCalendar c = new GregorianCalendar();
            c.setTime(new Date());
            XMLGregorianCalendar currentDate = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);

            return currentDate;
        }

        private static String prepareMSHTestMessage(String messageId, String uriMessagingXML) throws Exception {
            //if the messageId is null, create new unique messageId
            if (messageId == null) {
                messageId = UUID.randomUUID().toString();
            }

            //if uriMessagingXML is null, use the SAMPLE_MSH_MESSAGE instead
            if (uriMessagingXML == null) {
                uriMessagingXML = SAMPLE_MSH_MESSAGE;
            }

            Messaging messaging = Helper.parseMessagingXML(uriMessagingXML);
            //set messageId
            messaging.getUserMessage().getMessageInfo().setMessageId(messageId);
            //set timestamp
            messaging.getUserMessage().getMessageInfo().setTimestamp(LocalDateTime.now());

            SOAPMessage responseFromMSH = Helper.dispatchMessage(messaging);

            assertNotNull(responseFromMSH);
            assertNotNull(responseFromMSH.getSOAPBody());
            //response is no SOAPFault
            assertNull(responseFromMSH.getSOAPBody().getFault());

            return messageId;
        }

        private static String errorResultAsFormattedString(ErrorResultImplArray errorResultArray) {
            StringBuilder formattedOutput = new StringBuilder();

            for (ErrorResultImpl errorResult : errorResultArray.getItem()) {
                formattedOutput.append(LINE_SEPARATOR);
                formattedOutput.append("==========================================================");
                formattedOutput.append(LINE_SEPARATOR);
                formattedOutput.append("EBMS3 error code: " + errorResult.getErrorCode());
                formattedOutput.append(LINE_SEPARATOR);
                formattedOutput.append("Error details: " + errorResult.getErrorDetail());
                formattedOutput.append(LINE_SEPARATOR);
                formattedOutput.append("Error is related to message with messageId: " + errorResult.getMessageInErrorId());
                formattedOutput.append(LINE_SEPARATOR);
                formattedOutput.append("Role of MSH in context of this message transmission: " + errorResult.getMshRole());
                formattedOutput.append(LINE_SEPARATOR);
                formattedOutput.append("Time of notification: " + errorResult.getNotified());
                formattedOutput.append(LINE_SEPARATOR);
                formattedOutput.append("Message was sent/received: " + errorResult.getTimestamp());
                formattedOutput.append(LINE_SEPARATOR);
                formattedOutput.append("==========================================================");
                formattedOutput.append(LINE_SEPARATOR);
            }

            return formattedOutput.toString();
        }
    }



}
