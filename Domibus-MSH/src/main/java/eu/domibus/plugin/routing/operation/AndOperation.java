package eu.domibus.plugin.routing.operation;

/**
 * @author Cosmin Baciu
 * @since 3.2.5
 */
public class AndOperation extends AbstractLogicalOperation {

    @Override
    public boolean canShortCircuitOperation() {
        return intermediateResultList.contains(false);
    }

    @Override
    public boolean getResult() {
        //if one of the intermediate result is a false the result of the AND operation is false
        return !intermediateResultList.contains(false);
    }
}
