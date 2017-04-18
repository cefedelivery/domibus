package eu.domibus.api.message.ebms3.model;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlTransient;

/**
 * Base type for entity
 *
 * For convenience we are using the same base entity as domibus core
 */
@XmlTransient
@MappedSuperclass
public abstract class AbstractBaseEntity {

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
