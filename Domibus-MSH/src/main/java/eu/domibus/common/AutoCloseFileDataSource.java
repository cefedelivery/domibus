package eu.domibus.common;

import org.apache.commons.io.input.AutoCloseInputStream;

import javax.activation.FileDataSource;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author Cosmin Baciu
 * @since 3.3
 */
public class AutoCloseFileDataSource extends FileDataSource {

    public AutoCloseFileDataSource(String name) {
        super(name);
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return new AutoCloseInputStream(super.getInputStream());
    }
}
