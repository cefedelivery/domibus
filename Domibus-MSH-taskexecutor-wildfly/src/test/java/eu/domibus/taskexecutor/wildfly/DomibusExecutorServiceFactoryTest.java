package eu.domibus.taskexecutor.wildfly;

import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.Verifications;
import mockit.integration.junit4.JMockit;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.enterprise.concurrent.ManagedExecutorService;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Cosmin Baciu
 */
@RunWith(JMockit.class)
public class DomibusExecutorServiceFactoryTest {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DomibusExecutorServiceFactoryTest.class);

    @Tested
    DomibusExecutorServiceFactory domibusExecutorServiceFactory;

    @Test
    public void testGetObjectWithGlobalExecutorService(final @Injectable ManagedExecutorService managedExecutorService) throws Exception {
        new Expectations(domibusExecutorServiceFactory) {{
            domibusExecutorServiceFactory.getGlobalExecutorService();
            result = managedExecutorService;
        }};

        final ManagedExecutorService returnedExecutorService = domibusExecutorServiceFactory.getObject();
        new Verifications() {{
            assertTrue(managedExecutorService == returnedExecutorService);
        }};
    }

    @Test
    public void testGetObjectWithDefaultExecutorService(final @Injectable ManagedExecutorService managedExecutorService) throws Exception {
        new Expectations(domibusExecutorServiceFactory) {{
            domibusExecutorServiceFactory.getGlobalExecutorService();
            result = null;

            domibusExecutorServiceFactory.getDefaultExecutorService();
            result = managedExecutorService;
        }};

        final ManagedExecutorService returnedExecutorService = domibusExecutorServiceFactory.getObject();
        new Verifications() {{
            domibusExecutorServiceFactory.getDefaultExecutorService();
            assertTrue(managedExecutorService == returnedExecutorService);
        }};
    }

    @Test
    public void testGetObjectWithNoExecutorService(final @Injectable ManagedExecutorService managedExecutorService) throws Exception {
        new Expectations(domibusExecutorServiceFactory) {{
            domibusExecutorServiceFactory.getGlobalExecutorService();
            result = null;

            domibusExecutorServiceFactory.getDefaultExecutorService();
            result = null;
        }};

        final ManagedExecutorService returnedExecutorService = domibusExecutorServiceFactory.getObject();
        new Verifications() {{
            domibusExecutorServiceFactory.getGlobalExecutorService();
            domibusExecutorServiceFactory.getDefaultExecutorService();
            assertNull(returnedExecutorService);
        }};
    }

    @Test
    public void testLookupExecutorService(final @Injectable ManagedExecutorService managedExecutorService) throws Exception {
        final String jndiName = "myname";
        new Expectations(InitialContext.class) {{
            InitialContext.doLookup(jndiName);
            result = managedExecutorService;
        }};

        final ManagedExecutorService returnedExecutorService = domibusExecutorServiceFactory.lookupExecutorService(jndiName);

        new Verifications() {{
            assertTrue(managedExecutorService == returnedExecutorService);
        }};
    }

    @Test
    public void testLookupWorkManagerWhenLookupExceptionIsRaised(final @Injectable ManagedExecutorService managedExecutorService) throws Exception {
        final String jndiName = "myname";
        new Expectations(InitialContext.class) {{
            InitialContext.doLookup(jndiName);
            result = new NamingException();
        }};

        final ManagedExecutorService returnedExecutorService = domibusExecutorServiceFactory.lookupExecutorService(jndiName);

        new Verifications() {{
            assertTrue(null == returnedExecutorService);
        }};
    }

    @Test
    public void testGetDefaultExecutorService(final @Injectable ManagedExecutorService managedExecutorService) throws Exception {
        new Expectations(domibusExecutorServiceFactory) {{
            domibusExecutorServiceFactory.lookupExecutorService(DomibusExecutorServiceFactory.DEFAULT_EXECUTOR_SERVICE);
            result = managedExecutorService;
        }};

        final ManagedExecutorService returnedExecutorService = domibusExecutorServiceFactory.getDefaultExecutorService();

        new Verifications() {{
            domibusExecutorServiceFactory.lookupExecutorService(DomibusExecutorServiceFactory.DEFAULT_EXECUTOR_SERVICE);
            assertTrue(managedExecutorService == returnedExecutorService);
        }};
    }

    @Test
    public void testGetGlobalWorkManager(final @Injectable ManagedExecutorService managedExecutorService) throws Exception {
        final String executorServiceJndi = "myjndi";
        domibusExecutorServiceFactory.setExecutorServiceJndiName(executorServiceJndi);
        new Expectations(domibusExecutorServiceFactory) {{
            domibusExecutorServiceFactory.lookupExecutorService(executorServiceJndi);
            result = managedExecutorService;
        }};

        final ManagedExecutorService returnedExecutorService = domibusExecutorServiceFactory.getGlobalExecutorService();

        new Verifications() {{
            domibusExecutorServiceFactory.lookupExecutorService(executorServiceJndi);
            assertTrue(managedExecutorService == returnedExecutorService);
        }};
    }

}
