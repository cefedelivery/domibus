package eu.domibus.common.model.logging;

/**
 * Created by dussath on 6/13/17.
 */
public class RawEnvelopeDto {
    final String rawMessage;
    final int id;

    public RawEnvelopeDto(int id,String rawMessage) {
        this.id = id;
        this.rawMessage = rawMessage;
    }

    public String getRawMessage() {
        return rawMessage;
    }

    public int getId() {
        return id;
    }
}
