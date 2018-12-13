
package eu.domibus.plugin.handler;


import eu.domibus.messaging.MessagingProcessingException;
import eu.domibus.plugin.Submission;

/**
 * Pull requests are initiated through this interface
 *
 * @author idragusa
 * @since 4.1
 */
public interface MessagePuller {

    /**
     * Make Domibus initiate a pull request.
     *
     * @param mpc the mpc to be used in the pull request
     *
     */
    public void initiatePull(String mpc) ;
}
