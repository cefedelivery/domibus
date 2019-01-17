package eu.domibus.plugin.transformer;

import java.util.List;


public class DefaultOutgoingMessageTransformerList implements OutgoingMessageTransformerList {

    protected List<OutgoingMessageTransformer> outgoingMessageTransformers;

    @Override
    public List<OutgoingMessageTransformer> getOutgoingMessageTransformers() {
        return outgoingMessageTransformers;
    }

    public void setOutgoingMessageTransformers(List<OutgoingMessageTransformer> outgoingMessageTransformers) {
        this.outgoingMessageTransformers = outgoingMessageTransformers;
    }
}

