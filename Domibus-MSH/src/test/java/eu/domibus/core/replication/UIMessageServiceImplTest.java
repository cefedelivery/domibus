package eu.domibus.core.replication;

import eu.domibus.common.model.logging.MessageLogInfo;
import eu.domibus.core.converter.DomainCoreConverter;
import eu.domibus.web.rest.ro.MessageLogRO;
import eu.domibus.web.rest.ro.MessageLogResultRO;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.Verifications;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Catalin Enache
 * @since 4.1
 */
public class UIMessageServiceImplTest {

    @Injectable
    private UIMessageDao uiMessageDao;

    @Injectable
    private DomainCoreConverter domainConverter;

    @Tested
    UIMessageServiceImpl uiMessageService;

    private final int from = 0, max = 10;
    private final String column = "received";
    private final boolean asc = true;
    private final Map<String, Object> filters = new HashMap<>();
    private final UIMessageEntity uiMessageEntity = new UIMessageEntity();
    private final List<UIMessageEntity> uiMessageEntityList = Collections.singletonList(uiMessageEntity);

    @Test
    public void testFindPaged() {

        new Expectations() {{
            uiMessageDao.findPaged(from, max, column, asc, filters);
            result = uiMessageEntityList;
        }};

        //tested method
        uiMessageService.findPaged(from, max, column, asc, filters);

        new Verifications() {{
            uiMessageService.convertToMessageLogInfo(withAny(new UIMessageEntity()));
            times = 1;
        }};
    }

    @Test
    public void testCountAndFindPaged() {
        final int count = 20;

        new Expectations() {{
            uiMessageDao.countMessages(filters);
            result = count;

            uiMessageDao.findPaged(from, max, column, asc, filters);
            result = uiMessageEntityList;
        }};

        //tested method
        final MessageLogResultRO messageLogResultRO = uiMessageService.countAndFindPaged(from, max, column, asc, filters);
        Assert.assertNotNull(messageLogResultRO);
        Assert.assertSame(count, messageLogResultRO.getCount());
        Assert.assertEquals(uiMessageEntityList.size(), messageLogResultRO.getMessageLogEntries().size());

        new Verifications() {{
            uiMessageService.convertUIMessageEntity(withAny(new UIMessageEntity()));
            times = 1;
        }};
    }

    @Test
    public void testSaveOrUpdate() {

        //tested method
        uiMessageService.saveOrUpdate(uiMessageEntity);

        new Verifications() {{
            uiMessageDao.saveOrUpdate(uiMessageEntity);
        }};
    }

    @Test
    public void testConvertUIMessageEntity() {

        //tested method
        final MessageLogRO messageLogRO = uiMessageService.convertUIMessageEntity(uiMessageEntity);

        new Verifications() {{
            domainConverter.convert(uiMessageEntity, MessageLogRO.class);
        }};
    }

    @Test
    public void testConvertToMessageLogInfo() {

        //tested method
        uiMessageService.convertToMessageLogInfo(uiMessageEntity);

        new Verifications() {{
            domainConverter.convert(uiMessageEntity, MessageLogInfo.class);
        }};
    }


}