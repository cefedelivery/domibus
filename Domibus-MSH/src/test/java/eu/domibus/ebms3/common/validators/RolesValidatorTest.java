package eu.domibus.ebms3.common.validators;

import eu.domibus.common.model.configuration.BusinessProcesses;
import eu.domibus.common.model.configuration.Configuration;
import eu.domibus.common.model.configuration.Process;
import eu.domibus.common.model.configuration.Role;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.ReflectionUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * Created by musatmi on 10/07/2017.
 */
public class RolesValidatorTest {

    private RolesValidator validator = new RolesValidator();

    @Test
    public void validate() throws Exception {
        Configuration configuration = newConfiguration();
        final List<String> results = validator.validate(configuration);
        assertTrue(results.size() == 1);
        assertEquals(results.get(0),"For business process TestProcess the initiator role and the responder role are identical (eCODEXRole)");
    }

    private Configuration newConfiguration(){
        Configuration configuration = new Configuration();

        Role initiatorRole = new Role();
        initiatorRole.setName("eCODEXRole");
        initiatorRole.setValue("GW");

        Role responderRole = new Role();
        responderRole.setName("eCODEXRole");
        responderRole.setValue("GW");

        Process process = new Process();
        ReflectionTestUtils.setField(process,"name", "TestProcess");
        ReflectionTestUtils.setField(process,"initiatorRole", initiatorRole);
        ReflectionTestUtils.setField(process,"responderRole", responderRole);

        Set<Process> processesSet = new HashSet<>();
        processesSet.add(process);


        BusinessProcesses processes = new BusinessProcesses();

        ReflectionTestUtils.setField(processes,"processes", processesSet);
        configuration.setBusinessProcesses(processes);

        return configuration;
    }

}