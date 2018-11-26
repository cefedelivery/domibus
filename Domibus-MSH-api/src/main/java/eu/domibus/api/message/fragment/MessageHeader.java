package eu.domibus.api.message.fragment;

/**
 * @author Cosmin Baciu
 * @since 4.1
 */
public class MessageHeader {

    protected String boundary;

    protected String start;

    public String getBoundary() {
        return boundary;
    }

    public void setBoundary(String boundary) {
        this.boundary = boundary;
    }

    public String getStart() {
        return start;
    }

    public void setStart(String start) {
        this.start = start;
    }
}
