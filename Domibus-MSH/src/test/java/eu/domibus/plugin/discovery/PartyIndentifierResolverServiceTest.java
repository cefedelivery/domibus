package eu.domibus.plugin.discovery;

import com.google.common.collect.Lists;
import eu.domibus.common.dao.PartyDao;
import eu.domibus.common.exception.ConfigurationException;
import eu.domibus.common.model.configuration.Identifier;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import java.util.Collection;

/**
 * @author Sebastian-Ion TINCU
 */
@RunWith(JMockit.class)
public class PartyIndentifierResolverServiceTest {

    @Tested
    private PartyIndentifierResolverService partyIndentifierResolverService;

    @Injectable
    private PartyDao partyDao;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void throwsConfigurationExceptionWhenTryingToSolveIdentifiersForANullEndpoint() {
        // Given
        thrown.expect(ConfigurationException.class);
        thrown.expectMessage("Endpoint empty or null");

        // When
        partyIndentifierResolverService.resolveByEndpoint(null);
    }

    @Test
    public void throwsConfigurationExceptionWhenTryingToSolveIdentifiersForAnEmptyEndpoint() {
        // Given
        thrown.expect(ConfigurationException.class);
        thrown.expectMessage("Endpoint empty or null");

        // When
        partyIndentifierResolverService.resolveByEndpoint("");
    }

    @Test
    public void throwsConfigurationExceptionWhenReturningAnEmptyListOfPartyIdentifiersForAnEndpoint() {
        // Given
        thrown.expect(ConfigurationException.class);
        thrown.expectMessage("Can not resolve identifiers for endpoint: endpoint");

        new Expectations() {{
            partyDao.findPartyIdentifiersByEndpoint("endpoint"); result = Lists.<Identifier>newArrayList();
        }};

        // When
        partyIndentifierResolverService.resolveByEndpoint("endpoint");
    }

    @Test
    public void returnsNonEmptyListOfPartyIdentifiersWhenResolvingItForAnEndpoint(@Injectable Identifier identifier) {
        // Given
        new Expectations() {{
            partyDao.findPartyIdentifiersByEndpoint("endpoint"); result = Lists.newArrayList(identifier);
        }};

        // When
        Collection<Identifier> identifiers = partyIndentifierResolverService.resolveByEndpoint("endpoint");

        Assert.assertEquals("Should have returned the list of identifiers for the endpoint when not empty", Lists.newArrayList(identifier), identifiers);
    }


}