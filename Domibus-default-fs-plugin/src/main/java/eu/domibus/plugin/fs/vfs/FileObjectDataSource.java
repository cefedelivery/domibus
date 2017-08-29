/*
 * This product includes software developed at
 * The Apache Software Foundation (http://www.apache.org/).
 *
 * The following software was modified for this product:
 *     Apache Commons VFS (Sandbox)
 *     Copyright 2002-2016 The Apache Software Foundation
 */
package eu.domibus.plugin.fs.vfs;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.activation.DataSource;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;

import eu.domibus.plugin.fs.exception.FSPluginException;

/**
 *
 * @author @author FERNANDES Henrique, GONCALVES Bruno
 */
public class FileObjectDataSource implements DataSource {

    private final FileObject file;

    public FileObjectDataSource(final FileObject file) {
        this.file = file;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return file.getContent().getInputStream();
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        return file.getContent().getOutputStream();
    }

    @Override
    public String getContentType() {
        try {
            return file.getContent().getContentInfo().getContentType();
        } catch (final FileSystemException e) {
            throw new FSPluginException("Could not retrieve content type from FileObject", e);
        }
    }

    @Override
    public String getName() {
        return file.getName().getBaseName();
    }
}
