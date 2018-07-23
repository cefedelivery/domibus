package eu.domibus.core.replication;

import java.util.List;

/**
 * Dao interface for {@link UIMessageDiffEntity} entity
 * @author Catalin Enache
 * @since 4.0
 */
public interface UIMessageDiffDao {
    List<UIMessageDiffEntity> findAll();

    int countAll();
}
