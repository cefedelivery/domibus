package eu.domibus.plugin.fs;

import javax.activation.DataHandler;

import eu.domibus.plugin.fs.ebms3.UserMessage;

import java.util.List;

/**
 * File System message
 * 
 * @author FERNANDES Henrique, GONCALVES Bruno
 */
public class FSMessage {

    private final List<DataHandler> dataHandlers;
    
    private final UserMessage metadata;
    
    /**
     * Creates a FSMessage with the given data handlers and metadata
     * @param dataHandlers  The data handler list (one per payload)
     * @param metadata UserMessage metadata
     */
    public FSMessage(List<DataHandler> dataHandlers, UserMessage metadata) {
        this.dataHandlers = dataHandlers;
        this.metadata = metadata;
    }
    
    public List<DataHandler> getDataHandlers() {
        return dataHandlers;
    }

    public UserMessage getMetadata() {
        return metadata;
    }

}
