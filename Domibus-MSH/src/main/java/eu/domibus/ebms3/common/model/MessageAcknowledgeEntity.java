package eu.domibus.ebms3.common.model;

import eu.domibus.common.xmladapter.XMLGregorianCalendarAdapter;

import javax.persistence.*;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.Date;

/**
 * @author baciu
 */
@Entity
@Table(name = "TB_MESSAGE_ACKNOWLEDGE")
public class MessageAcknowledgeEntity extends AbstractBaseEntity {

    @Column(name = "MESSAGE_ID")
    protected String messageId;

    @Column(name = "USERNAME")
    private String username;

    @Column(name = "ORIGINAL_USER")
    private String originalUser;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "CREATE_DATE")
    protected Date createDate;
}
