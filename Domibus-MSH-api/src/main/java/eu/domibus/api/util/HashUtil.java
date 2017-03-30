package eu.domibus.api.util;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

/**
 * @author Cosmin Baciu
 * @since 3.3
 */
public interface HashUtil {

    String getMD5Hash(String stringToBeHashed) throws NoSuchAlgorithmException, UnsupportedEncodingException;

    String getSHA224Hash(String stringToBeHashed) throws NoSuchAlgorithmException, UnsupportedEncodingException;

    String getSHA256Hash(String stringToBeHashed) throws NoSuchAlgorithmException, UnsupportedEncodingException;
}
