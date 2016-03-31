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

package eu.domibus.common.model.org.oasis_open.docs.ebxml_bp.ebbp_signals_2_0;

import javax.xml.bind.annotation.*;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * This defines the content structure for identiying various parameters
 * pertaining to the business signal. "OriginalMessageIdentifier" captures the value of
 * the transport message identifier for original message to which this business signal
 * is being sent. If business message has an identifier, that can be captured by the
 * "OriginalDocumentIdentifier" attribute. "OriginalMessageDateTime" is the time when
 * the original message was sent. "ThisMessageDateTime" is the time when this signal
 * message is being sent. The following optional elements are there to provide access
 * to information that can be used by processing logic outside the business process
 * engine. One example of this could be a monitoring application which can use this
 * information to provide status of a collaboration. "FromPartyInfo" describes the
 * party id that is sending the signal message. "ToPartyInfo" describes the party id
 * that is being sent the signal message. The roles described below are based on the
 * implicit relationship between the partner sending the signal message and the partner
 * who sent the original message to which this particular signal is being sent. The
 * role relationship between partner sending the business message and the partner
 * receiving it is captured in the process definition (ebBP). "FromRole" captures the
 * role being played by the party that is sending the signal message. "ToRole" captures
 * the role played by the party that is being sent the signal message.
 * "ProcessSpecificationInfo" type descibes the process information (ebBP) which
 * defines the runtime collaborations for which this signal is being sent
 * "CollaborationIdentifier" is the unique identifer that associates the signal with a
 * particular collaboration. This could come from the business message itself or in
 * case of ebXML MSH, could be the messaging level header "ConversationId"
 * "BusinessActivityIdentifier" identifies the business Requesting or Responding
 * activity to which this signal is being sent. This would identify the
 * "BusinessAction" from the process definition (ebBP) and could be implemented using
 * the "name" attribute on either the RequestingBusinessActivity or the
 * RespondingBusinessActivity.
 * <p/>
 * <p>Java class for SignalIdentificationInformation complex type.
 * <p/>
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p/>
 * <pre>
 * &lt;complexType name="SignalIdentificationInformation">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="OriginalMessageIdentifier" type="{http://docs.oasis-open.org/ebxml-bp/ebbp-signals-2.0}non-empty-string"/>
 *         &lt;element name="OriginalDocumentIdentifier" type="{http://docs.oasis-open.org/ebxml-bp/ebbp-signals-2.0}non-empty-string" minOccurs="0"/>
 *         &lt;element name="OriginalMessageDateTime" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
 *         &lt;element name="ThisMessageDateTime" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
 *         &lt;element name="FromPartyInfo" type="{http://docs.oasis-open.org/ebxml-bp/ebbp-signals-2.0}PartyInfoType" minOccurs="0"/>
 *         &lt;element name="ToPartyInfo" type="{http://docs.oasis-open.org/ebxml-bp/ebbp-signals-2.0}PartyInfoType" minOccurs="0"/>
 *         &lt;element name="FromRole" type="{http://docs.oasis-open.org/ebxml-bp/ebbp-signals-2.0}RoleType" minOccurs="0"/>
 *         &lt;element name="ToRole" type="{http://docs.oasis-open.org/ebxml-bp/ebbp-signals-2.0}RoleType" minOccurs="0"/>
 *         &lt;element name="ProcessSpecificationInfo" type="{http://docs.oasis-open.org/ebxml-bp/ebbp-signals-2.0}ProcessSpecificationInfoType" minOccurs="0"/>
 *         &lt;element name="CollaborationIdentifier" type="{http://docs.oasis-open.org/ebxml-bp/ebbp-signals-2.0}non-empty-string" minOccurs="0"/>
 *         &lt;element name="BusinessActivityIdentifier" type="{http://docs.oasis-open.org/ebxml-bp/ebbp-signals-2.0}non-empty-string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SignalIdentificationInformation", namespace = "http://docs.oasis-open.org/ebxml-bp/ebbp-signals-2.0", propOrder = {
        "originalMessageIdentifier",
        "originalDocumentIdentifier",
        "originalMessageDateTime",
        "thisMessageDateTime",
        "fromPartyInfo",
        "toPartyInfo",
        "fromRole",
        "toRole",
        "processSpecificationInfo",
        "collaborationIdentifier",
        "businessActivityIdentifier"
})
@XmlSeeAlso({
        AcceptanceAcknowledgement.class,
        ReceiptAcknowledgement.class,
        Exception.class
})
public class SignalIdentificationInformation {

    @XmlElement(name = "OriginalMessageIdentifier", namespace = "http://docs.oasis-open.org/ebxml-bp/ebbp-signals-2.0", required = true)
    protected String originalMessageIdentifier;
    @XmlElement(name = "OriginalDocumentIdentifier", namespace = "http://docs.oasis-open.org/ebxml-bp/ebbp-signals-2.0")
    protected String originalDocumentIdentifier;
    @XmlElement(name = "OriginalMessageDateTime", namespace = "http://docs.oasis-open.org/ebxml-bp/ebbp-signals-2.0", required = true)
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar originalMessageDateTime;
    @XmlElement(name = "ThisMessageDateTime", namespace = "http://docs.oasis-open.org/ebxml-bp/ebbp-signals-2.0", required = true)
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar thisMessageDateTime;
    @XmlElement(name = "FromPartyInfo", namespace = "http://docs.oasis-open.org/ebxml-bp/ebbp-signals-2.0")
    protected PartyInfoType fromPartyInfo;
    @XmlElement(name = "ToPartyInfo", namespace = "http://docs.oasis-open.org/ebxml-bp/ebbp-signals-2.0")
    protected PartyInfoType toPartyInfo;
    @XmlElement(name = "FromRole", namespace = "http://docs.oasis-open.org/ebxml-bp/ebbp-signals-2.0")
    protected RoleType fromRole;
    @XmlElement(name = "ToRole", namespace = "http://docs.oasis-open.org/ebxml-bp/ebbp-signals-2.0")
    protected RoleType toRole;
    @XmlElement(name = "ProcessSpecificationInfo", namespace = "http://docs.oasis-open.org/ebxml-bp/ebbp-signals-2.0")
    protected ProcessSpecificationInfoType processSpecificationInfo;
    @XmlElement(name = "CollaborationIdentifier", namespace = "http://docs.oasis-open.org/ebxml-bp/ebbp-signals-2.0")
    protected String collaborationIdentifier;
    @XmlElement(name = "BusinessActivityIdentifier", namespace = "http://docs.oasis-open.org/ebxml-bp/ebbp-signals-2.0")
    protected String businessActivityIdentifier;

    /**
     * Gets the value of the originalMessageIdentifier property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getOriginalMessageIdentifier() {
        return this.originalMessageIdentifier;
    }

    /**
     * Sets the value of the originalMessageIdentifier property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setOriginalMessageIdentifier(final String value) {
        this.originalMessageIdentifier = value;
    }

    /**
     * Gets the value of the originalDocumentIdentifier property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getOriginalDocumentIdentifier() {
        return this.originalDocumentIdentifier;
    }

    /**
     * Sets the value of the originalDocumentIdentifier property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setOriginalDocumentIdentifier(final String value) {
        this.originalDocumentIdentifier = value;
    }

    /**
     * Gets the value of the originalMessageDateTime property.
     *
     * @return possible object is
     * {@link XMLGregorianCalendar }
     */
    public XMLGregorianCalendar getOriginalMessageDateTime() {
        return this.originalMessageDateTime;
    }

    /**
     * Sets the value of the originalMessageDateTime property.
     *
     * @param value allowed object is
     *              {@link XMLGregorianCalendar }
     */
    public void setOriginalMessageDateTime(final XMLGregorianCalendar value) {
        this.originalMessageDateTime = value;
    }

    /**
     * Gets the value of the thisMessageDateTime property.
     *
     * @return possible object is
     * {@link XMLGregorianCalendar }
     */
    public XMLGregorianCalendar getThisMessageDateTime() {
        return this.thisMessageDateTime;
    }

    /**
     * Sets the value of the thisMessageDateTime property.
     *
     * @param value allowed object is
     *              {@link XMLGregorianCalendar }
     */
    public void setThisMessageDateTime(final XMLGregorianCalendar value) {
        this.thisMessageDateTime = value;
    }

    /**
     * Gets the value of the fromPartyInfo property.
     *
     * @return possible object is
     * {@link PartyInfoType }
     */
    public PartyInfoType getFromPartyInfo() {
        return this.fromPartyInfo;
    }

    /**
     * Sets the value of the fromPartyInfo property.
     *
     * @param value allowed object is
     *              {@link PartyInfoType }
     */
    public void setFromPartyInfo(final PartyInfoType value) {
        this.fromPartyInfo = value;
    }

    /**
     * Gets the value of the toPartyInfo property.
     *
     * @return possible object is
     * {@link PartyInfoType }
     */
    public PartyInfoType getToPartyInfo() {
        return this.toPartyInfo;
    }

    /**
     * Sets the value of the toPartyInfo property.
     *
     * @param value allowed object is
     *              {@link PartyInfoType }
     */
    public void setToPartyInfo(final PartyInfoType value) {
        this.toPartyInfo = value;
    }

    /**
     * Gets the value of the fromRole property.
     *
     * @return possible object is
     * {@link RoleType }
     */
    public RoleType getFromRole() {
        return this.fromRole;
    }

    /**
     * Sets the value of the fromRole property.
     *
     * @param value allowed object is
     *              {@link RoleType }
     */
    public void setFromRole(final RoleType value) {
        this.fromRole = value;
    }

    /**
     * Gets the value of the toRole property.
     *
     * @return possible object is
     * {@link RoleType }
     */
    public RoleType getToRole() {
        return this.toRole;
    }

    /**
     * Sets the value of the toRole property.
     *
     * @param value allowed object is
     *              {@link RoleType }
     */
    public void setToRole(final RoleType value) {
        this.toRole = value;
    }

    /**
     * Gets the value of the processSpecificationInfo property.
     *
     * @return possible object is
     * {@link ProcessSpecificationInfoType }
     */
    public ProcessSpecificationInfoType getProcessSpecificationInfo() {
        return this.processSpecificationInfo;
    }

    /**
     * Sets the value of the processSpecificationInfo property.
     *
     * @param value allowed object is
     *              {@link ProcessSpecificationInfoType }
     */
    public void setProcessSpecificationInfo(final ProcessSpecificationInfoType value) {
        this.processSpecificationInfo = value;
    }

    /**
     * Gets the value of the collaborationIdentifier property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getCollaborationIdentifier() {
        return this.collaborationIdentifier;
    }

    /**
     * Sets the value of the collaborationIdentifier property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setCollaborationIdentifier(final String value) {
        this.collaborationIdentifier = value;
    }

    /**
     * Gets the value of the businessActivityIdentifier property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getBusinessActivityIdentifier() {
        return this.businessActivityIdentifier;
    }

    /**
     * Sets the value of the businessActivityIdentifier property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setBusinessActivityIdentifier(final String value) {
        this.businessActivityIdentifier = value;
    }

}
