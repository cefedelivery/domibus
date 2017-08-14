package eu.domibus.plugin.fs;

import eu.domibus.plugin.fs.ebms3.UserMessage;

import javax.activation.DataHandler;
import java.util.Map;

/**
 * File System message
 * 
 * @author FERNANDES Henrique, GONCALVES Bruno
 */
public class FSMessage {

    private final Map<String, DataHandler> dataHandlers;
    
    private final UserMessage metadata;
    
    /**
     * Creates a FSMessage with the given data handlers and metadata
     * @param dataHandlers  The data handler map (contentId, payload)
     * @param metadata UserMessage metadata
     */
    public FSMessage(Map<String, DataHandler> dataHandlers, UserMessage metadata) {
        this.dataHandlers = dataHandlers;
        this.metadata = metadata;
    }

    /**
     * @return The data handler map (contentId, payload)
     */
    public Map<String, DataHandler> getDataHandlers() {
        return dataHandlers;
    }

    /**
     * @return The UserMessage metadata
     */
    public UserMessage getMetadata() {
        return metadata;
    }

}
