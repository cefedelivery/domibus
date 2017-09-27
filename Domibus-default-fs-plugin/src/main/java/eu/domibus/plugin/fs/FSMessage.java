package eu.domibus.plugin.fs;

import eu.domibus.plugin.fs.ebms3.UserMessage;

import java.util.Map;

/**
 * File System message
 * 
 * @author FERNANDES Henrique, GONCALVES Bruno
 */
public class FSMessage {

    private final Map<String, FSPayload> payloads;
    
    private final UserMessage metadata;
    
    /**
     * Creates a FSMessage with the given payloads and metadata
     * @param payloads The payloads map (contentId, payload)
     * @param metadata UserMessage metadata
     */
    public FSMessage(Map<String, FSPayload> payloads, UserMessage metadata) {
        this.payloads = payloads;
        this.metadata = metadata;
    }

    /**
     * @return The payload map (contentId, payload)
     */
    public Map<String, FSPayload> getPayloads() {
        return payloads;
    }

    /**
     * @return The UserMessage metadata
     */
    public UserMessage getMetadata() {
        return metadata;
    }

}
