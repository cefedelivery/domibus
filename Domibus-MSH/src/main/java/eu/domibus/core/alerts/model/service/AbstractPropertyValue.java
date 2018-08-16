package eu.domibus.core.alerts.model.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY)
@JsonSubTypes({
        @JsonSubTypes.Type(value = StringPropertyValue.class, name = "StringPropertyValue"),

        @JsonSubTypes.Type(value = DatePropertyValue.class, name = "DatePropertyValue") }
)
public abstract class AbstractPropertyValue<T> {

    protected String key;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public AbstractPropertyValue() {
    }


    public abstract T getValue();

    public abstract void  setValue(T t);


}
