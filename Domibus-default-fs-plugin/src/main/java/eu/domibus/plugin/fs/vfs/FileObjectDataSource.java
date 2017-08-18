package eu.domibus.plugin.fs.vfs;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.activation.DataSource;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;

/**
 *
 * @author @author FERNANDES Henrique, GONCALVES Bruno
 */
public class FileObjectDataSource implements DataSource
{
    private final FileObject file;

    public FileObjectDataSource(final FileObject file)
    {
        this.file = file;
    }

    @Override
    public InputStream getInputStream() throws IOException
    {
        return file.getContent().getInputStream();
    }

    @Override
    public OutputStream getOutputStream() throws IOException
    {
        return file.getContent().getOutputStream();
    }

    @Override
    public String getContentType()
    {
        try
        {
            return file.getContent().getContentInfo().getContentType();
        }
        catch (final FileSystemException e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getName()
    {
        return file.getName().getBaseName();
    }
}
