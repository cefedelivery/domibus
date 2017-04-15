package eu.domibus.ebms3.common.dao;

/**
 * Created by idragusa on 4/14/17.
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
