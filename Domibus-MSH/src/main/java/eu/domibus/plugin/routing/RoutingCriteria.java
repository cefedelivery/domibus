package eu.domibus.plugin.routing;

import eu.domibus.api.message.ebms3.model.AbstractBaseEntity;
import eu.domibus.api.message.ebms3.model.UserMessage;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Abstract Class of Routing Criteria
 *
 * @author Christian Walczac
 */
@Entity
@Table(name = "TB_ROUTING_CRITERIA")
public class RoutingCriteria extends AbstractBaseEntity implements IRoutingCriteria {

    @Column(name = "NAME")
    private String name;
    @Column(name = "EXPRESSION")
    private String expression;
    @Transient
    private HashMap<String, Pattern> patternHashMap;
    @Transient
    private String tooltip;
    @Transient
    private String inputPattern;

    /**
     * Standard Constructor
     *
     * @param name of Routing Criteria
     */
    public RoutingCriteria(final String name, final String tooltip, final String inputPattern) {
        this.name = name;
        this.tooltip = tooltip;
        this.inputPattern = inputPattern;
        patternHashMap = new HashMap<>();
    }

    protected RoutingCriteria() {
    }

    @Override
    public String getInputPattern() {
        return inputPattern;
    }

    @Override
    public void setInputPattern(final String inputPattern) {
        this.inputPattern = inputPattern;
    }

    @Override
    public String getTooltip() {
        return tooltip;
    }

    @Override
    public void setTooltip(final String tooltip) {
        this.tooltip = tooltip;
    }

    @Override
    public String getExpression() {
        return expression;
    }

    @Override
    public void setExpression(final String expression) {
        this.expression = expression;
    }

    private Pattern getPattern(final String expression) {
        if (patternHashMap.containsKey(expression)) {
            return patternHashMap.get(expression);
        } else {
            final Pattern pattern = Pattern.compile(expression);
            patternHashMap.put(expression, pattern);
            return pattern;
        }

    }

    public boolean matches(final String candidate) {

        final Matcher m = getPattern(expression).matcher(candidate);

        return m.matches();
    }

    /**
     * Returns if $UserMessage matches expression
     *
     * @param candidate user message to match
     * @return result
     */
    @Override
    public boolean matches(final UserMessage candidate, final String expression) {
        throw new UnsupportedOperationException("This method must be implemented by a subclass");
    }

    @Override
    public String getName() {
        return name;
    }

}
