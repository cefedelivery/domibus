package eu.domibus.property;

import org.apache.cxf.common.util.StringUtils;
import org.apache.log4j.Logger;

import java.util.Properties;

/**
 * Created by Cosmin Baciu on 6/15/2016.
 */
public class PropertyResolver {

    private static final Logger LOGGER = Logger.getLogger(PropertyResolver.class);

    private static final String START_DELIMITER = "{";
    private static final String END_DELIMITER = "}";
    private static final int RESOLVE_LEVEL = 3;

    public String getResolvedProperty(String propertyName) {
        return getResolvedProperty(propertyName, new Properties(), true);
    }

    public String getResolvedProperty(String propertyName, Properties properties, boolean includeSystemProperties) {
        String result = propertyName;

        if(includeSystemProperties) {
            properties.putAll(System.getProperties());
        }

        for (int i = 0; i < RESOLVE_LEVEL; i++) {
            result = getResolvedProperty(result, properties, START_DELIMITER, END_DELIMITER);
        }
        return result;
    }

    public String getResolvedProperty(String propertyName, Properties properties, String startVarDelimit, String endVarDelimit) {
        if (StringUtils.isEmpty(propertyName)) {
            return null;
        }

        startVarDelimit = "$" + startVarDelimit;
        String value = propertyName;
        int startIndex = 0;
        int endIndex = 0;

        startIndex = value.indexOf(startVarDelimit);

        while (startIndex != -1) {
            endIndex = value.indexOf(endVarDelimit, startIndex);

            if (endIndex == -1) {
                // Restore value in case the variable reference is not valid
                value = propertyName;
                break;
            }

            String variable = value.substring(startIndex + startVarDelimit.length(), endIndex);

            String result = properties.getProperty(variable);
            if (StringUtils.isEmpty(result)) {
                result = startVarDelimit + variable + endVarDelimit;
            }

            // Replace the system variable reference with the system variable value.
            value = value.substring(0, startIndex) + result + value.substring(endIndex + endVarDelimit.length());
            // Search for another variable reference.
            startIndex = value.indexOf(startVarDelimit, startIndex + result.length());
        }

        return value;
    }

    public static PropertyResolver create() {
        return new PropertyResolver();
    }


}
