package eu.domibus.common.services.impl;

import eu.domibus.common.model.configuration.Process;
import eu.domibus.util.PojoInstaciatorUtil;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by dussath on 6/1/17.
 */
public class PullContextTest {

    @Test
    public void checkProcessValidityWithMoreThanOneLeg() throws Exception {
        PullContext pullContext = new PullContext();
        pullContext.setProcess(PojoInstaciatorUtil.instanciate(Process.class, "legs{[name:leg1];[name:leg2]}"));
        pullContext.checkProcessValidity();
        assertEquals(false, pullContext.isValid());
    }

    @Test
    public void checkProcessValidityWithZeroLeg() throws Exception {
        PullContext pullContext = new PullContext();
        pullContext.setProcess(PojoInstaciatorUtil.instanciate(Process.class));
        pullContext.checkProcessValidity();
        assertEquals(false, pullContext.isValid());
    }

    @Test
    public void checkProcessValidityWithOneLeg() throws Exception {
        PullContext pullContext = new PullContext();
        pullContext.setProcess(PojoInstaciatorUtil.instanciate(Process.class, "legs{[name:leg1]}"));
        pullContext.checkProcessValidity();
        assertEquals(true, pullContext.isValid());
    }

}