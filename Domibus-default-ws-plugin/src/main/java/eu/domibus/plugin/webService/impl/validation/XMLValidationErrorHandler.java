package eu.domibus.plugin.webService.impl.validation;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXParseException;

public class XMLValidationErrorHandler implements ErrorHandler {
    protected boolean valid;

    protected StringBuilder buffer;

    protected int numErrors;


    public XMLValidationErrorHandler() {
        valid = true;
        numErrors = 0;
        buffer = new StringBuilder();
    }

    @Override
    public void error(SAXParseException ex) {
        addError(ex);
    }

    @Override
    public void fatalError(SAXParseException ex) {
        addError(ex);
    }

    @Override
    public void warning(SAXParseException ex) {
        // Warning messages are ignored.
    }

    public boolean isValid() {
        return valid;
    }

    public String getErrorMessages() {
        return buffer.toString();
    }

    protected void addError(String msg, SAXParseException ex) {
        valid = false;
        if (numErrors == 0) {
            buffer.append("\n");
        } else {
            buffer.append("\n\n");
        }
        buffer.append(msg);
        numErrors++;

    }

    protected String getErrorMessage(SAXParseException ex) {
        return "line " + ex.getLineNumber() + " column " + ex.getColumnNumber() + " of " + ex.getSystemId()
                + ": " + ex.getMessage();
    }

    protected void addError(SAXParseException ex) {
        addError(getErrorMessage(ex), ex);
    }

}