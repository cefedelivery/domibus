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

package eu.domibus.common.validators;

import eu.domibus.common.ErrorCode;
import eu.domibus.common.exception.EbMS3Exception;
import eu.domibus.common.model.configuration.Payload;
import eu.domibus.common.model.configuration.PayloadProfile;
import eu.domibus.ebms3.common.dao.PModeProvider;
import eu.domibus.ebms3.common.model.Messaging;
import eu.domibus.ebms3.common.model.PartInfo;
import eu.domibus.ebms3.common.model.Property;
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

    @Autowired
    private PModeProvider pModeProvider;

    public void validate(final Messaging messaging, final String pmodeKey) throws EbMS3Exception {
        final List<Payload> modifiableProfileList = new ArrayList<>();
        final PayloadProfile profile = this.pModeProvider.getLegConfiguration(pmodeKey).getPayloadProfile();
        if (profile == null) {
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
                throw new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0010, "Payload profiling error, missing payload:" + payload, messaging.getUserMessage().getMessageInfo().getMessageId(), null);

            }
        }
    }
}
