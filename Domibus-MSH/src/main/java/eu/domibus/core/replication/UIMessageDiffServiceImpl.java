package eu.domibus.core.replication;

import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.common.util.WarningUtil;
import eu.domibus.core.converter.DomainCoreConverter;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UIMessageDiffServiceImpl implements UIMessageDiffService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(UIMessageDiffServiceImpl.class);

    /** max no of records to be synchronized using cron job */
    static final String MAX_ROWS_KEY = "domibus.ui.replication.sync.cron.max.rows";

    @Autowired
    private UIMessageDiffDao uiMessageDiffDao;

    @Autowired
    private DomibusPropertyProvider domibusPropertyProvider;

    @Autowired
    private DomainCoreConverter domainConverter;

    @Autowired
    private UIMessageService uiMessageService;

    @Override
    @Transactional(readOnly = true, propagation=Propagation.SUPPORTS)
    public int countAll() {
        LOG.debug("start to count UIMessages to be synced");
        long startTime = System.currentTimeMillis();

        int recordsToSync = uiMessageDiffDao.countAllNative();

        LOG.debug("{} milliseconds to count the records", System.currentTimeMillis() - startTime);
        return recordsToSync;
    }

    @Override
    @Transactional(readOnly = true, propagation=Propagation.SUPPORTS)
    public List<UIMessageDiffEntity> findAll() {
        LOG.debug("start to find UIMessages to be synced");
        long startTime = System.currentTimeMillis();

        List<UIMessageDiffEntity> uiMessageDiffEntityList = uiMessageDiffDao.findAllNative();

        LOG.debug("{} milliseconds to find all UIMessages to be synced", System.currentTimeMillis() - startTime);
        return uiMessageDiffEntityList;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void findAndSyncUIMessages() {
        LOG.debug("start counting differences for UIReplication");

        int rowsToSyncCount = countAll();
        LOG.debug("found {} differences between native tables and TB_MESSAGE_UI", rowsToSyncCount);

        if (rowsToSyncCount == 0) {
            return;
        }

        // check how many rows to sync
        int maxRowsToSync = NumberUtils.toInt(domibusPropertyProvider.getDomainProperty(MAX_ROWS_KEY));
        if (rowsToSyncCount > maxRowsToSync) {
            LOG.warn(WarningUtil.warnOutput("There are more than {} rows to sync into TB_MESSAGE_UI table " +
                    "please use the REST resource instead."), maxRowsToSync);
            return;
        }

        List<UIMessageEntity> uiMessageEntityList =
                        findAll().
                        stream().
                        map(objects -> convertToUIMessageEntity(objects)).
                        collect(Collectors.toList());

        if (!uiMessageEntityList.isEmpty()) {
            LOG.debug("start to update TB_MESSAGE_UI");

            uiMessageEntityList.stream().forEach(uiMessageEntity ->
                    uiMessageService.saveOrUpdate(uiMessageEntity));

            LOG.debug("finish to update TB_MESSAGE_UI");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int findAndSyncUIMessages(int limit) {
        LOG.debug("find and sync first {} UIMessages", limit);
        long startTime = System.currentTimeMillis();


        int recordsToSync = countAll();

        LOG.debug("{} milliseconds to count the records", System.currentTimeMillis() - startTime);
        startTime = System.currentTimeMillis();

        if (recordsToSync == 0) {
            LOG.debug("no records to sync");
            return 0;
        }

        List<UIMessageEntity> uiMessageEntityList =
                uiMessageDiffDao.findAll(limit).
                        stream().
                        map(objects -> convertToUIMessageEntity(objects)).
                        collect(Collectors.toList());

        LOG.debug("{} milliseconds to fetch the records", System.currentTimeMillis() - startTime);
        startTime = System.currentTimeMillis();

        if (!uiMessageEntityList.isEmpty()) {
            LOG.debug("start to update TB_MESSAGE_UI");

            uiMessageEntityList.stream().forEach(uiMessageEntity ->
                    uiMessageService.saveOrUpdate(uiMessageEntity));

            LOG.debug("finish to update TB_MESSAGE_UI after {} milliseconds", System.currentTimeMillis() - startTime);
        }
        return recordsToSync;
    }


    /**
     * Converts one record of the diff query to {@link UIMessageEntity}
     *
     * @param diffEntity
     * @return
     */
    protected UIMessageEntity convertToUIMessageEntity(UIMessageDiffEntity diffEntity) {
        if (null == diffEntity) {
            return null;
        }

        return domainConverter.convert(diffEntity, UIMessageEntity.class);
    }
}
