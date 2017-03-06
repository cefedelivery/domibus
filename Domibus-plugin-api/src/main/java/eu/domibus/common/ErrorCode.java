package eu.domibus.common;

/**
 * @author Christian Koch, Stefan Mueller
 * @version 1.0
 * @since 3.0
 */
public enum ErrorCode {
    EBMS_0001("EBMS:0001"), EBMS_0002("EBMS:0002"), EBMS_0003("EBMS:0003"), EBMS_0004("EBMS:0004"), EBMS_0005("EBMS:0005"), EBMS_0006("EBMS:0006"), EBMS_0007("EBMS:0007"),
    EBMS_0008("EBMS:0008"), EBMS_0009("EBMS:0009"), EBMS_0010("EBMS:0010"), EBMS_0011("EBMS:0011"), EBMS_0101("EBMS:0101"), EBMS_0102("EBMS:0102"), EBMS_0103("EBMS:0103"),
    EBMS_0201("EBMS:0201"), EBMS_0202("EBMS:0202"), EBMS_0301("EBMS:0301"), EBMS_0302("EBMS:0302"), EBMS_0303("EBMS:0303"), EBMS_0020("EBMS:0020"), EBMS_0021("EBMS:0021"),
    EBMS_0022("EBMS:0022"), EBMS_0023("EBMS:0023"), EBMS_0030("EBMS:0030"), EBMS_0031("EBMS:0031"), EBMS_0040("EBMS:0040"), EBMS_0041("EBMS:0041"), EBMS_0042("EBMS:0042"),
    EBMS_0043("EBMS:0043"), EBMS_0044("EBMS:0044"), EBMS_0045("EBMS:0045"), EBMS_0046("EBMS:0046"), EBMS_0047("EBMS:0047"), EBMS_0048("EBMS:0048"), EBMS_0049("EBMS:0049"),
    EBMS_0050("EBMS:0050"), EBMS_0051("EBMS:0051"), EBMS_0052("EBMS:0052"), EBMS_0053("EBMS:0053"), EBMS_0054("EBMS:0054"), EBMS_0055("EBMS:0055"), EBMS_0060("EBMS:0060"),
    EBMS_0065("EBMS:0065");


    public static final String SEVERITY_FAILURE = "failure";
    public static final String SEVERITY_WARNING = "warning";
    private static final String ORIGIN_EBMS = "ebMS";
    private static final String ORIGIN_RELIABILITY = "reliability";
    private static final String ORIGIN_SECURITY = "security";
    private String errorCodeName;


    ErrorCode(final String errorCodeName) {
        this.errorCodeName = errorCodeName;
    }

    public static ErrorCode findBy(final String errorCodeName) {
        for (final ErrorCode errorCode : ErrorCode.values()) {
            if (errorCode.getErrorCodeName().equals(errorCodeName)) {
                return errorCode;
            }
        }

        throw new IllegalArgumentException("No ErrorCode found for ErrorCodeName: " + errorCodeName);
    }

    public String getErrorCodeName() {
        return this.errorCodeName;
    }

    @Override
    public String toString() {
        return "ErrorCode{" +
                "errorCodeName='" + errorCodeName + '\'' +
                '}';
    }


    public enum Categories {
        CONTENT, UNPACKAGING, PROCESSING, COMMUNICATION
    }


    public enum OriginErrorCode {

        EBMS_0001(ORIGIN_EBMS, ErrorCode.EBMS_0001),
        EBMS_0002(ORIGIN_EBMS, ErrorCode.EBMS_0002),
        EBMS_0003(ORIGIN_EBMS, ErrorCode.EBMS_0003),
        EBMS_0004(ORIGIN_EBMS, ErrorCode.EBMS_0004),
        EBMS_0005(ORIGIN_EBMS, ErrorCode.EBMS_0005),
        EBMS_0006(ORIGIN_EBMS, ErrorCode.EBMS_0006),
        EBMS_0007(ORIGIN_EBMS, ErrorCode.EBMS_0007),
        EBMS_0008(ORIGIN_EBMS, ErrorCode.EBMS_0008),
        EBMS_0009(ORIGIN_EBMS, ErrorCode.EBMS_0009),
        EBMS_0010(ORIGIN_EBMS, ErrorCode.EBMS_0010),
        EBMS_0301(ORIGIN_EBMS, ErrorCode.EBMS_0301),
        EBMS_0302(ORIGIN_EBMS, ErrorCode.EBMS_0302),
        EBMS_0303(ORIGIN_EBMS, ErrorCode.EBMS_0303),
        EBMS_0011(ORIGIN_EBMS, ErrorCode.EBMS_0011),
        EBMS_0020(ORIGIN_EBMS, ErrorCode.EBMS_0020),
        EBMS_0021(ORIGIN_EBMS, ErrorCode.EBMS_0021),
        EBMS_0022(ORIGIN_EBMS, ErrorCode.EBMS_0022),
        EBMS_0023(ORIGIN_EBMS, ErrorCode.EBMS_0023),
        EBMS_0030(ORIGIN_EBMS, ErrorCode.EBMS_0030),
        EBMS_0031(ORIGIN_EBMS, ErrorCode.EBMS_0031),
        EBMS_0040(ORIGIN_EBMS, ErrorCode.EBMS_0040),
        EBMS_0041(ORIGIN_EBMS, ErrorCode.EBMS_0041),
        EBMS_0042(ORIGIN_EBMS, ErrorCode.EBMS_0042),
        EBMS_0043(ORIGIN_EBMS, ErrorCode.EBMS_0043),
        EBMS_0044(ORIGIN_EBMS, ErrorCode.EBMS_0044),
        EBMS_0045(ORIGIN_EBMS, ErrorCode.EBMS_0045),
        EBMS_0046(ORIGIN_EBMS, ErrorCode.EBMS_0046),
        EBMS_0047(ORIGIN_EBMS, ErrorCode.EBMS_0047),
        EBMS_0048(ORIGIN_EBMS, ErrorCode.EBMS_0048),
        EBMS_0049(ORIGIN_EBMS, ErrorCode.EBMS_0049),
        EBMS_0050(ORIGIN_EBMS, ErrorCode.EBMS_0050),
        EBMS_0051(ORIGIN_EBMS, ErrorCode.EBMS_0051),
        EBMS_0052(ORIGIN_EBMS, ErrorCode.EBMS_0052),
        EBMS_0053(ORIGIN_EBMS, ErrorCode.EBMS_0053),
        EBMS_0054(ORIGIN_EBMS, ErrorCode.EBMS_0054),
        EBMS_0055(ORIGIN_EBMS, ErrorCode.EBMS_0055),
        EBMS_0060(ORIGIN_EBMS, ErrorCode.EBMS_0060),
        EBMS_0065(ORIGIN_EBMS, ErrorCode.EBMS_0065),
        EBMS_0101(ORIGIN_SECURITY, ErrorCode.EBMS_0101),
        EBMS_0102(ORIGIN_SECURITY, ErrorCode.EBMS_0102),
        EBMS_0103(ORIGIN_SECURITY, ErrorCode.EBMS_0103),
        EBMS_0201(ORIGIN_RELIABILITY, ErrorCode.EBMS_0201),
        EBMS_0202(ORIGIN_RELIABILITY, ErrorCode.EBMS_0202);


        private final ErrorCode errorCode;


        private final String origin;

        OriginErrorCode(final String origin, final ErrorCode errorCode) {
            this.origin = origin;
            this.errorCode = errorCode;
        }

        /**
         * "This OPTIONAL attribute identifies the functional module within which the
         * error occurred. This module could be the the ebMS Module, the Reliability Module,
         * or the Security Module. Possible values for this attribute include "ebMS",
         * "reliability", and "security". The use of other modules, and thus their
         * corresponding @origin values, may be specified elsewhere, such as in a
         * forthcoming Part 2 of this specification."
         * (OASIS ebXML Messaging Services Version 3.0: Part 1, Core Features, 1 October 2007)
         */
        public ErrorCode getErrorCode() {
            return this.errorCode;
        }

        /**
         * "This REQUIRED attribute is a unique identifier for the type of error."
         * (OASIS ebXML Messaging Services Version 3.0: Part 1, Core Features, 1 October 2007)
         */
        public String getOrigin() {
            return this.origin;
        }
    }

    public enum EbMS3ErrorCode {

        EBMS_0301(OriginErrorCode.EBMS_0301, "MissingReceipt", SEVERITY_FAILURE, Categories.COMMUNICATION),
        EBMS_0302(OriginErrorCode.EBMS_0302, "InvalidReceipt", SEVERITY_FAILURE, Categories.COMMUNICATION),
        EBMS_0303(OriginErrorCode.EBMS_0303, "DecompressionFailure", SEVERITY_FAILURE, Categories.COMMUNICATION),
        EBMS_0001(OriginErrorCode.EBMS_0001, "ValueNotRecognized", SEVERITY_FAILURE, Categories.CONTENT),
        EBMS_0002(OriginErrorCode.EBMS_0002, "FeatureNotSupported", SEVERITY_WARNING, Categories.CONTENT),
        EBMS_0003(OriginErrorCode.EBMS_0003, "ValueInconsistent", SEVERITY_FAILURE, Categories.CONTENT),
        EBMS_0004(OriginErrorCode.EBMS_0004, "Other", SEVERITY_FAILURE, Categories.CONTENT),
        EBMS_0005(OriginErrorCode.EBMS_0005, "ConnectionFailure", SEVERITY_FAILURE, Categories.COMMUNICATION),
        EBMS_0006(OriginErrorCode.EBMS_0006, "EmptyMessagePartitionChannel", SEVERITY_WARNING, Categories.COMMUNICATION),
        EBMS_0007(OriginErrorCode.EBMS_0007, "MimeInconsistency", SEVERITY_FAILURE, Categories.UNPACKAGING),
        EBMS_0008(OriginErrorCode.EBMS_0008, "FeatureNotSupported", SEVERITY_FAILURE, Categories.UNPACKAGING),
        EBMS_0009(OriginErrorCode.EBMS_0009, "InvalidHeader", SEVERITY_FAILURE, Categories.UNPACKAGING),
        EBMS_0010(OriginErrorCode.EBMS_0010, "ProcessingModeMismatch", SEVERITY_FAILURE, Categories.PROCESSING),
        EBMS_0011(OriginErrorCode.EBMS_0011, "ExternalPayloadError", SEVERITY_FAILURE, Categories.CONTENT),
        EBMS_0101(OriginErrorCode.EBMS_0101, "FailedAuthentication", SEVERITY_FAILURE, Categories.PROCESSING),
        EBMS_0102(OriginErrorCode.EBMS_0102, "FailedDecryption", SEVERITY_FAILURE, Categories.PROCESSING),
        EBMS_0103(OriginErrorCode.EBMS_0103, "PolicyNoncompliance", SEVERITY_FAILURE, Categories.PROCESSING),
        EBMS_0201(OriginErrorCode.EBMS_0201, "DysfunctionalReliability", SEVERITY_FAILURE, Categories.PROCESSING),
        EBMS_0202(OriginErrorCode.EBMS_0202, "DeliveryFailure", SEVERITY_FAILURE, Categories.COMMUNICATION),
        EBMS_0020(OriginErrorCode.EBMS_0020, "RoutingFailure", SEVERITY_FAILURE, Categories.PROCESSING),
        EBMS_0021(OriginErrorCode.EBMS_0021, "MPCCapacityExceeded", SEVERITY_FAILURE, Categories.PROCESSING),
        EBMS_0022(OriginErrorCode.EBMS_0022, "MessagePersistenceTimeout", SEVERITY_FAILURE, Categories.PROCESSING),
        EBMS_0023(OriginErrorCode.EBMS_0023, "MessageExpired", SEVERITY_WARNING, Categories.PROCESSING),
        EBMS_0030(OriginErrorCode.EBMS_0030, "BundlingError", SEVERITY_FAILURE, Categories.CONTENT),
        EBMS_0031(OriginErrorCode.EBMS_0031, "RelatedMessageFailed", SEVERITY_FAILURE, Categories.PROCESSING),
        EBMS_0040(OriginErrorCode.EBMS_0040, "BadFragmentGroup", SEVERITY_FAILURE, Categories.CONTENT),
        EBMS_0041(OriginErrorCode.EBMS_0041, "DuplicateMessageSize", SEVERITY_FAILURE, Categories.CONTENT),
        EBMS_0042(OriginErrorCode.EBMS_0042, "DuplicateFragmentCount", SEVERITY_FAILURE, Categories.CONTENT),
        EBMS_0043(OriginErrorCode.EBMS_0043, "DuplicateMessageHeader", SEVERITY_FAILURE, Categories.CONTENT),
        EBMS_0044(OriginErrorCode.EBMS_0044, "DuplicateAction", SEVERITY_FAILURE, Categories.CONTENT),
        EBMS_0045(OriginErrorCode.EBMS_0045, "DuplicateCompressionInfo", SEVERITY_FAILURE, Categories.CONTENT),
        EBMS_0046(OriginErrorCode.EBMS_0046, "DuplicateFragment", SEVERITY_FAILURE, Categories.CONTENT),
        EBMS_0047(OriginErrorCode.EBMS_0047, "BadFragmentStructure", SEVERITY_FAILURE, Categories.UNPACKAGING),
        EBMS_0048(OriginErrorCode.EBMS_0048, "BadFragmentNum", SEVERITY_FAILURE, Categories.CONTENT),
        EBMS_0049(OriginErrorCode.EBMS_0049, "BadFragmentCount", SEVERITY_FAILURE, Categories.CONTENT),
        EBMS_0050(OriginErrorCode.EBMS_0050, "FragmentSizeExceeded", SEVERITY_WARNING, Categories.UNPACKAGING),
        EBMS_0051(OriginErrorCode.EBMS_0051, "ReceiveIntervalExceeded", SEVERITY_FAILURE, Categories.UNPACKAGING),
        EBMS_0052(OriginErrorCode.EBMS_0052, "BadProperties", SEVERITY_WARNING, Categories.UNPACKAGING),
        EBMS_0053(OriginErrorCode.EBMS_0053, "HeaderMismatch", SEVERITY_FAILURE, Categories.UNPACKAGING),
        EBMS_0054(OriginErrorCode.EBMS_0054, "OutOfStorageSpace", SEVERITY_FAILURE, Categories.UNPACKAGING),
        EBMS_0055(OriginErrorCode.EBMS_0055, "DecompressionError", SEVERITY_FAILURE, Categories.PROCESSING),
        EBMS_0060(OriginErrorCode.EBMS_0060, "ResponseUsing-AlternateMEP", SEVERITY_WARNING, Categories.PROCESSING),
        EBMS_0065(OriginErrorCode.EBMS_0065, "Invalid Xml", SEVERITY_FAILURE, Categories.PROCESSING);

        private final OriginErrorCode code;


        private final String shortDescription;


        private final String severity;


        private final Categories category;


        EbMS3ErrorCode(final OriginErrorCode code, final String shortDescription, final String severity, final Categories category) {
            this.code = code;
            this.shortDescription = shortDescription;
            this.severity = severity;
            this.category = category;
        }

        public static EbMS3ErrorCode findErrorCodeBy(final String originErrorCode) {
            for (final EbMS3ErrorCode errorCode : EbMS3ErrorCode.values()) {
                if (errorCode.getCode().getErrorCode().equals(ErrorCode.findBy(originErrorCode))) {
                    return errorCode;
                }
            }

            throw new IllegalArgumentException("No EbMS3ErrorCode found for OriginErrorCode: " + originErrorCode);
        }

        public OriginErrorCode getCode() {
            return this.code;
        }

        /**
         * "This OPTIONAL attribute provides a short description of the error
         * that can be reported in a log, in order to facilitate readability."
         * (OASIS ebXML Messaging Services Version 3.0: Part 1, Core Features, 1 October 2007)
         */
        public String getShortDescription() {
            return this.shortDescription;
        }

        /**
         * "This REQUIRED attribute indicates the severity of the error. Valid
         * values are: warning, failure.
         * The warning value indicates that a potentially disabling condition
         * has been detected, but no message processing and/or exchange has failed
         * so far. In particular, if the message was supposed to be delivered to
         * a consumer, it would be delivered even though a warning was issued.
         * Other related messages in the conversation or MEP can be generated and
         * exchanged in spite of this problem.
         * The failure value indicates that the processing of a message did not
         * proceed as expected, and cannot be considered successful. If, in spite
         * of this, the message payload is in a state of being delivered, the
         * default behavior is not to deliver it, unless an agreement states otherwise
         * (see OpCtx-ErrorHandling). This error does not presume the ability of the
         * MSH to process other messages, although the conversation or the MEP instance
         * this message was involved in is at risk of being invalid."
         * (OASIS ebXML Messaging Services Version 3.0: Part 1, Core Features, 1 October 2007)
         */
        public String getSeverity() {
            return this.severity;
        }

        /**
         * "This OPTIONAL attribute identifies the type of error related to a particular
         * origin. For example: Content, Packaging, UnPackaging, Communication, InternalProcess."
         * (OASIS ebXML Messaging Services Version 3.0: Part 1, Core Features, 1 October 2007)
         */
        public Categories getCategory() {
            return this.category;
        }


        @Override
        public String toString() {
            return "EbMS3ErrorCode{" +
                    "code=" + this.code +
                    ", shortDescription='" + this.shortDescription + '\'' +
                    ", severity=" + this.severity +
                    ", category=" + this.category +
                    '}' + super.toString();
        }
    }
}
