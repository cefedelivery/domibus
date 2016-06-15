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

package eu.domibus.common.model.org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704;

import eu.domibus.common.ErrorCode;

import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * An ebMS Error is represented by an eb:Error XML infoset, regardless of the way it is reported. Each error
 * raised by an MSH has the following properties:
 * • origin (optional attribute)
 * • category (optional attribute)
 * • errorCode (required attribute)
 * • severity (required attribute)
 * • refToMessageInError (optional attribute)
 * • shortDescription (optional attribute)
 * • Description (optional element)
 * • ErrorDetail (optional element)
 *
 * @author Christian Koch
 * @version 1.0
 * @since 3.0
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Error", propOrder = {"description", "errorDetail"})


public class Error {

    @XmlElement(name = "Description")

    protected Description description;
    @XmlElement(name = "ErrorDetail")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")

    protected String errorDetail;
    @XmlAttribute(name = "category")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")

    protected String category;
    @XmlAttribute(name = "refToMessageInError")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")

    protected String refToMessageInError;
    @XmlAttribute(name = "errorCode", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")

    protected String errorCode;
    @XmlAttribute(name = "origin")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")

    protected String origin;
    @XmlAttribute(name = "severity", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")

    protected String severity;
    @XmlAttribute(name = "shortDescription")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")

    protected String shortDescription;

    /**
     * This OPTIONAL element provides a narrative description of the error in the language defined by the
     * xml:lang attribute. The content of this element is left to implementation-specific decisions.
     *
     * @return possible object is {@link Description }
     */
    public Description getDescription() {
        return this.description;
    }

    /**
     * SThis OPTIONAL element provides a narrative description of the error in the language defined by the
     * xml:lang attribute. The content of this element is left to implementation-specific decisions.
     *
     * @param value allowed object is {@link Description }
     */
    public void setDescription(final Description value) {
        this.description = value;
    }

    /**
     * This OPTIONAL element provides additional details about the context in which the error occurred. For
     * example, it may be an exception trace.
     *
     * @return possible object is {@link String }
     */
    public String getErrorDetail() {
        return this.errorDetail;
    }

    /**
     * This OPTIONAL element provides additional details about the context in which the error occurred. For
     * example, it may be an exception trace.
     *
     * @param value allowed object is {@link String }
     */
    public void setErrorDetail(final String value) {
        this.errorDetail = value;
    }

    /**
     * This OPTIONAL attribute identifies the type of error related to a particular origin. For example: Content,
     * Packaging, UnPackaging, Communication, InternalProcess.
     *
     * @return possible object is {@link String }
     */
    public String getCategory() {
        return this.category;
    }

    /**
     * This OPTIONAL attribute identifies the type of error related to a particular origin. For example: Content,
     * Packaging, UnPackaging, Communication, InternalProcess.
     *
     * @param value allowed object is {@link String }
     */
    public void setCategory(final String value) {
        this.category = value;
    }

    /**
     * This OPTIONAL attribute indicates the MessageId of the message in error, for which this error is raised.
     *
     * @return possible object is {@link String }
     */
    public String getRefToMessageInError() {
        return this.refToMessageInError;
    }

    /**
     * This OPTIONAL attribute indicates the MessageId of the message in error, for which this error is raised.
     *
     * @param value allowed object is {@link String }
     */
    public void setRefToMessageInError(final String value) {
        this.refToMessageInError = value;
    }

    /**
     * This REQUIRED attribute is a unique identifier for the type of error.
     *
     * @return possible object is {@link String }
     * @see ErrorCode
     */
    public String getErrorCode() {
        return this.errorCode;
    }

    /**
     * This REQUIRED attribute is a unique identifier for the type of error.
     *
     * @param value allowed object is {@link String }
     * @see ErrorCode
     */
    public void setErrorCode(final String value) {
        this.errorCode = value;
    }

    /**
     * This OPTIONAL attribute identifies the functional module within which the error occurred. This module
     * could be the the ebMS Module, the Reliability Module, or the Security Module. Possible values for this
     * attribute include "ebMS", "reliability", and "security". The use of other modules, and thus their
     * corresponding @origin values, may be specified elsewhere, such as in a forthcoming Part 2 of this
     * specification.
     *
     * @return possible object is {@link String }
     */
    public String getOrigin() {
        return this.origin;
    }

    /**
     * This OPTIONAL attribute identifies the functional module within which the error occurred. This module
     * could be the the ebMS Module, the Reliability Module, or the Security Module. Possible values for this
     * attribute include "ebMS", "reliability", and "security". The use of other modules, and thus their
     * corresponding @origin values, may be specified elsewhere, such as in a forthcoming Part 2 of this
     * specification.
     *
     * @param value allowed object is {@link String }
     */
    public void setOrigin(final String value) {
        this.origin = value;
    }

    /**
     * This REQUIRED attribute indicates the severity of the error. Valid values are: warning, failure.
     * The warning value indicates that a potentially disabling condition has been detected, but no message
     * processing and/or exchange has failed so far. In particular, if the message was supposed to be delivered
     * to a consumer, it would be delivered even though a warning was issued. Other related messages in the
     * conversation or MEP can be generated and exchanged in spite of this problem.
     * The failure value indicates that the processing of a message did not proceed as expected, and cannot be
     * considered successful. If, in spite of this, the message payload is in a state of being delivered, the default
     * behavior is not to deliver it, unless an agreement states otherwise (see OpCtx-ErrorHandling). This error
     * does not presume the ability of the MSH to process other messages, although the conversation or the
     * MEP instance this message was involved in is at risk of being invalid.
     *
     * @return possible object is {@link String }
     */
    public String getSeverity() {
        return this.severity;
    }

    /**
     * This REQUIRED attribute indicates the severity of the error. Valid values are: warning, failure.
     * The warning value indicates that a potentially disabling condition has been detected, but no message
     * processing and/or exchange has failed so far. In particular, if the message was supposed to be delivered
     * to a consumer, it would be delivered even though a warning was issued. Other related messages in the
     * conversation or MEP can be generated and exchanged in spite of this problem.
     * The failure value indicates that the processing of a message did not proceed as expected, and cannot be
     * considered successful. If, in spite of this, the message payload is in a state of being delivered, the default
     * behavior is not to deliver it, unless an agreement states otherwise (see OpCtx-ErrorHandling). This error
     * does not presume the ability of the MSH to process other messages, although the conversation or the
     * MEP instance this message was involved in is at risk of being invalid.
     *
     * @param value allowed object is {@link String }
     */
    public void setSeverity(final String value) {
        this.severity = value;
    }

    /**
     * This OPTIONAL attribute provides a short description of the error that can be reported in a log, in order to
     * facilitate readability.
     *
     * @return possible object is {@link String }
     */
    public String getShortDescription() {
        return this.shortDescription;
    }

    /**
     * This OPTIONAL attribute provides a short description of the error that can be reported in a log, in order to
     * facilitate readability.
     *
     * @param value allowed object is {@link String }
     */
    public void setShortDescription(final String value) {
        this.shortDescription = value;
    }
}
