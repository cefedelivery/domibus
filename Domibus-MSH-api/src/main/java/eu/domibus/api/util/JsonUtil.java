package eu.domibus.api.util;

import java.util.List;
import java.util.Map;

/**
 * Created by Cosmin Baciu on 22-Aug-16.
 */
public interface JsonUtil {

    Map<String, Object> jsonToMap(String map);

    List<String> jsonToList(String list);
}
