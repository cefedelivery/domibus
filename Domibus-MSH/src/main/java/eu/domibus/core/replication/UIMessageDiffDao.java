package eu.domibus.core.replication;

import java.util.List;

/**
 * DAO interface for {@link UIMessageDiffEntity} entity
 *
 * @author Catalin Enache
 * @since 4.0
 */
public interface UIMessageDiffDao {

    /**
     * count all rows of {@code V_MESSAGE_UI_DIFF}
     *
     * @return
     */
    int countAll();

    /**
     * count all rows of {@code V_MESSAGE_UI_DIFF}
     *
     * @return
     */
    int countAllNative();

    /**
     * returns max rows equals to limit parameter of of {@code V_MESSAGE_UI_DIFF}
     *
     * @param limit
     * @return
     */
    List<UIMessageDiffEntity> findAll(int limit);

    /**
     * returns max rows equals to limit parameter of of {@code V_MESSAGE_UI_DIFF}
     *
     * @param limit
     * @return
     */
    List<UIMessageDiffEntity> findAllNative(int limit);

    /**
     * returns all rows of of {@code V_MESSAGE_UI_DIFF}
     *
     * @return
     */
    List<UIMessageDiffEntity> findAll();


    /**
     * returns all rows of of {@code V_MESSAGE_UI_DIFF}
     *
     * @return
     */
    List<UIMessageDiffEntity> findAllNative();
}
