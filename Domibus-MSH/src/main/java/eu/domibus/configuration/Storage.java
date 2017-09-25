package eu.domibus.configuration;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlTransient;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * @version 2.0
 * @Author Ioana Dragusanu
 * @Author Martini Federico
 */
@Component
public class Storage {

    public static final String ATTACHMENT_STORAGE_LOCATION = "domibus.attachment.storage.location";

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(Storage.class);

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
            if (storageDirectory == null) {
                Path path = createLocation(location);
                if (path != null) {
                    storageDirectory = path.toFile();
                    LOG.info("Initialized payload folder on path [{}]", path);
                } else {
                    LOG.warn("There was an error initializing the payload folder, so Domibus will be using the database");
                }
            }
        } else {
            LOG.warn("No file system storage defined. This is fine for small attachments but might lead to database issues when processing large payloads");
            storageDirectory = null;
        }
    }

    /**
     * It attempts to create the directory whenever is not present.
     * It works also when the location is a symbolic link.
     *
     * @param path
     * @return Path
     */
    private Path createLocation(String path) {
        try {
            Path payloadPath = FileSystems.getDefault().getPath(path).normalize().toAbsolutePath();
            // Checks if the path exists, if not it creates it
            if (Files.notExists(payloadPath)) {
                Files.createDirectories(payloadPath);
                LOG.debug(payloadPath.toAbsolutePath() + "has been created!");
            }
            if (Files.isSymbolicLink(payloadPath)) {
                payloadPath = Files.readSymbolicLink(payloadPath);
            }
            return payloadPath;
        } catch (IOException ioEx) {
            LOG.error("Error creating/accessing the payload folder [{}]", path, ioEx);
        }
        return null;
    }

}
