package eu.domibus.common.model.common;

/**
 * @author Thomas Dussart
 * @since 4.0
 * <p>
 * Indicates which type of modification has been done on the entity.
 */
public enum ModificationType {
    /**
     * Indicates that the entity was added.
     */
    ADD,
    /**
     * Indicates that the entity was modified.
     */
    MOD,
    /**
     * Indicates that the entity was deleted.
     */
    DEL;
}
