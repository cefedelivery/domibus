package eu.domibus.common.services.impl;

import eu.domibus.common.ErrorCode;
import eu.domibus.common.exception.EbMS3Exception;
import eu.domibus.common.services.SoapService;
import eu.domibus.ebms3.common.model.Messaging;
import eu.domibus.ebms3.common.model.ObjectFactory;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.helpers.IOUtils;
import org.apache.cxf.interceptor.StaxInInterceptor;
import org.apache.neethi.builders.converters.StaxToDOMConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;


/**
 * @author Thomas Dussart
 * @since 3.3
 */

@Service
public class SoapServiceImpl implements SoapService {

    @Qualifier("jaxbContextEBMS")
    @Autowired
    private JAXBContext jaxbContext;


    public Messaging getMessage(final SoapMessage message) throws IOException, JAXBException, EbMS3Exception {
        final InputStream inputStream = message.getContent(InputStream.class);
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        IOUtils.copy(inputStream, byteArrayOutputStream); //FIXME: do not copy the whole byte[], use SequenceInputstream instead
        final byte[] data = byteArrayOutputStream.toByteArray();
        message.setContent(InputStream.class, new ByteArrayInputStream(data));
        new StaxInInterceptor().handleMessage(message);
        final XMLStreamReader xmlStreamReader = message.getContent(XMLStreamReader.class);
        final Element soapEnvelope = new StaxToDOMConverter().convert(xmlStreamReader);
        message.removeContent(XMLStreamReader.class);
        message.setContent(InputStream.class, new ByteArrayInputStream(data));
        //message.setContent(XMLStreamReader.class, XMLInputFactory.newInstance().createXMLStreamReader(message.getContent(InputStream.class)));
        final Node messagingNode = soapEnvelope.getElementsByTagNameNS(ObjectFactory._Messaging_QNAME.getNamespaceURI(), ObjectFactory._Messaging_QNAME.getLocalPart()).item(0);
        if(messagingNode == null) {
            throw new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0009, "Messaging header is empty!", null, null);
        }

        return ((JAXBElement<Messaging>) this.jaxbContext.createUnmarshaller().unmarshal(messagingNode)).getValue();
    }
}


