package eu.domibus.plugin.fs;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

import eu.domibus.common.MessageStatus;

/**
 * Helper to create and recognize derived file names
 * 
 * @author FERNANDES Henrique, GONCALVES Bruno
 */
public class FSFileNameHelper {
    
    private static final String NAME_SEPARATOR = "_";
    private static final String UUID_PATTERN = "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}";
    private static final Pattern PROCESSED_FILE_PATTERN = Pattern.compile(
            UUID_PATTERN + ".*", Pattern.CASE_INSENSITIVE);
    private static final List<String> STATE_SUFFIXES;
    
    static {
        List<String> tempStateSuffixes = new LinkedList<>();
        for (MessageStatus status : MessageStatus.values()) {
            tempStateSuffixes.add(status.name());
        }
        
        STATE_SUFFIXES = Collections.unmodifiableList(tempStateSuffixes);
    }

    public static boolean isAnyState(final String fileName) {
        return StringUtils.endsWithAny(fileName, STATE_SUFFIXES.toArray(new String[0]));
    }
    
    public static boolean isProcessed(final String fileName) {
        return PROCESSED_FILE_PATTERN.matcher(fileName).matches();
    }
    
    public static String deriveFileName(final String fileName, final String messageId) {
        return messageId + NAME_SEPARATOR + fileName;
    }

}
