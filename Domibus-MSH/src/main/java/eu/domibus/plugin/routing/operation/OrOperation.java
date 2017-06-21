package eu.domibus.plugin.routing.operation;

/**
 * @author Cosmin Baciu
 * @since 3.2.5
 */
public class OrOperation extends AbstractLogicalOperation {

    @Override
    public boolean canShortCircuitOperation() {
        return intermediateResultList.contains(true);
    }

    @Override
    public boolean getResult() {
        //if one of the intermediate result is a true the result of the OR operation is true
        return intermediateResultList.contains(true);
    }
}
