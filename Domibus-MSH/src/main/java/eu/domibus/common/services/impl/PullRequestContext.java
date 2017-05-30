package eu.domibus.common.services.impl;

import eu.domibus.common.model.configuration.Process;

/**
 * Created by dussath on 5/30/17.
 */
public class PullRequestContext {
    private PullRequestStatus pullRequestStatus;
    private Process process;

    public PullRequestContext(PullRequestStatus pullRequestStatus, Process process) {
        this.pullRequestStatus = pullRequestStatus;
        this.process = process;
    }

    public PullRequestStatus getPullRequestStatus() {
        return pullRequestStatus;
    }


    public Process getProcess() {
        return process;
    }


}
