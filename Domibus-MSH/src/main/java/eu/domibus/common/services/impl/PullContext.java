package eu.domibus.common.services.impl;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Sets;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.common.model.configuration.Mpc;
import eu.domibus.common.model.configuration.Party;
import eu.domibus.common.model.configuration.Process;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;

import javax.xml.soap.SOAPMessage;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static eu.domibus.common.services.impl.PullRequestStatus.*;
import static eu.domibus.common.services.impl.PullRequestStatus.TOO_MANY_RESPONDER;

/**
 * Created by dussath on 5/24/17.
 *
 * Contextual class for a pull.
 */
public class PullContext {

    private Process process;
    private Party currentMsh;
    private Party responder;
    private Party initiator;
    private SOAPMessage pullMessage;
    private String pModeKey;
    private Set<PullRequestStatus> pullRequestStatuses=new HashSet<>();
    private String mpcQualifiedName;
    private LegConfiguration currentLegConfiguration;

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(PullContext .class);

    public Set<Party> getInitiatorParties() {
        Set<Party> responderParties = new HashSet<>(process.getInitiatorParties());
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

    String getResponderName() {
        return responder.getName();
    }

    String getLegName() {
        return getCurrentLegConfiguration().getName();
    }

    String getActionName() {
        return getCurrentLegConfiguration().getAction().getName();
    }

    String getServiceName() {
        return getCurrentLegConfiguration().getService().getName();
    }

    LegConfiguration getLegConfiguration() {
        return getCurrentLegConfiguration();
    }

    String extractQualifiedNameFromProcess() {
        Mpc defaultMpc = getCurrentLegConfiguration().getDefaultMpc();
        if (defaultMpc != null) {
            return defaultMpc.getQualifiedName();
        }
        return "";
    }


    public Process getProcess() {
        return process;
    }

    Party getCurrentMsh() {
        return currentMsh;
    }

    public Party getResponder() {
        return responder;
    }

    SOAPMessage getPullMessage() {
        return pullMessage;
    }

    public String getpModeKey() {
        return pModeKey;
    }

    void addRequestStatus(final PullRequestStatus pullRequestStatus) {
        if(!ONE_MATCHING_PROCESS.equals(pullRequestStatus)){
            pullRequestStatuses.remove(pullRequestStatus);
        }
        pullRequestStatuses.add(pullRequestStatus);
    }

    public void setProcess(Process process) {
        this.process = process;
    }

    void setCurrentMsh(Party currentMsh) {
        this.currentMsh = currentMsh;
    }

    public void setResponder(Party responder) {
        this.responder = responder;
    }

    public boolean isValid() {
        return pullRequestStatuses.size()==1 && ONE_MATCHING_PROCESS.equals(pullRequestStatuses.iterator().next());
    }



    public String createWarningMessageForIncomingPullRequest(){
        return createWarningMessage(INITIATOR_NOT_FOUND);
    }

    private String createWarningMessage(PullRequestStatus... skipStatus){
        StringBuilder stringBuilder=new StringBuilder();
        Sets.SetView<PullRequestStatus> difference = Sets.difference(pullRequestStatuses, Sets.newHashSet(skipStatus));
        for (PullRequestStatus pullRequestStatus : difference) {
            stringBuilder.append(errorMessageFactory(pullRequestStatus)).append("\n");
        }
        return stringBuilder.toString();
    }
    private String errorMessageFactory(PullRequestStatus pullRequestStatus){
        switch (pullRequestStatus){
            case NO_PROCESSES:return "No process was found for the configuration";
            case INVALID_SOAP_MESSAGE:return "Invalid soap message";
            case NO_PROCESS_LEG:return "No leg configuration found";
            case MORE_THAN_ONE_LEG_FOR_THE_SAME_MPC:return "More than one leg for the same mpc";
            case TOO_MANY_PROCESSES:return "To many processes found";
            case INITIATOR_NOT_FOUND:return "No processes matching with request for the current MSH";
            case TOO_MANY_RESPONDER:return "Pull process should only have one responder configured for mpc";
        }
        return "";
    }


    void setPullMessage(SOAPMessage pullMessage) {
        this.pullMessage = pullMessage;
    }

    public void setpModeKey(String pModeKey) {
        this.pModeKey = pModeKey;
    }


    void setMpcQualifiedName(String mpcQualifiedName) {
        this.mpcQualifiedName = mpcQualifiedName;
    }

    public String getMpcQualifiedName() {
        return mpcQualifiedName;
    }

    public Party getInitiator() {
        return initiator;
    }

    public void setInitiator(Party initiator) {
        this.initiator = initiator;
    }

    private LegConfiguration getCurrentLegConfiguration() {
        return currentLegConfiguration;
    }

    void setCurrentLegConfiguration(LegConfiguration currentLegConfiguration) {
        this.currentLegConfiguration = currentLegConfiguration;
    }

    public LegConfiguration filterLegOnMpc(){
        checkProcessValidity();
        if(isValid()) {
            Collection<LegConfiguration> filter = Collections2.filter(process.getLegs(), new Predicate<LegConfiguration>() {
                @Override
                public boolean apply(LegConfiguration legConfiguration) {
                   return mpcQualifiedName.equals(legConfiguration.getDefaultMpc().getQualifiedName());
                }
            });
            return filter.iterator().next();
        }
        else throw new IllegalArgumentException("Method should be called after correct context setup.");
    }
    void checkProcessValidity() {
        Collection<LegConfiguration> legsWithSameMpc = Collections2.filter(getProcess().getLegs(), new Predicate<LegConfiguration>() {
            @Override
            public boolean apply(LegConfiguration legConfiguration) {
                return getMpcQualifiedName().equals(legConfiguration.getDefaultMpc().getQualifiedName());
            }
        });
        LOG.info("Checking process configuration for pullrequest with mpc "+getMpcQualifiedName());
        if(!getProcess().getInitiatorParties().contains(getCurrentMsh())){
            LOG.error("Only one leg authorized in a oneway pull. PMode skipped!");
            addRequestStatus(INITIATOR_NOT_FOUND);
        }
        if (legsWithSameMpc.size() > 0) {
            LOG.error("Only one leg authorized in a oneway pull. PMode skipped!");
            addRequestStatus(MORE_THAN_ONE_LEG_FOR_THE_SAME_MPC);
        }
        if (getProcess().getLegs().size() == 0) {
            LOG.error("No legs configured. PMode skipped!");
            addRequestStatus(NO_PROCESS_LEG);
        }
        if(getProcess().getResponderParties().size()>1){
            LOG.error("Pull process should only have one responder configured for mpc");
            addRequestStatus(TOO_MANY_RESPONDER);
        }
        if(getProcess().getResponderParties().size()==0){
            //@question what to do here.
            throw new RuntimeException("No responder");
        }
    }

}
