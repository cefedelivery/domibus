package eu.domibus.common.model.common;

import eu.domibus.common.listener.CustomRevisionEntityListener;
import org.hibernate.envers.DefaultRevisionEntity;
import org.hibernate.envers.DefaultTrackingModifiedEntitiesRevisionEntity;
import org.hibernate.envers.ModifiedEntityNames;
import org.hibernate.envers.RevisionEntity;

import javax.persistence.*;
import java.util.Date;
import java.util.Set;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
@Entity
@Table(name = "TB_REV_INFO")
@RevisionEntity( CustomRevisionEntityListener.class )
public class RevisionLog  extends DefaultTrackingModifiedEntitiesRevisionEntity {
    private String userName;

    private Date revisionDate;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    @Override
    public Date getRevisionDate() {
        return revisionDate;
    }

    public void setRevisionDate(Date revisionDate) {
        this.revisionDate = revisionDate;
    }

    @ElementCollection
    @JoinTable(name = "TB_REVCHANGES", joinColumns = @JoinColumn(name = "REV"))
    @Column(name = "ENTITYNAME")
    @ModifiedEntityNames
    private Set<String> modifiedEntityNames;

}
