package eu.domibus.acknowledge.entities;

import eu.domibus.ebms3.common.model.AbstractBaseEntity;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.Set;

/**
 * Created by migueti on 15/03/2017.
 */
@Entity
@Table(name = "tb_message_acknowledge")
public class MessageAcknowledge extends AbstractBaseEntity {

    @Column(name = "ID_PK")
    private String id;

    @Column(name = "FK_MESSAGE_ID")
    private String messageId;

    @Column(name = "FROM")
    private String from;

    @Column(name = "TO")
    private String to;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "FK_")
    private Set<MessageAcknowledgeProperty> properties;


}
