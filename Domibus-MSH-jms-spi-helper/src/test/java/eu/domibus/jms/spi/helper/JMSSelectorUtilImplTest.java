package eu.domibus.jms.spi.helper;

import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Cosmin Baciu
 * @since 3.2
 */
@RunWith(JMockit.class)
public class JMSSelectorUtilImplTest {

    @Tested
    JMSSelectorUtilImpl selectorUtil;

    @Test
    public void testGetSelector()  {
        String selector = selectorUtil.getSelector("myMessageId");
        Assert.assertEquals("JMSMessageID = 'myMessageId'", selector);
    }

    @Test
    public void testGetSelectorWithMultipleIds()  {
        String selector = selectorUtil.getSelector(new String[]{"message1", "message2"});
        Assert.assertEquals("JMSMessageID IN ('message1', 'message2')", selector);
    }

    @Test
    public void testGetSelectorWithClause() throws Exception {
        Map<String, Object> criteria = new HashMap<String, Object>();
        criteria.put("JMSType", "myType");
        criteria.put("JMSTimestamp_from", 123L);
        criteria.put("JMSTimestamp_to", 456L);
        criteria.put("selectorClause", "JMSMessageID = 'myMessageId'");
        String selector = selectorUtil.getSelector(criteria);
        Assert.assertEquals("JMSType='myType' and JMSTimestamp>=123 and JMSTimestamp<=456 and JMSMessageID = 'myMessageId'", selector);

        criteria = new HashMap<String, Object>();
        criteria.put("JMSType", "my'Type'e'");
        criteria.put("JMSTimestamp_from", 123L);
        criteria.put("JMSTimestamp_to", 456L);
        criteria.put("selectorClause", "JMSMessageID = 'myMessageId'");
        selector = selectorUtil.getSelector(criteria);
        Assert.assertEquals("JMSType='my''Type''e''' and JMSTimestamp>=123 and JMSTimestamp<=456 and JMSMessageID = 'myMessageId'", selector);
        //test even number of apostrophes in the string
        Assert.assertEquals(0, selector.replaceAll("[^']", "").length() % 2);

    }


}
