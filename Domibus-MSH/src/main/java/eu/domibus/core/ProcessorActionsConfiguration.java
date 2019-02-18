package eu.domibus.core;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.xml.namespace.QName;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Cosmin Baciu
 * @since 4.1
 */
@Configuration
public class ProcessorActionsConfiguration {

    @Bean("processorActions")
    public Map<QName, Object> createProcessorMap() {
        Map<QName, Object> result = new HashMap<>();

        QName saml1 = new QName("urn:oasis:names:tc:SAML:1.0", "Assertion");
        QName saml2 = new QName("urn:oasis:names:tc:SAML:2.0:assertion", "Assertion");
        result.put(saml1, null);
        result.put(saml2, null);

        return result;
    }
}
