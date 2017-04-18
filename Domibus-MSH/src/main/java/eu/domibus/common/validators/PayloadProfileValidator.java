package eu.domibus.common.validators;

import eu.domibus.common.ErrorCode;
import eu.domibus.common.exception.EbMS3Exception;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.common.model.configuration.Payload;
import eu.domibus.common.model.configuration.PayloadProfile;
import eu.domibus.ebms3.common.dao.PModeProvider;
import eu.domibus.api.message.ebms3.model.Messaging;
import eu.domibus.api.message.ebms3.model.PartInfo;
import eu.domibus.api.message.ebms3.model.Property;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.logging.DomibusMessageCode;
import org.apache.commons.lang.StringUtils;
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
        final List<Payload> modifiableProfileList = new ArrayList<>();
        final LegConfiguration legConfiguration = this.pModeProvider.getLegConfiguration(pmodeKey);
        final PayloadProfile profile = legConfiguration.getPayloadProfile();
        if (profile == null) {
            LOG.businessInfo(DomibusMessageCode.BUS_PAYLOAD_PROFILE_VALIDATION_SKIP, legConfiguration.getName());
            // no profile means everything is valid
            return;
        }

        modifiableProfileList.addAll(profile.getPayloads());
        final int size = 0;
        for (final PartInfo partInfo : messaging.getUserMessage().getPayloadInfo().getPartInfo()) {
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
                throw new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0010, "Payload profiling for this exchange does not include a payload with CID: " + cid, messaging.getUserMessage().getMessageInfo().getMessageId(), null);
            }
            modifiableProfileList.remove(profiled);
            final Collection<Property> partProperties = partInfo.getPartProperties().getProperties();
            String mime = null;
            for (final Property partProperty : partProperties) {
                if (Property.MIME_TYPE.equals(partProperty.getName())) {
                    mime = partProperty.getValue();
                    break;
                }
            }
            if (mime == null) {
                LOG.businessError(DomibusMessageCode.BUS_PAYLOAD_WITH_MIME_TYPE_MISSING, partInfo.getHref());
                throw new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0010, "Payload profiling for this exchange requires all message parts to declare a MimeType property" + partInfo.getHref(), messaging.getUserMessage().getMessageInfo().getMessageId(), null);
            }
            if ((!StringUtils.equalsIgnoreCase(profiled.getMimeType(), mime)) ||
                    (partInfo.isInBody() != profiled.isInBody()))//|| (profiled.getMaxSize() > 0 && profiled.getMaxSize() < partInfo.getBinaryData().length)) {
                throw new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0010, "Payload profiling error: expected: " + profiled + ", got " + partInfo, messaging.getUserMessage().getMessageInfo().getMessageId(), null);
        } //FIXME: size handling not possible with datahandlers
           /* size += partInfo.getBinaryData().length;
            if (profile.getMaxSize() > 0 && size > profile.getMaxSize()) {
                throw new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0010, "Payload profiling error, max allowed size of combined elements is " + profile.getMaxSize(), null, null);
            }

    }*/
        for (final Payload payload : modifiableProfileList) {
            if (payload.isRequired()) {
                LOG.businessError(DomibusMessageCode.BUS_PAYLOAD_MISSING, payload);
                throw new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0010, "Payload profiling error, missing payload:" + payload, messaging.getUserMessage().getMessageInfo().getMessageId(), null);

            }
        }

        LOG.businessInfo(DomibusMessageCode.BUS_PAYLOAD_PROFILE_VALIDATION, profile.getName());
    }
}
