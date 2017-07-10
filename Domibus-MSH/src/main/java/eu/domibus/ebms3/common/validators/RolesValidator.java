/*
 * Copyright 2015 e-CODEX Project
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they
 * will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the
 * Licence.
 * You may obtain a copy of the Licence at:
 * http://ec.europa.eu/idabc/eupl5
 * Unless required by applicable law or agreed to in
 * writing, software distributed under the Licence is
 * distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied.
 * See the Licence for the specific language governing
 * permissions and limitations under the Licence.
 */

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
import java.util.List;

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

        return issues;
    }


}
