package eu.domibus.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;

/**
 * <p>Configuration providing common property sources to the {@code Environment} in order
 * to ensure the placeholders get correctly replaced inside bean definition property values
 * and {@code @Value} fields. It is intended to be used as a replacement for individual
 * {@code PropertySource} annotations placed on classes implementing {@code Condition}.</p>
 *
 * @author Sebastian-Ion TINCU
 * @see org.springframework.core.env.Environment
 * @see org.springframework.context.annotation.Condition
 * @since 4.1
 */
@Configuration
// The following property sources are being checked in the reverse order (first
// the file://../domibus.properties, then the classpath://../domibus.properties
// and finally the classpath://../domibus-default.properties).
@PropertySources({
        @PropertySource("classpath:config/domibus-default.properties"),
        @PropertySource("classpath:config/domibus.properties"),
        @PropertySource("file:///${domibus.config.location}/domibus.properties"),
})
public class DomibusPropertyConfig {

}