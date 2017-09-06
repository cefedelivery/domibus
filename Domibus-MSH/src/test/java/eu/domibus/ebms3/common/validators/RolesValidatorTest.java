package eu.domibus.ebms3.common.validators;

import eu.domibus.common.model.configuration.Configuration;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author musatmi
 * @since 3.3
 */
public class RolesValidatorTest extends AbstractValidatorTest {

    private RolesValidator validator = new RolesValidator();

    @Test
    public void validate() throws Exception {
        Configuration configuration = newConfiguration("RolesConfiguration.json");
        final List<String> results = validator.validate(configuration);
        assertTrue(results.size() == 2);
        assertEquals("For the business process [TestProcess], the initiator role name and the responder role name are identical [eCODEXRole]", results.get(0));
        assertEquals("For the business process [TestProcess], the initiator role value and the responder role value are identical [GW]", results.get(1));
    }
}