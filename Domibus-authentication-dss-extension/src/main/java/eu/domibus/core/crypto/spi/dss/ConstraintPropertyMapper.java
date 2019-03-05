package eu.domibus.core.crypto.spi.dss;

import eu.europa.esig.dss.validation.process.MessageTag;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
@Component
public class ConstraintPropertyMapper extends PropertyGroupMapper<ConstraintInternal> {

    private static final Logger LOG = LoggerFactory.getLogger(ConstraintPropertyMapper.class);

    static final String DOMIBUS_DSS_DEFAULT_CONSTRAINT_NAME = "domibus.dss.default.constraint.name";

    static final String DOMIBUS_DSS_DEFAULT_CONSTRAINT_STATUS = "domibus.dss.default.constraint.status";

    private Environment env;

    public ConstraintPropertyMapper(Environment env) {
        this.env = env;
    }

    @Override
    ConstraintInternal transForm(Map<String, ImmutablePair<String, String>> keyValues) {
        if (keyValues.isEmpty()) {
            throw new IllegalStateException("Constraints are mandatory.");
        }
        final String constraintName = keyValues.get(DOMIBUS_DSS_DEFAULT_CONSTRAINT_NAME).getRight();
        if (constraintName == null) {
            throw new IllegalStateException("Constraint name can not be empty");
        }
        final MessageTag constraintEnum = MessageTag.valueOf(constraintName);
        final String constraintStatus = keyValues.get(DOMIBUS_DSS_DEFAULT_CONSTRAINT_STATUS).getRight();
        return new ConstraintInternal(constraintEnum.name(), constraintStatus);
    }

    @Override
    Environment getEnvironment() {
        return env;
    }
}
