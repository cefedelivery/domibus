
    package eu.domibus.common;

    /**
     * @author Christian Koch, Stefan Mueller
     */
    public enum MessageStatus {
        /**
         * The message is ready to be put in the send queue .
         */
        READY_TO_SEND,
        /**
         * The message is ready to get pulled.
         */
        READY_TO_PULL,
        /**
         * The message is being pulled.
         */
        BEING_PULLED,
        /**
         * The message is in the send queue.
         */
        SEND_ENQUEUED,
        /**
         * the message is being transferred to another MSH.
         */
        SEND_IN_PROGRESS,
        /**
         * the message has been sent, but no receipt has been received yet.
         */
        WAITING_FOR_RECEIPT,
        /**
         * the message has been sent and acknowledged by the receiving MSH. The nature of the acknowledgement
         * (http 2xx, NRR receipt etc.) is dependent on the corresponding message PMode.
         */
        ACKNOWLEDGED,
        /**
         * the message has been sent and acknowledged by the receiving MSH with an acknowledgement containing an ebMS3
         * error of severity WARNING. The nature of the acknowledgement(http 2xx, NRR receipt etc.) is dependent on the
         * corresponding message PMode. This IS CONSIDERED a SUCCESSFUL message transfer.
         */
        ACKNOWLEDGED_WITH_WARNING,
        /**
         * The last attempt to send the message has failed. There will either be retries, or the status will change to
         * SEND_FAILURE (depending on the corresponding PMode)
         */
        SEND_ATTEMPT_FAILED,

        /**
         * The final send attempt of the message has failed and there will be no more retries
         */
        SEND_FAILURE,

        /**
         * The corresponding message can not be found on the server. This indicates a request with an erroneous messageId
         */
        NOT_FOUND,

        /**
         * The last attempt to send the message has failed. There will  be a retry once the waiting interval of the
         * corresponding PMode has passed.
         */
        WAITING_FOR_RETRY,

        /**
         * The message has been received successfully.
         */
        RECEIVED,

        /**
         * The message has been received and an acknowledgement containing an ebMS3
         * error of severity WARNING has been returned to the sender. This IS CONSIDERED a SUCCESSFUL message transfer.
         */
        RECEIVED_WITH_WARNINGS,

        /**
         * The message has been processed by the MSH but the payloads have been deleted due to the message retention
         * policy of the corresponding PMode.
         */
        DELETED,

        /**
         * The message has been downloaded by the receiving access point.
         */
        DOWNLOADED
    }
