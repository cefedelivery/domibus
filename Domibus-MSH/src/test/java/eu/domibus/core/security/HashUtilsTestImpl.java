package eu.domibus.core.security;

import eu.domibus.util.HashUtilImpl;
import org.junit.Assert;
import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;


/**
 * Created by idragusa on 10/14/16.
 */
public class HashUtilsTestImpl {

    @Test
    public void testSHA256Hash() throws NoSuchAlgorithmException, UnsupportedEncodingException {
        String password = "123456";
        String expected = "8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92";

        String hash = new HashUtilImpl().getSHA256Hash(password);
        System.out.println(hash);
        Assert.assertEquals(expected, hash);
    }
}
