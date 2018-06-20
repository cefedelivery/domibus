package eu.domibus.ext.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serializable;
import java.util.Set;

/**
 * DTO class for Payload Info
 *
 * It stores information about Payload
 *
 * @author Tiago Miguel
 * @since 3.3
 */
public class PayloadInfoDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * {@link Set} of Part Info {@link PartInfoDTO}
     */
    private Set<PartInfoDTO> partInfo;

    /**
     * Gets the Set of Part Info
     * @return {@link Set} of Part Info {@link PartInfoDTO}
     */
    public Set<PartInfoDTO> getPartInfo() {
        return partInfo;
    }

    /**
     * Sets the Set of Part Info
     * @param partInfo {@link Set} of Part Info {@link PartInfoDTO}
     */
    public void setPartInfo(Set<PartInfoDTO> partInfo) {
        this.partInfo = partInfo;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("partInfo", partInfo)
                .toString();
    }
}
