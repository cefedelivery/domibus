package eu.domibus.plugin.webService.impl.validation;

import eu.domibus.common.model.org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.Messaging;
import eu.domibus.ext.services.DomibusPropertyExtService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.plugin.webService.generated.*;
import eu.domibus.plugin.webService.impl.BackendWebServiceFaultFactory;
import org.apache.cxf.binding.soap.SoapFault;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.xml.sax.SAXException;

import javax.activation.DataHandler;
import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.util.JAXBSource;
import javax.xml.namespace.QName;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Cosmin Baciu
 * @since 4.0
 */
@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class WSPluginSchemaValidation {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(WSPluginSchemaValidation.class);

    @Autowired
    protected BackendWebServiceFaultFactory backendWebServiceFaultFactory;

    @Autowired
    protected DomibusPropertyExtService domibusPropertyExtService;

    protected boolean schemaValidationEnabled() {
        return Boolean.valueOf(domibusPropertyExtService.getProperty("wsplugin.schema.validation.enabled", "true"));
    }

    public void validateSubmitMessage(SubmitRequest submitRequest, Messaging ebMSHeaderInfo) throws SubmitMessageFault {
        if(!schemaValidationEnabled()) {
            LOG.debug("Schema validation is disabled");
            return;
        }

        try {
            final XMLValidationErrorHandler messagingValidationResult = validateMessaging(ebMSHeaderInfo);
            if (!messagingValidationResult.isValid()) {
                throw new SubmitMessageFault("Messaging is not valid against the XSD", backendWebServiceFaultFactory.generateDefaultFaultDetail(messagingValidationResult.getErrorMessages()));
            }
            final XMLValidationErrorHandler submitRequestValidationResult = validateSubmitRequest(submitRequest);
            if (!submitRequestValidationResult.isValid()) {
                throw new SubmitMessageFault("submitRequest is not valid against the XSD", backendWebServiceFaultFactory.generateDefaultFaultDetail(submitRequestValidationResult.getErrorMessages()));
            }
        } catch (JAXBException | IOException | SAXException e) {
            LOG.error("Could not validate against XSD", e);
            throw new SubmitMessageFault("Could not validate against XSD", backendWebServiceFaultFactory.generateDefaultFaultDetail("Could not validate against XSD"), e);
        }
    }

    public void validateRetrieveMessage(RetrieveMessageRequest retrieveMessageRequest) throws RetrieveMessageFault {
        if(!schemaValidationEnabled()) {
            LOG.debug("Schema validation is disabled");
            return;
        }

        try {
            final XMLValidationErrorHandler newStackTraceErrorHandler = validateRetrieveMessageRequest(retrieveMessageRequest);
            if (!newStackTraceErrorHandler.isValid()) {
                throw new RetrieveMessageFault("retrieveMessageRequest is not valid against the XSD", backendWebServiceFaultFactory.generateDefaultFaultDetail(newStackTraceErrorHandler.getErrorMessages()));
            }
        } catch (JAXBException | IOException | SAXException e) {
            LOG.error("Could not validate against XSD", e);
            throw new RetrieveMessageFault("Could not validate against XSD", backendWebServiceFaultFactory.generateDefaultFaultDetail(e.getMessage()));
        }
    }


    public void validateGetStatus(final StatusRequest statusRequest) throws StatusFault {
        if(!schemaValidationEnabled()) {
            LOG.debug("Schema validation is disabled");
            return;
        }

        try {
            final XMLValidationErrorHandler newStackTraceErrorHandler = validateGetStatusRequest(statusRequest);
            if (!newStackTraceErrorHandler.isValid()) {
                throw new StatusFault("Status request is not valid against the XSD", backendWebServiceFaultFactory.generateDefaultFaultDetail(newStackTraceErrorHandler.getErrorMessages()));
            }
        } catch (JAXBException | IOException | SAXException e) {
            LOG.error("Could not validate against XSD", e);
            throw new StatusFault("Could not validate against XSD", backendWebServiceFaultFactory.generateDefaultFaultDetail(e.getMessage()));
        }
    }


    public void validateGetMessageErrorsRequest(final GetErrorsRequest messageErrorsRequest) {
        if(!schemaValidationEnabled()) {
            LOG.debug("Schema validation is disabled");
            return;
        }

        try {
            final XMLValidationErrorHandler newStackTraceErrorHandler = validateGetMessageErrors(messageErrorsRequest);
            if (!newStackTraceErrorHandler.isValid()) {
                throw new SoapFault("getErrorsRequest is not valid against the XSD:" + newStackTraceErrorHandler.getErrorMessages(), new QName("http://org.ecodex.backend/1_1/", "getErrorsRequest"));
            }

        } catch (JAXBException | IOException | SAXException e) {
            LOG.error("Could not validate against XSD", e);
            throw new RuntimeException("Could not validate against XSD", e);
        }
    }


    protected XMLValidationErrorHandler validateGetStatusRequest(StatusRequest statusRequest) throws IOException, SAXException, JAXBException {
        LOG.debug("Validate [{}] against XSD", statusRequest.getClass());

        JAXBContext jaxbContext = JAXBContext.newInstance(eu.domibus.plugin.webService.generated.ObjectFactory.class);
        JAXBSource jaxbSource = new JAXBSource(jaxbContext, new JAXBElement<>(new QName("http://org.ecodex.backend/1_1/", "statusRequest"), StatusRequest.class, statusRequest));
        return validate(jaxbSource);
    }

    protected XMLValidationErrorHandler validateMessaging(Messaging ebMSHeaderInfo) throws JAXBException, IOException, SAXException {
        LOG.debug("Validate Messaging object against XSD");

        JAXBContext jaxbContext = JAXBContext.newInstance(eu.domibus.common.model.org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.ObjectFactory.class);
        JAXBSource jaxbSource = new JAXBSource(jaxbContext, new JAXBElement<>(new QName("http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/", "Messaging"), Messaging.class, ebMSHeaderInfo));
        return validate(jaxbSource);
    }

    protected XMLValidationErrorHandler validateGetMessageErrors(GetErrorsRequest getErrorsRequest) throws JAXBException, IOException, SAXException {
        LOG.debug("Validate Messaging object against XSD");

        JAXBContext jaxbContext = JAXBContext.newInstance(eu.domibus.plugin.webService.generated.ObjectFactory.class);
        JAXBSource jaxbSource = new JAXBSource(jaxbContext, new JAXBElement<>(new QName("http://org.ecodex.backend/1_1/", "getErrorsRequest"), GetErrorsRequest.class, getErrorsRequest));
        return validate(jaxbSource);
    }

    protected XMLValidationErrorHandler validateRetrieveMessageRequest(RetrieveMessageRequest retrieveMessageRequest) throws IOException, SAXException, JAXBException {
        LOG.debug("Validate [{}] object against XSD", retrieveMessageRequest.getClass());

        JAXBContext jaxbContext = JAXBContext.newInstance(eu.domibus.plugin.webService.generated.ObjectFactory.class);
        JAXBSource jaxbSource = new JAXBSource(jaxbContext, retrieveMessageRequest);
        return validate(jaxbSource);
    }

    protected XMLValidationErrorHandler validateSubmitRequest(SubmitRequest submitRequest) throws JAXBException, IOException, SAXException {
        LOG.debug("Validate SubmitRequest object against XSD");

        //save the data handlers into a temporary list
        List<DataHandler> handlers = new ArrayList<>();
        final List<LargePayloadType> payloads = submitRequest.getPayload();
        if (payloads != null) {
            for (LargePayloadType payload : payloads) {
                handlers.add(payload.getValue());

                //put a temporary data handler to make the validation pass for large files
                payload.setValue(new DataHandler(new Object(), ""));
            }
        }

        JAXBContext jaxbContext = JAXBContext.newInstance(eu.domibus.plugin.webService.generated.ObjectFactory.class);
        JAXBSource jaxbSource = new JAXBSource(jaxbContext, submitRequest);
        final XMLValidationErrorHandler validationResult = validate(jaxbSource);

        //put back the data handlers
        if (payloads != null) {
            for (int index = 0; index < payloads.size(); index++) {
                final LargePayloadType largePayloadType = payloads.get(index);
                largePayloadType.setValue(handlers.get(index));
            }
        }
        return validationResult;
    }

    protected Validator createValidator() throws IOException, SAXException {
        SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        schemaFactory.setResourceResolver(new ClassBasedResourceResolver(eu.domibus.common.model.org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.Messaging.class));
        javax.xml.validation.Schema schema = schemaFactory.newSchema(new StreamSource(new ClassPathResource("schemas/BackendService_1_1.xsd").getInputStream()));
        return schema.newValidator();
    }

    protected XMLValidationErrorHandler validate(JAXBSource jaxbSource) throws IOException, SAXException, JAXBException {
        Validator validator = createValidator();
        final XMLValidationErrorHandler newStackTraceErrorHandler = new XMLValidationErrorHandler();
        validator.setErrorHandler(newStackTraceErrorHandler);
        validator.validate(jaxbSource);
        return newStackTraceErrorHandler;
    }
}
