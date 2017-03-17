package eu.domibus.acknowledge.entities;

import eu.domibus.ebms3.common.model.AbstractBaseEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Created by migueti on 16/03/2017.
 */
@Entity
@Table(name = "tb_message_acknowledge_property")
public class MessageAcknowledgeProperty extends AbstractBaseEntity {

    @Column(name = "ID_PK")
    Long propertyId;
}
