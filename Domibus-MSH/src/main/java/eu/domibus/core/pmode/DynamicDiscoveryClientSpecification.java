package eu.domibus.core.pmode;

/**
 * @author Ioana Dragusanu (idragusa)
 * @since 3.2.5
 */
public enum DynamicDiscoveryClientSpecification {
    OASIS("OASIS"),
    PEPPOL("PEPPOL");

    private final String name;

    DynamicDiscoveryClientSpecification(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}