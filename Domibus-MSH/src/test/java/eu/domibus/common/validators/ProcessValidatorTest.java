package eu.domibus.common.validators;

import com.google.common.collect.Lists;
import eu.domibus.api.pmode.PModeException;
import eu.domibus.common.exception.EbMS3Exception;
import eu.domibus.common.model.configuration.Process;
import eu.domibus.common.services.impl.PullProcessStatus;
import eu.domibus.util.PojoInstaciatorUtil;
import org.junit.Test;

import java.util.List;
import java.util.Set;

import static eu.domibus.common.services.impl.PullProcessStatus.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Thomas Dussart
 * @since 3.3
 */

public class ProcessValidatorTest {
    @Test
    public void checkProcessValidityWithMoreThanOneLegAndDifferentResponder() throws Exception {
        Process process = PojoInstaciatorUtil.instanciate(Process.class, "mep[name:oneway]", "legs{[name:leg1];[name:leg2]}");
        Set<PullProcessStatus> pullProcessStatuses = getProcessStatuses(process);
        assertEquals(2, pullProcessStatuses.size());
        assertTrue(pullProcessStatuses.contains(MORE_THAN_ONE_LEG_FOR_THE_SAME_MPC));
        assertTrue(pullProcessStatuses.contains(NO_RESPONDER));
        process = PojoInstaciatorUtil.instanciate(Process.class, "mep[name:oneway]", "legs{[name:leg1];[name:leg2]}", "responderParties{[name:resp1];[name:resp2]}");
        pullProcessStatuses = getProcessStatuses(process);
        assertEquals(2, pullProcessStatuses.size());
        assertTrue(pullProcessStatuses.contains(MORE_THAN_ONE_LEG_FOR_THE_SAME_MPC));
        assertTrue(pullProcessStatuses.contains(TOO_MANY_RESPONDER));
        process = PojoInstaciatorUtil.instanciate(Process.class, "mep[name:oneway]", "legs{[name:leg1];[name:leg2]}", "responderParties{[name:resp1]}");
        pullProcessStatuses = getProcessStatuses(process);
        assertEquals(1, pullProcessStatuses.size());
        assertTrue(pullProcessStatuses.contains(MORE_THAN_ONE_LEG_FOR_THE_SAME_MPC));
    }

    @Test
    public void checkEmptyProcessStatus() throws Exception {
        Set<PullProcessStatus> processStatuses = getProcessStatuses(PojoInstaciatorUtil.instanciate(Process.class));
        assertTrue(processStatuses.contains(NO_PROCESS_LEG));
        assertTrue(processStatuses.contains(NO_RESPONDER));
    }


    @Test
    public void checkProcessWithNoLegs() throws Exception {
        Set<PullProcessStatus> processStatuses = getProcessStatuses(PojoInstaciatorUtil.instanciate(Process.class, "mep[name:oneway]", "responderParties{[name:resp1]}"));
        assertEquals(1, processStatuses.size());
        assertTrue(processStatuses.contains(NO_PROCESS_LEG));
    }

    @Test
    public void checkTooManyProcesses() throws Exception {
        List<Process> processes = Lists.newArrayList(PojoInstaciatorUtil.instanciate(Process.class, "mep[name:oneway]", "legs{[name:leg1]}", "responderParties{[name:resp1]}"),
                PojoInstaciatorUtil.instanciate(Process.class, "mep[name:oneway]", "legs{[name:leg1]}", "responderParties{[name:resp1]}"));
        ProcessValidator processValidator = new ProcessValidator();
        Set<PullProcessStatus> pullProcessStatuses = processValidator.verifyPullProcessStatus(processes);
        assertEquals(1, pullProcessStatuses.size());
        assertTrue(pullProcessStatuses.contains(TOO_MANY_PROCESSES));
    }

    @Test
    public void checkProcessValidityWithOneLeg() throws Exception {
        Set<PullProcessStatus> processStatuses = getProcessStatuses(PojoInstaciatorUtil.instanciate(Process.class, "mep[name:oneway]", "legs{[name:leg1]}", "responderParties{[name:resp1]}"));
        assertEquals(1, processStatuses.size());
        assertTrue(processStatuses.contains(ONE_MATCHING_PROCESS));
    }

    @Test
    public void checkNoProcess() throws Exception {
        ProcessValidator processValidator = new ProcessValidator();
        Set<PullProcessStatus> pullProcessStatuses = processValidator.verifyPullProcessStatus(Lists.<Process>newArrayList());
        assertEquals(1, pullProcessStatuses.size());
        assertTrue(pullProcessStatuses.contains(NO_PROCESSES));
    }

    @Test
    public void createProcessWarningMessage() {
        ProcessValidator processValidator = new ProcessValidator();
        Process process = PojoInstaciatorUtil.instanciate(Process.class);
        try {
            processValidator.validatePullProcess(Lists.newArrayList(process));
            assertTrue(false);
        } catch (PModeException e) {
            assertTrue(e.getMessage().contains("No leg configuration found"));
            assertTrue(e.getMessage().contains("No responder configured"));
        }

    }

    @Test
    public void testOneWayPullOnlySupported() throws EbMS3Exception {
        ProcessValidator processValidator = new ProcessValidator();
        Process process = PojoInstaciatorUtil.instanciate(Process.class, "mep[name:twoway]", "mepBinding[name:pull]", "legs{[name:leg1,defaultMpc[name:test1,qualifiedName:qn1]];[name:leg2,defaultMpc[name:test2,qualifiedName:qn2]]}", "responderParties{[name:resp1]}");
        try {
            processValidator.validatePullProcess(Lists.newArrayList(process));
            assertTrue(false);
        } catch (PModeException e) {
            assertTrue(e.getMessage().contains("Invalid mep. Only one way supported"));
        }
    }


    private Set<PullProcessStatus> getProcessStatuses(Process process) {
        ProcessValidator processValidator = new ProcessValidator();
        return processValidator.verifyPullProcessStatus(Lists.newArrayList(process));
    }

}