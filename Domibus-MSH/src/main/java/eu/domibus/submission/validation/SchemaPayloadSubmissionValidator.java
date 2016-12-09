package eu.domibus.submission.validation;

import eu.domibus.common.validators.XmlValidationEventHandler;
import eu.domibus.plugin.Submission;
import eu.domibus.plugin.validation.SubmissionValidationException;
import eu.domibus.plugin.validation.SubmissionValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.util.Set;

/**
 * Created by Cosmin Baciu on 04-Aug-16.
 */
public class SchemaPayloadSubmissionValidator implements SubmissionValidator {

    protected static final Logger LOG = LoggerFactory.getLogger(SchemaPayloadSubmissionValidator.class);

    protected JAXBContext jaxbContext;
    protected Resource schema;

    @Override
    public void validate(Submission submission) throws SubmissionValidationException {
        LOG.debug("Validating submission");

        Set<Submission.Payload> payloads = submission.getPayloads();
        if (payloads == null) {
            LOG.debug("There are no payloads to validate");
            return;
        }
        for (Submission.Payload payload : payloads) {
            validatePayload(payload);
        }
    }

    protected void validatePayload(Submission.Payload payload) {
        XmlValidationEventHandler jaxbValidationEventHandler = new XmlValidationEventHandler();
        try {
            SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            StreamSource xsdSource = new StreamSource(schema.getInputStream());
            Schema schema = sf.newSchema(xsdSource);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            unmarshaller.setSchema(schema);
            unmarshaller.setEventHandler(jaxbValidationEventHandler);
            unmarshaller.unmarshal(payload.getPayloadDatahandler().getInputStream());
            if (jaxbValidationEventHandler.hasErrors()) {
                throw new SubmissionValidationException("Error validating payload [" + payload.getContentId() + "]:" + jaxbValidationEventHandler.getErrorMessage());
            }
        } catch (SubmissionValidationException e) {
            throw e;
        } catch (Exception e) {
            String message = "Error validating the payload [" + payload.getContentId() + "]";
            if (jaxbValidationEventHandler.hasErrors()) {
                message += ":" + jaxbValidationEventHandler.getErrorMessage();
            }
            throw new SubmissionValidationException(message, e);
        }
    }

    public void setJaxbContext(JAXBContext jaxbContext) {
        this.jaxbContext = jaxbContext;
    }

    public void setSchema(Resource schema) {
        this.schema = schema;
    }
}
