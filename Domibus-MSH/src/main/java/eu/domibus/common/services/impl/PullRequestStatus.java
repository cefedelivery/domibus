package eu.domibus.common.services.impl;

/**
 * @author Thomas Dussart
 * @since 3.3
 */
public enum PullRequestStatus {
    TOO_MANY_PROCESSES,
    TOO_MANY_RESPONDER,
    NO_RESPONDER,
    MORE_THAN_ONE_LEG_FOR_THE_SAME_MPC,
    NO_PROCESSES,
    NO_PROCESS_LEG,
    INVALID_SOAP_MESSAGE,
    ONE_MATCHING_PROCESS
}
