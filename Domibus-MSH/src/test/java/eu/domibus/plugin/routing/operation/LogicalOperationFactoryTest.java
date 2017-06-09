package eu.domibus.plugin.routing.operation;

import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Cosmin Baciu
 * @since 3.2.5
 */
@RunWith(JMockit.class)
public class LogicalOperationFactoryTest {

    @Tested
    LogicalOperationFactory logicalOperationFactory;


    @Test
    public void testCreateAndOperation() throws Exception {
        final LogicalOperation andOperation = logicalOperationFactory.create(LogicalOperator.AND);
        Assert.assertTrue(andOperation instanceof AndOperation);
    }

    @Test
    public void testCreateOrOperation() throws Exception {
        final LogicalOperation orOperation = logicalOperationFactory.create(LogicalOperator.OR);
        Assert.assertTrue(orOperation instanceof OrOperation);
    }
}
