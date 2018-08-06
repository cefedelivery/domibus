package eu.domibus.configuration;

import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.common.util.WarningUtil;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @version 2.0
 * @author Ioana Dragusanu
 * @author Martini Federico
 */
@Component
public class Storage {

    public static final String ATTACHMENT_STORAGE_LOCATION = "domibus.attachment.storage.location";

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(Storage.class);

    private File storageDirectory = null;

    @Autowired
    protected DomibusPropertyProvider domibusPropertyProvider;

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
        final String location = domibusPropertyProvider.getProperty(ATTACHMENT_STORAGE_LOCATION);
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
        Path payloadPath = null;
        try {
            payloadPath = Paths.get(path).normalize();
            // Checks if the path exists, if not it creates it
            if (Files.notExists(payloadPath)) {
                Files.createDirectories(payloadPath);
                LOG.info("The payload folder " + payloadPath.toAbsolutePath() + " has been created!");
            } else {
                if (Files.isSymbolicLink(payloadPath)) {
                    payloadPath = Files.readSymbolicLink(payloadPath);
                }

                if (!Files.isWritable(payloadPath)) {
                    throw new IOException("Write permission for payload folder " + payloadPath.toAbsolutePath() + " is not granted.");
                }
            }
        } catch (IOException ioEx) {
            LOG.error("Error creating/accessing the payload folder [{}]", path, ioEx);

            // Takes temporary folder by default if it faces any issue while creating defined path.
            payloadPath = Paths.get(System.getProperty("java.io.tmpdir"));
            LOG.warn(WarningUtil.warnOutput("The temporary payload folder " + payloadPath.toAbsolutePath() + " has been selected!"));
        }
        return payloadPath;
    }

}
