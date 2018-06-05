package eu.domibus.api.party;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.Column;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSchemaType;

public class PartyIdType {

    private final static Logger LOG = LoggerFactory.getLogger(PartyIdType.class);

    protected String name;

    protected String value;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "PartyIdType{" + "name=" + name + ", value=" + value + '}';
    }

}
