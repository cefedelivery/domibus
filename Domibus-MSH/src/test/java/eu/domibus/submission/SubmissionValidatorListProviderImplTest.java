package eu.domibus.submission;

import eu.domibus.plugin.validation.SubmissionValidatorList;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.Verifications;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.ApplicationContext;

/**
 * Created by baciuco on 08/08/2016.
 */
@RunWith(JMockit.class)
public class SubmissionValidatorListProviderImplTest {

    @Injectable
    ApplicationContext applicationContext;

    @Tested
    SubmissionValidatorListProviderImpl submissionValidatorListProvider;

    @Test
    public void testGetSubmissionValidatorList() throws Exception {
        new Expectations() {{
            applicationContext.getBeanNamesForType(SubmissionValidatorList.class);
            result = new String[]{"wsPlugin", "jmsPlugin", "customPlugin"};
        }};

        submissionValidatorListProvider.getSubmissionValidatorList("customPlugin");

        new Verifications() {{
            String springBean = null;
            applicationContext.getBean(springBean = withCapture(), SubmissionValidatorList.class);
            times = 1;

            Assert.assertEquals(springBean, "customPlugin");
        }};

    }

    @Test
    public void testGetSubmissionValidatorListWithNoBeanFound() throws Exception {
        new Expectations() {{
            applicationContext.getBeanNamesForType(SubmissionValidatorList.class);
            result = new String[]{"wsPlugin", "jmsPlugin", "customPlugin"};
        }};

        SubmissionValidatorList noPlugin = submissionValidatorListProvider.getSubmissionValidatorList("noPlugin");
        Assert.assertNull(noPlugin);

        new Verifications() {{
            applicationContext.getBean(anyString, SubmissionValidatorList.class);
            times = 0;
        }};
    }
}
