package eu.domibus.api.util;

public interface DomibusStringUtil {

    static String uncamelcase(String str) {
        String result = str.replaceAll("(\\p{Ll})(\\p{Lu})","$1 $2");
        return result.substring(0,1).toUpperCase() + result.substring(1);
    }
}
