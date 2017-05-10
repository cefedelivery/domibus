package eu.domibus.plugin.routing.operation;

/**
 * @author Cosmin Baciu
 * @since 3.2.5
 */
public interface LogicalOperation {

    boolean canShortCircuitOperation();

    void addIntermediateResult(boolean intermediateResult);

    boolean getResult();
}
