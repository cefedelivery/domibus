package eu.domibus.common.model.configuration;

import eu.domibus.util.PojoInstaciatorUtil;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Created by dussath on 5/19/17.
 *
 */
public class ProcessTest {
    @Test
    public void testGetMepValue() throws Exception {
        assertEquals("",Process.getMepValue(new Process()));
        assertEquals("Mock", Process.getMepValue(PojoInstaciatorUtil.instanciateProcess(Process.class)));
    }

    @Test
    public void testGetBindingValue() throws Exception {
        assertNull("",Process.getBindingValue(new Process()));
        assertEquals("Mock", Process.getBindingValue(PojoInstaciatorUtil.instanciateProcess(Process.class)));
    }


}