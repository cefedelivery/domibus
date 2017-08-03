package eu.domibus.plugin.fs;

import org.apache.commons.vfs2.FileObject;

import eu.domibus.plugin.fs.ebms3.UserMessage;

/**
 * File System message
 * 
 * @author FERNANDES Henrique, GONCALVES Bruno
 */
public class FSMessage {
    
    FileObject file;
    
    UserMessage metadata;

    public FSMessage() {
    }

    public FSMessage(FileObject file, UserMessage metadata) {
        this.file = file;
        this.metadata = metadata;
    }

    public FileObject getFile() {
        return file;
    }

    public void setFile(FileObject file) {
        this.file = file;
    }

    public UserMessage getMetadata() {
        return metadata;
    }

    public void setMetadata(UserMessage metadata) {
        this.metadata = metadata;
    }
    
}
