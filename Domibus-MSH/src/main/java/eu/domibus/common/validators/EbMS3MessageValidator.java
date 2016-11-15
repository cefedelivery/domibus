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
import eu.domibus.ebms3.common.dao.PModeProvider;
import eu.domibus.ebms3.common.model.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.w3c.dom.Node;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Christian Koch, Stefan Mueller
 */
public class EbMS3MessageValidator {
    private static final Log LOG = LogFactory.getLog(EbMS3MessageValidator.class);
    private static final String RFC2822_PATTERN_STRING = "^[-!#$%&'*+/0-9=?A-Z^_a-z{|}~](\\.?[-!#$%&'*+/0-9=?A-Z^_a-z{|}~])*@[a-zA-Z](-?[a-zA-Z0-9])*(\\.[a-zA-Z](-?[a-zA-Z0-9])*)+$";
    private static final Pattern RFC_2822 = Pattern.compile(EbMS3MessageValidator.RFC2822_PATTERN_STRING);

    @Autowired
    private PModeProvider pModeProvider;

    @Qualifier("jaxbContextEBMS")
    @Autowired
    private JAXBContext ebmsContext;

    public void validate(final SOAPMessage message, final String pModeKey) throws EbMS3Exception {
        final Messaging messaging;
        try {
            messaging = this.ebmsContext.createUnmarshaller().unmarshal((Node) message.getSOAPHeader().getChildElements(ObjectFactory._Messaging_QNAME).next(), Messaging.class).getValue();
        } catch (JAXBException | SOAPException e) {
            EbMS3MessageValidator.LOG.error("", e);
            throw new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0004, "unable to parse message", null, e);
        }

        if (messaging.getUserMessage() == null && messaging.getSignalMessage() == null) { //There is no ebms message
            EbMS3MessageValidator.LOG.error("messaging element is empty");
            throw new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0004, null, null, null);
        }

        final UserMessage userMessage = messaging.getUserMessage();
        final SignalMessage signalMessage = messaging.getSignalMessage();

        if (userMessage != null) {
            this.validateUserMessage(userMessage, pModeKey);
        }
        if (signalMessage != null) {
            this.validateSignalMessage(signalMessage, pModeKey);
        }


    }

    private void validateSignalMessage(final SignalMessage signalMessage, final String pModeKey) {


    }

    private void validateUserMessage(final UserMessage userMessage, final String pModeKey) throws EbMS3Exception {
        final String mpc = userMessage.getMpc();
        //check if we know the mpc
        if (!this.pModeProvider.isMpcExistant(mpc)) {
            throw new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0001, "no mpc " + mpc + " found in configuration", null, null);
        }
        final MessageInfo messageInfo = userMessage.getMessageInfo();
        final Date timestamp = messageInfo.getTimestamp();
        if (timestamp == null) {
            throw new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0009, "required element eb:Messaging/eb:UserMessage/eb:Timestamp missing", null, null);
        }
        final String messageId = messageInfo.getMessageId();
        if (messageId == null) {
            throw new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0009, "required element eb:Messaging/eb:UserMessage/eb:MessageId missing", null, null);
        }
        if (!EbMS3MessageValidator.RFC_2822.matcher(messageId).matches()) {
            throw new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0009, "element eb:Messaging/eb:UserMessage/eb:MessageId does not conform to RFC2822 [CORE 5.2.2.1]", null, null);
        }


    }

    /**
     * Validations pertaining to the field - UserMessage/MessageInfo/MessageId<br/><br/>
     * <b><u>As per ebms_core-3.0-spec-cs-02.pdf:</u></b><br/>
     * &ldquo;b:Messaging/eb:UserMessage/eb:MessageInfo/eb:MessageId:
     * This REQUIRED element has a value representing – for each message - a globally unique identifier <b>conforming to MessageId [RFC2822].</b>
     * Note: In the Message-Id and Content-Id MIME headers, values are always surrounded by angle brackets. However references in mid: or cid: scheme URI's and
     * the MessageId and RefToMessageId elements MUST NOT include these delimiters.&rdquo;<br/><br/>
     * <p>
     * <b><u>As per RFC2822 :</u></b><br/>
     * &ldquo;2.1. General Description - At the most basic level, a message is a series of characters.  A message that is conformant with this standard is comprised of
     * characters with values in the range 1 through 127 and interpreted as US-ASCII characters [ASCII].&rdquo;<br/><br/>
     * <p>
     * &ldquo;3.6.4. Identification fields: The "Message-ID:" field provides a unique message identifier that refers to a particular version of a particular message.
     * The uniqueness of the message identifier is guaranteed by the host that generates it (see below).
     * This message identifier is <u>intended to be machine readable and not necessarily meaningful to humans.</u>
     * A message identifier pertains to exactly one instantiation of a particular message; subsequent revisions to the message each receive new message identifiers.&rdquo;<br/><br/>
     * <p>
     * Though the above specifications state the message id can be any ASCII character, practically the message ids might need to be referenced by persons and documents.
     * Hence all non printable characters (ASCII 0 to 31 and 127) should be avoided.<br/><br/>
     * <p>
     * RFC2822 also states the better algo for generating a unique id is - put a combination of the current absolute date and time along with
     * some other currently unique (perhaps sequential) identifier available on the system + &ldquo;@&rdquo; + domain name (or a domain literal IP address) of the host on which the
     * message identifier. As seen from acceptance and production setup, existing clients of Domibus sending message id is not following this format. Hence, although it is good, it is not enforced.
     * Only control character restriction is enforced.
     *
     * @param messageId
     * @throws EbMS3Exception
     */
    public void validateMessageId(final String messageId) throws EbMS3Exception {

        if (messageId == null) {
            throw new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0009, "required element eb:Messaging/eb:UserMessage/eb:MessageId missing", null, null);
        }

        if (messageId.length() > 255) {
            throw new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0008, "MessageId value is too long (over 255 characters)", null, null);
        }

        //Validating for presence of non printable control characters.
        Pattern patternNoControlChar = Pattern.compile("^[\\x20-\\x7E]*$");
        Matcher m = patternNoControlChar.matcher(messageId);
        if (!m.matches()) {
            throw new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0009, "element eb:Messaging/eb:UserMessage/eb:MessageId does not conform to RFC2822 [CORE 5.2.2.1]", null, null);
        }
    }
}
