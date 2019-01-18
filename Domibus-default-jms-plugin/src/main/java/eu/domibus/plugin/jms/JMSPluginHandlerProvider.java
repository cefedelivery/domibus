package eu.domibus.plugin.jms;

import eu.domibus.plugin.transformer.OutgoingMessageTransformerList;
import eu.domibus.plugin.transformer.PluginHandler;
import eu.domibus.plugin.validation.SubmissionValidatorList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class JMSPluginHandlerProvider implements PluginHandler {

    @Autowired
    JMSOutgoingMessageTransformerList jmsOutgoingMessageTransformerList;

    @Override
    public SubmissionValidatorList getSubmissionValidatorList() {
        return null;
    }

    @Override
    public OutgoingMessageTransformerList getOutgoingMessageTransformerList() {
        return jmsOutgoingMessageTransformerList;
    }
}

