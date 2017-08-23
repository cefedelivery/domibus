package eu.domibus.ebms3.common.validators;

import eu.domibus.common.model.configuration.BusinessProcesses;
import eu.domibus.common.model.configuration.Configuration;
import eu.domibus.common.model.configuration.Process;
import eu.domibus.common.model.configuration.Role;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author musatmi
 * @since 3.3
 */
@Component
@Order(1)
public class RolesValidator implements ConfigurationValidator {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(RolesValidator.class);

    @Override
    public List<String> validate(Configuration configuration) {

        List<String> issues = new ArrayList<>();

        final BusinessProcesses businessProcesses = configuration.getBusinessProcesses();
        for (Process process : businessProcesses.getProcesses()) {
            final Role initiatorRole = process.getInitiatorRole();
            final Role responderRole = process.getResponderRole();
            if (initiatorRole.equals(responderRole)) {
                issues.add("For business process " + process.getName() + " the initiator role and the responder role are identical (" + initiatorRole.getName() + ")");
            }
        }

        return Collections.unmodifiableList(issues);
    }


}
