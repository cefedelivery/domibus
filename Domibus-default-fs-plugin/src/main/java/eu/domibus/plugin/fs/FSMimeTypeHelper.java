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
    
    /**
     * Detects the MIME type of a document with the given file name.
     * @param fileName the file name of the document
     * @return detected MIME type
     */
    public static String getMimeType(String fileName) {
        return TIKA.detect(fileName);
    }
    
    /**
     * Returns the preferred file extension for the given MIME type, or an empty
     * string if no extensions are known.
     * @param mimeString the MIME type
     * @return preferred file extension or empty string
     * @throws MimeTypeException if the given media type name is invalid
     */
    public static String getExtension(String mimeString) throws MimeTypeException {
        MimeType mimeType = MIME_TYPES.forName(mimeString);

        return mimeType.getExtension();
    }
    
    /**
     * Fixes XML MIME type or passes MIME type through. Returns {@code text/xml}
     * if {@code mimeType} = {@code application/xml} otherwise passes value through.
     * @param mimeType the MIME type to fix, if needed
     * @return {@code text/xml} if {@code mimeType} = {@code application/xml},
     * {@code mimeType} otherwise
     */
    public static String fixMimeType(final String mimeType) {
        if (mimeType.equalsIgnoreCase(MediaType.APPLICATION_XML_VALUE)) {
            return MediaType.TEXT_XML_VALUE;
        } else {
            return mimeType;
        }
    }

}
