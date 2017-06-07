package eu.domibus.common.services.impl;

import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.common.model.configuration.Party;
import eu.domibus.common.model.configuration.Process;
import eu.domibus.util.PojoInstaciatorUtil;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.Map;

import static eu.domibus.common.services.impl.PullContext.MPC;
import static eu.domibus.common.services.impl.PullContext.NOTIFY_BUSINNES_ON_ERROR;
import static eu.domibus.common.services.impl.PullRequestStatus.*;
import static org.junit.Assert.*;

/**
 * Created by dussath on 6/1/17.
 *
 */
public class PullContextTest {

    @Test
    public void checkProcessValidityWithMoreThanOneLegAndDifferentReponder() throws Exception {
        PullContext pullContext = new PullContext();
        pullContext.setProcess(PojoInstaciatorUtil.instanciate(Process.class, "legs{[name:leg1];[name:leg2]}"));
        pullContext.checkProcessValidity();
        assertEquals(false, pullContext.isValid());
        assertEquals(2,pullContext.getPullRequestStatuses().size());
        assertTrue(pullContext.getPullRequestStatuses().contains(MORE_THAN_ONE_LEG_FOR_THE_SAME_MPC));
        assertTrue(pullContext.getPullRequestStatuses().contains(NO_RESPONDER));
        pullContext.setProcess(PojoInstaciatorUtil.instanciate(Process.class, "legs{[name:leg1];[name:leg2]}","responderParties{[name:resp1];[name:resp2]}"));
        pullContext.checkProcessValidity();
        assertEquals(2,pullContext.getPullRequestStatuses().size());
        assertTrue(pullContext.getPullRequestStatuses().contains(MORE_THAN_ONE_LEG_FOR_THE_SAME_MPC));
        assertTrue(pullContext.getPullRequestStatuses().contains(TOO_MANY_RESPONDER));
        pullContext.setProcess(PojoInstaciatorUtil.instanciate(Process.class, "legs{[name:leg1];[name:leg2]}","responderParties{[name:resp1]}"));
        pullContext.checkProcessValidity();
        assertEquals(1,pullContext.getPullRequestStatuses().size());
        assertTrue(pullContext.getPullRequestStatuses().contains(MORE_THAN_ONE_LEG_FOR_THE_SAME_MPC));
    }

    @Test
    public void checkProcessValidityWithZeroLeg() throws Exception {
        PullContext pullContext = new PullContext();
        pullContext.setProcess(PojoInstaciatorUtil.instanciate(Process.class));
        pullContext.checkProcessValidity();
        assertEquals(false, pullContext.isValid());
    }

    @Test
    public void checkProcessWithNoLegs() throws Exception {
        PullContext pullContext = new PullContext();
        pullContext.setProcess(PojoInstaciatorUtil.instanciate(Process.class, "responderParties{[name:resp1]}"));
        pullContext.checkProcessValidity();
        assertEquals(false, pullContext.isValid());
        assertEquals(1, pullContext.getPullRequestStatuses().size());
        assertTrue(pullContext.getPullRequestStatuses().contains(NO_PROCESS_LEG));
    }

    @Test
    public void checkProcessValidityWithOneLeg() throws Exception {
        PullContext pullContext = new PullContext();
        pullContext.setProcess(PojoInstaciatorUtil.instanciate(Process.class, "legs{[name:leg1]}","responderParties{[name:resp1]}"));
        pullContext.checkProcessValidity();
        assertEquals(true, pullContext.isValid());
        assertTrue(pullContext.getPullRequestStatuses().contains(ONE_MATCHING_PROCESS));
    }

    @Test
    public void filterLegOnMpc(){
        PullContext pullContext=new PullContext();
        pullContext.setProcess(PojoInstaciatorUtil.instanciate(Process.class, "legs{[name:leg1,defaultMpc[name:test1,qualifiedName:qn1]];[name:leg2,defaultMpc[name:test2,qualifiedName:qn2]]}","responderParties{[name:resp1]}"));
        LegConfiguration legConfiguration = pullContext.filterLegOnMpc("qn1");
        assertEquals("qn1",legConfiguration.getDefaultMpc().getQualifiedName());
    }

    @Test
    public void createProcessWarningMessage(){
        PullContext pullContext=new PullContext();
        try {
            pullContext.checkProcessValidity();
            assertTrue(false);
        }catch (IllegalArgumentException i){

        }
        pullContext.setProcess(PojoInstaciatorUtil.instanciate(Process.class));
        pullContext.checkProcessValidity();
        assertEquals("No leg configuration found\n" +
                "No responder configured",pullContext.createProcessWarningMessage().trim());
    }

    @Test
    public void send(){
        PullContext pullContext=new PullContext();
        pullContext.setProcess(PojoInstaciatorUtil.instanciate(Process.class, "agreement[name:agr1]","legs{[name:leg1,defaultMpc[name:test1,qualifiedName:qn1]];[name:leg2,defaultMpc[name:test2,qualifiedName:qn2]]}","responderParties{[name:resp1]}","initiatorParties{[name:init1];[name:init2]}"));
        pullContext.setResponder(PojoInstaciatorUtil.instanciate(Party.class," [name:resp1]"));
        ArgumentCaptor<Map> argument = ArgumentCaptor.forClass(Map.class);
        PullContextCommand mock = Mockito.mock(PullContextCommand.class);
        pullContext.send(mock);
        Mockito.verify(mock,Mockito.times(4)).execute(argument.capture());
        Map pullMessage = argument.getAllValues().get(0);
        assertEquals("qn1", pullMessage.get(MPC));
        assertEquals("resp1:init1:Mock:Mock:agr1:leg1", pullMessage.get(PullContext.PMODE_KEY));
        assertEquals("false", pullMessage.get(NOTIFY_BUSINNES_ON_ERROR));
        pullMessage = argument.getAllValues().get(1);
        assertEquals("qn1", pullMessage.get(MPC));
        assertEquals("resp1:init2:Mock:Mock:agr1:leg1",pullMessage.get(PullContext.PMODE_KEY));
        assertEquals("false", pullMessage.get(NOTIFY_BUSINNES_ON_ERROR));
        pullMessage = argument.getAllValues().get(2);
        assertEquals("qn2",pullMessage.get(MPC));
        assertEquals("resp1:init1:Mock:Mock:agr1:leg2",pullMessage.get(PullContext.PMODE_KEY));
        assertEquals("false", pullMessage.get(NOTIFY_BUSINNES_ON_ERROR));
        pullMessage = argument.getAllValues().get(3);
        assertEquals("qn2",pullMessage.get(MPC));
        assertEquals("resp1:init2:Mock:Mock:agr1:leg2",pullMessage.get(PullContext.PMODE_KEY));
        assertEquals("false", pullMessage.get(NOTIFY_BUSINNES_ON_ERROR));
    }

}