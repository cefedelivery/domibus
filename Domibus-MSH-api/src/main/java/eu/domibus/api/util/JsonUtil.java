package eu.domibus.api.util;

/**
 * Created by Cosmin Baciu on 22-Aug-16.
 */
public interface JsonUtil {

    String writeValueAsString(Object object);

    public <T> T readValue(String content, Class<T> valueType);

}
