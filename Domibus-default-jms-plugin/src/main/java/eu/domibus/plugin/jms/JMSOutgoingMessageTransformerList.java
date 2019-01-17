package eu.domibus.plugin.jms;

import eu.domibus.plugin.transformer.OutgoingMessageTransformer;
import eu.domibus.plugin.transformer.OutgoingMessageTransformerList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;


@Service
public class JMSOutgoingMessageTransformerList implements OutgoingMessageTransformerList {

    protected List<OutgoingMessageTransformer> outgoingMessageTransformers;

    @Autowired
    JMSOutgoingMessageTransformer jmsOutgoingMessageTransformer;

    @PostConstruct
    public void init() {
        outgoingMessageTransformers = new ArrayList<>();
        outgoingMessageTransformers.add(jmsOutgoingMessageTransformer);
    }

    @Override
    public List<OutgoingMessageTransformer> getOutgoingMessageTransformers() {
        return outgoingMessageTransformers;
    }

    public void setOutgoingMessageTransformers(List<OutgoingMessageTransformer> outgoingMessageTransformers) {
        this.outgoingMessageTransformers = outgoingMessageTransformers;
    }
}

