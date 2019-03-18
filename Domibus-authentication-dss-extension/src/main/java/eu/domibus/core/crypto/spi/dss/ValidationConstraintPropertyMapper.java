package eu.domibus.core.crypto.spi.dss;

import eu.domibus.ext.services.DomainContextExtService;
import eu.domibus.ext.services.DomibusPropertyExtService;
import eu.europa.esig.dss.validation.process.MessageTag;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;

import java.util.List;
import java.util.Map;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
public class ValidationConstraintPropertyMapper extends PropertyGroupMapper<ConstraintInternal> {

    private static final Logger LOG = LoggerFactory.getLogger(ValidationConstraintPropertyMapper.class);

    static final String DOMIBUS_DSS_DEFAULT_CONSTRAINT_NAME = "domibus.dss.default.constraint.name";

    static final String DOMIBUS_DSS_DEFAULT_CONSTRAINT_STATUS = "domibus.dss.default.constraint.status";


    public ValidationConstraintPropertyMapper(final DomibusPropertyExtService domibusPropertyExtService,
                                              final DomainContextExtService domainContextExtService,
                                              final Environment environment) {
        super(domibusPropertyExtService,
                domainContextExtService, environment);
    }

    public List<ConstraintInternal> map() {
        return super.map(
                DOMIBUS_DSS_DEFAULT_CONSTRAINT_NAME,
                DOMIBUS_DSS_DEFAULT_CONSTRAINT_STATUS
        );
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

}
