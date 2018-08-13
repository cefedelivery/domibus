package eu.domibus.web.rest;

import eu.domibus.core.replication.UIReplicationDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger LOG = LoggerFactory.getLogger(UIReplicationResource.class);

    @Autowired
    private UIReplicationDataService uiReplicationDataService;

    @RequestMapping(value = {"/count"}, method = RequestMethod.GET)
    public ResponseEntity<String> countData() {
        LOG.debug("count data was requested");

        int rowsToBeSynced = uiReplicationDataService.countSyncUIMessages();

        return ResponseEntity
                .ok()
                .body(rowsToBeSynced + " records to be synced for TB_MESSAGE_UI table");
    }

    @RequestMapping(value = {"/sync"}, method = RequestMethod.GET)
    public ResponseEntity<String> syncData(@RequestParam(value = "limit", defaultValue = "10000") int limit) {
        LOG.debug("sync data was requested with limit={}", limit);

        int syncedRows = uiReplicationDataService.findAndSyncUIMessages(limit);

        return ResponseEntity
                .ok()
                .body(syncedRows == 0 ? "no records were synced for TB_MESSAGE_UI table" :
                        syncedRows + " were successfully synced for TB_MESSAGE_UI table");
    }


}
