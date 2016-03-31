package eu.domibus.plugin.routing;

/**
 * @author Christian Walczac
 */
public interface CriteriaFactory {
    IRoutingCriteria getInstance();

    public String getName();

    public String getTooltip();

    public String getInputPattern();
}
