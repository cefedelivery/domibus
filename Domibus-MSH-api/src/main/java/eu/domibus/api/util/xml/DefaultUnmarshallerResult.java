package eu.domibus.api.util.xml;



import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * Created by Cosmin Baciu on 14-Sep-16.
 */
public class DefaultUnmarshallerResult implements UnmarshallerResult {

    protected Object result;
    protected boolean valid;
    protected List<String> errors;

    @Override
    public <T> T getResult() {
        return (T) result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    @Override
    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    @Override
    public List<String> getErrors() {
        return errors;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors;
    }

    @Override
    public String getErrorMessage() {
        return StringUtils.join(errors, "\n");
    }
}
