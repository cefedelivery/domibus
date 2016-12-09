package eu.domibus.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlTransient;
import java.io.File;
import java.util.Properties;

/**
 * Created by idragusa on 5/12/16.
 */
@Component
public class Storage {

    public static final String ATTACHMENT_STORAGE_LOCATION = "domibus.attachment.storage.location";
    private static final Logger LOG = LoggerFactory.getLogger(Storage.class);
    private File storageDirectory = null;
    @Transient
    @XmlTransient
    @Autowired
    @Qualifier("domibusProperties")
    private Properties domibusProperties;

    public Storage() {
        storageDirectory = null;
    }
    public Storage(File storageDirectory) {
        this.storageDirectory = storageDirectory;
    }

    public void setStorageDirectory(File storageDirectory) {
        this.storageDirectory = storageDirectory;
    }

    public File getStorageDirectory() {
        return storageDirectory;
    }

    @PostConstruct
    public void initFileSystemStorage() {
        final String location = domibusProperties.getProperty(ATTACHMENT_STORAGE_LOCATION);
        if (location != null && !location.isEmpty()) {
            if(storageDirectory == null) {
                storageDirectory = new File(location);
                if (!storageDirectory.exists()) {
                    throw new IllegalArgumentException("The configured storage location " + storageDirectory.getAbsolutePath() + " does not exist");
                }
            }
        } else {
            LOG.warn("No file system storage defined. This is fine for small attachments but might lead to database issues when processing large payloads");
            storageDirectory = null;
        }
    }
}
