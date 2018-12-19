package eu.domibus.ebms3.common.model;

import javax.xml.namespace.QName;

/**
 * @author Cosmin Baciu
 * @author Christian Koch, Stefan Mueller
 */
public class NonRepudiationConstants {

    public static final String NS_NRR = "http://docs.oasis-open.org/ebxml-bp/ebbp-signals-2.0";
    public static final String NRR_LN = "NonRepudiationInformation";
    public static final String URI_WSU_NS = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd";
    public static final QName ID_QNAME = new QName(NonRepudiationConstants.URI_WSU_NS, "Id", "wsu");
    public static final String ID_NAMESPACE_URI = "http://www.w3.org/2000/xmlns/";
    public static final String ID_QUALIFIED_NAME = "xmlns:wsu";

}