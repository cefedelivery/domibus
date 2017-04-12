package eu.domibus.plugin.webService.util;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

/**
 * Created by idragusa on 4/11/17.
 */

public class InputStreamTest {

    @Test
    public void TestEmptyInputStream() throws Exception {
        InputStream is = new FileInputStream(Thread.currentThread().getContextClassLoader().getResource("utilData/emptyPayload.xml").getFile());
        System.out.println(is.available());
        Assert.assertTrue(is.available() == 0);

        is = new FileInputStream(Thread.currentThread().getContextClassLoader().getResource("utilData/somePayload.xml").getFile());
        System.out.println(is.available());
        Assert.assertTrue(is.available() > 0);
    }
}
