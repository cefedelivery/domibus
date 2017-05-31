package eu.domibus.plugin.routing.operation;

import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Cosmin Baciu
 * @since 3.2.5
 */
@RunWith(JMockit.class)
public class OrOperationTest {

    @Tested
    OrOperation orOperation;

    @Test
    public void testGetResultWithTrueAndFalse() throws Exception {
        orOperation.addIntermediateResult(true);
        orOperation.addIntermediateResult(false);

        assertTrue(orOperation.getResult());
    }

    @Test
    public void testGetResultWithFalseAndTrue() throws Exception {
        orOperation.addIntermediateResult(false);
        orOperation.addIntermediateResult(true);

        assertTrue(orOperation.getResult());
    }

    @Test
    public void testGetResultWithTrueAndTrue() throws Exception {
        orOperation.addIntermediateResult(true);
        orOperation.addIntermediateResult(true);

        assertTrue(orOperation.getResult());
    }

    @Test
    public void testGetResultWithFalseAndFalse() throws Exception {
        orOperation.addIntermediateResult(false);
        orOperation.addIntermediateResult(false);

        assertFalse(orOperation.getResult());
    }

    @Test
    public void testGetResultWithShortCircuit() throws Exception {
        orOperation.addIntermediateResult(true);
        assertTrue(orOperation.canShortCircuitOperation());
        orOperation.addIntermediateResult(false);
        assertTrue(orOperation.canShortCircuitOperation());
        orOperation.addIntermediateResult(true);
        assertTrue(orOperation.getResult());
    }
}
