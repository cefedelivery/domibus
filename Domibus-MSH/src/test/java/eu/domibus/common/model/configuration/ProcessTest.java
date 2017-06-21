package eu.domibus.common.model.configuration;

import eu.domibus.util.PojoInstaciatorUtil;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @author Thomas Dussart
 * @since 3.3
 *
 */
public class ProcessTest {
    @Test
    public void testGetMepValue() throws Exception {
        assertEquals("",Process.getMepValue(new Process()));
        assertEquals("Mock", Process.getMepValue(PojoInstaciatorUtil.instanciate(Process.class)));
    }

    @Test
    public void testGetBindingValue() throws Exception {
        assertEquals("",Process.getBindingValue(new Process()));
        assertEquals("Mock", Process.getBindingValue(PojoInstaciatorUtil.instanciate(Process.class)));
    }


}