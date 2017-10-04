package eu.domibus.security;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Thomas Dussart
 * @since 3.3
 */

public class DefaultCredentials {
    private final static String USER="user";
    private final static String ADMIN="admin";
    private final static String USER_PWD="123456"; //NOSONAR
    private final static String ADMIN_PWD="123456"; //NOSONAR
    private final static Map<String,String> DEFAULT_CREDENTIALS;

    static {
        DEFAULT_CREDENTIALS=new HashMap<>();
        DEFAULT_CREDENTIALS.put(USER,USER_PWD);
        DEFAULT_CREDENTIALS.put(ADMIN,ADMIN_PWD);
    }

    public static String getDefaultPasswordForUser(final String userName){
        return DEFAULT_CREDENTIALS.get(userName);
    }
}
