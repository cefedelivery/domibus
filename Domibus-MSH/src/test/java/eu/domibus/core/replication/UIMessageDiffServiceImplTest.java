package eu.domibus.core.replication;

import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.core.converter.DomainCoreConverter;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Collections;
import java.util.List;

/**
 * JUnit for {@link UIMessageDiffServiceImpl}
 *
 * @author Catalin Enache
 * @since 4.0
 */
@RunWith(JMockit.class)
public class UIMessageDiffServiceImplTest {

    @Tested
    UIMessageDiffServiceImpl uiMessageDiffService;

    @Injectable
    UIMessageDiffDao uiMessageDiffDao;

    @Injectable
    DomibusPropertyProvider domibusPropertyProvider;

    @Injectable
    private DomainCoreConverter domainConverter;

    @Injectable
    UIMessageService uiMessageService;


    @Test
    public void testCountAll() {

        final int records = 214;
        new Expectations() {{
            uiMessageDiffDao.countAllNative();
            result = records;

        }};

        //tested method
        final int recordsToSync = uiMessageDiffService.countAll();
        Assert.assertEquals(records, recordsToSync);

        new FullVerifications() {{
            uiMessageDiffDao.countAllNative();
        }};
    }

    @Test
    public void testFindAll(final @Mocked UIMessageDiffEntity uiMessageDiffEntity) {
        final List<UIMessageDiffEntity> uiMessageDiffEntityList = Collections.singletonList(uiMessageDiffEntity);

        new Expectations() {{
            uiMessageDiffDao.findAllNative();
            result = uiMessageDiffEntity;

        }};

        //tested method
        List<UIMessageDiffEntity> result = uiMessageDiffService.findAll();


        new FullVerifications() {{
            uiMessageDiffDao.findAllNative();
        }};
    }


    @Test
    public void testFindAndSyncUIMessages(final @Mocked UIMessageDiffEntity uiMessageDiffEntity) {
        final int countAllRows = 10;
        final List<UIMessageDiffEntity> uiMessageDiffEntityList = Collections.singletonList(uiMessageDiffEntity);

        new Expectations(uiMessageDiffService) {{
            domibusPropertyProvider.getDomainProperty(UIMessageDiffServiceImpl.MAX_ROWS_KEY);
            result = 10000;

            uiMessageDiffService.countAll();
            result = countAllRows;

            uiMessageDiffService.findAll();
            result = uiMessageDiffEntityList;
        }};

        //tested method
        uiMessageDiffService.findAndSyncUIMessages();

        new FullVerifications(uiMessageDiffService) {{
            uiMessageDiffService.convertToUIMessageEntity(uiMessageDiffEntity);

            uiMessageService.saveOrUpdate(withAny(new UIMessageEntity()));
        }};
    }

    @Test
    public void testFindAndSyncUIMessagesWithLimit(final @Mocked UIMessageDiffEntity uiMessageDiffEntity) {
        final int countAllRows = 10;
        final int limit = 20;
        final List<UIMessageDiffEntity> uiMessageDiffEntityList = Collections.singletonList(uiMessageDiffEntity);

        new Expectations(uiMessageDiffService) {{

            uiMessageDiffService.countAll();
            result = countAllRows;

            uiMessageDiffService.findAll(limit);
            result = uiMessageDiffEntityList;
        }};

        //tested method
        final int syncedRows = uiMessageDiffService.findAndSyncUIMessages(limit);
        Assert.assertEquals(10, syncedRows);

        new FullVerifications(uiMessageDiffService) {{
            int actualValue;
            uiMessageDiffService.findAll(actualValue = withCapture());
            Assert.assertEquals(limit, actualValue);

            uiMessageDiffService.convertToUIMessageEntity(uiMessageDiffEntity);

            uiMessageService.saveOrUpdate(withAny(new UIMessageEntity()));
        }};
    }

    @Test
    public void testConvertToUIMessageEntity_EntityNotNull_ResultOK(final @Mocked UIMessageDiffEntity uiMessageDiffEntity) {

        //tested method
        uiMessageDiffService.convertToUIMessageEntity(uiMessageDiffEntity);

        new Verifications() {{
            //just test the call to converter
            domainConverter.convert(uiMessageDiffEntity, UIMessageEntity.class);
            times = 1;
        }};
    }

    @Test
    public void testConvertToUIMessageEntity_EntityNull_ResultNull(final @Mocked UIMessageDiffEntity uiMessageDiffEntity) {

        //tested method
        final UIMessageEntity uiMessageEntity = uiMessageDiffService.convertToUIMessageEntity(uiMessageDiffEntity);
        Assert.assertNull(uiMessageEntity);
    }

}