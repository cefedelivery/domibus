package eu.domibus.plugin.fs;

import eu.domibus.plugin.fs.ebms3.UserMessage;
import org.apache.commons.vfs2.FileObject;

/**
 * File System message
 * 
 * @author FERNANDES Henrique, GONCALVES Bruno
 */
public class FSMessage {

    private final FileObject file;
    
    private final UserMessage metadata;

    /**
     * Creates a FSMessage with the given file and metadata
     * @param file The file object
     * @param metadata UserMessage metadata
     */
    public FSMessage(FileObject file, UserMessage metadata) {
        this.file = file;
        this.metadata = metadata;
    }

    public FileObject getFile() {
        return file;
    }

    public UserMessage getMetadata() {
        return metadata;
    }

}
