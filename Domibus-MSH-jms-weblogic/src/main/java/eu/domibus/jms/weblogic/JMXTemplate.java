package eu.domibus.jms.weblogic;

/**
 * @author Cosmin Baciu
 * @since 3.3
 */
public interface JMXTemplate {

    <T> T query(JMXOperation jmxOperation);
}
