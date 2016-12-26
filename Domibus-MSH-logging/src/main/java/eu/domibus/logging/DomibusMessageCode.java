package eu.domibus.logging;


import eu.domibus.logging.api.MessageCode;

/**
 * @author Cosmin Baciu
 * @since 3.3
 */
public enum DomibusMessageCode implements MessageCode {

    BUS_MESSAGE_RECEIVED("BUS-001", "The message with ID [{}] has been successfully received"),
    BUS_MESSAGE_RECEIVE_FAILED("BUS-002", "Failed to receive the message with ID [{}]"),
    BUS_MESSAGE_VALIDATION_FAILED("BUS-003", "Failed to validate the message with ID [{}]"),
    BUS_BACKEND_NOTIFICATION_FAILED("BUS-004", "Failed to notify backend for incoming message with ID [{}]"),
    BUS_CHARSET_INVALID("BUS-005", "Invalid charset [{}] used for message with ID [{}]"),
    BUS_RECEIPT_INVALID_WITH_NO_SECURITY_HEADER("BUS-006", "Invalid NonRepudiationInformation: no security header found for message with ID [{}]"),
    BUS_RECEIPT_INVALID_WITH_MULTIPLE_SECURITY_HEADERS("BUS-007", "Invalid NonRepudiationInformation: multiple security headers found for message with ID [{}]"),
    BUS_RECEIPT_INVALID_WITH_MESSAGING_NOT_SIGNED("BUS-008", "Invalid NonRepudiationInformation: eb:Messaging not signed for message with ID [{}]"),
    BUS_RECEIPT_INVALID_NOT_MATCHING_THE_MESSAGE("BUS-009", "Invalid NonRepudiationInformation: non repudiation information and request message do not match for message with ID [{}]"),
    BUS_RECEIPT_INVALID_EMPTY("BUS-010", "There is no content inside the receipt element received by the responding gateway for message with ID [{}]"),
    BUS_RECEIPT_GENERAL_ERROR("BUS-011", "Reliability check failed, check your configuration for message with ID [{}]"),
    BUS_RECEIPT_CHECK_SUCCESSFUL("BUS-012", "Reliability check was successful for message with ID [{}]"),

    SEC_CONNECTION_ATTEMPT("SEC-001", "TODO");

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
