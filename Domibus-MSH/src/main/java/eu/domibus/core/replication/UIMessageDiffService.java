package eu.domibus.core.replication;

import java.util.List;

public interface UIMessageDiffService {

    /**
     * count all records of {@code V_MESSAGE_UI_DIFF} view
     *
     * @return
     */
    int countAll();

    /**
     * brings all records from {@code V_MESSAGE_UI_DIFF} view
     *
     * @return
     */
    List<UIMessageDiffEntity> findAll();

    /**
     * brings {@code limit} records from {@code V_MESSAGE_UI_DIFF} view
     *
     * @return
     */
    List<UIMessageDiffEntity> findAll(int limit);

    /**
     * run the diff query against {@code V_MESSAGE_UI} view and sync the data
     */
    void findAndSyncUIMessages();

    /**
     * run the diff query against {@code V_MESSAGE_UI} view and sync the data
     */
    int findAndSyncUIMessages(int limit);
}
