/*
 * This product includes software developed at
 * The Apache Software Foundation (http://www.apache.org/).
 *
 * The following software was modified for this product:
 *     Apache Commons VFS (Sandbox)
 *     Copyright 2002-2016 The Apache Software Foundation
 */
package eu.domibus.plugin.fs.vfs.smb;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileNotFoundException;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.FileTypeHasNoContentException;
import org.apache.commons.vfs2.UserAuthenticationData;
import org.apache.commons.vfs2.provider.AbstractFileName;
import org.apache.commons.vfs2.provider.AbstractFileObject;
import org.apache.commons.vfs2.provider.UriParser;
import org.apache.commons.vfs2.util.UserAuthenticatorUtils;

import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileInputStream;
import jcifs.smb.SmbFileOutputStream;

/**
 * A file in an SMB file system.
 *
 * @author FERNANDES Henrique, GONCALVES Bruno
 */
public class SmbFileObject
        extends AbstractFileObject<SmbFileSystem> {

    private SmbFile file;

    protected SmbFileObject(final AbstractFileName name,
            final SmbFileSystem fileSystem) throws FileSystemException {
        super(name, fileSystem);
    }

    /**
     * Attaches this file object to its file resource.
     *
     * @throws Exception if an error occurs.
     */
    @Override
    protected void doAttach() throws Exception {
        // Defer creation of the SmbFile to here
        if (file == null) {
            file = createSmbFile(getName());
        }
    }

    @Override
    protected void doDetach() throws Exception {
        // file closed through content-streams
        file = null;
    }

    private SmbFile createSmbFile(final FileName fileName)
            throws MalformedURLException, SmbException, FileSystemException {
        final SmbFileName smbFileName = (SmbFileName) fileName;

        final String path = smbFileName.getUriWithoutAuth();

        UserAuthenticationData authData = null;
        SmbFile createdFile;
        try {
            authData = UserAuthenticatorUtils.authenticate(
                    getFileSystem().getFileSystemOptions(),
                    SmbFileProvider.getAuthenticatorTypes());

            NtlmPasswordAuthentication auth = null;
            if (authData != null) {
                auth = new NtlmPasswordAuthentication(
                        UserAuthenticatorUtils.toString(
                                UserAuthenticatorUtils.getData(authData, UserAuthenticationData.DOMAIN,
                                        UserAuthenticatorUtils.toChar(smbFileName.getDomain()))),
                        UserAuthenticatorUtils.toString(
                                UserAuthenticatorUtils.getData(authData, UserAuthenticationData.USERNAME,
                                        UserAuthenticatorUtils.toChar(smbFileName.getUserName()))),
                        UserAuthenticatorUtils.toString(
                                UserAuthenticatorUtils.getData(authData, UserAuthenticationData.PASSWORD,
                                        UserAuthenticatorUtils.toChar(smbFileName.getPassword()))));
            }

            // if auth == null SmbFile uses default credentials
            // ("jcifs.smb.client.domain", "?"), ("jcifs.smb.client.username", "GUEST"),
            // ("jcifs.smb.client.password", BLANK)
            // ANONYMOUS=("","","")
            createdFile = new SmbFile(path, auth);

            if (createdFile.isDirectory() && !createdFile.toString().endsWith("/")) {
                createdFile = new SmbFile(path + "/", auth);
            }
            return createdFile;
        } finally {
            UserAuthenticatorUtils.cleanup(authData); // might be null
        }
    }

    /**
     * Determines the type of the file, returns null if the file does not exist.
     *
     * @return the type of the file.
     * @throws Exception if an error occurs.
     */
    @Override
    protected FileType doGetType() throws Exception {
        if (!file.exists()) {
            return FileType.IMAGINARY;
        } else if (file.isDirectory()) {
            return FileType.FOLDER;
        } else if (file.isFile()) {
            return FileType.FILE;
        }

        throw new FileSystemException("eu.domibus.plugin.fs.vfs.provider.smb/get-type.error", getName());
    }

    /**
     * Lists the children of the file. Is only called if {@link #doGetType}
     * returns {@link FileType#FOLDER}.
     *
     * @return a possible empty String array if the file is a directory or null
     * or an exception if the file is not a directory or can't be read.
     * @throws Exception if an error occurs.
     */
    @Override
    protected String[] doListChildren() throws Exception {
        // VFS-210: do not try to get listing for anything else than directories
        if (!file.isDirectory()) {
            return ArrayUtils.EMPTY_STRING_ARRAY;
        }

        return UriParser.encode(file.list());
    }

    /**
     * Determines if this file is hidden.
     *
     * @return true if the file is hidden, false otherwise.
     * @throws Exception if an error occurs.
     */
    @Override
    protected boolean doIsHidden() throws Exception {
        return file.isHidden();
    }

    /**
     * Deletes the file.
     *
     * @throws Exception if an error occurs
     */
    @Override
    protected void doDelete() throws Exception {
        file.delete();
    }

    @Override
    protected void doRename(final FileObject newfile) throws Exception {
        file.renameTo(createSmbFile(newfile.getName()));
    }

    /**
     * Creates this file as a folder.
     *
     * @throws Exception if an error occurs.
     */
    @Override
    protected void doCreateFolder() throws Exception {
        file.mkdir();
        file = createSmbFile(getName());
    }

    /**
     * Returns the size of the file content (in bytes).
     *
     * @return The size of the file in bytes.
     * @throws Exception if an error occurs.
     */
    @Override
    protected long doGetContentSize() throws Exception {
        return file.length();
    }

    /**
     * Returns the last modified time of this file.
     *
     * @return The last modification time.
     * @throws Exception if an error occurs.
     */
    @Override
    protected long doGetLastModifiedTime()
            throws Exception {
        return file.getLastModified();
    }

    /**
     * Creates an input stream to read the file content from.
     *
     * @return An InputStream to read the file content.
     * @throws Exception if an error occurs.
     */
    @Override
    protected InputStream doGetInputStream() throws Exception {
        try {
            return new SmbFileInputStream(file);
        } catch (final SmbException e) {
            // See https://msdn.microsoft.com/en-us/library/ee441884.aspx
            // In particular, information for ERRbadfile
            if (e.getNtStatus() == SmbException.NT_STATUS_NO_SUCH_FILE
                    || e.getNtStatus() == SmbException.NT_STATUS_NO_SUCH_DEVICE
                    || e.getNtStatus() == SmbException.NT_STATUS_OBJECT_NAME_NOT_FOUND) {
                throw new FileNotFoundException(getName());
            } else if (file.isDirectory()) {
                throw new FileTypeHasNoContentException(getName());
            }

            throw e;
        }
    }

    /**
     * Creates an output stream to write the file content to.
     *
     * @param bAppend true if the file should be appended to, false if it should
     * be overwritten.
     * @return An OutputStream to write to the file.
     * @throws Exception if an error occurs.
     */
    @Override
    protected OutputStream doGetOutputStream(final boolean bAppend) throws Exception {
        return new SmbFileOutputStream(file, bAppend);
    }

    @Override
    protected boolean doSetLastModifiedTime(final long modtime) throws Exception {
        file.setLastModified(modtime);
        return true;
    }
}
