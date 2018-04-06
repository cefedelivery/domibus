package eu.domibus.core.message;

import eu.domibus.common.dao.SignalMessageLogDao;
import eu.domibus.common.model.logging.SignalMessageLog;
import eu.domibus.ebms3.common.model.Ebms3Constants;
import eu.domibus.ebms3.common.model.MessageSubtype;
import mockit.Injectable;
import mockit.Tested;
import mockit.Verifications;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

/**
 * @author Tiago Miguel
 * @since 4.0
 */
@RunWith(Parameterized.class)
public class SignalMessageLogDefaultServiceTest {

    @Tested
    SignalMessageLogDefaultService signalMessageLogDefaultService;

    @Injectable
    SignalMessageLogDao signalMessageLogDao;

    @Parameterized.Parameter(0)
    public String userMessageService;

    @Parameterized.Parameter(1)
    public String userMessageAction;

    @Parameterized.Parameters(name = "{index}: usermessageService=\"{0}\" usermessageAction=\"{1}\"")
    public static Collection<Object[]> values() {
        return Arrays.asList(new Object[][]{
                {"service","action"},
                {Ebms3Constants.TEST_SERVICE, Ebms3Constants.TEST_ACTION}
        });
    }

    @Test
    public void testSave() {
        final String messageId = "1";
        signalMessageLogDefaultService.save(messageId, userMessageService, userMessageAction);

        new Verifications() {{
            SignalMessageLog signalMessageLog;
            signalMessageLogDao.create(signalMessageLog = withCapture());

            Assert.assertEquals(messageId, signalMessageLog.getMessageId());
            MessageSubtype messageSubtype = signalMessageLogDefaultService.checkTestMessage(userMessageService, userMessageAction) ?
                    MessageSubtype.TEST : null;
            Assert.assertEquals(messageSubtype, signalMessageLog.getMessageSubtype());
        }};

    }
}
