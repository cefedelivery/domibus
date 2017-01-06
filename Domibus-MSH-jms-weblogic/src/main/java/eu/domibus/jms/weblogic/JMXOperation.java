package eu.domibus.jms.weblogic;

import javax.management.MBeanServerConnection;

/**
 * @author Cosmin Baciu
 * @since 3.3
 */
public interface JMXOperation {

    <T> T execute(MBeanServerConnection mBeanServerConnection);
}
