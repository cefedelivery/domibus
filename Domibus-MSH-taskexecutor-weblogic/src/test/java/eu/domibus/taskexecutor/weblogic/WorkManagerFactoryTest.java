package eu.domibus.taskexecutor.weblogic;

import commonj.work.WorkManager;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.Verifications;
import mockit.integration.junit4.JMockit;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Cosmin Baciu
 */
@RunWith(JMockit.class)
public class WorkManagerFactoryTest {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(WorkManagerFactoryTest.class);

    @Tested
    WorkManagerFactory workManagerFactory;

    @Test
    public void testGetObjectWithGlobalWorkManager(final @Injectable WorkManager workManager) throws Exception {
        new Expectations(workManagerFactory) {{
            workManagerFactory.getGlobalWorkManager();
            result = workManager;
        }};

        final WorkManager returnedWorkManager = workManagerFactory.getObject();
        new Verifications() {{
            assertTrue(workManager == returnedWorkManager);
        }};
    }

    @Test
    public void testGetObjectWithDefaultWorkManager(final @Injectable WorkManager workManager) throws Exception {
        new Expectations(workManagerFactory) {{
            workManagerFactory.getGlobalWorkManager();
            result = null;

            workManagerFactory.getDefaultWorkManager();
            result = workManager;
        }};

        final WorkManager returnedWorkManager = workManagerFactory.getObject();
        new Verifications() {{
            workManagerFactory.getDefaultWorkManager();
            assertTrue(workManager == returnedWorkManager);
        }};
    }

    @Test
    public void testGetObjectWithNoWorkManager(final @Injectable WorkManager workManager) throws Exception {
        new Expectations(workManagerFactory) {{
            workManagerFactory.getGlobalWorkManager();
            result = null;

            workManagerFactory.getDefaultWorkManager();
            result = null;
        }};

        final WorkManager returnedWorkManager = workManagerFactory.getObject();
        new Verifications() {{
            workManagerFactory.getGlobalWorkManager();
            workManagerFactory.getDefaultWorkManager();
            assertNull(returnedWorkManager);
        }};
    }

    @Test
    public void testLookupWorkManager(final @Injectable WorkManager workManager) throws Exception {
        final String jndiName = "myname";
        new Expectations(InitialContext.class) {{
            InitialContext.doLookup(jndiName);
            result = workManager;
        }};

        final WorkManager returnedWorkManager = workManagerFactory.lookupWorkManager(jndiName);

        new Verifications() {{
            assertTrue(workManager == returnedWorkManager);
        }};
    }

    @Test
    public void testLookupWorkManagerWhenLookupExceptionIsRaised(final @Injectable WorkManager workManager) throws Exception {
        final String jndiName = "myname";
        new Expectations(InitialContext.class) {{
            InitialContext.doLookup(jndiName);
            result = new NamingException();
        }};

        final WorkManager returnedWorkManager = workManagerFactory.lookupWorkManager(jndiName);

        new Verifications() {{
            assertTrue(null == returnedWorkManager);
        }};
    }

    @Test
    public void testGetDefaultWorkManager(final @Injectable WorkManager workManager) throws Exception {
        new Expectations(workManagerFactory) {{
            workManagerFactory.lookupWorkManager(WorkManagerFactory.DEFAULT_WORK_MANAGER);
            result = workManager;
        }};

        final WorkManager returnedWorkManager = workManagerFactory.getDefaultWorkManager();

        new Verifications() {{
            workManagerFactory.lookupWorkManager(WorkManagerFactory.DEFAULT_WORK_MANAGER);
            assertTrue(workManager == returnedWorkManager);
        }};
    }

    @Test
    public void testGetGlobalWorkManager(final @Injectable WorkManager workManager) throws Exception {
        final String workManagerJndiName = "myjndi";
        workManagerFactory.setWorkManagerJndiName(workManagerJndiName);
        new Expectations(workManagerFactory) {{
            workManagerFactory.lookupWorkManager(workManagerJndiName);
            result = workManager;
        }};

        final WorkManager returnedWorkManager = workManagerFactory.getGlobalWorkManager();

        new Verifications() {{
            workManagerFactory.lookupWorkManager(workManagerJndiName);
            assertTrue(workManager == returnedWorkManager);
        }};
    }

}
