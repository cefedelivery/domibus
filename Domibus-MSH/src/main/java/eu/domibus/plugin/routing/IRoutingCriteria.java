package eu.domibus.plugin.routing;

import eu.domibus.api.message.ebms3.model.UserMessage;

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
