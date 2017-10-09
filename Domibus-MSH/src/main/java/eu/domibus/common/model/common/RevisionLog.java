package eu.domibus.common.model.common;

import eu.domibus.common.listener.CustomRevisionEntityListener;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.envers.DefaultRevisionEntity;
import org.hibernate.envers.RevisionEntity;

import javax.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Own implementation of hibernate-envers Revision entity, in order to store the user and the modification type.
 *
 * @author Thomas Dussart
 * @since 4.0
 */
@Entity
@Table(name = "TB_REV_INFO")
@RevisionEntity( CustomRevisionEntityListener.class )
public class RevisionLog extends DefaultRevisionEntity {
    /**
     * User involve in this modification
     */
    private String userName;

    /**
     * Date of the modification.
     */
    private Date revisionDate;

    @ElementCollection(fetch = FetchType.EAGER)
    @JoinTable(name = "TB_REV_CHANGES", joinColumns = @JoinColumn(name = "REV"))
    @Fetch(FetchMode.JOIN)
    private Set<EntityRevisionType> revisionTypes = new HashSet<>();

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

    public void addEntityRevisionType(final String name, final ModificationType modificationType) {
        EntityRevisionType entityRevisionType = new EntityRevisionType();
        entityRevisionType.setName(name);
        entityRevisionType.setModificationType(modificationType);
        this.revisionTypes.add(entityRevisionType);
    }







}
