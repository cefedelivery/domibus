package eu.domibus.core.alerts.model.service;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
@JsonIdentityInfo(generator=ObjectIdGenerators.IntSequenceGenerator.class,property="@id", scope = StringPropertyValue.class)
public class StringPropertyValue extends AbstractPropertyValue<String> {

    private String value;

    public StringPropertyValue() {
    }

    public StringPropertyValue(final String key,final String value) {
        this.key=key;
        this.value=value;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public void setValue(String value) {
        this.value=value;
    }
}
