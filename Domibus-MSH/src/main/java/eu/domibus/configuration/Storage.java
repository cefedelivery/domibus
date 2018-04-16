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
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
                Path path = pathValidation(location);
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
    private Path pathValidation(String path) {
        Path payloadPath = null;
        try {
            payloadPath = Paths.get(path).normalize();
            // Checks if the path exists, if not it creates it
            if (Files.notExists(payloadPath)) {
                try {
                    Files.createDirectories(payloadPath);
                    LOG.info(payloadPath.toAbsolutePath() + " has been created!");
                } catch (FileSystemException exc) {
                    LOG.error("Error creating/accessing the payload folder [{}]", path, exc);

                    // Takes temporary folder by default if it faces any issue while creating defined path.
                    payloadPath = Paths.get(System.getProperty("java.io.tmpdir"));
                    LOG.info("The temporary path " + payloadPath.toAbsolutePath() + " has been selected!");
                }
            }

            if (Files.isSymbolicLink(payloadPath)) {
                payloadPath = Files.readSymbolicLink(payloadPath);
            }

            if (!Files.isWritable(payloadPath)) {
                throw new IOException("Write permission for payload path is not granted.");
            }
            return payloadPath;
        } catch (IOException ioEx) {
            LOG.error("Error creating/accessing the payload folder [{}]", path, ioEx);

            // Takes temporary folder by default if it faces any issue while creating defined path.
            payloadPath = Paths.get(System.getProperty("java.io.tmpdir"));
            LOG.info("The temporary path " + payloadPath.toAbsolutePath() + " has been selected!");
        }
        return null;
    }

}
