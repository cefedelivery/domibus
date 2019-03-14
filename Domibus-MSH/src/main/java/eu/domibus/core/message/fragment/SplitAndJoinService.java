package eu.domibus.core.message.fragment;

import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.ebms3.common.model.UserMessage;

import javax.xml.soap.SOAPMessage;
import java.util.List;

/**
 * Class responsible for handling operations related to SplitAndJoin like: rejoin the source message based on message fragments, etc
 *
 * @author Cosmin Baciu
 * @since 4.1
 */
public interface SplitAndJoinService {

    /**
     * Checks if the leg is configured to use SplitAndJoin
     *
     * @param legConfiguration
     * @return
     */
    boolean mayUseSplitAndJoin(LegConfiguration legConfiguration);

    /**
     * Generates the file name for the source message
     *
     * @param temporaryDirectoryLocation
     * @return
     */
    String generateSourceFileName(String temporaryDirectoryLocation);

    /**
     * Rejoins the source message based on the message fragments associated to a specific group
     *
     * @param groupId
     * @return
     */
    SOAPMessage rejoinSourceMessage(String groupId);

    void createMessageFragments(UserMessage userMessage, MessageGroupEntity messageGroupEntity, List<String> fragmentFiles);

}
