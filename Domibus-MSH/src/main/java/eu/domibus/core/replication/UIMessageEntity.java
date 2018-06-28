package eu.domibus.core.replication;

import eu.domibus.api.message.attempt.MessageAttemptStatus;
import eu.domibus.common.MessageStatus;
import eu.domibus.ebms3.common.model.AbstractBaseEntity;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "TB_UI_MESSAGE")
public class UIMessageEntity extends AbstractBaseEntity {

    @Column(name = "MESSAGE_ID")
    private String messageId;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS")
    private MessageStatus status;

    //TODO other columns
}
