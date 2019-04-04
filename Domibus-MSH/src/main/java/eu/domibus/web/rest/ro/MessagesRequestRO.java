package eu.domibus.web.rest.ro;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import eu.domibus.web.rest.JmsResource;
import eu.domibus.web.rest.validators.NotBlacklisted;

import java.util.Date;

/**
 * Created by musatmi on 15/05/2017.
 */
public class MessagesRequestRO {

    @NotBlacklisted
    private String source;
    @NotBlacklisted
    private String jmsType;

    private Date fromDate;
    private Date toDate;
    private String selector;

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getJmsType() {
        return jmsType;
    }

    public void setJmsType(String jmsType) {
        this.jmsType = jmsType;
    }

    public Date getFromDate() {
        return fromDate;
    }

    public void setFromDate(Date fromDate) {
        this.fromDate = fromDate;
    }

    public Date getToDate() {
        return toDate;
    }

    public void setToDate(Date toDate) {
        this.toDate = toDate;
    }

    public String getSelector() {
        return selector;
    }

    public void setSelector(String selector) {
        this.selector = selector;
    }

}
