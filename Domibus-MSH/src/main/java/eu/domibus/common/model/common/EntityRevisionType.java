package eu.domibus.common.model.common;

import org.hibernate.envers.RevisionType;

import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

/**
 * @author Thomas Dussart
 * @since 4.0
 * <p>
 * This class is used to store the type of entity modified in hibernate and the type of modification.
 * The type of modification is also reflected in the different audit tables by {@link RevisionType}, but we
 * wanted to have it centralized in order to facilitate the audit log queries.
 */
@Embeddable
public class EntityRevisionType {

    /**
     * The logical name of the entity.
     */
    private String name;

    /**
     * The type of modification of the entity.
     */
    @Enumerated(EnumType.STRING)
    private ModificationType modificationType;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ModificationType getModificationType() {
        return modificationType;
    }

    public void setModificationType(ModificationType modificationType) {
        this.modificationType = modificationType;
    }
}
