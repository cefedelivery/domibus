package eu.domibus.common.services.impl;

import com.google.common.base.Predicate;
import com.google.common.collect.*;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.common.model.configuration.Party;
import eu.domibus.common.model.configuration.Process;
import eu.domibus.ebms3.common.context.MessageExchangeConfiguration;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;

import java.util.*;

import static eu.domibus.common.services.impl.PullRequestStatus.*;

/**
 * @author Thomas Dussart
 * @since 3.3
 * <p>
 * Contextual class for a pull.
 */
public class PullContext {

    private Process process;
    private Party responder;
    private Party initiator;
    private String pModeKey;
    private Set<PullRequestStatus> pullRequestStatuses = new HashSet<>();
    private String mpcQualifiedName;
    private LegConfiguration currentLegConfiguration;
    public static final String MPC = "mpc";
    public static final String PMODE_KEY = "pmodKey";
    public static final String NOTIFY_BUSINNES_ON_ERROR = "not";

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(PullContext.class);

    public String getAgreement() {
        if (process.getAgreement() != null) {
            return process.getAgreement().getName();
        }
        return "";
    }


    public Process getProcess() {
        return process;
    }


    public Party getResponder() {
        return responder;
    }


    public String getpModeKey() {
        return pModeKey;
    }

    void addRequestStatus(final PullRequestStatus pullRequestStatus) {
        if (!ONE_MATCHING_PROCESS.equals(pullRequestStatus)) {
            pullRequestStatuses.remove(ONE_MATCHING_PROCESS);
        }
        pullRequestStatuses.add(pullRequestStatus);
    }

    public void setProcess(Process process) {
        this.process = process;
    }


    public void setResponder(Party responder) {
        this.responder = responder;
    }

    public boolean isValid() {
        boolean valid = false;
        if (process == null) {
            addRequestStatus(NO_PROCESSES);
        } else {
            pullRequestStatuses.clear();
            checkMpcConfiguration();
            checkLegConfiguration();
            checkResponderConfiguration();
            if (pullRequestStatuses.isEmpty()) {
                addRequestStatus(ONE_MATCHING_PROCESS);
                valid = true;
            }
        }
        return valid;
    }


    public String createProcessWarningMessage() {
        return createWarningMessage();
    }

    private String createWarningMessage(PullRequestStatus... skipStatus) {
        StringBuilder stringBuilder = new StringBuilder();
        Sets.SetView<PullRequestStatus> difference = Sets.difference(pullRequestStatuses, Sets.newHashSet(skipStatus));
        for (PullRequestStatus pullRequestStatus : difference) {
            stringBuilder.append(errorMessageFactory(pullRequestStatus)).append("\n");
        }
        return stringBuilder.toString();
    }

    private String errorMessageFactory(PullRequestStatus pullRequestStatus) {
        switch (pullRequestStatus) {
            case NO_PROCESSES:
                return "No process was found for the configuration";
            case INVALID_SOAP_MESSAGE:
                return "Invalid soap message";
            case NO_PROCESS_LEG:
                return "No leg configuration found";
            case MORE_THAN_ONE_LEG_FOR_THE_SAME_MPC:
                return "More than one leg for the same mpc";
            case TOO_MANY_PROCESSES:
                return "To many processes found";
            case TOO_MANY_RESPONDER:
                return "Pull process should only have one responder configured for mpc";
            case NO_RESPONDER:
                return "No responder configured";
        }
        return "";
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


    private void setCurrentLegConfiguration(LegConfiguration currentLegConfiguration) {
        this.currentLegConfiguration = currentLegConfiguration;
        setMpcQualifiedName(currentLegConfiguration.getDefaultMpc().getQualifiedName());
    }

    public LegConfiguration filterLegOnMpc() {
        if (isValid() && mpcQualifiedName != null) {
            Collection<LegConfiguration> filter = Collections2.filter(process.getLegs(), new Predicate<LegConfiguration>() {
                @Override
                public boolean apply(LegConfiguration legConfiguration) {
                    return mpcQualifiedName.equals(legConfiguration.getDefaultMpc().getQualifiedName());
                }
            });
            return filter.iterator().next();
        } else throw new IllegalArgumentException("Method should be called after correct context setup.");
    }

    private void checkResponderConfiguration() {
        if (getProcess().getResponderParties().size() > 1) {
            LOG.error("Pull process should only have one responder configured for mpc");
            addRequestStatus(TOO_MANY_RESPONDER);
        }
        if (getProcess().getResponderParties().size() == 0) {
            LOG.error("No responder configured.");
            addRequestStatus(NO_RESPONDER);
        }
    }

    private void checkLegConfiguration() {
        if (getProcess().getLegs().size() == 0) {
            LOG.error("No legs configured. PMode skipped!");
            addRequestStatus(NO_PROCESS_LEG);
        }
    }

    private void checkMpcConfiguration() {
        Multiset<String> mpcs = HashMultiset.create();
        for (LegConfiguration legConfiguration : process.getLegs()) {
            mpcs.add(legConfiguration.getDefaultMpc().getQualifiedName());
        }

        for (String mpc : mpcs) {
            if (mpcs.count(mpc) > 1) {
                LOG.error("Only one leg authorized with the same mpc in a oneway pull. PMode skipped!");
                addRequestStatus(MORE_THAN_ONE_LEG_FOR_THE_SAME_MPC);
                break;
            }
        }
    }


    Set<PullRequestStatus> getPullRequestStatuses() {
        return Collections.unmodifiableSet(pullRequestStatuses);
    }

    public void send(PullContextCommand command) {
        for (LegConfiguration legConfiguration : process.getLegs()) {
            setCurrentLegConfiguration(legConfiguration);
            for (Party party : process.getInitiatorParties()) {
                setInitiator(party);
                MessageExchangeConfiguration messageExchangeConfiguration = new MessageExchangeConfiguration(getAgreement(),
                        initiator.getName(),
                        responder.getName(),
                        currentLegConfiguration.getService().getName(),
                        currentLegConfiguration.getAction().getName(),
                        currentLegConfiguration.getName());
                setpModeKey(messageExchangeConfiguration.getReversePmodeKey());
                Map<String, String> map = Maps.newHashMap();
                map.put(MPC,getMpcQualifiedName());
                map.put(PMODE_KEY,getpModeKey());
                map.put(NOTIFY_BUSINNES_ON_ERROR,String.valueOf(legConfiguration.getErrorHandling().isBusinessErrorNotifyConsumer()));
                command.execute(map);
            }
        }
    }
}
