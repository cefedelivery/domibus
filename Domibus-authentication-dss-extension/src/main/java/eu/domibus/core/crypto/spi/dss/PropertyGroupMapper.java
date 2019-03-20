package eu.domibus.core.crypto.spi.dss;

import com.google.common.collect.Lists;
import eu.domibus.ext.services.DomainContextExtService;
import eu.domibus.ext.services.DomibusPropertyExtService;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * @author Thomas Dussart
 * @since 4.1
 */

public abstract class PropertyGroupMapper<E> {

    private static final Logger LOG = LoggerFactory.getLogger(PropertyGroupMapper.class);

    private final DomibusPropertyExtService domibusPropertyExtService;

    private final DomainContextExtService domainContextExtService;

    private final Environment environment;

    private final Pattern passwordPattern = Pattern.compile(".*password.*", Pattern.CASE_INSENSITIVE);


    public PropertyGroupMapper(final DomibusPropertyExtService domibusPropertyExtService,
                               final DomainContextExtService domainContextExtService,
                               final Environment environment) {
        this.domibusPropertyExtService = domibusPropertyExtService;
        this.domainContextExtService = domainContextExtService;
        this.environment = environment;
    }

    protected List<E> map(String... propertyNames) {
        int count = 0;
        boolean propertyEmpty = false;
        List<E> elements = new ArrayList<>();
        do {
            Map<String, ImmutablePair<String, String>> keyValues = new HashMap<>();
            int propertyCount = 0;
            for (String propertyName : Lists.newArrayList(propertyNames)) {
                final String format = propertyName + "[%s]";
                if (propertyCount == 0) {
                    final String firstPropertyKey = String.format(format, count);
                    final String firstPropertyValue = getPropertyValue(firstPropertyKey);
                    propertyEmpty = StringUtils.isEmpty(firstPropertyValue);
                    if (propertyEmpty) {
                        break;
                    }
                    keyValues.put(propertyName, new ImmutablePair<>(propertyName, firstPropertyValue));
                    if (!passwordPattern.matcher(firstPropertyKey).matches()) {
                        LOG.debug("Property:[{}] has following value:[{}]", firstPropertyKey, firstPropertyValue);
                    }
                    propertyCount++;
                } else {
                    final String otherPropertyKey = String.format(format, count);
                    final String otherPropertyValue = getPropertyValue(otherPropertyKey);
                    keyValues.put(propertyName, new ImmutablePair<>(propertyName, otherPropertyValue));
                    if (!passwordPattern.matcher(otherPropertyKey).matches()) {
                        LOG.debug("Property:[{}] has following value:[{}]", otherPropertyKey, otherPropertyValue);
                    }
                }
            }
            if (!propertyEmpty) {
                elements.add(transForm(keyValues));
            }
            count++;
        } while (!propertyEmpty);
        return elements;
    }

    private String getPropertyValue(String key) {
        String propertyValue = domibusPropertyExtService.getDomainProperty(domainContextExtService.getCurrentDomain(), key);
        if (StringUtils.isEmpty(propertyValue)) {
            propertyValue = environment.getProperty(key);
        }
        return propertyValue;
    }

    abstract E transForm(Map<String, ImmutablePair<String, String>> keyValues);

}
