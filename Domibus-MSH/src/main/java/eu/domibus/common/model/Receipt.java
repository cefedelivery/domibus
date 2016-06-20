/*
 * Copyright 2015 e-CODEX Project
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they
 * will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the
 * Licence.
 * You may obtain a copy of the Licence at:
 * http://ec.europa.eu/idabc/eupl5
 * Unless required by applicable law or agreed to in
 * writing, software distributed under the Licence is
 * distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied.
 * See the Licence for the specific language governing
 * permissions and limitations under the Licence.
 */

package eu.domibus.common.model;

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
 * The eb:Receipt element MAY occur zero or one times; and, if present, SHOULD contain a single
 * ebbpsig:NonRepudiationInformation child element, as defined in the ebBP Signal Schema [ebBP-SIG].
 * The value of eb:MessageInfo/eb:RefToMessageId MUST refer to the message for which this signal is a
 * receipt.
 *
 * @author Christian Koch
 * @version 1.0
 * @since 3.0
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Receipt", propOrder = "any")
@Entity
@Table(name = "TB_RECEIPT")
public class Receipt extends AbstractBaseEntity {
    @SuppressWarnings("JpaAttributeTypeInspection")
    @XmlAnyElement(lax = false)
    @XmlJavaTypeAdapter(ToStringAdapter.class)
    @ElementCollection
    @Lob
    @CollectionTable(name = "TB_RECEIPT_DATA", joinColumns = @JoinColumn(name = "RECEIPT_ID"))
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
