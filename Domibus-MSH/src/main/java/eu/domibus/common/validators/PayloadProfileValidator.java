package eu.domibus.common.validators;

import eu.domibus.common.ErrorCode;
import eu.domibus.common.exception.EbMS3Exception;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.common.model.configuration.Payload;
import eu.domibus.common.model.configuration.PayloadProfile;
import eu.domibus.common.services.impl.CompressionService;
import eu.domibus.core.pmode.PModeProvider;
import eu.domibus.ebms3.common.model.Messaging;
import eu.domibus.ebms3.common.model.PartInfo;
import eu.domibus.ebms3.common.model.Property;
import eu.domibus.ebms3.common.model.UserMessage;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.logging.DomibusMessageCode;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Christian Koch, Stefan Mueller
 */
@Service
public class PayloadProfileValidator {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(PayloadProfileValidator.class);

    @Autowired
    private PModeProvider pModeProvider;


    public void validate(final Messaging messaging, final String pmodeKey) throws EbMS3Exception {
        final LegConfiguration legConfiguration = this.pModeProvider.getLegConfiguration(pmodeKey);
        final boolean isCompressEnabledInPmode = legConfiguration.isCompressPayloads();

        validateCompressPayloads(isCompressEnabledInPmode, messaging.getUserMessage());
        validatePayloadProfile(legConfiguration, messaging.getUserMessage());
    }

    public void validateCompressPayloads(final boolean isCompressEnabledInPmode, final UserMessage userMessage) throws EbMS3Exception {
        if(userMessage.getPayloadInfo() == null) {
            return;
        }

        for (final PartInfo partInfo : userMessage.getPayloadInfo().getPartInfo()) {
            validatePartInfo(isCompressEnabledInPmode, partInfo);
        }

    }

    protected void validatePartInfo(final boolean isCompressEnabledInPmode, final PartInfo partInfo) throws EbMS3Exception {

        if(partInfo.getPartProperties() == null) {
            if(isCompressEnabledInPmode) {
                LOG.warn("Compression is enabled in the pMode, CompressionType and MimeType properties are not present in [{}]", partInfo.getHref());
            }
            return;
        }

        boolean compress = false;
        String mimeType = null;
        for(Property property : partInfo.getPartProperties().getProperties()) {
            if(CompressionService.COMPRESSION_PROPERTY_KEY.equalsIgnoreCase(property.getName())) {
                if(!CompressionService.COMPRESSION_PROPERTY_VALUE.equalsIgnoreCase(property.getValue())) {
                    throw new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0052, CompressionService.COMPRESSION_PROPERTY_VALUE + " is the only accepted value for CompressionType. Got " + property.getValue(), null, null);
                }
                compress = true;
            }
            if(Property.MIME_TYPE.equalsIgnoreCase(property.getName())) {
                mimeType = property.getValue();
            }
        }


        if(compress == true && mimeType == null) {
                throw new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0052, "Missing MimeType property when compressions is required", null, null);
        }
    }

    public void validatePayloadProfile(final LegConfiguration legConfiguration, final UserMessage userMessage) throws EbMS3Exception {
        final List<Payload> modifiableProfileList = new ArrayList<>();
        final boolean profileCompress = legConfiguration.isCompressPayloads();

        final PayloadProfile profile = legConfiguration.getPayloadProfile();
        if (profile == null) {
            LOG.businessInfo(DomibusMessageCode.BUS_PAYLOAD_PROFILE_VALIDATION_SKIP, legConfiguration.getName());
            // no profile means everything is valid
            return;
        }

        modifiableProfileList.addAll(profile.getPayloads());
        final int size = 0;
        for (final PartInfo partInfo : userMessage.getPayloadInfo().getPartInfo()) {
            Payload profiled = null;
            final String cid = (partInfo.getHref() == null ? "" : partInfo.getHref());
            for (final Payload p : modifiableProfileList) {
                if (StringUtils.equalsIgnoreCase(p.getCid(), cid)) {
                    profiled = p;
                    break;
                }
            }
            if (profiled == null) {
                LOG.businessError(DomibusMessageCode.BUS_PAYLOAD_WITH_CID_MISSING, cid);
                throw new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0010, "Payload profiling for this exchange does not include a payload with CID: " + cid, userMessage.getMessageInfo().getMessageId(), null);
            }
            modifiableProfileList.remove(profiled);

            String mime = null;
            if(partInfo.getPartProperties() != null) {
                final Collection<Property> partProperties = partInfo.getPartProperties().getProperties();
                for (final Property partProperty : partProperties) {
                    if (Property.MIME_TYPE.equalsIgnoreCase(partProperty.getName())) {
                        mime = partProperty.getValue();
                        break;
                    }
                }
            }

            if (profiled.getMimeType() != null) {
                if ((!StringUtils.equalsIgnoreCase(profiled.getMimeType(), mime)) ||
                        (partInfo.isInBody() != profiled.isInBody()))//|| (profiled.getMaxSize() > 0 && profiled.getMaxSize() < partInfo.getBinaryData().length)) {
                    throw new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0010, "Payload profiling error: expected: " + profiled + ", got " + partInfo, userMessage.getMessageInfo().getMessageId(), null);
            }

        } //FIXME: size handling not possible with datahandlers
           /* size += partInfo.getBinaryData().length;
            if (profile.getMaxSize() > 0 && size > profile.getMaxSize()) {
                throw new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0010, "Payload profiling error, max allowed size of combined elements is " + profile.getMaxSize(), null, null);
            }

    }*/
        for (final Payload payload : modifiableProfileList) {
            if (payload.isRequired()) {
                LOG.businessError(DomibusMessageCode.BUS_PAYLOAD_MISSING, payload);
                throw new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0010, "Payload profiling error, missing payload:" + payload, userMessage.getMessageInfo().getMessageId(), null);

            }
        }

        LOG.businessInfo(DomibusMessageCode.BUS_PAYLOAD_PROFILE_VALIDATION, profile.getName());
    }
}
