package eu.domibus.api.pmode.domain;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class ReceptionAwareness {

    protected String name;

    protected int retryTimeout;

    protected int retryCount;

    protected boolean duplicateDetection;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getRetryTimeout() {
        return retryTimeout;
    }

    public void setRetryTimeout(int retryTimeout) {
        this.retryTimeout = retryTimeout;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }

    public boolean isDuplicateDetection() {
        return duplicateDetection;
    }

    public void setDuplicateDetection(boolean duplicateDetection) {
        this.duplicateDetection = duplicateDetection;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        ReceptionAwareness that = (ReceptionAwareness) o;

        return new EqualsBuilder()
                .append(retryTimeout, that.retryTimeout)
                .append(retryCount, that.retryCount)
                .append(duplicateDetection, that.duplicateDetection)
                .append(name, that.name)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(name)
                .append(retryTimeout)
                .append(retryCount)
                .append(duplicateDetection)
                .toHashCode();
    }
}
