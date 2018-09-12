package eu.domibus.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.domibus.api.util.JsonException;
import eu.domibus.api.util.JsonUtil;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Created by Cosmin Baciu on 22-Aug-16.
 */
@Component
public class JsonUtilImpl implements JsonUtil {

    private ObjectMapper mapper = new ObjectMapper();

    @Override
    public String writeValueAsString(Object object) {
        try {
            return mapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new JsonException(e);
        }
    }

    @Override
    public <T> T readValue(String content, Class<T> valueType) {
        try {
            return (T) mapper.readValue(content, valueType);
        } catch (IOException e) {
            throw new JsonException(e);
        }
    }
}
