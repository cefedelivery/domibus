package eu.domibus.common.validators;

import eu.domibus.common.ErrorCode;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.ebms3.common.dao.PModeProvider;
import eu.domibus.common.exception.EbMS3Exception;
import eu.domibus.common.model.configuration.Property;
import eu.domibus.common.model.configuration.PropertySet;
import eu.domibus.ebms3.common.model.Messaging;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.logging.DomibusMessageCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author Christian Koch, Stefan Mueller
 * @version 3.0
 * @since 3.0
 */

@Service
public class PropertyProfileValidator {
    private static final DomibusLogger LOGGER = DomibusLoggerFactory.getLogger(PropertyProfileValidator.class);

    @Autowired
    private PModeProvider pModeProvider;

    public void validate(final Messaging messaging, final String pmodeKey) throws EbMS3Exception {
        final List<Property> modifiablePropertyList = new ArrayList<>();
        final LegConfiguration legConfiguration = this.pModeProvider.getLegConfiguration(pmodeKey);
        final PropertySet propSet = legConfiguration.getPropertySet();
        if (propSet == null) {
            LOGGER.businessInfo(DomibusMessageCode.BUS_PROPERTY_PROFILE_VALIDATION_SKIP, legConfiguration.getName());
            // no profile means everything is valid
            return;
        }

        final Set<Property> profile = propSet.getProperties();

        modifiablePropertyList.addAll(profile);

        for (final eu.domibus.ebms3.common.model.Property property : messaging.getUserMessage().getMessageProperties().getProperty()) {
            Property profiled = null;
            for (final Property profiledProperty : modifiablePropertyList) {
                if (profiledProperty.getKey().equals(property.getName())) {
                    profiled = profiledProperty;
                    break;
                }
            }
            modifiablePropertyList.remove(profiled);
            if (profiled == null) {
                LOGGER.businessError(DomibusMessageCode.BUS_PROPERTY_MISSING, property.getName());
                throw new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0010, "Property profiling for this exchange does not include a property named [" + property.getName() + "]", messaging.getUserMessage().getMessageInfo().getMessageId(), null);
            }

            switch (profiled.getDatatype().toLowerCase()) {
                case "string":
                    break;
                case "int":
                    try {
                        Integer.parseInt(property.getValue());
                        break;
                    } catch (final NumberFormatException e) {
                        throw new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0010, "Property profiling for this exchange requires a INTEGER datatype for property named: " + property.getName() + ", but got " + property.getValue(), messaging.getUserMessage().getMessageInfo().getMessageId(), null);
                    }
                case "boolean":
                    if (property.getValue().equalsIgnoreCase("false") || property.getValue().equalsIgnoreCase("true")) {
                        break;
                    }
                    throw new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0010, "Property profiling for this exchange requires a BOOLEAN datatype for property named: " + property.getName() + ", but got " + property.getValue(), messaging.getUserMessage().getMessageInfo().getMessageId(), null);
                default:
                    PropertyProfileValidator.LOGGER.warn("Validation for Datatype " + profiled.getDatatype() + " not possible. This type is not known by the validator. The value will be accepted unchecked");
            }


        }
        for (final Property property : modifiablePropertyList) {
            if (property.isRequired()) {
                LOGGER.businessError(DomibusMessageCode.BUS_PROPERTY_MISSING, property.getName());
                throw new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0010, "Required property missing [" + property.getName() + "]", messaging.getUserMessage().getMessageInfo().getMessageId(), null);
            }
        }

        LOGGER.businessInfo(DomibusMessageCode.BUS_PROPERTY_PROFILE_VALIDATION, propSet.getName());
    }
}
