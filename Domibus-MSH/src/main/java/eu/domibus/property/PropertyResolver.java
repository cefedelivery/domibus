package eu.domibus.property;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.cxf.common.util.StringUtils;

import java.util.Properties;

/**
 * Created by Cosmin Baciu on 6/15/2016.
 */
public class PropertyResolver {

    private static final Logger LOG = LoggerFactory.getLogger(PropertyResolver.class);

    private static final String START_DELIMITER = "{";
    private static final String END_DELIMITER = "}";
    private static final int RESOLVE_LEVEL = 3;

    private String startDelimiter = START_DELIMITER;
    private String endDelimiter = END_DELIMITER;
    private Integer resolveLevel = RESOLVE_LEVEL;

    public String getResolvedProperty(String propertyName) {
        return getResolvedProperty(propertyName, new Properties(), true);
    }

    public String getResolvedProperty(String propertyName, Properties properties, boolean includeSystemProperties) {
        String result = propertyName;

        if(includeSystemProperties) {
            LOG.debug("Adding the system properties to the available properties");
            properties.putAll(System.getProperties());
        }

        for (int i = 0; i < resolveLevel; i++) {
            result = getResolvedProperty(result, properties, startDelimiter, endDelimiter);
        }
        return result;
    }

    public String getResolvedProperty(String propertyName, Properties properties, String startVarDelimit, String endVarDelimit) {
        if (StringUtils.isEmpty(propertyName)) {
            LOG.debug("Could not resolve property: the property name is null");
            return null;
        }

        startVarDelimit = "$" + startVarDelimit;
        String value = propertyName;

        int startIndex = value.indexOf(startVarDelimit);

        while (startIndex != -1) {
            int endIndex = value.indexOf(endVarDelimit, startIndex);

            if (endIndex == -1) {
                // Restore value in case the variable reference is not valid
                value = propertyName;
                LOG.debug("Could not resolve property [ " + propertyName + "]." + "Could not find the associated end index for start index " + startIndex );
                break;
            }

            String variable = value.substring(startIndex + startVarDelimit.length(), endIndex);

            String result = properties.getProperty(variable);
            if (StringUtils.isEmpty(result)) {
                result = startVarDelimit + variable + endVarDelimit;
            }

            LOG.debug("Property [" + variable + "] = [" + result + "]");

            // Replace the variable reference with the variable value.
            value = value.substring(0, startIndex) + result + value.substring(endIndex + endVarDelimit.length());
            // Search for another variable reference.
            startIndex = value.indexOf(startVarDelimit, startIndex + result.length());
        }
        LOG.debug("Property [" + propertyName + "] has been resolved to [" + value + "]");

        return value;
    }


    public void setStartDelimiter(String startDelimiter) {
        this.startDelimiter = startDelimiter;
    }

    public void setEndDelimiter(String endDelimiter) {
        this.endDelimiter = endDelimiter;
    }

    public void setResolveLevel(Integer resolveLevel) {
        this.resolveLevel = resolveLevel;
    }

    public String getStartDelimiter() {
        return startDelimiter;
    }

    public Integer getResolveLevel() {
        return resolveLevel;
    }

    public String getEndDelimiter() {
        return endDelimiter;
    }
}
