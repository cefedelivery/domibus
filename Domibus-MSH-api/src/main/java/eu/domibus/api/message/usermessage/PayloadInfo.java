package eu.domibus.api.message.usermessage;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import java.util.Set;

public class PayloadInfo {

    private Set<PartInfo> partInfo;

    public Set<PartInfo> getPartInfo() {
        return partInfo;
    }

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
