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
 * @author musatmi
 * @since 3.3
 */
public class RolesValidatorTest {

    private RolesValidator validator = new RolesValidator();
    private ObjectMapper mapper = new ObjectMapper();

    static class YourClassKeyDeserializer extends KeyDeserializer {
        @Override
        public Party deserializeKey(final String key, final DeserializationContext ctxt) throws IOException, JsonProcessingException {
            final Party party = new Party();
            party.setName(key);
            return party; // replace null with your logic
        }
    }

    @Before
    public void init() {
        SimpleModule simpleModule = new SimpleModule();
        simpleModule.addKeyDeserializer(Party.class, new YourClassKeyDeserializer());
        mapper.registerModule(simpleModule);
    }

    @Test
    public void validate() throws Exception {
        Configuration configuration = newConfiguration();
        final List<String> results = validator.validate(configuration);
        assertTrue(results.size() == 1);
        assertEquals(results.get(0), "For business process TestProcess the initiator role and the responder role are identical (eCODEXRole)");
    }

    private Configuration newConfiguration() throws IOException {
        return mapper.readValue(getResourceAsString("Configuration.json"), Configuration.class);
    }

    private String getResourceAsString(String resourceName) throws IOException {
        ClassPathResource json = new ClassPathResource(this.getClass().getPackage().getName().replaceAll("\\.","\\/") + "/" + resourceName);
        return IOUtils.toString(json.getInputStream());
    }

}