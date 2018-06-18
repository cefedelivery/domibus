/*
 * This product includes software developed at
 * The Apache Software Foundation (http://www.apache.org/).
 *
 * The following software was modified for this product:
 *     Apache Commons VFS (Sandbox)
 *     Copyright 2002-2016 The Apache Software Foundation
 */
package eu.domibus.plugin.fs.vfs.smb;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.apache.commons.vfs2.Capability;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileSystem;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.UserAuthenticationData;
import org.apache.commons.vfs2.provider.AbstractOriginatingFileProvider;
import org.apache.commons.vfs2.provider.FileProvider;

/**
 * A provider for SMB (Samba, Windows share) file systems.
 *
 * @author FERNANDES Henrique, GONCALVES Bruno
 */
public class SmbFileProvider
        extends AbstractOriginatingFileProvider
        implements FileProvider {

    /**
     * Authentication data supported by this provider.
     */
    private static final UserAuthenticationData.Type[] AUTHENTICATOR_TYPES = new UserAuthenticationData.Type[]{
        UserAuthenticationData.USERNAME, UserAuthenticationData.PASSWORD, UserAuthenticationData.DOMAIN
    };

    static final Collection<Capability> CAPABILITIES
            = Collections.unmodifiableCollection(Arrays.asList(Capability.CREATE,
            Capability.DELETE,
            Capability.RENAME,
            Capability.GET_TYPE,
            Capability.GET_LAST_MODIFIED,
            Capability.SET_LAST_MODIFIED_FILE,
            Capability.SET_LAST_MODIFIED_FOLDER,
            Capability.LIST_CHILDREN,
            Capability.READ_CONTENT,
            Capability.URI,
            Capability.WRITE_CONTENT,
            Capability.APPEND_CONTENT));

    public SmbFileProvider() {
        super();
        setFileNameParser(SmbFileNameParser.getInstance());
    }

    /**
     * Creates the filesystem.
     *
     * @param name The name of the root file of the file system to create.
     * @param fileSystemOptions The FileSystem options.
     * @return The FileSystem.
     * @throws FileSystemException if an error occurs.
     */
    @Override
    protected FileSystem doCreateFileSystem(final FileName name, final FileSystemOptions fileSystemOptions)
            throws FileSystemException {
        return new SmbFileSystem(name, fileSystemOptions);
    }

    @Override
    public Collection<Capability> getCapabilities() {
        return CAPABILITIES;
    }

    static UserAuthenticationData.Type[] getAuthenticatorTypes() {
        return AUTHENTICATOR_TYPES;
    }
}
