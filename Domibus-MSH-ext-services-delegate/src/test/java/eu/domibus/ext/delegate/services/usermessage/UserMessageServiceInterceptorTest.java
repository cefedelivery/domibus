package eu.domibus.ext.delegate.services.usermessage;

import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.usermessage.UserMessageService;
import eu.domibus.api.util.AOPUtil;
import eu.domibus.ext.domain.UserMessageDTO;
import eu.domibus.ext.exceptions.DomibusErrorCode;
import eu.domibus.ext.exceptions.UserMessageExtException;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JMockit.class)
public class UserMessageServiceInterceptorTest {

    @Tested
    UserMessageServiceInterceptor userMessageServiceInterceptor;

    @Injectable
    UserMessageService userMessageService;

    @Injectable
    AOPUtil aopUtil;

    @Test
    public void testIntercept(@Injectable final ProceedingJoinPoint joinPoint) throws Throwable {
        // Given
        final UserMessageDTO userMessageDTO = new UserMessageDTO();

        new Expectations() {{
           joinPoint.proceed();
           result = userMessageDTO;
        }};

        // When
        final Object interceptedResult = userMessageServiceInterceptor.intercept(joinPoint);

        // Then
        Assert.assertEquals(userMessageDTO, interceptedResult);
    }

    @Test
    public void testInterceptWhenExtExceptionIsRaised(@Injectable final ProceedingJoinPoint joinPoint) throws Throwable {
        // Given
        final UserMessageExtException userMessageExtException = new UserMessageExtException(DomibusErrorCode.DOM_001, "test");

        new Expectations() {{
           joinPoint.proceed();
           result = userMessageExtException;
        }};

        // When
        try {
            userMessageServiceInterceptor.intercept(joinPoint);
        } catch(UserMessageExtException e) {
            // Then
            Assert.assertTrue(userMessageExtException == e);
            return;
        }
        Assert.fail();
    }

    @Test
    public void testInterceptWhenCoreExceptionIsRaised(@Injectable final ProceedingJoinPoint joinPoint) throws Throwable {
        // Given
        final eu.domibus.api.message.UserMessageException userMessageException = new eu.domibus.api.message.UserMessageException(DomibusCoreErrorCode.DOM_001, "test");

        new Expectations() {{
           joinPoint.proceed();
           result = userMessageException;
        }};

        // When
        try {
            userMessageServiceInterceptor.intercept(joinPoint);
        } catch (UserMessageExtException e) {
            Assert.assertTrue(e.getCause() == userMessageException);
            return;
        }
        Assert.fail();
    }
}
