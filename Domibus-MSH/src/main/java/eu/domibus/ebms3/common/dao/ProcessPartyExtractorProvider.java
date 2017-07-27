package eu.domibus.ebms3.common.dao;

import org.springframework.stereotype.Component;

import static eu.domibus.plugin.BackendConnector.Mode.PULL;

/**
 * @author Thomas Dussart
 * @since 3.3
 */
@Component
public class ProcessPartyExtractorProvider {

    ProcessTypePartyExtractor getProcessTypePartyExtractor(String processType, final String senderParty, final String receiverParty) {
        ProcessTypePartyExtractor processTypePartyExtractor = new PushProcessPartyExtractor(senderParty, receiverParty);
        if (PULL.getFileMapping().equals(processType)) {
            processTypePartyExtractor = new PullProcessPartyExtractor(senderParty, receiverParty);
        }
        return processTypePartyExtractor;
    }
}
