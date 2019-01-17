package eu.domibus.submission;

import eu.domibus.plugin.transformer.OutgoingMessageTransformerList;

/**
 * Created by Cosmin Baciu on 04-Aug-16.
 */
public interface OutgoingMessageTransformerListProvider {

    OutgoingMessageTransformerList getOutgoingMessageTransformerList(String backendName);
}
