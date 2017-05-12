package eu.domibus.web.rest;

import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by musatmi on 11/05/2017.
 */
@RunWith(JMockit.class)
public class MessageResourceTest {

    @Test
    public void testZip() throws Exception {
        MessageResource messageResource = new MessageResource();

        Map<String,byte[]> map = new HashMap<>();
        map.put("plm","plm".getBytes());
        map.put("tlsaf","fsfdga".getBytes());

        final byte[] zip = messageResource.zip(map);
        Files.write(Paths.get("C:\\dev\\plm.zip"),zip);

    }


}
