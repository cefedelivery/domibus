package eu.domibus.common.model.configuration;

import eu.domibus.ebms3.common.model.AbstractBaseEntity;

import javax.persistence.*;
import javax.xml.bind.annotation.*;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Christian Koch, Stefan Mueller
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {

})
@Entity
@Table(name = "TB_CONFIGURATION")
@XmlRootElement(name = "configuration")
@NamedQueries({@NamedQuery(name = "Configuration.count", query = "SELECT COUNT(c.entityId) FROM Configuration c"), @NamedQuery(name = "Configuration.getConfiguration", query = "select conf from Configuration conf")})
public class Configuration extends AbstractBaseEntity {

    @XmlElement(required = true, name = "businessProcesses")
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "FK_BUSINESSPROCESSES")
    protected BusinessProcesses businessProcesses;
    @XmlElement(required = true, name = "mpcs")
    @Transient
    private Mpcs mpcsXml; //NOSONAR
    @XmlTransient
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "FK_CONFIGURATION")
    private Set<Mpc> mpcs;
    @XmlAttribute(name = "party", required = true)
    @Transient
    private String partyXml;
    @XmlTransient
    @JoinColumn(name = "FK_PARTY")
    @OneToOne
    private Party party;

    private void initMpcs() {
        if (this.mpcs == null) {
            this.mpcs = new HashSet<>(this.mpcsXml.getMpc());
        }
    }

    private void initParty() {
        for (final Party party1 : this.businessProcesses.getParties()) {
            if (party1.getName().equals(this.partyXml)) {
                this.party = party1;
                break;
            }
        }
    }

    public BusinessProcesses getBusinessProcesses() {
        return this.businessProcesses;
    }

    public void setBusinessProcesses(final BusinessProcesses businessProcesses) {
        this.businessProcesses = businessProcesses;
    }

    public Party getParty() {
        return this.party;
    }

    public void setParty(final Party party) {
        this.party = party;
    }

    public Set<Mpc> getMpcs() {
        return this.mpcs;
    }

    public Mpcs getMpcsXml() {
        return mpcsXml;
    }

    public void setMpcs(final Set<Mpc> mpcs) {
        this.mpcs = mpcs;
    }

    @PrePersist
    private void preparePersist() {
        this.initMpcs();
        this.businessProcesses.init(this);

        this.initParty();
    }


}
