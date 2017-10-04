package eu.domibus.spring;

import eu.domibus.property.PropertyResolver;

import java.util.Enumeration;
import java.util.Properties;

public class PrefixedProperties extends Properties {

    public PrefixedProperties(Properties props, String prefix) {
        if (props == null) {
            return;
        }
        PropertyResolver propertyResolver = new PropertyResolver();

        Enumeration<String> en = (Enumeration<String>) props.propertyNames();
        while (en.hasMoreElements()) {
            String propName = en.nextElement();
            String propValue = props.getProperty(propName);

            if (propName.startsWith(prefix)) {
                String key = propName.substring(prefix.length());
                String resolved = propertyResolver.getResolvedProperty(propValue, props, false);
                setProperty(key, resolved);
            }
        }
    }
}