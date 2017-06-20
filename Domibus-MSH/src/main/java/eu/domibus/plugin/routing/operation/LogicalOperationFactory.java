package eu.domibus.plugin.routing.operation;

/**
 * @author Cosmin Baciu
 * @since 3.2.5
 */
public class LogicalOperationFactory {

    public LogicalOperation create(LogicalOperator operator) {
        if (LogicalOperator.AND == operator) {
            return new AndOperation();
        } else if (LogicalOperator.OR == operator) {
            return new OrOperation();
        }
        throw new IllegalArgumentException("Unknown logical operator [" + operator + "]");
    }
}
