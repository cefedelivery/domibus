package eu.domibus.plugin.routing;

import eu.domibus.api.message.ebms3.model.PartyId;
import eu.domibus.api.message.ebms3.model.UserMessage;

/**
 * From criteria for user messages
 *
 * @author Christian Walczac
 */

public class FromRoutingCriteriaFactory implements CriteriaFactory {


    private static final String NAME = "FROM";
    private static final String TOOLTIP = "Type in the filtering rule: [PARTYID]:[TYPE]. Combine with regular expression.";
    private static final String INPUTPATTERN = "\\\\w+[:]\\\\w+";

    @Override
    public String getTooltip() {
        return TOOLTIP;
    }

    @Override
    public String getInputPattern() {
        return INPUTPATTERN;
    }

    @Override
    public IRoutingCriteria getInstance() {
        return new FromRoutingCriteria(NAME, TOOLTIP, INPUTPATTERN);
    }

    @Override
    public String getName() {
        return NAME;
    }

    private class FromRoutingCriteria extends RoutingCriteria implements IRoutingCriteria {

        private FromRoutingCriteria(final String name, final String tooltip, final String inputPattern) {
            super(name, tooltip, inputPattern);
        }

        @Override
        public boolean matches(final UserMessage userMessage, final String expression) {
            setExpression(expression);
            for (final PartyId partyId : userMessage.getPartyInfo().getFrom().getPartyId()) {
                if (matches(partyId.getValue() + ":" + partyId.getType())) {
                    return true;
                }
            }

            return false;
        }

    }
}

