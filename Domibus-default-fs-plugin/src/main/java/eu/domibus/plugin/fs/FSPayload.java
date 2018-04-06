package eu.domibus.plugin.fs;

import javax.activation.DataHandler;

/**
 * Represents an AS4 payload
 * 
 * @author FERNANDES Henrique, GONCALVES Bruno
 */
public class FSPayload {
    
    private final String mimeType;

    private final String fileName;
    
    private final DataHandler dataHandler;

    /**
     * Creates a new FSPayload
     * @param mimeType the payload MIME type
     * @param fileName the payload file name
     * @param dataHandler the payload DataHandler
     */
    public FSPayload(String mimeType, String fileName, DataHandler dataHandler) {
        this.mimeType = mimeType;
        this.fileName = fileName;
        this.dataHandler = dataHandler;
    }

    /**
     * @return the payload MIME type
     */
    public String getMimeType() {
        return mimeType;
    }

    /**
     * @return the payload file name
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * @return the payload DataHandler
     */
    public DataHandler getDataHandler() {
        return dataHandler;
    }
    
}
