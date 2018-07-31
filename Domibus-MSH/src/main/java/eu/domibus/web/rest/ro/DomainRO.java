package eu.domibus.web.rest.ro;

import java.io.Serializable;
import java.util.List;

/**
 * @author Ion Perpegel
 * @since 4.0
 */
public class DomainRO implements Serializable {

    private String code;
    private String name;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
