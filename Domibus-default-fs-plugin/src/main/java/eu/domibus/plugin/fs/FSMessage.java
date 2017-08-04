package eu.domibus.plugin.fs;

import javax.activation.DataHandler;

import eu.domibus.plugin.fs.ebms3.UserMessage;

/**
 * File System message
 * 
 * @author FERNANDES Henrique, GONCALVES Bruno
 */
public class FSMessage {

    private final DataHandler dataHandler;
    
    private final UserMessage metadata;
    
    /**
     * Creates a FSMessage with the given data handler and metadata
     * @param dataHandler  The data handler
     * @param metadata UserMessage metadata
     */
    public FSMessage(DataHandler dataHandler, UserMessage metadata) {
        this.dataHandler = dataHandler;
        this.metadata = metadata;
    }
    
    public DataHandler getDataHandler() {
        return dataHandler;
    }

    public UserMessage getMetadata() {
        return metadata;
    }

}
