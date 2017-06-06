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
public class AndOperationTest {

    @Tested
    AndOperation andOperation;

    @Test
    public void testGetResultWithTrueAndFalse() throws Exception {
        andOperation.addIntermediateResult(true);
        andOperation.addIntermediateResult(false);

        assertFalse(andOperation.getResult());
    }

    @Test
    public void testGetResultWithFalseAndTrue() throws Exception {
        andOperation.addIntermediateResult(false);
        andOperation.addIntermediateResult(true);

        assertFalse(andOperation.getResult());
    }

    @Test
    public void testGetResultWithTrueAndTrue() throws Exception {
        andOperation.addIntermediateResult(true);
        andOperation.addIntermediateResult(true);

        assertTrue(andOperation.getResult());
    }

    @Test
    public void testGetResultWithShortCircuit() throws Exception {
        andOperation.addIntermediateResult(true);
        assertFalse(andOperation.canShortCircuitOperation());
        andOperation.addIntermediateResult(false);
        assertTrue(andOperation.canShortCircuitOperation());
        andOperation.addIntermediateResult(true);
        assertFalse(andOperation.getResult());
    }
}
