package eu.domibus.common.validators;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.pmode.PModeException;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.common.model.configuration.Mep;
import eu.domibus.common.model.configuration.Process;
import eu.domibus.common.services.impl.PullProcessStatus;
import eu.domibus.core.pull.PullMessageService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.plugin.BackendConnector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static eu.domibus.common.services.impl.PullProcessStatus.*;

/**
 * @author Thomas Dussart
 * @since 3.3
 */
@Component
public class ProcessValidator {

    private final static DomibusLogger LOG = DomibusLoggerFactory.getLogger(ProcessValidator.class);

    @Autowired
    PullMessageService pullMessageService;

    /**
     * In the case of pull process some restrictions are applied to the configuration.
     * This method validate that the configuration is in sync with the restrictions.
     *
     * @param pullProcesses the list of potential pull processes.
     * @throws PModeException in case of process misconfiguration
     */

    @Transactional(propagation = Propagation.SUPPORTS, noRollbackFor = PModeException.class)
    public void validatePullProcess(List<Process> pullProcesses) {
        Set<Process> processes = new HashSet<>(pullProcesses);
        Set<PullProcessStatus> pullProcessStatuses = verifyPullProcessStatus(processes);
        if (!uniqueCorrectlyConfiguredPullProcess(pullProcessStatuses)) {
            LOG.error("There is a misconfiguration with pull processes:");
            for (Process process : processes) {
                LOG.error("Process name:" + process.getName());
            }
            throw new PModeException(DomibusCoreErrorCode.DOM_003, createWarningMessage(pullProcessStatuses));
        }
    }

    Set<PullProcessStatus> verifyPullProcessStatus(Set<Process> pullProcesses) {
        Set<PullProcessStatus> pullProcessStatuses = new HashSet<>();
        pullProcessStatuses.add(checkOnlyOnePullProcess(pullProcesses));
        if (pullProcesses.size() == 1) {
            Process process = pullProcesses.iterator().next();
            pullProcessStatuses.add(checkMpcConfiguration(process));
            pullProcessStatuses.add(checkLegConfiguration(process));
            pullProcessStatuses.add(checkResponderConfiguration(process));
            pullProcessStatuses.add(checkMepConfiguration(process));
            if (pullProcessStatuses.size() > 1) {
                pullProcessStatuses.remove(ONE_MATCHING_PROCESS);
            }
        }
        return pullProcessStatuses;
    }

    private PullProcessStatus checkMepConfiguration(Process process) {
        PullProcessStatus status = INVALID_MEP;
        Mep binding = process.getMep();
        if (binding != null) {
            String name = binding.getName();
            if (name != null) {
                if (BackendConnector.Mep.ONE_WAY.getFileMapping().equalsIgnoreCase(name)) {
                    status = ONE_MATCHING_PROCESS;
                }
            }
        }
        return status;
    }

    private boolean uniqueCorrectlyConfiguredPullProcess(Set<PullProcessStatus> pullProcessStatuses) {
        return pullProcessStatuses.size() == 1 && pullProcessStatuses.contains(ONE_MATCHING_PROCESS);
    }


    private PullProcessStatus checkOnlyOnePullProcess(final Set<Process> pullProcesses) {
        PullProcessStatus status = ONE_MATCHING_PROCESS;
        if (pullProcesses.size() > 1) {
            status = TOO_MANY_PROCESSES;
        } else if (pullProcesses.size() == 0) {
            status = NO_PROCESSES;
        }
        return status;
    }

    protected PullProcessStatus checkMpcConfiguration(final Process process) {
        if (pullMessageService.allowMultipleLegsInPullProcess()) {
            return checkMpcConfigurationSameSecurityPolicy(process);
        } else {
            return checkMpcConfigurationOneLegPerMpc(process);
        }
    }

    /**
     * Verifies the mpc configured in the process's legs
     * Multiple legs configured with the same mpc are allowed as long as they have the same security policy.
     *
     * @param process the process to be checked.
     * @return the resulted status
     */
    protected PullProcessStatus checkMpcConfigurationSameSecurityPolicy(final Process process) {
        PullProcessStatus status = ONE_MATCHING_PROCESS;
        // when there are multiple legs with the same mpc in a pull process, all legs must have the same security policy
        HashMap<String, String> mpcsSecurityMapping = new HashMap<>();
        for (LegConfiguration legConfiguration : process.getLegs()) {
            if(mpcExistsWithDifferentSecurityPolicy(legConfiguration, mpcsSecurityMapping)) {
                LOG.warn("Different security policies configured in legs authorized with the same mpc in a oneway pull process [{}].", process.getName());
                status = MULTIPLE_LEGS_DIFFERENT_SECURITY;
                break;
            }
            mpcsSecurityMapping.put(legConfiguration.getDefaultMpc().getQualifiedName(), legConfiguration.getSecurity().getName());
        }
        return status;
    }

    protected boolean mpcExistsWithDifferentSecurityPolicy(LegConfiguration legConfiguration, HashMap<String, String> mpcsSecurityMapping) {
        String mpc = legConfiguration.getDefaultMpc().getQualifiedName();
        String securityPolicyName = legConfiguration.getSecurity().getName();
        if (mpcsSecurityMapping.containsKey(mpc) &&
                !mpcsSecurityMapping.get(mpc).equals(securityPolicyName)) {
            return true;
        }
        return false;
    }

    /**
     * Verifies the mpc configured in the process's legs.
     * Multiple legs configured with the same mpc are not allowed.
     *
     * @param process the process to be checked.
     * @return the resulted status
     */
    protected PullProcessStatus checkMpcConfigurationOneLegPerMpc(final Process process) {
        PullProcessStatus status = ONE_MATCHING_PROCESS;
        Multiset<String> mpcs = HashMultiset.create();
        for (LegConfiguration legConfiguration : process.getLegs()) {
            mpcs.add(legConfiguration.getDefaultMpc().getQualifiedName());
        }

        for (String mpc : mpcs) {
            if (mpcs.count(mpc) > 1) {
                LOG.warn("Only one leg authorized with the same mpc in a oneway pull. PMode skipped!");
                status = MORE_THAN_ONE_LEG_FOR_THE_SAME_MPC;
                break;
            }
        }
        return status;
    }

    private PullProcessStatus checkLegConfiguration(final Process process) {
        PullProcessStatus status = ONE_MATCHING_PROCESS;
        if (process.getLegs().size() == 0) {
            LOG.warn("No legs configured. PMode skipped!");
            status = NO_PROCESS_LEG;
        }
        return status;
    }

    private PullProcessStatus checkResponderConfiguration(final Process process) {
        PullProcessStatus status = ONE_MATCHING_PROCESS;

        if (pullMessageService.allowDynamicInitiatorInPullProcess()) {
            LOG.debug("There is no need for a verification of the number of " +
                    "initiators because the property thate allows dynamic initiator in pull process is enabled");
            return status;
        }

        if (process.getInitiatorParties().size() > 1) {
            LOG.warn("Pull process should only have one responder configured for mpc");
            status = TOO_MANY_RESPONDER;
        }
        if (process.getInitiatorParties().size() == 0) {
            LOG.warn("No responder configured.");
            status = NO_RESPONDER;
        }
        return status;
    }

    private String createWarningMessage(final Set<PullProcessStatus> statuses) {
        statuses.remove(ONE_MATCHING_PROCESS);
        StringBuilder stringBuilder = new StringBuilder();
        for (PullProcessStatus status : statuses) {
            stringBuilder.append(errorMessageFactory(status));
        }
        return stringBuilder.toString();
    }

    private String errorMessageFactory(PullProcessStatus pullProcessStatus) {
        switch (pullProcessStatus) {
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
            case INVALID_MEP:
                return "Invalid mep. Only one way supported";
        }
        return "";
    }


}
