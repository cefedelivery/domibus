package eu.domibus.plugin.fs;

import javax.activation.DataHandler;

/**
 * Represents an AS4 payload
 * 
 * @author FERNANDES Henrique, GONCALVES Bruno
 */
public class FSPayload {
    
    private final String mimeType;
    
    private final DataHandler dataHandler;

    /**
     * Creates a new FSPayload
     * @param mimeType the payload MIME type
     * @param dataHandler the payload DataHandler
     */
    public FSPayload(String mimeType, DataHandler dataHandler) {
        this.mimeType = mimeType;
        this.dataHandler = dataHandler;
    }

    /**
     * @return the payload MIME type
     */
    public String getMimeType() {
        return mimeType;
    }

    /**
     * @return the payload DataHandler
     */
    public DataHandler getDataHandler() {
        return dataHandler;
    }
    
}
