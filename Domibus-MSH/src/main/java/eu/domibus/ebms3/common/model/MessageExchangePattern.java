/*
 * Copyright 2015 e-CODEX Project
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they
 * will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the
 * Licence.
 * You may obtain a copy of the Licence at:
 * http://ec.europa.eu/idabc/eupl5
 * Unless required by applicable law or agreed to in
 * writing, software distributed under the Licence is
 * distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied.
 * See the Licence for the specific language governing
 * permissions and limitations under the Licence.
 */

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
