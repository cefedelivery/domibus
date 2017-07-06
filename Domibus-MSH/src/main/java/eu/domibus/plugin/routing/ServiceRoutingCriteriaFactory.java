package eu.domibus.plugin.routing;

import eu.domibus.ebms3.common.model.Service;
import eu.domibus.ebms3.common.model.UserMessage;

/**
 * Service criteria for user messages
 * <p/>
 *
 * @author Christian Walczac
 */

public class ServiceRoutingCriteriaFactory implements CriteriaFactory {

    private static final String NAME = "SERVICE";
    private static final String TOOLTIP = "Type in the filtering rule: [SERVICE]:[TYPE]. Combine with regular expression.";
    private static final String INPUTPATTERN = "\\\\w+[:]\\\\w+";

    @Override
    public IRoutingCriteria getInstance() {
        return new ServiceRoutingCriteriaEntity(NAME, TOOLTIP, INPUTPATTERN);
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getTooltip() {
        return TOOLTIP;
    }

    @Override
    public String getInputPattern() {
        return INPUTPATTERN;
    }

    private class ServiceRoutingCriteriaEntity extends RoutingCriteriaEntity {
        private ServiceRoutingCriteriaEntity(final String name, final String tooltip, final String inputPattern) {
            super(name, tooltip, inputPattern);
        }

        @Override
        public boolean matches(final UserMessage userMessage, final String expression) {
            setExpression(expression);
            final Service service = userMessage.getCollaborationInfo().getService();
            return matches(service.getValue() + ":" + service.getType());
        }

    }
}