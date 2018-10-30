package eu.domibus.api.jms;

/**
 * The method will return a String which uniquely will identify a server instance for the cluster topic
 *
 * @author Catalin Enache
 * @since 4.1
 */
public interface JMSMessageServerName {

    String getUniqueServerName();
}
