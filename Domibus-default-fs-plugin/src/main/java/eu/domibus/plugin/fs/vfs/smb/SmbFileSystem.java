/*
 * This product includes software developed at
 * The Apache Software Foundation (http://www.apache.org/).
 *
 * The following software was modified for this product:
 *     Apache Commons VFS (Sandbox)
 *     Copyright 2002-2016 The Apache Software Foundation
 */
package eu.domibus.plugin.fs.vfs.smb;

import java.util.Collection;

import org.apache.commons.vfs2.Capability;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.provider.AbstractFileName;
import org.apache.commons.vfs2.provider.AbstractFileSystem;

/**
 * An SMB file system.
 *
 * @author FERNANDES Henrique, GONCALVES Bruno
 */
public class SmbFileSystem extends AbstractFileSystem {

    protected SmbFileSystem(final FileName rootName, final FileSystemOptions fileSystemOptions) {
        super(rootName, null, fileSystemOptions);
    }

    /**
     * Creates a file object.
     *
     * @param name name referencing the new file.
     * @return new created FileObject.
     * @throws FileSystemException if an error occurs.
     */
    @Override
    protected FileObject createFile(final AbstractFileName name) throws FileSystemException {
        return new SmbFileObject(name, this);
    }

    /**
     * Returns the capabilities of this file system.
     */
    @Override
    protected void addCapabilities(final Collection<Capability> caps) {
        caps.addAll(SmbFileProvider.CAPABILITIES);
    }
}
