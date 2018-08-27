package eu.domibus.core.pmode;

import org.springframework.stereotype.Component;

import static eu.domibus.plugin.BackendConnector.Mode.PULL;

/**
 * @author Thomas Dussart
 * @since 3.3
 */
@Component
public class ProcessPartyExtractorProvider {

    public ProcessTypePartyExtractor getProcessTypePartyExtractor(String processType, final String senderParty, final String receiverParty) {
        ProcessTypePartyExtractor processTypePartyExtractor = new PushProcessPartyExtractor(senderParty, receiverParty);
        if (PULL.getFileMapping().equalsIgnoreCase(processType)) {
            processTypePartyExtractor = new PullProcessPartyExtractor(senderParty, receiverParty);
        }
        return processTypePartyExtractor;
    }
}
