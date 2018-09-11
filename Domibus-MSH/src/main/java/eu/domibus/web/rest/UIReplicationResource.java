package eu.domibus.web.rest;

import eu.domibus.core.replication.UIMessageDiffService;
import eu.domibus.core.replication.UIReplicationSignalService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST resource to sync manually any unsynchronized data
 *
 * @author Catalin Enache
 * @since 4.0
 */
@RestController
@RequestMapping(value = "/rest/uireplication")
public class UIReplicationResource {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(UIReplicationResource.class);

    @Autowired
    private UIMessageDiffService uiMessageDiffService;

    @Autowired
    private UIReplicationSignalService uiReplicationSignalService;

    @RequestMapping(value = {"/count"}, method = RequestMethod.GET)
    public ResponseEntity<String> countData() {
        if (!uiReplicationSignalService.isReplicationEnabled()) {
            LOG.debug("UIReplication is disabled - no processing will occur");
            return ResponseEntity
                    .ok()
                    .body( "UIReplication is disabled. No records will be count to be synced for TB_MESSAGE_UI table");
        }

        LOG.debug("count data was requested");

        int rowsToBeSynced = uiMessageDiffService.countAll();

        return ResponseEntity
                .ok()
                .body(rowsToBeSynced + " records to be synced for TB_MESSAGE_UI table");
    }

    @RequestMapping(value = {"/sync"}, method = RequestMethod.GET)
    public ResponseEntity<String> syncData(@RequestParam(value = "limit", defaultValue = "10000") int limit) {
        if (!uiReplicationSignalService.isReplicationEnabled()) {
            LOG.debug("UIReplication is disabled - no processing will occur");
            return ResponseEntity
                    .ok()
                    .body( "UIReplication is disabled. No records will be synced for TB_MESSAGE_UI table");
        }

        LOG.debug("sync data was requested with limit={}", limit);

        int syncedRows = uiMessageDiffService.findAndSyncUIMessages(limit);

        return ResponseEntity
                .ok()
                .body(syncedRows == 0 ? "no records were synced for TB_MESSAGE_UI table" :
                        syncedRows + " were successfully synced for TB_MESSAGE_UI table");
    }


}
