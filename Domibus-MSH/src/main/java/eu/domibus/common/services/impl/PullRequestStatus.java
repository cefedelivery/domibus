package eu.domibus.common.services.impl;

/**
 * Created by dussath on 5/30/17.
 */
public enum PullRequestStatus {
    TOO_MANY_PROCESSES,
    TOO_MANY_RESPONDER,
    MORE_THAN_ONE_LEG_FOR_THE_SAME_MPC,
    NO_PROCESSES,
    NO_PROCESS_LEG,
    INVALID_SOAP_MESSAGE,
    INITIATOR_NOT_FOUND,
    ONE_MATCHING_PROCESS
}
