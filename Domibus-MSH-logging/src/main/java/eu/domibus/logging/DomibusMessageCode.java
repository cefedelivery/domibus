package eu.domibus.logging;


import eu.domibus.logging.api.MessageCode;

/**
 * @author Cosmin Baciu
 * @since 3.3
 */
public enum DomibusMessageCode implements MessageCode {

    BUS_MESSAGE_RECEIVED("BUS-001", "Message successfully received"),
    BUS_MESSAGE_RECEIVE_FAILED("BUS-002", "Failed to receive message"),
    BUS_MESSAGE_VALIDATION_FAILED("BUS-003", "Failed to validate message"),
    BUS_BACKEND_NOTIFICATION_FAILED("BUS-004", "Failed to notify backend for incoming message"),
    BUS_MESSAGE_CHARSET_INVALID("BUS-005", "Invalid charset [{}] used"),
    BUS_MESSAGE_RECEIPT_INVALID_WITH_NO_SECURITY_HEADER("BUS-006", "Invalid NonRepudiationInformation: no security header found"),
    BUS_MESSAGE_RECEIPT_INVALID_WITH_MULTIPLE_SECURITY_HEADERS("BUS-007", "Invalid NonRepudiationInformation: multiple security headers found"),
    BUS_MESSAGE_RECEIPT_INVALID_WITH_MESSAGING_NOT_SIGNED("BUS-008", "Invalid NonRepudiationInformation: eb:Messaging not signed"),
    BUS_MESSAGE_RECEIPT_INVALID_NOT_MATCHING_THE_MESSAGE("BUS-009", "Invalid NonRepudiationInformation: non repudiation information and request message do not match"),
    BUS_MESSAGE_RECEIPT_INVALID_EMPTY("BUS-010", "There is no content inside the receipt element received by the responding gateway"),
    BUS_MESSAGE_RECEIPT_GENERAL_ERROR("BUS-011", "Reliability check failed, check your configuration"),
    BUS_MESSAGE_RECEIPT_CHECK_SUCCESSFUL("BUS-012", "Reliability check was successful"),
    BUS_MESSAGE_PAYLOAD_COMPRESSION_FAILURE_MISSING_MIME_TYPE("BUS-013", "Compression failure: no mime type found for payload with cid [{}]"),
    BUS_MESSAGE_PAYLOAD_GENERAL_COMPRESSION_FAILURE("BUS-014", "Error compressing message"),
    BUS_MESSAGE_PAYLOAD_DECOMPRESSION_FAILURE_MISSING_MIME_TYPE("BUS-015", "Decompression failure: no mime type found for payload with cid [{}]"),
    BUS_MESSAGE_ACTION_FOUND("BUS-016", "Message action [{}] found for value [{}]"),
    BUS_MESSAGE_ACTION_NOT_FOUND("BUS-017", "Message action not found for value [{}]"),
    BUS_MESSAGE_AGREEMENT_FOUND("BUS-018", "Message agreement [{}] found for value [{}]"),
    BUS_MESSAGE_AGREEMENT_NOT_FOUND("BUS-019", "Message agreement not found for value [{}]"),
    BUS_PARTY_ID_FOUND("BUS-020", "Party id [{}] found for value [{}]"),
    BUS_PARTY_ID_NOT_FOUND("BUS-021", "Party id not found for value [{}]"),
    BUS_PARTY_ID_INVALID_URI("BUS-022", "Party [{}] is not a valid URI [CORE] 5.2.2.3"),
    BUS_MESSAGE_SERVICE_FOUND("BUS-023", "Message service [{}] found for value [{}]"),
    BUS_MESSAGE_SERVICE_NOT_FOUND("BUS-024", "Message service not found for value [{}]"),
    BUS_MESSAGE_SERVICE_INVALID_URI("BUS-025", "Message service [{}] is not a valid URI [CORE] 5.2.2.8"),
    BUS_LEG_NAME_FOUND("BUS-026", "Leg name found [{}] for agreement [{}], senderParty [{}], receiverParty [{}], service [{}] and action [{}]"),
    BUS_LEG_NAME_NOT_FOUND("BUS-027", "Leg name not found found for agreement [{}], senderParty [{}], receiverParty [{}], service [{}] and action [{}]"),
    BUS_MESSAGE_SEND_SUCCESS("BUS-028", "Message sent successfully"),
    BUS_MESSAGE_SEND_FAILURE("BUS-029", "Message send failure"),

    SEC_SECURITY_POLICY_OUTGOING_NOT_FOUND("SEC-001", "Security policy [{}] was not found for outgoing message"),
    SEC_SECURITY_POLICY_OUTGOING_USE("SEC-002", "Security policy [{}] is used for outgoing message"),
    SEC_SECURITY_ALGORITHM_OUTGOING_USE("SEC-003", "Security algorithm [{}] is used for outgoing message"),
    SEC_SECURITY_ALGORITHM_INCOMING_USE("SEC-004", "Security algorithm [{}] is used for incoming message"),
    SEC_SECURITY_USER_OUTGOING_USE("SEC-005", "Security encryption username [{}] is used for outgoing message"),
    SEC_SECURITY_POLICY_INCOMING_NOT_FOUND("SEC-006", "Security policy [{}] for incoming message  was not found"),
    SEC_SECURITY_POLICY_INCOMING_USE("SEC-007", "Security policy [{}] for incoming message is used"),
    SEC_UNSECURED_LOGIN_ALLOWED("SEC-008", "Unsecure login is allowed, no authentication will be performed"),
    SEC_BASIC_AUTHENTICATION_USE("SEC-009", "Basic authentication is used"),
    SEC_X509CERTIFICATE_AUTHENTICATION_USE("SEC-010", "X509Certificate authentication is used"),
    SEC_BLUE_COAT_AUTHENTICATION_USE("SEC-011", "Blue coat authentication is used"),
    SEC_CONNECTION_ATTEMPT("SEC-012", "The host [{}] attempted to access [{}]"),
    SEC_AUTHORIZED_ACCESS("SEC-013", "The host [{}] has been granted access to [{}] with roles [{}]"),
    SEC_UNAUTHORIZED_ACCESS("SEC-014", "The host [{}] has been refused access to [{}]"),
    SEC_CERTIFICATE_EXPIRED("SEC-015", "Certificate is not valid at the current date [{}]. Certificate valid from [{}] to [{}]"),
    SEC_CERTIFICATE_NOT_YET_VALID("SEC-016", "Certificate is not yet valid at the current date [{}]. Certificate valid from [{}] to [{}]");


    String code;
    String message;

    DomibusMessageCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
