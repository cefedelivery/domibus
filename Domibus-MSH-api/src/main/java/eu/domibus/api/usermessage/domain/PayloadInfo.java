package eu.domibus.api.usermessage.domain;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import java.util.List;
import java.util.Set;

/**
 * @author Tiago Miguel
 * @since 3.3.1
 */
public class PayloadInfo {

    /**
     * {@link List} of Part Info {@link PartInfo}
     */
    protected List<PartInfo> partInfo;

    /**
     * Gets the List of Part Info
     * @return {@link List} of Part Info {@link PartInfo}
     */
    public List<PartInfo> getPartInfo() {
        return partInfo;
    }

    /**
     * Sets the List of Part Info
     * @param partInfo {@link List} of Part Info {@link PartInfo}
     */
    public void setPartInfo(List<PartInfo> partInfo) {
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
