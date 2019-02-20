package eu.domibus.web.rest.ro;

import java.io.Serializable;

/**
 * @author Cosmin Baciu
 * @since 3.3
 */
public class DomibusInfoRO implements Serializable {

    private String version;

    private String versionNumber;

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getVersionNumber() {
        return versionNumber;
    }

    public void setVersionNumber(String versionNumber) {
        this.versionNumber = versionNumber;
    }
}
