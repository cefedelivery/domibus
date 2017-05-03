package eu.domibus.ebms3.common.model;

/**
 * @author Christian Koch, Stefan Mueller
 */
public enum MessageExchangePattern {
    ONE_WAY_PUSH("http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/push"),
    ONE_WAY_PULL("http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/pull"),
    TWO_WAY_SYNC("http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/sync"),
    TWO_WAY_PUSH_PUSH("http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/pushAndPush"),
    TWO_WAY_PUSH_PULL("http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/pushAndPull"),
    TWO_WAY_PULL_PUSH("http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/pullAndPush");

    private final String uri;

    MessageExchangePattern(final String uri) {
        this.uri = uri;
    }

    public String getUri() {
        return this.uri;
    }
}
