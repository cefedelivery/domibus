package eu.domibus.ebms3.common.model;

import eu.domibus.common.xmladapter.ToStringAdapter;
import org.w3c.dom.Element;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.ArrayList;
import java.util.List;

/**
 * The Header element encapsulates the whole message header
 * including the eb:Messaging (UserMessage or SignalMessage) and the wsse:Security header.
 *
 * @author idragusa
 * @since 3.2.5
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SecurityHeader", propOrder = "any")
@Entity
@Table(name = "TB_SECURITYHEADER")
public class SecurityHeader extends AbstractBaseEntity {
    @SuppressWarnings("JpaAttributeTypeInspection")
    @XmlAnyElement(lax = false)
    @XmlJavaTypeAdapter(ToStringAdapter.class)
    @ElementCollection
    @Lob
    @CollectionTable(name = "TB_SECURITYHEADER_DATA", joinColumns = @JoinColumn(name = "SECURITYHEADER_ID"))
    @Column(name = "RAW_XML")
    protected List<String> any;

    /**
     * Gets the value of the any property.
     * <p/>
     * <p/>
     * This accessor method returns a reference to the live list, not a
     * snapshot. Therefore any modification you make to the returned list will
     * be present inside the JAXB object. This is why there is not a
     * <CODE>set</CODE> method for the any property.
     * <p/>
     * <p/>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAny().add(newItem);
     * </pre>
     * <p/>
     * <p/>
     * <p/>
     * Objects of the following type(s) are allowed in the list null null     {@link Object }
     * {@link Element }
     */
    public List<String> getAny() {
        if (this.any == null) {
            this.any = new ArrayList<>();
        }
        return this.any;
    }
}
