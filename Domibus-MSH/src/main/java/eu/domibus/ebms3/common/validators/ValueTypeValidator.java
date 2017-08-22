package eu.domibus.ebms3.common.validators;

import eu.domibus.common.model.configuration.*;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author idragusa
 * @since 3.3
 *
 * The ebMS3 standard specifies that if @type is missing the value must be an URI.
 * This involves the elements PartyId, Service and AgreementRef.
 * After consultations between eSens and the vendors the decision is to accept
 * the type anyURI (and not require an absolute URI which is too restrictive)
 */
@Component
@Order(2)
public class ValueTypeValidator implements ConfigurationValidator {

    @Override
    public List<String> validate(Configuration configuration) {

        List<String> issues = new ArrayList<>();

        final BusinessProcesses businessProcesses = configuration.getBusinessProcesses();

        for (Party party : businessProcesses.getParties()) {
            for (Identifier identifier : party.getIdentifiers()) {
                if (identifier.getPartyIdType() == null ||
                        (identifier.getPartyIdType() != null && StringUtils.isEmpty(identifier.getPartyIdType().getValue()))) {
                    try {
                        URI.create(identifier.getPartyId());
                    } catch (IllegalArgumentException exc) {
                        issues.add("PartyIdType is empty and the partyId is not an URI for " + party.getName());
                    }
                }
            }
        }

        for (Service service : businessProcesses.getServices()) {
            if (StringUtils.isEmpty(service.getServiceType())) {
                try {
                    URI.create(service.getValue());
                } catch (IllegalArgumentException exc) {
                    issues.add("Service type is empty and the service value is not an URI for " + service.getName());
                }
            }
        }

        for (Agreement agreement : businessProcesses.getAgreements()) {
            if (StringUtils.isEmpty(agreement.getType())) {
                try {
                        URI.create(agreement.getValue());
                } catch (IllegalArgumentException exc) {
                    issues.add("Agreement type is empty and the agreement value is not an URI for " + agreement.getName());
                }
            }
        }

        return Collections.unmodifiableList(issues);
    }
}
