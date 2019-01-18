package eu.domibus.plugin.transformer;

import eu.domibus.plugin.validation.SubmissionValidatorList;

/**
 * Created by Cosmin Baciu on 04-Aug-16.
 */
public interface PluginHandler {

    SubmissionValidatorList getSubmissionValidatorList();

    OutgoingMessageTransformerList getOutgoingMessageTransformerList();
}
