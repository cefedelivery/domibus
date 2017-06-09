package eu.domibus.plugin.routing.operation;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Cosmin Baciu
 * @since 3.2.5
 */
public abstract class AbstractLogicalOperation implements LogicalOperation {

    protected List<Boolean> intermediateResultList = new ArrayList<>();

    @Override
    public void addIntermediateResult(boolean intermediateResult) {
        intermediateResultList.add(intermediateResult);
    }

    public List<Boolean> getIntermediateResultList() {
        return intermediateResultList;
    }

    public void setIntermediateResultList(List<Boolean> intermediateResultList) {
        this.intermediateResultList = intermediateResultList;
    }
}
