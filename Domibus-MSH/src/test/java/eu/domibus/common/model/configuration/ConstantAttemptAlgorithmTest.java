package eu.domibus.common.model.configuration;

import mockit.Mock;
import mockit.MockUp;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


@RunWith(JMockit.class)
public class ConstantAttemptAlgorithmTest {

    private RetryStrategy retryStrategy = RetryStrategy.CONSTANT;
    private static final long SYSTEM_DATE_IN_MILLIS_FOR_YEAR_TRANSITION = 1451602799000L;
    private static final long SYSTEM_DATE_IN_MILLIS_FIRST_OF_JANUARY_2016 = 1451602800000L;
    private static final int MINUTES_FROM_01_01_2016_TO_31_12_3999 = 1043483039;

    @Test
    public void compute_NegativeMaxAttempts_ReturnNull() {
        Assert.assertNull(retryStrategy.getAlgorithm().compute(new Date(), -1, 20));
    }

    @Test
    public void compute_NegativeTimeoutInMinutes_ReturnNull() {
        Assert.assertNull(retryStrategy.getAlgorithm().compute(new Date(), 2, -1));
    }

    @Test
    public void compute_NullForDate_ReturnNull() {
        Assert.assertNull(retryStrategy.getAlgorithm().compute(null, 2, 1));
    }

    @Test
    @Ignore
    public void compute_TransitionToNextYear_ValidResult() {
        /*Mock System.currentTimeMillis() in order to have a fixed current date.
          The mocked date is: 2015/12/31 23:59:59
         */
        new SystemMockYearTransition();


        Assert.assertEquals("current time in millis is not as expected, maybe mocking of System.currentTimeMillis() does not work?", 1451602799000L, System.currentTimeMillis());

        Date nextAttempt = retryStrategy.getAlgorithm().compute(new Date(SYSTEM_DATE_IN_MILLIS_FOR_YEAR_TRANSITION), 2, 2);
        Assert.assertNotNull("calculated nextAttempt is null", nextAttempt);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(nextAttempt);
        Assert.assertEquals("transition to year does not work correctly", 2016, calendar.get(Calendar.YEAR));
    }

    @Test
    public void compute_MaxAttemptsIntegerMAXVALUE_ValidResult()  {
        new SystemMockFirstOfJanuary2016();


        Date nextAttempt = retryStrategy.getAlgorithm().compute(new Date(SYSTEM_DATE_IN_MILLIS_FIRST_OF_JANUARY_2016), Integer.MAX_VALUE, 1);

        Assert.assertNotNull(nextAttempt);
    }

    @Test
    @Ignore
    public void compute_TimeInMinutesTill_31_12_3999_ExpectedResult()  {
        new SystemMockFirstOfJanuary2016();

        Date nextAttempt = retryStrategy.getAlgorithm().compute(new Date(SYSTEM_DATE_IN_MILLIS_FIRST_OF_JANUARY_2016), 1, MINUTES_FROM_01_01_2016_TO_31_12_3999);

        Assert.assertEquals(parseDateString("3999/12/31 23:59:00"), nextAttempt);
    }

    @Test
    public void compute_TimeInMinutesIntegerMAXVALUE_ValidResult() {
        new SystemMockFirstOfJanuary2016();

        Date nextAttempt = retryStrategy.getAlgorithm().compute(new Date(SYSTEM_DATE_IN_MILLIS_FIRST_OF_JANUARY_2016), 1, Integer.MAX_VALUE);

        Assert.assertNotNull(nextAttempt);
    }

    private static class SystemMockFirstOfJanuary2016 extends MockUp<System> {
        @Mock
        public static long currentTimeMillis() {
            return SYSTEM_DATE_IN_MILLIS_FIRST_OF_JANUARY_2016;
        }
    }

    private static class SystemMockYearTransition extends MockUp<System> {
        @Mock
        public static long currentTimeMillis() {
            return SYSTEM_DATE_IN_MILLIS_FOR_YEAR_TRANSITION;
        }
    }


    private Date parseDateString(String dateInString) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date receiveDate = null;
        try {
            receiveDate = dateFormat.parse(dateInString);
        } catch (ParseException e) {
            assert false;
        }
        return receiveDate;
    }

}