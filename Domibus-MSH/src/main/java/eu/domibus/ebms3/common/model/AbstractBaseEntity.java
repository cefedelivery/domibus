package eu.domibus.ebms3.common.model;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlTransient;
import java.io.Serializable;

/**
 * Base type for entity
 *
 * For convenience we are using the same base entity as domibus core
 */
@XmlTransient
@MappedSuperclass
public abstract class AbstractBaseEntity implements Serializable {

    @Id
    @XmlTransient
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ID_PK")
    private int entityId;

    /**
     * @return the primary key of the entity
     */
    public int getEntityId() {
        return this.entityId;
    }


    public void setEntityId(int entityId) {
        this.entityId = entityId;
    }

    @Override
    public int hashCode() {
        //noinspection NonFinalFieldReferencedInHashCode
        return this.entityId;
    }

    @Override
    public boolean equals(final Object other) {
        //noinspection NonFinalFieldReferenceInEquals
        return ((other != null) &&
                this.getClass().equals(other.getClass()) &&
                (this.entityId == ((AbstractBaseEntity) other).entityId));
    }
}
