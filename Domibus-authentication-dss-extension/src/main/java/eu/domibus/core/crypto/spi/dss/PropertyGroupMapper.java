package eu.domibus.core.crypto.spi.dss;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Thomas Dussart
 * @since 4.0
 */

public abstract class PropertyGroupMapper<E> {

    private static final Logger LOG = LoggerFactory.getLogger(PropertyGroupMapper.class);

    protected List<E> map(String... propertyNames) {
        int count = 0;
        boolean propertyExist = false;
        List<E> elements = new ArrayList<>();
        do {
            Map<String, ImmutablePair<String, String>> keyValues = new HashMap<>();
            int propertyCount = 0;
            for (String propertyName : Lists.newArrayList(propertyNames)) {
                final String format = propertyName + "[%s]";
                if (propertyCount == 0) {
                    final String firstPropertyKey = String.format(format, count);
                    propertyExist = getEnvironment().containsProperty(firstPropertyKey);
                    LOG.debug("Property for key:[{}] exist:[{}]", firstPropertyKey, propertyExist);
                    if (!propertyExist) {
                        break;
                    }
                    final String firstPropertyValue = getEnvironment().getProperty(firstPropertyKey);
                    keyValues.put(propertyName, new ImmutablePair<>(propertyName, firstPropertyValue));
                    LOG.debug("Property:[{}] has following value:[{}]", firstPropertyKey, firstPropertyValue);
                    propertyCount++;
                } else {
                    final String otherPropertyKey = String.format(format, count);
                    final String otherPropertyValue = getEnvironment().getProperty(otherPropertyKey);
                    keyValues.put(propertyName, new ImmutablePair<>(propertyName, otherPropertyValue));
                    LOG.debug("Property:[{}] has following value:[{}]", otherPropertyKey, otherPropertyValue);
                }
            }
            if (propertyExist) {
                elements.add(transForm(keyValues));
            }
            count++;
        } while (propertyExist);
        return elements;
    }

    abstract E transForm(Map<String, ImmutablePair<String, String>> keyValues);

    abstract Environment getEnvironment();

}
