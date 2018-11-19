package eu.domibus.ebms3.receiver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
public class KeyIdentifierTokenReference implements TokenReference{

    private static final Logger LOG = LoggerFactory.getLogger(KeyIdentifierTokenReference.class);

    private String EncodingType;

    private String valueType;

    private String key;

}
