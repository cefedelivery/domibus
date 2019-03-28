package eu.domibus.core.message.fragment;

import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.ebms3.common.model.UserMessage;

import javax.xml.soap.SOAPMessage;
import java.io.File;

/**
 * Class responsible for handling operations related to SplitAndJoin like: rejoin the source message based on message fragments, etc
 *
 * @author Cosmin Baciu
 * @since 4.1
 */
public interface SplitAndJoinService {

    void createUserFragmentsFromSourceFile(String sourceMessageFileName, SOAPMessage sourceMessageRequest, UserMessage userMessage, String contentTypeString, boolean compression);

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
     * Rejoins the source message file from the message fragments associated to a specific group
     *
     * @param groupId
     * @return
     */
    File rejoinMessageFragments(String groupId);

    SOAPMessage getUserMessage(File sourceMessageFileName, String contentTypeString);

    /**
     * Rejoins the source message from a file present on disk
     *
     * @param groupId
     * @param sourceMessageFile
     * @param backendName
     */
    void rejoinSourceMessage(String groupId, String sourceMessageFile, String backendName);

    void setSourceMessageAsFailed(final UserMessage userMessage);

    void setUserMessageFragmentAsFailed(String messageId);

    void splitAndJoinSendFailed(final String groupId);

    void sendSourceMessageReceipt(String sourceMessageId, String pModeKey);

    void splitAndJoinReceiveFailed(String groupId, String errorDetail);
}
