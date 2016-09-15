package eu.domibus.submission;

import eu.domibus.plugin.validation.DefaultSubmissionValidatorList;
import eu.domibus.plugin.validation.SubmissionValidationException;
import eu.domibus.plugin.validation.SubmissionValidatorList;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

/**
 * Created by baciuco on 08/08/2016.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:spring/submissionValidatorListProviderContext.xml")
public class SubmissionValidatorListProviderImplTestIT {

    @Autowired
    SubmissionValidatorListProvider submissionValidatorListProvider;

    @Test
    public void testGetSubmissionValidatorListWithOneMatch() throws Exception {
        SubmissionValidatorList wsSubmissionValidatorList = submissionValidatorListProvider.getSubmissionValidatorList("ws");
        Assert.assertNotNull(wsSubmissionValidatorList);

    }

    @Test
    public void testGetSubmissionValidatorListWithNoMatch() throws Exception {
        SubmissionValidatorList wsSubmissionValidatorList = submissionValidatorListProvider.getSubmissionValidatorList("noExistingPlugin");
        Assert.assertNull(wsSubmissionValidatorList);

    }

    @Test(expected = SubmissionValidationException.class)
    public void testGetSubmissionValidatorListWithMultipleMatches() throws Exception {
        SubmissionValidatorList wsSubmissionValidatorList = submissionValidatorListProvider.getSubmissionValidatorList("jms");
        Assert.assertNotNull(wsSubmissionValidatorList);

    }
}
