package eu.domibus.ebms3.sender;

import eu.domibus.api.configuration.DomibusConfigurationService;
import mockit.Deencapsulation;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Mock;
import mockit.MockUp;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Optional;

/**
 * @author Sebastian-Ion TINCU
 */
@RunWith(JMockit.class)
public class TLSReaderTest {

    public static final String CONFIG_LOCATION = "configLocation";

    @Injectable
    private Path domainSpecificPath;

    @Injectable
    private Path defaultPath;

    @Injectable
    private DomibusConfigurationService domibusConfigurationService;

    @Tested
    private TLSReader tlsReader;

    private String domainCode;

    private Optional<Path> clientAuthenticationPath;

    boolean domainSpecificPathExists, defaultPathExists;

    @Before
    public void setUp() {
        new Expectations() {{
            domibusConfigurationService.getConfigLocation();
            result = CONFIG_LOCATION;
        }};
        givenPathsMocks();
    }

    @Test
    public void returnsTheClientAuthenticationFromTheDomainSpecificPathIfPresent() {
        givenDomainCode("TAXUD");
        givenDomainSpecificPathFound();

        whenRetrievingTheClientAuthenticationPath();

        Assert.assertSame("Should have returned the domain specific path if present", clientAuthenticationPath.orElse(null), domainSpecificPath);
    }

    @Test
    public void returnsTheClientAuthenticationFromTheDefaultPathIfPresentWhenTheDomainSpecificPathDoesNotExist() {
        givenDomainCode("TAXUD");
        givenDomainSpecificPathNotFound();
        givenDefaultPathFound();

        whenRetrievingTheClientAuthenticationPath();

        Assert.assertSame("Should have returned the default path if present when the domain specific path is missing", clientAuthenticationPath.orElse(null), defaultPath);
    }

    @Test
    public void returnsNoClientAuthenticationWhenTheDefaultPathAndTheDomainSpecificPathDoNotExist() {
        givenDomainCode("TAXUD");
        givenDomainSpecificPathNotFound();
        givenDefaultPathNotFound();

        whenRetrievingTheClientAuthenticationPath();

        Assert.assertFalse("Should have returned no path when the domain specific and the default paths are both missing", clientAuthenticationPath.isPresent());
    }

    @Test
    public void stripsTheDomainCodeForWhitespacesBeforeLookingUpTheClientAuthenticationFromTheDomainSpecificPath() {
        givenDomainCode("   TAXUD\t ");
        givenDomainSpecificPathFound();
        new MockUp<Paths>() {
            @Mock
            public Path get(String first, String... more) {
                if(!isDomainSpecificScenario(more)) {
                    throw new IllegalArgumentException("The domain code should have been stripped down of whitespace characters");
                }
                return domainSpecificPath;
            }
        };

        whenRetrievingTheClientAuthenticationPath();
    }


    private void givenDomainCode(String domainCode) {
        this.domainCode = domainCode;
    }

    private void givenDomainSpecificPathFound() {
        domainSpecificPathExists = true;
    }

    private void givenDomainSpecificPathNotFound() {
        domainSpecificPathExists = false;
    }

    private void givenDefaultPathFound() {
        defaultPathExists = true;
    }

    private void givenDefaultPathNotFound() {
        defaultPathExists = false;
    }

    // There is no nicer way to mock static methods even in JMockit and new MockUp definitions overwrite previous ones so they need to be defined once per mocked up class
    private void givenPathsMocks() {
        new MockUp<Paths>() {
            @Mock
            public Path get(String first, String... more) {
                return isDomainSpecificScenario(more) ? domainSpecificPath : defaultPath;
            }
        };

        new MockUp<Files>() {
            @Mock
            public boolean exists(Path path, LinkOption... options) {
                if(path == domainSpecificPath) {
                    return domainSpecificPathExists;
                }else if(path == defaultPath) {
                    return defaultPathExists;
                }else {
                    throw new IllegalArgumentException("Should have been invoked with the domain specific path or the default path");
                }
            }
        };
    }

    private boolean isDomainSpecificScenario(String... more) {
        Optional<String> first = Arrays.stream(more).findFirst();
        return first.isPresent() && first.get().startsWith(StringUtils.stripToEmpty(domainCode) + "_");
    }

    private void whenRetrievingTheClientAuthenticationPath() {
        clientAuthenticationPath = Deencapsulation.invoke(tlsReader, "getClientAuthenticationPath", domainCode);
    }


}