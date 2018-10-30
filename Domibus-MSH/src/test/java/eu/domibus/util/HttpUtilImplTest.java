package eu.domibus.util;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.junit.Assert.*;

public class HttpUtilImplTest {

    @Test
    public void name() throws IOException, InterruptedException {
        HttpUtilImpl httpUtil = new HttpUtilImpl();

        ByteArrayInputStream byteArrayInputStream = httpUtil.downloadURLViaProxy("http://crl.sbca.telesec.de/rl/TeleSec_Business_CA_1.crl", "158.169.9.13", 8012, "baciuco", "Trutica2");
        System.out.println(byteArrayInputStream);

//        Thread.sleep(20 * 1000L);
//
//        byteArrayInputStream = httpUtil.downloadURLViaProxy("http://crl.sbca.telesec.de/rl/TeleSec_Business_CA_1.crl", "158.169.9.13", 8012, "baciuco", "Trutica2");
//        System.out.println(byteArrayInputStream);

    }
}