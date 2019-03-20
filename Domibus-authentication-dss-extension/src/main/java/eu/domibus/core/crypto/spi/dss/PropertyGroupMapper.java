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
 * <p>
 * This class has a similar behavior than @ConfigurationProperties annotation and allows
 * to parse list of properties in the format domibus.example.name[0],domibus.example.name[1].
 * <p>
 * Subclasses implement a transform method to create the model needed <E> and the map method will then return a List<E>.
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
            for (String propertyName : Lists.newArrayList(propertyNames)) {
                final String format = propertyName + "[%s]";
                final String propertyKey = String.format(format, count);
                if (!propertyKeyExists(propertyKey)) {
                    propertyEmpty = true;
                    break;
                }
                final String propertyValue = getPropertyValue(propertyKey);
                if (!passwordPattern.matcher(propertyKey).matches()) {
                    LOG.debug("Property:[{}] has following value:[{}]", propertyKey, propertyValue);
                }
                keyValues.put(propertyName, new ImmutablePair<>(propertyName, propertyValue));
            }
            if (!propertyEmpty) {
                elements.add(transForm(keyValues));
            }
            count++;
        } while (!propertyEmpty);
        return elements;
    }

    private boolean propertyKeyExists(final String key) {
        final boolean keyExistsInDomain = domibusPropertyExtService.containsDomainPropertyKey(domainContextExtService.getCurrentDomain(), key);
        if (keyExistsInDomain) {
            return keyExistsInDomain;
        }
        return environment.containsProperty(key);

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
