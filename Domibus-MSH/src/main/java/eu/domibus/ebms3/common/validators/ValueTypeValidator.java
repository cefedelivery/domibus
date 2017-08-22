package eu.domibus.ebms3.common.validators;

import eu.domibus.common.model.configuration.*;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.net.URI;
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

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(ValueTypeValidator.class);

    @Override
    public List<String> validate(Configuration configuration) {

        List<String> issues = new ArrayList<>();

        final BusinessProcesses businessProcesses = configuration.getBusinessProcesses();

        for (Party party : businessProcesses.getParties()) {
            issues.addAll(validateIdentifiers(party));
        }

        for (Service service : businessProcesses.getServices()) {
            issues.addAll(validateService(service));
        }

        for (Agreement agreement : businessProcesses.getAgreements()) {
            issues.addAll(validateAgreement(agreement));
        }

        return Collections.unmodifiableList(issues);
    }

    protected List<String> validateIdentifiers(Party party) {
        List<String> issues = new ArrayList<>();
        if(party == null){
            return issues;
        }

        for (Identifier identifier : party.getIdentifiers()) {
            if (identifier.getPartyIdType() == null ||
                    (identifier.getPartyIdType() != null && StringUtils.isEmpty(identifier.getPartyIdType().getValue()))) {
                try {
                    URI.create(identifier.getPartyId());
                } catch (IllegalArgumentException exc) {
                    String msg = "PartyIdType is empty and the partyId is not an URI for " + party.getName();
                    issues.add(msg);
                    LOG.debug(msg);
                }
            }
        }
        return Collections.unmodifiableList(issues);
    }

    protected List<String> validateService(Service service) {
        List<String> issues = new ArrayList<>();
        if (StringUtils.isEmpty(service.getServiceType())) {
            try {
                URI.create(service.getValue());
            } catch (IllegalArgumentException exc) {
                String msg = "Service type is empty and the service value is not an URI for " + service.getName();
                issues.add(msg);
                LOG.debug(msg);
            }
        }
        return Collections.unmodifiableList(issues);
    }

    protected List<String> validateAgreement(Agreement agreement) {
        List<String> issues = new ArrayList<>();
        if (StringUtils.isEmpty(agreement.getType())) {
            try {
                URI.create(agreement.getValue());
            } catch (IllegalArgumentException exc) {
                String msg = "Agreement type is empty and the agreement value is not an URI for " + agreement.getName();
                issues.add(msg);
                LOG.debug(msg);
            }
        }
        return Collections.unmodifiableList(issues);
    }
}
