package eu.domibus.common.validators;

import eu.domibus.api.configuration.DomibusConfigurationService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.common.ErrorCode;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.common.util.DomibusPropertiesService;
import eu.domibus.core.pmode.PModeProvider;
import eu.domibus.common.exception.EbMS3Exception;
import eu.domibus.common.model.configuration.Property;
import eu.domibus.common.model.configuration.PropertySet;
import eu.domibus.ebms3.common.model.MessageProperties;
import eu.domibus.ebms3.common.model.Messaging;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.logging.DomibusMessageCode;
import eu.domibus.messaging.MessageConstants;
import org.apache.commons.collections.CollectionUtils;
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
    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(PropertyProfileValidator.class);

    @Autowired
    private PModeProvider pModeProvider;

    @Autowired
    DomibusConfigurationService domibusConfigurationService;

    public void validate(final Messaging messaging, final String pmodeKey) throws EbMS3Exception {
        // in the 4-corner model, originalSender and finalRecipient are required properties
        validateForCornerModel(messaging);

        final List<Property> modifiablePropertyList = new ArrayList<>();
        final LegConfiguration legConfiguration = this.pModeProvider.getLegConfiguration(pmodeKey);
        final PropertySet propSet = legConfiguration.getPropertySet();
        if (propSet == null || CollectionUtils.isEmpty(propSet.getProperties())) {
            LOG.businessInfo(DomibusMessageCode.BUS_PROPERTY_PROFILE_VALIDATION_SKIP, legConfiguration.getName());
            // no profile means everything is valid
            return;
        }

        final Set<Property> profile = propSet.getProperties();

        modifiablePropertyList.addAll(profile);
        eu.domibus.ebms3.common.model.MessageProperties messageProperties = new MessageProperties();
        if(messaging.getUserMessage().getMessageProperties() != null) {
            messageProperties = messaging.getUserMessage().getMessageProperties();
        }

        for (final eu.domibus.ebms3.common.model.Property property : messageProperties.getProperty()) {
            Property profiled = null;
            for (final Property profiledProperty : modifiablePropertyList) {
                if (profiledProperty.getKey().equals(property.getName())) {
                    profiled = profiledProperty;
                    break;
                }
            }
            modifiablePropertyList.remove(profiled);
            if (profiled == null) {
                LOG.businessError(DomibusMessageCode.BUS_PROPERTY_MISSING, property.getName());
                throw new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0010, "Property profiling for this exchange does not include a property named [" + property.getName() + "]", messaging.getUserMessage().getMessageInfo().getMessageId(), null);
            }

            switch (profiled.getDatatype().toLowerCase()) {
                case "string":
                    break;
                case "int":
                    try {
                        Integer.parseInt(property.getValue()); //NOSONAR: Validation is done via exception
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
                    PropertyProfileValidator.LOG.warn("Validation for Datatype " + profiled.getDatatype() + " not possible. This type is not known by the validator. The value will be accepted unchecked");
            }


        }
        for (final Property property : modifiablePropertyList) {
            if (property.isRequired()) {
                LOG.businessError(DomibusMessageCode.BUS_PROPERTY_MISSING, property.getName());
                throw new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0010, "Required property missing [" + property.getName() + "]", messaging.getUserMessage().getMessageInfo().getMessageId(), null);
            }
        }

        LOG.businessInfo(DomibusMessageCode.BUS_PROPERTY_PROFILE_VALIDATION, propSet.getName());
    }

    // in the 4-corner model, originalSender and finalRecipient are required properties
    protected void validateForCornerModel(final Messaging messaging) throws EbMS3Exception {
        if(!domibusConfigurationService.isFourCornerEnabled()) {
            return;
        }

        LOG.debug("Validating 4-corner model properties.");

        if(messaging.getUserMessage().getMessageProperties() == null) {
            throw new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0010, "MessageProperties is REQUIRED in the four corner model.", messaging.getUserMessage().getMessageInfo().getMessageId(), null);
        }

        boolean hasOriginalSender = false;
        boolean hasFinalRecipient = false;
        for (final eu.domibus.ebms3.common.model.Property property : messaging.getUserMessage().getMessageProperties().getProperty()) {
            if(MessageConstants.ORIGINAL_SENDER.equals(property.getName())) {
                LOG.debug("Found property originalSender.");
                hasOriginalSender = true;
            }
            if(MessageConstants.FINAL_RECIPIENT.equals(property.getName())) {
                LOG.debug("Found property finalRecipient.");
                hasFinalRecipient = true;
            }
        }

        if(!hasFinalRecipient || !hasOriginalSender) {
            throw new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0010, "OriginalSender and FinalRecipient are REQUIRED properties in the four corner model.", messaging.getUserMessage().getMessageInfo().getMessageId(), null);
        }
    }
}
