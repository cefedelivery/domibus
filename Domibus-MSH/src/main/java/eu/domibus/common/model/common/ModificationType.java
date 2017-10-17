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
    ADD("Created"),
    /**
     * Indicates that the entity was modified.
     */
    MOD("Modified"),
    /**
     * Indicates that the entity was deleted.
     */
    DEL("Deleted");

    private final String label;

    ModificationType(final String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
