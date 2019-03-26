package eu.domibus.core.crypto.spi.dss;

/**
 * @author Thomas Dussart
 * @since 4.1
 *
 * Model class containing the constraints configured in the property file.
 * Used to compare with the constraints of the DSS validation report.
 */
public class ConstraintInternal {

    private String name;

    private String status;

    public ConstraintInternal(String name, String status) {
        this.name = name;
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public String getStatus() {
        return status;
    }
}
