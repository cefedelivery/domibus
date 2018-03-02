package eu.domibus.plugin.fs.worker;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.plugin.fs.FSFilesManager;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author FERNANDES Henrique, GONCALVES Bruno
 */
@Service
public class FSPurgeReceivedService extends FSAbstractPurgeService {
    
    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(FSPurgeReceivedService.class);
    
    @Override
    public void purgeMessages() {
        LOG.debug("Purging received file system messages...");

        super.purgeMessages();
    }

    @Override
    public FileObject[] findAllDescendants(final FileObject targetFolder) throws FileSystemException {

        List<FileObject> allDescendants = new ArrayList<>();

        FileObject[] level1Folders = targetFolder.getChildren();
        for (FileObject level1Folder : level1Folders) {
            if (FileType.FOLDER.equals(level1Folder.getType())) {

                //go to messageID/level2 folders
                FileObject[] level2Folders = level1Folder.getChildren();
                for (FileObject level2Folder : level2Folders) {
                    if (FileType.FOLDER.equals(level2Folder.getType())) {
                        allDescendants.add(level2Folder);
                    }

                }
            }
        }

        return allDescendants.toArray(new FileObject[0]);
    }

    @Override
    protected String getTargetFolderName() {
        return FSFilesManager.INCOMING_FOLDER;
    }

    @Override
    protected Integer getExpirationLimit(String domain) {
        return fsPluginProperties.getReceivedPurgeExpired(domain);
    }

}
