package eu.domibus.common.model.logging;

/**
 * @author Thomas Dussart
 * @since 3.3
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
