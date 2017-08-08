package eu.domibus.plugin.fs;

import org.apache.tika.Tika;
import org.apache.tika.mime.MimeType;
import org.apache.tika.mime.MimeTypeException;
import org.apache.tika.mime.MimeTypes;
import org.springframework.http.MediaType;

/**
 * Helper to convert between files, MIME types and extensions
 * 
 * @author FERNANDES Henrique, GONCALVES Bruno
 */
public class FSMimeTypeHelper {
    
    private static final Tika TIKA = new Tika();
    private static final MimeTypes MIME_TYPES = MimeTypes.getDefaultMimeTypes();
    
    public static String getMimeType(String fileName) {
        return TIKA.detect(fileName);
    }
    
    public static String getExtension(String mimeString) throws MimeTypeException {
        MimeType mimeType = MIME_TYPES.forName(mimeString);

        return mimeType.getExtension();
    }
    
    public static String fixMimeType(final String mimeType) {
        if (mimeType.equalsIgnoreCase(MediaType.APPLICATION_XML_VALUE)) {
            return MediaType.TEXT_XML_VALUE;
        } else {
            return mimeType;
        }
    }

}
