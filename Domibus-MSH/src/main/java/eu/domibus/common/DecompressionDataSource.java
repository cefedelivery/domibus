/*
 * Copyright 2015 e-CODEX Project
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they
 * will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the
 * Licence.
 * You may obtain a copy of the Licence at:
 * http://ec.europa.eu/idabc/eupl5
 * Unless required by applicable law or agreed to in
 * writing, software distributed under the Licence is
 * distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied.
 * See the Licence for the specific language governing
 * permissions and limitations under the Licence.
 */

package eu.domibus.common;

import javax.activation.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;

/**
 * @author Christian Koch, Stefan Mueller
 */
public class DecompressionDataSource implements DataSource {

    private final DataSource source;
    private final String mime;

    public DecompressionDataSource(final DataSource source, final String mime) {
        this.source = source;
        this.mime = mime;
    }


    @Override
    public InputStream getInputStream() throws IOException {
        return new GZIPInputStream(source.getInputStream());
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getContentType() {
        return mime;
    }

    @Override
    public String getName() {
        return "decompressionDataSource - " + source.getName();
    }
}
