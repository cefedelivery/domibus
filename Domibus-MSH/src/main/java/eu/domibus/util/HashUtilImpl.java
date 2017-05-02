package eu.domibus.util;

import eu.domibus.api.util.HashUtil;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Security;

/**
 * @author feriaad, Cosmin Baciu
 */
@Component
public class HashUtilImpl implements HashUtil {

    public static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

    /**
     * Returns the MD5 hash of the given String
     *
     * @param stringToBeHashed
     * @return the MD5 hash of the given string
     * @throws NoSuchAlgorithmException
     * @throws UnsupportedEncodingException
     */
    @Override
    public String getMD5Hash(String stringToBeHashed) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        return getHash(stringToBeHashed, "MD5");
    }

    /**
     * Returns the SHA224 hash of the given String
     *
     * @param stringToBeHashed
     * @return the SHA224 hash of the given string
     * @throws NoSuchAlgorithmException
     * @throws UnsupportedEncodingException
     */
    @Override
    public String getSHA224Hash(String stringToBeHashed) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        return getHash(stringToBeHashed, "SHA224");
    }
    
    /**
     * Returns the SHA256 hash of the given String
     *
     * @param stringToBeHashed
     * @return the SHA256 hash of the given string
     * @throws NoSuchAlgorithmException
     * @throws UnsupportedEncodingException
     */
    @Override
    public String getSHA256Hash(String stringToBeHashed) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        return getHash(stringToBeHashed, "SHA256");
    }
    
    /**
     * Returns the hash of the given String
     *
     * @param stringToBeHashed
     * @return the SHA224 hash of the given string
     * @throws NoSuchAlgorithmException
     * @throws UnsupportedEncodingException
     */
    private String getHash(String stringToBeHashed, String algorithm) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        if (Security.getProvider("BC") == null) {
            Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        }
        MessageDigest md = MessageDigest.getInstance(algorithm, new org.bouncycastle.jce.provider.BouncyCastleProvider());
        md.reset();
        md.update(stringToBeHashed.getBytes(DEFAULT_CHARSET));
        byte[] hashBytes = md.digest();

        //convert the byte to hex format method 2
        StringBuilder hexString = new StringBuilder();
        for (byte hashByte : hashBytes) {
            String hex = Integer.toHexString(0xff & hashByte);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
