package eu.domibus.api.usermessage.domain;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Tiago Miguel
 * @since 3.3.1
 */
public class PayloadInfo {

    /**
     * {@link Set} of Part Info {@link PartInfo}
     */
    protected Set<PartInfo> partInfo;

    /**
     * Gets the Set of Part Info
     * @return {@link Set} of Part Info {@link PartInfo}
     */
    public Set<PartInfo> getPartInfo() {
        return partInfo;
    }

    /**
     * Sets the Set of Part Info
     * @param partInfo {@link Set} of Part Info {@link PartInfo}
     */
    public void setPartInfo(Set<PartInfo> partInfo) {
        this.partInfo = partInfo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        PayloadInfo that = (PayloadInfo) o;

        return new EqualsBuilder()
                .append(partInfo, that.partInfo)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(partInfo)
                .toHashCode();
    }
}
