package eu.domibus.ebms3.common.validators;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.KeyDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import eu.domibus.common.model.configuration.*;
import eu.domibus.common.model.configuration.Process;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.ReflectionUtils;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * Created by musatmi on 10/07/2017.
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
        return mapper.readValue("{\"businessProcesses\": {\"processes\": [{\"name\": \"TestProcess\",\"initiatorRole\": {\"name\": \"eCODEXRole\",\"value\": \"GW\"},\"responderRole\": {\"name\": \"eCODEXRole\",\"value\": \"GW\"}}]}}", Configuration.class);
    }

}