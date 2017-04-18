package eu.domibus.common.model.configuration;

import eu.domibus.api.message.ebms3.model.AbstractBaseEntity;

import javax.persistence.*;
import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Christian Koch, Stefan Mueller
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = "partyMpc")
@Entity
@Table(name = "TB_LEG")
@NamedQueries({
        @NamedQuery(name = "LegConfiguration.findForPartiesAndAgreements",
                query = "select p.legs from Process p where :SENDER_PARTY in (select party.name from p.initiatorParties party) and :RECEIVER_PARTY in (select party.name from p.responderParties party) and (p.agreement.name=:AGREEMENT and p.agreement is not null)"),
        @NamedQuery(name = "LegConfiguration.findForPartiesAndAgreementsOAE",
                query = "select p.legs from Process p where :SENDER_PARTY in (select party.name from p.initiatorParties party) and :RECEIVER_PARTY in (select party.name from p.responderParties party) and p.agreement is null"),
        @NamedQuery(name = "LegConfiguration.findForPartiesAndAgreementEmpty",
                query = "select p.legs from Process p where :SENDER_PARTY in (select party.name from p.initiatorParties party) and :RECEIVER_PARTY in (select party.name from p.responderParties party) and (p.agreement.name='agreementEmpty')"),
        @NamedQuery(name = "LegConfiguration.findForPMode",
                query = "select l.name from LegConfiguration l where l.service.name=:SERVICE and l.action.name=:ACTION and l.name in :CANDIDATES"),
        @NamedQuery(name = "LegConfiguration.findByName",
                query = "select l from LegConfiguration l where l.name=:NAME")})

public class LegConfiguration extends AbstractBaseEntity {


    @Transient
    protected final List<PartyMpc> partyMpc = new ArrayList<>();
    @XmlTransient
    @ElementCollection(fetch = FetchType.EAGER, targetClass = Mpc.class)
    @MapKeyClass(Party.class)
    private final Map<Party, Mpc> partyMpcMap = new HashMap<>(); //FIXME: use it
    @XmlAttribute(name = "name", required = true)
    @Column(name = "NAME")
    protected String name;
    @XmlAttribute(name = "reliability", required = true)
    @Transient
    protected String reliabilityXml;
    @XmlAttribute(name = "security", required = true)
    @Transient
    protected String securityXml;
    @XmlAttribute(name = "receptionAwareness", required = true)
    @Transient
    protected String receptionAwarenessXml;
    @XmlAttribute(name = "service", required = true)
    @Transient
    protected String serviceXml;
    @XmlAttribute(name = "action", required = true)
    @Transient
    protected String actionXml;
    @XmlAttribute(name = "defaultMpc", required = true)
    @Transient
    protected String mpcXml;
    @XmlAttribute(name = "propertySet")
    @Transient
    protected String propertySetXml;
    @XmlAttribute(name = "payloadProfile")
    @Transient
    protected String payloadProfileXml;
    @XmlAttribute(name = "errorHandling", required = true)
    @Transient
    protected String errorHandlingXml;
    @XmlTransient
    @ManyToOne
    @JoinColumn(name = "FK_SECURITY")
    private Security security;
    @XmlTransient
    @ManyToOne
    @JoinColumn(name = "FK_RELIABILITY")
    private Reliability reliability;
    @XmlTransient
    @ManyToOne
    @JoinColumn(name = "FK_RECEPTION_AWARENESS")
    private ReceptionAwareness receptionAwareness;
    @XmlTransient
    @ManyToOne
    @JoinColumn(name = "FK_SERVICE")
    private Service service;
    @XmlTransient
    @ManyToOne
    @JoinColumn(name = "FK_ACTION")
    private Action action;
    @XmlTransient
    @ManyToOne
    @JoinColumn(name = "FK_MPC")
    private Mpc defaultMpc;
    @XmlTransient
    @ManyToOne
    @JoinColumn(name = "FK_PROPERTY_SET")
    private PropertySet propertySet;
    @XmlTransient
    @ManyToOne
    @JoinColumn(name = "FK_PAYLOAD_PROFILE")
    private PayloadProfile payloadProfile;
    @XmlTransient
    @ManyToOne
    @JoinColumn(name = "FK_ERROR_HANDLING")
    private ErrorHandling errorHandling;
    @XmlAttribute(name = "compressPayloads", required = true)
    @Column(name = "COMPRESS_PAYLOADS")
    private boolean compressPayloads;

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof LegConfiguration)) return false;
        if (!super.equals(o)) return false;

        final LegConfiguration that = (LegConfiguration) o;

        if (!name.equals(that.name)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + name.hashCode();
        return result;
    }

    public Map<Party, Mpc> getPartyMpcMap() {
        return this.partyMpcMap;
    }

    public String getName() {
        return this.name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public Reliability getReliability() {
        return this.reliability;
    }

    public void setReliability(final Reliability reliability) {
        this.reliability = reliability;
    }

    public Service getService() {
        return this.service;
    }

    public void setService(final Service service) {
        this.service = service;
    }

    public Mpc getDefaultMpc() {
        return this.defaultMpc;
    }

    public void setDefaultMpc(final Mpc mpc) {
        this.defaultMpc = mpc;
    }

    public PropertySet getPropertySet() {
        return this.propertySet;
    }

    public void setPropertySet(final PropertySet propertySet) {
        this.propertySet = propertySet;
    }

    public PayloadProfile getPayloadProfile() {
        return this.payloadProfile;
    }

    public void setPayloadProfile(final PayloadProfile payloadProfile) {
        this.payloadProfile = payloadProfile;
    }

    public ErrorHandling getErrorHandling() {
        return this.errorHandling;
    }

    public void setErrorHandling(final ErrorHandling errorHandling) {
        this.errorHandling = errorHandling;
    }

    public Security getSecurity() {
        return this.security;
    }

    public void setSecurity(final Security security) {
        this.security = security;
    }

    public ReceptionAwareness getReceptionAwareness() {
        return this.receptionAwareness;
    }

    public boolean isCompressPayloads() {
        return this.compressPayloads;
    }

    public void setCompressPayloads(final boolean compressPayloads) {
        this.compressPayloads = compressPayloads;
    }

    public void init(final Configuration configuration) {
        for (final Reliability rel : configuration.getBusinessProcesses().getAs4Reliability()) {
            if (rel.getName().equals(this.reliabilityXml)) {
                this.reliability = rel;
                break;
            }
        }
        for (final ReceptionAwareness ra : configuration.getBusinessProcesses().getAs4ConfigReceptionAwareness()) {
            if (ra.getName().equals(this.receptionAwarenessXml)) {
                this.receptionAwareness = ra;
                break;
            }
        }

        for (final Service ser : configuration.getBusinessProcesses().getServices()) {
            if (ser.getName().equals(this.serviceXml)) {
                this.service = ser;
                break;
            }
        }

        for (final Security sec : configuration.getBusinessProcesses().getSecurities()) {
            if (sec.getName().equals(this.securityXml)) {
                this.security = sec;
                break;
            }
        }

        for (final Action a : configuration.getBusinessProcesses().getActions()) {
            if (a.getName().equals(this.actionXml)) {
                this.action = a;
                break;
            }
        }
        for (final Mpc m : configuration.getMpcs()) {
            if (m.getName().equals(this.mpcXml)) {
                this.defaultMpc = m;
                break;
            }
        }
        for (final PropertySet ps : configuration.getBusinessProcesses().getPropertySets()) {
            if (ps.getName().equals(this.propertySetXml)) {
                this.propertySet = ps;
                break;
            }
        }
        for (final PayloadProfile pp : configuration.getBusinessProcesses().getPayloadProfiles()) {
            if (pp.getName().equals(this.payloadProfileXml)) {
                this.payloadProfile = pp;
                break;
            }
        }
        for (final ErrorHandling eh : configuration.getBusinessProcesses().getErrorHandlings()) {
            if (eh.getName().equals(this.errorHandlingXml)) {
                this.errorHandling = eh;
                break;
            }
        }
        for (final PartyMpc pmpc : this.partyMpc) {
            Party key = null;
            Mpc value = null;
            for (final Mpc m : configuration.getMpcs()) {
                if (m.getName().equals(pmpc.getMpc())) {
                    value = m;
                    break;
                }

            }
            for (final Party p : configuration.getBusinessProcesses().getParties()) {
                if (p.getName().equals(pmpc.getParty())) {
                    key = p;
                    break;
                }
            }
            this.partyMpcMap.put(key, value);
        }
    }

    public Action getAction() {
        return this.action;
    }
}
