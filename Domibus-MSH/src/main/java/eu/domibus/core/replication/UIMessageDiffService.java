package eu.domibus.core.replication;

import java.util.List;

public interface UIMessageDiffService {

    int countAll();

    List<UIMessageDiffEntity> findAll();

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
