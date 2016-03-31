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


/**
 * <p>Java class for anonymous complex type.
 * <p/>
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p/>
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;extension base="{http://docs.oasis-open.org/ebxml-bp/ebbp-signals-2.0}SignalIdentificationInformation">
 *       &lt;sequence>
 *         &lt;element name="ExceptionType">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;choice>
 *                   &lt;element name="ReceiptException">
 *                     &lt;simpleType>
 *                       &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *                         &lt;enumeration value="Syntax"/>
 *                         &lt;enumeration value="Authorization"/>
 *                         &lt;enumeration value="Signature"/>
 *                         &lt;enumeration value="Sequence"/>
 *                       &lt;/restriction>
 *                     &lt;/simpleType>
 *                   &lt;/element>
 *                   &lt;element name="AcceptanceException">
 *                     &lt;simpleType>
 *                       &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *                         &lt;enumeration value="Business"/>
 *                         &lt;enumeration value="Performance"/>
 *                       &lt;/restriction>
 *                     &lt;/simpleType>
 *                   &lt;/element>
 *                   &lt;element name="GeneralException">
 *                     &lt;simpleType>
 *                       &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *                       &lt;/restriction>
 *                     &lt;/simpleType>
 *                   &lt;/element>
 *                 &lt;/choice>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="Reason" type="{http://docs.oasis-open.org/ebxml-bp/ebbp-signals-2.0}non-empty-string"/>
 *         &lt;element name="ExceptionMessage" type="{http://docs.oasis-open.org/ebxml-bp/ebbp-signals-2.0}non-empty-string" minOccurs="0"/>
 *         &lt;any namespace='##other' minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
        "exceptionType",
        "reason",
        "exceptionMessage",
        "any"
})
@XmlRootElement(name = "Exception", namespace = "http://docs.oasis-open.org/ebxml-bp/ebbp-signals-2.0")
public class Exception
        extends SignalIdentificationInformation {

    @XmlElement(name = "ExceptionType", namespace = "http://docs.oasis-open.org/ebxml-bp/ebbp-signals-2.0", required = true)
    protected ExceptionType exceptionType;
    @XmlElement(name = "Reason", namespace = "http://docs.oasis-open.org/ebxml-bp/ebbp-signals-2.0", required = true)
    protected String reason;
    @XmlElement(name = "ExceptionMessage", namespace = "http://docs.oasis-open.org/ebxml-bp/ebbp-signals-2.0")
    protected String exceptionMessage;
    @XmlAnyElement(lax = true)
    protected Object any;

    /**
     * Gets the value of the exceptionType property.
     *
     * @return possible object is
     * {@link ExceptionType }
     */
    public ExceptionType getExceptionType() {
        return this.exceptionType;
    }

    /**
     * Sets the value of the exceptionType property.
     *
     * @param value allowed object is
     *              {@link ExceptionType }
     */
    public void setExceptionType(final ExceptionType value) {
        this.exceptionType = value;
    }

    /**
     * Gets the value of the reason property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getReason() {
        return this.reason;
    }

    /**
     * Sets the value of the reason property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setReason(final String value) {
        this.reason = value;
    }

    /**
     * Gets the value of the exceptionMessage property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getExceptionMessage() {
        return this.exceptionMessage;
    }

    /**
     * Sets the value of the exceptionMessage property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setExceptionMessage(final String value) {
        this.exceptionMessage = value;
    }

    /**
     * Gets the value of the any property.
     *
     * @return possible object is
     * {@link Object }
     */
    public Object getAny() {
        return this.any;
    }

    /**
     * Sets the value of the any property.
     *
     * @param value allowed object is
     *              {@link Object }
     */
    public void setAny(final Object value) {
        this.any = value;
    }


    /**
     * <p>Java class for anonymous complex type.
     * <p/>
     * <p>The following schema fragment specifies the expected content contained within this class.
     * <p/>
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;choice>
     *         &lt;element name="ReceiptException">
     *           &lt;simpleType>
     *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
     *               &lt;enumeration value="Syntax"/>
     *               &lt;enumeration value="Authorization"/>
     *               &lt;enumeration value="Signature"/>
     *               &lt;enumeration value="Sequence"/>
     *             &lt;/restriction>
     *           &lt;/simpleType>
     *         &lt;/element>
     *         &lt;element name="AcceptanceException">
     *           &lt;simpleType>
     *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
     *               &lt;enumeration value="Business"/>
     *               &lt;enumeration value="Performance"/>
     *             &lt;/restriction>
     *           &lt;/simpleType>
     *         &lt;/element>
     *         &lt;element name="GeneralException">
     *           &lt;simpleType>
     *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
     *             &lt;/restriction>
     *           &lt;/simpleType>
     *         &lt;/element>
     *       &lt;/choice>
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
            "receiptException",
            "acceptanceException",
            "generalException"
    })
    public static class ExceptionType {

        @XmlElement(name = "ReceiptException", namespace = "http://docs.oasis-open.org/ebxml-bp/ebbp-signals-2.0")
        protected String receiptException;
        @XmlElement(name = "AcceptanceException", namespace = "http://docs.oasis-open.org/ebxml-bp/ebbp-signals-2.0")
        protected String acceptanceException;
        @XmlElement(name = "GeneralException", namespace = "http://docs.oasis-open.org/ebxml-bp/ebbp-signals-2.0")
        protected String generalException;

        /**
         * Gets the value of the receiptException property.
         *
         * @return possible object is
         * {@link String }
         */
        public String getReceiptException() {
            return this.receiptException;
        }

        /**
         * Sets the value of the receiptException property.
         *
         * @param value allowed object is
         *              {@link String }
         */
        public void setReceiptException(final String value) {
            this.receiptException = value;
        }

        /**
         * Gets the value of the acceptanceException property.
         *
         * @return possible object is
         * {@link String }
         */
        public String getAcceptanceException() {
            return this.acceptanceException;
        }

        /**
         * Sets the value of the acceptanceException property.
         *
         * @param value allowed object is
         *              {@link String }
         */
        public void setAcceptanceException(final String value) {
            this.acceptanceException = value;
        }

        /**
         * Gets the value of the generalException property.
         *
         * @return possible object is
         * {@link String }
         */
        public String getGeneralException() {
            return this.generalException;
        }

        /**
         * Sets the value of the generalException property.
         *
         * @param value allowed object is
         *              {@link String }
         */
        public void setGeneralException(final String value) {
            this.generalException = value;
        }

    }

}
