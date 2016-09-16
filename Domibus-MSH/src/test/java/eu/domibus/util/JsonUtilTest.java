package eu.domibus.util;

import org.junit.Test;

import java.util.List;

/**
 * Created by Cosmin Baciu on 22-Aug-16.
 */
public class JsonUtilTest {

    JsonUtilImpl jsonUtil = new JsonUtilImpl();

    @Test
    public void testJsonToList() throws Exception {
        String json = "['1', '2']";
        List<String> strings = jsonUtil.jsonToList(json);
    }
}
