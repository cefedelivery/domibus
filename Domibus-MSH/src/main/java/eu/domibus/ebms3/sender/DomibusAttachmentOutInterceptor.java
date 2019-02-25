package eu.domibus.ebms3.sender;

import org.apache.cxf.interceptor.AttachmentOutInterceptor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class DomibusAttachmentOutInterceptor extends AttachmentOutInterceptor {

    protected static final Map<String, List<String>> headers = new HashMap<>();
    public static final String CONTENT_ID_SOURCE_MESSAGE = "split.root.message@cxf.apache.org";


    public DomibusAttachmentOutInterceptor() {
        final ArrayList<String> headers = new ArrayList<>();
        headers.add(CONTENT_ID_SOURCE_MESSAGE);
        DomibusAttachmentOutInterceptor.headers.put("Content-ID", headers);
    }

    @Override
    protected Map<String, List<String>> getRootHeaders() {
        return headers;
    }
}
