package eu.domibus.core.pmode;

import eu.domibus.common.MSHRole;
import eu.domibus.common.dao.MessagingDao;
import eu.domibus.ebms3.common.context.MessageExchangeConfiguration;
import eu.domibus.ebms3.common.dao.PModeProvider;
import eu.domibus.ebms3.common.model.UserMessage;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.Verifications;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Cosmin Baciu
 * @since 3.3
 */
@RunWith(JMockit.class)
public class PModeDefaultServiceTest {

    @Tested
    PModeDefaultService pModeDefaultService;

    @Injectable
    MessagingDao messagingDao;

    @Injectable
    private PModeProvider pModeProvider;


    @Test
    public void testGetLegConfiguration(@Injectable final UserMessage userMessage,
                                        @Injectable final eu.domibus.common.model.configuration.LegConfiguration legConfigurationEntity) throws Exception {
        final String messageId = "1";
        final String pmodeKey = "1";
        final MessageExchangeConfiguration messageExchangeConfiguration=new MessageExchangeConfiguration("1",",","","","","");
        new Expectations() {{
            messagingDao.findUserMessageByMessageId(messageId);
            result = userMessage;

            pModeProvider.findUserMessageExchangeContext(userMessage, MSHRole.SENDING);
            result = messageExchangeConfiguration;

            pModeProvider.getLegConfiguration(messageExchangeConfiguration.getPmodeKey());
            result = legConfigurationEntity;

        }};

        pModeDefaultService.getLegConfiguration(messageId);

        new Verifications() {{
            pModeDefaultService.convert(legConfigurationEntity);
        }};
    }
}
