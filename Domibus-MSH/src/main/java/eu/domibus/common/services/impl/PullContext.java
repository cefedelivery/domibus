package eu.domibus.common.services.impl;

import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.common.model.configuration.Mpc;
import eu.domibus.common.model.configuration.Party;
import eu.domibus.common.model.configuration.Process;

import javax.xml.soap.SOAPMessage;
import java.util.HashSet;
import java.util.Set;

import static eu.domibus.common.services.impl.PullRequestStatus.ONE_MATCHING_PROCESS;

/**
 * Created by dussath on 5/24/17.
 */
public class PullContext {

    private Process process;
    private Party currentMsh;
    private Party toBePulled;
    private SOAPMessage pullMessage;
    private String pModeKey;
    private Set<PullRequestStatus> pullRequestStatuses=new HashSet<>();
    private String initiatorName;
    private String mpcName;

    public Set<Party> getResponderParties() {
        Set<Party> responderParties = new HashSet<>(process.getResponderParties());
        responderParties.remove(currentMsh);
        return responderParties;
    }

    public String getAgreement() {
        if (process.getAgreement() != null) {
            return process.getAgreement().getName();
        }
        return "";
    }

    String getCurrentMSHName() {
        return currentMsh.getName();
    }

    String getToBePulledName() {
        return toBePulled.getName();
    }

    String getLegName() {
        return process.getLegs().iterator().next().getName();
    }

    String getActionName() {
        return process.getLegs().iterator().next().getAction().getName();
    }

    String getServiceName() {
        return process.getLegs().iterator().next().getService().getName();
    }

    String getMpcName() {
        Mpc defaultMpc = process.getLegs().iterator().next().getDefaultMpc();
        if (defaultMpc != null) {
            return defaultMpc.getName();
        }
        return "";
    }

    LegConfiguration getLegConfiguration() {
        return process.getLegs().iterator().next();
    }


    public Process getProcess() {
        return process;
    }

    public Party getCurrentMsh() {
        return currentMsh;
    }

    public Party getToBePulled() {
        return toBePulled;
    }

    public SOAPMessage getPullMessage() {
        return pullMessage;
    }

    public String getpModeKey() {
        return pModeKey;
    }

    public void addRequestStatus(PullRequestStatus pullRequestStatus) {
        pullRequestStatuses.add(pullRequestStatus);
    }

    public void setProcess(Process process) {
        this.process = process;
    }

    public void setCurrentMsh(Party currentMsh) {
        this.currentMsh = currentMsh;
    }

    public void setToBePulled(Party toBePulled) {
        this.toBePulled = toBePulled;
    }

    public boolean isValid() {
        return pullRequestStatuses.size()==1 && ONE_MATCHING_PROCESS.equals(pullRequestStatuses.iterator().next());
    }


    public void setPullMessage(SOAPMessage pullMessage) {
        this.pullMessage = pullMessage;
    }

    public void setpModeKey(String pModeKey) {
        this.pModeKey = pModeKey;
    }


    public String getInitiatorName() {
        return initiatorName;
    }

    public void setInitiatorName(String initiatorName) {
        this.initiatorName = initiatorName;
    }

    public void setMpcName(String mpcName) {
        this.mpcName = mpcName;
    }
}
