package eu.domibus.messaging;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.zip.GZIPOutputStream;

/**
 * Created by idragusa on 10/28/16.
 */
public class MessagingUtils {

    public static byte[] compress(String filename) throws IOException {
        return compress(Files.readAllBytes(Paths.get(filename)));
    }

    public static byte[] compress(byte[] data) throws IOException {
        final byte[] buffer = new byte[1024];
        InputStream sourceStream = new ByteArrayInputStream(data);
        ByteArrayOutputStream compressedContent = new ByteArrayOutputStream();
        GZIPOutputStream targetStream = new GZIPOutputStream(compressedContent);
        int i;
        while ((i = sourceStream.read(buffer)) > 0) {
            targetStream.write(buffer, 0, i);
        }
        sourceStream.close();
        targetStream.finish();
        targetStream.close();
        byte[] result = compressedContent.toByteArray();
        return result;
    }

}
