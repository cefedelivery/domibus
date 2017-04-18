package eu.domibus.plugin.routing;

import eu.domibus.api.message.ebms3.model.Service;
import eu.domibus.api.message.ebms3.model.UserMessage;

/**
 * Service criteria for user messages
 * <p/>
 *
 * @author Christian Walczac
 */

public class ServiceRoutingCriteriaFactory implements CriteriaFactory {

    private static final String NAME = "SEVICE";
    private static final String TOOLTIP = "Type in the filtering rule: [SERVICE]:[TYPE]. Combine with regular expression.";
    private static final String INPUTPATTERN = "\\\\w+[:]\\\\w+";

    @Override
    public IRoutingCriteria getInstance() {
        return new ServiceRoutingCriteria(NAME, TOOLTIP, INPUTPATTERN);
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

    private class ServiceRoutingCriteria extends RoutingCriteria {
        private ServiceRoutingCriteria(final String name, final String tooltip, final String inputPattern) {
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