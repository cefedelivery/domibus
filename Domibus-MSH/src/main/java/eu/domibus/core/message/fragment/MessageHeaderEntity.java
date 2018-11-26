package eu.domibus.core.message.fragment;

import eu.domibus.ebms3.common.model.AbstractBaseEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
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



}
