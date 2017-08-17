
package eu.domibus.messaging;

import eu.domibus.common.ErrorCode;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * This exception is useful to transmit XML errors which can occur during XML parsing and or during XML validation.
 *
 * @author Federico Martini
 */
public class XmlProcessingException extends MessagingProcessingException {

    private Set<String> errors = new LinkedHashSet<>();

    public XmlProcessingException(Throwable cause) {
        super(cause);
    }

    public XmlProcessingException(String message) {
        super(message);
        super.setEbms3ErrorCode(ErrorCode.EBMS_0065);
    }

    public XmlProcessingException(String message, Throwable cause) {
        super(message, cause);
        super.setEbms3ErrorCode(ErrorCode.EBMS_0065);
    }

    public Set<String> getErrors() {
        return errors;
    }

    public void setErrors(Set<String> errors) {
        this.errors = errors;
    }

    public void addErrors(Collection<String> errors) {
        this.errors.addAll(errors);
    }
}
