package eu.domibus.plugin.fs.worker;

import mockit.Expectations;
import mockit.Injectable;
import mockit.NonStrictExpectations;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.config.ListFactoryBean;

import java.util.ArrayList;
import java.util.List;

/**
 * @author FERNANDES Henrique (hfernandes@linkare.com)
 */
@RunWith(JMockit.class)
public class FSPluginTriggerListBootTest {

    @Tested
    private FSPluginTriggerListBoot fsPluginTriggerListBoot;

    @Injectable
    private ListFactoryBean domibusStandardTriggerList;

    @Injectable
    private ListFactoryBean fsPluginTriggerList;

    private List<Object> standardList;
    private List<Object> fsList;

    @Before
    public void recordExpectationsForPostConstruct() throws Exception {
        standardList = new ArrayList<>();
        standardList.add("DomibusTriggerA");

        fsList = new ArrayList<>();
        fsList.add("FSTriggerA");
        fsList.add("FSTriggerB");

        new NonStrictExpectations() {{
            fsPluginTriggerList.getObject();
            result = fsList;

            domibusStandardTriggerList.getObject();
            result = standardList;
        }};
    }

    @Test
    public void testInit() throws Exception {
        // fsPluginTriggerListBoot.init() called automatically by JMockit

        Assert.assertEquals(3, standardList.size());
        Assert.assertEquals("DomibusTriggerA", standardList.get(0));
        Assert.assertEquals("FSTriggerA", standardList.get(1));
        Assert.assertEquals("FSTriggerB", standardList.get(2));
    }
}