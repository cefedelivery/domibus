package eu.domibus.ebms3.common.validators;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.KeyDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import eu.domibus.common.model.configuration.Configuration;
import eu.domibus.common.model.configuration.Party;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author idragusa
 * @since 3.3
 */
public class ValueTypeValidatorTest extends AbstractValidatorTest {

    private ValueTypeValidator validator = new ValueTypeValidator();

    @Test
    public void validate() throws Exception {
        Configuration configuration = newConfiguration("ValueTypeConfiguration.json");
        final List<String> results = validator.validate(configuration);
        assertTrue(results.size() == 4);
        assertTrue(results.contains("PartyIdType is empty and the partyId is not an URI for red_gw"));
        assertTrue(results.contains("Service type is empty and the service value is not an URI for testService1"));
        assertTrue(results.contains("Agreement type is empty and the agreement value is not an URI for agreement2"));
        assertTrue(results.contains("Agreement type is empty and the agreement value is not an URI for agreement1"));
    }
}