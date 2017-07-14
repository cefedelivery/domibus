package eu.domibus.common.services.impl;

import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.common.model.configuration.Party;
import eu.domibus.common.model.configuration.Process;
import eu.domibus.util.PojoInstaciatorUtil;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Thomas Dussart
 * @since 3.3
 */
public class PullContextTest {


    @Test
    public void filterLegOnMpc() {
        Process process = PojoInstaciatorUtil.instanciate(Process.class, "legs{[name:leg1,defaultMpc[name:test1,qualifiedName:qn1]];[name:leg2,defaultMpc[name:test2,qualifiedName:qn2]]}", "responderParties{[name:resp1]}");
        PullContext pullContext = new PullContext(process, new Party(), "qn1");
        LegConfiguration legConfiguration = pullContext.filterLegOnMpc();
        assertEquals("qn1", legConfiguration.getDefaultMpc().getQualifiedName());
    }

    @Test(expected = NullPointerException.class)
    public void testInstanciationWithIllegalMpc() {
        Process process = PojoInstaciatorUtil.instanciate(Process.class, "legs{[name:leg1,defaultMpc[name:test1,qualifiedName:qn1]];[name:leg2,defaultMpc[name:test2,qualifiedName:qn2]]}", "responderParties{[name:resp1]}");
        PullContext pullContext = new PullContext(process, new Party(), null);
    }

}


