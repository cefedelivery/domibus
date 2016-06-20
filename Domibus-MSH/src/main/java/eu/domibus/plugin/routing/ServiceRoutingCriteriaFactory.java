/*
 * Copyright 2015 e-CODEX Project
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they
 * will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the
 * Licence.
 * You may obtain a copy of the Licence at:
 * http://ec.europa.eu/idabc/eupl5
 * Unless required by applicable law or agreed to in
 * writing, software distributed under the Licence is
 * distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied.
 * See the Licence for the specific language governing
 * permissions and limitations under the Licence.
 */

package eu.domibus.plugin.routing;

import eu.domibus.common.model.Service;
import eu.domibus.common.model.UserMessage;

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