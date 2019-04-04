package eu.domibus.core.message.fragment;

import eu.domibus.ebms3.common.model.AbstractBaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Entity class for storing the message header details as specified in the SplitAndJoin specs.
 *
 * @author Cosmin Baciu
 * @since 4.1
 */
@Entity
@Table(name = "TB_MESSAGE_HEADER")
public class MessageHeaderEntity extends AbstractBaseEntity {

    @Column(name = "BOUNDARY")
    String boundary;

    @Column(name = "START")
    String start;

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

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("boundary", boundary)
                .append("start", start)
                .toString();
    }
}
