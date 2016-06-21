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

import eu.domibus.ebms3.common.model.UserMessage;

/**
 * Routing Interface for incoming user messages
 *
 * @author Christian Walczac
 */
public interface IRoutingCriteria {

    /**
     * Returns if $UserMessage matches expression
     *
     * @param candidate user message to match
     * @return result
     */
    public boolean matches(UserMessage candidate, String expression);

    /**
     * Returns name of Routing Criteria
     *
     * @return name of Routing Criteria
     */
    public String getName();

    String getInputPattern();

    void setInputPattern(String inputPattern);

    String getTooltip();

    void setTooltip(String tooltip);

    public String getExpression();

    public void setExpression(String expression);


}
