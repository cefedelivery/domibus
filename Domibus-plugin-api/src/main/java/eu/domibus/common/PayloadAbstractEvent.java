package eu.domibus.common;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Cosmin Baciu
 * @since 4.1
 */
public abstract class PayloadAbstractEvent implements Serializable {

    protected String messageId;

    protected String cid;

    protected String mime;

    protected String fileName;

    protected Map<String, Object> properties = new HashMap<>(); //NOSONAR

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public void addProperty(String key, Object value) {
        properties.put(key, value);
    }

    public Map<String, Object> getProperties() {
        return Collections.unmodifiableMap(properties);
    }

    public String getCid() {
        return cid;
    }

    public void setCid(String cid) {
        this.cid = cid;
    }

    public String getMime() {
        return mime;
    }

    public void setMime(String mime) {
        this.mime = mime;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }


}
