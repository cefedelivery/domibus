package eu.domibus.common.validators;

import eu.domibus.common.ErrorCode;
import eu.domibus.common.exception.EbMS3Exception;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by venugar on 11/14/16.
 */

@Service
public class BackendMessageValidator {

    private static final Log LOG = LogFactory.getLog(BackendMessageValidator.class);

    @Autowired
    @Qualifier("domibusProperties")
    private Properties domibusProperties;


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
        String messageIdPattern = domibusProperties.getProperty("domibus.sendMessage.messageIdPattern");
        LOG.debug("MessageIdPattern Read From File :" + messageIdPattern);
        Pattern patternNoControlChar = Pattern.compile(messageIdPattern);
        Matcher m = patternNoControlChar.matcher(messageId);
        if (!m.matches()) {
            throw new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0009, "element eb:Messaging/eb:UserMessage/eb:MessageId does not conform to RFC2822 [CORE 5.2.2.1]", null, null);
        }
    }


    public void validateRefToMessageId(final String refToMessageId) throws EbMS3Exception {

        if (refToMessageId.length() > 255) {
            throw new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0008, "RefToMessageId value is too long (over 255 characters)", refToMessageId, null);
        }

        //Validating for presence of non printable control characters.
        String messageIdPattern = domibusProperties.getProperty("domibus.sendMessage.messageIdPattern");
        LOG.debug("MessageIdPattern Read From File :" + messageIdPattern);
        Pattern patternNoControlChar = Pattern.compile(messageIdPattern);
        Matcher m = patternNoControlChar.matcher(refToMessageId);
        if (!m.matches()) {
            throw new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0009, "element eb:Messaging/eb:UserMessage/eb:MessageInfo/eb:RefToMessageId does not conform to RFC2822 [CORE 5.2.2.1]", null, null);
        }
    }
}
