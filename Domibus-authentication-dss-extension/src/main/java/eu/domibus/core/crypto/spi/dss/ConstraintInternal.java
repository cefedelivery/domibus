package eu.domibus.core.crypto.spi.dss;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
public class ConstraintInternal {

    private static final Logger LOG = LoggerFactory.getLogger(ConstraintInternal.class);

    private String name;

    private String status;

    public ConstraintInternal(String name, String status) {
        this.name = name;
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public String getStatus() {
        return status;
    }
}
