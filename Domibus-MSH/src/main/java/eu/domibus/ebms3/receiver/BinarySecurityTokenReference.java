package eu.domibus.ebms3.receiver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
public class BinarySecurityTokenReference implements TokenReference{

    private static final Logger LOG = LoggerFactory.getLogger(BinarySecurityTokenReference.class);

    private String valueType;

    private String uri;

    public BinarySecurityTokenReference(String valueType, String uri) {
        this.valueType = valueType;
        this.uri = uri;
    }

    public String getValueType() {
        return valueType;
    }

    public String getUri() {
        return uri;
    }
}
