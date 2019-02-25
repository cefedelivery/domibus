package eu.domibus.common.model.configuration;

import eu.domibus.ebms3.common.model.AbstractBaseEntity;

import javax.persistence.*;
import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Christian Koch, Stefan Mueller
 * @author Cosmin Baciu
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
        "rolesXml",
        "partiesXml",
        "mepsXml",
        "propertiesXml",
        "payloadProfilesXml",
        "securitiesXml",
        "errorHandlingsXml",
        "agreementsXml",
        "servicesXml",
        "actionsXml",
        "as4Xml",
        "splittingConfigurationsXml",
        "legConfigurationsXml",
        "processes"
})
@Entity
@Table(name = "TB_BUSINESS_PROCESS")
public class BusinessProcesses extends AbstractBaseEntity {

    @XmlElement(required = true, name = "roles")
    @Transient
    protected Roles rolesXml; //NOSONAR
    @XmlElement(required = true, name = "parties")
    @Transient
    protected Parties partiesXml; //NOSONAR
    @XmlElement(required = true, name = "meps")
    @Transient
    protected Meps mepsXml; //NOSONAR
    @XmlElement(name = "properties")
    @Transient
    protected Properties propertiesXml; //NOSONAR
    @XmlElement(required = true, name = "payloadProfiles")
    @Transient
    protected PayloadProfiles payloadProfilesXml; //NOSONAR
    @XmlElement(required = true, name = "errorHandlings")
    @Transient
    protected ErrorHandlings errorHandlingsXml; //NOSONAR
    @XmlElement(required = true, name = "agreements")
    @Transient
    protected Agreements agreementsXml; //NOSONAR
    @XmlElement(required = true, name = "services")
    @Transient
    protected Services servicesXml; //NOSONAR
    @XmlElement(required = true, name = "actions")
    @Transient
    protected Actions actionsXml; //NOSONAR
    @XmlElement(required = true, name = "as4")
    @Transient
    protected As4 as4Xml; //NOSONAR
    @XmlElement(required = true, name = "securities")
    @Transient
    protected Securities securitiesXml; //NOSONAR

    @XmlElement(name = "splittingConfigurations")
    @Transient
    protected SplittingConfigurations splittingConfigurationsXml; //NOSONAR

    @XmlElement(required = true, name = "legConfigurations")
    @Transient
    protected LegConfigurations legConfigurationsXml; //NOSONAR

    @XmlElement(required = true, name = "process")
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "FK_BUSINESSPROCESS")
    private List<Process> processes;


    @XmlTransient
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "FK_BUSINESSPROCESS")
    private Set<Role> roles;
    @XmlTransient
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "FK_BUSINESSPROCESS")
    private Set<PartyIdType> partyIdTypes;
    @XmlTransient
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "FK_BUSINESSPROCESS")
    private List<Party> parties;
    @XmlTransient
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "FK_BUSINESSPROCESS")
    private Set<Binding> mepBindings;
    @XmlTransient
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "FK_BUSINESSPROCESS")
    private Set<Mep> meps;
    @XmlTransient
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "FK_BUSINESSPROCESS")
    private Set<Property> properties;
    @XmlTransient
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "FK_BUSINESSPROCESS")
    private Set<PropertySet> propertySets;
    @XmlTransient
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "FK_BUSINESSPROCESS")
    private Set<PayloadProfile> payloadProfiles;
    @XmlTransient
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "FK_BUSINESSPROCESS")
    private Set<Payload> payloads;
    @XmlTransient
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "FK_BUSINESSPROCESS")
    private Set<ErrorHandling> errorHandlings;
    @XmlTransient
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "FK_BUSINESSPROCESS")
    private Set<Agreement> agreements;
    @XmlTransient
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "FK_BUSINESSPROCESS")
    private Set<Service> services;
    @XmlTransient
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "FK_BUSINESSPROCESS")
    private Set<Action> actions;
    @XmlTransient
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "FK_BUSINESSPROCESS")
    private Set<ReceptionAwareness> as4ConfigReceptionAwareness;
    @XmlTransient
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "FK_BUSINESSPROCESS")
    private Set<Reliability> as4Reliability;

    @XmlTransient
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "FK_BUSINESSPROCESS")
    private Set<Splitting> splittings;

    @XmlTransient
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "FK_BUSINESSPROCESS")
    private Set<LegConfiguration> legConfigurations;
    @XmlTransient
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "FK_BUSINESSPROCESS")
    private Set<Security> securities;

    public Set<PartyIdType> getPartyIdTypes() {
        return this.partyIdTypes;
    }

    public void setPartyIdTypes(final Set<PartyIdType> partyIdTypes) {
        this.partyIdTypes = partyIdTypes;
    }

    public Set<Reliability> getAs4Reliability() {
        return this.as4Reliability;
    }

    public void setAs4Reliability(final Set<Reliability> as4Reliability) {
        this.as4Reliability = as4Reliability;
    }

    public Set<Binding> getMepBindings() {
        return this.mepBindings;
    }

    public void setMepBindings(final Set<Binding> mepBindings) {
        this.mepBindings = mepBindings;
    }

    public Set<Mep> getMeps() {
        return this.meps;
    }

    public void setMeps(final Set<Mep> meps) {
        this.meps = meps;
    }

    public Set<PropertySet> getPropertySets() {
        return this.propertySets;
    }

    public void setPropertySets(final Set<PropertySet> propertySets) {
        this.propertySets = propertySets;
    }

    public Set<Payload> getPayloads() {
        return this.payloads;
    }

    public void setPayloads(final Set<Payload> payloads) {
        this.payloads = payloads;
    }

    public Set<ReceptionAwareness> getAs4ConfigReceptionAwareness() {
        return this.as4ConfigReceptionAwareness;
    }

    public void setAs4ConfigReceptionAwareness(final Set<ReceptionAwareness> as4ConfigReceptionAwareness) {
        this.as4ConfigReceptionAwareness = as4ConfigReceptionAwareness;
    }

    public Set<Security> getSecurities() {
        return this.securities;
    }

    public void setSecurities(final Set<Security> securities) {
        this.securities = securities;
    }

    void init(final Configuration configuration) {
        for (final Role role : this.rolesXml.getRole()) {
            role.init(configuration);
        }
        this.roles = new HashSet<>();
        roles.addAll(this.rolesXml.getRole());
        for (final PartyIdType partyIdType : this.partiesXml.getPartyIdTypes().getPartyIdType()) {
            partyIdType.init(configuration);
        }
        this.partyIdTypes = new HashSet<>();
        this.partyIdTypes.addAll(this.partiesXml.getPartyIdTypes().getPartyIdType());
        for (final Party party : this.partiesXml.getParty()) {
            party.init(configuration);
        }
        this.parties = new ArrayList<>();
        this.parties.addAll(this.partiesXml.getParty());
        for (final Binding binding : this.mepsXml.getBinding()) {
            binding.init(configuration);
        }
        this.mepBindings = new HashSet<>();
        this.mepBindings.addAll(this.mepsXml.getBinding());
        for (final Mep mep : this.mepsXml.getMep()) {
            mep.init(configuration);
        }
        this.meps = new HashSet<>();
        this.meps.addAll(this.mepsXml.getMep());
        for (final Property property : this.propertiesXml.getProperty()) {
            property.init(configuration);
        }
        this.properties = new HashSet<>();
        this.properties.addAll(this.propertiesXml.getProperty());
        for (final PropertySet propertySet : this.propertiesXml.getPropertySet()) {
            propertySet.init(configuration);
        }
        this.propertySets = new HashSet<>();
        this.propertySets.addAll(this.propertiesXml.getPropertySet());
        for (final Payload payload : this.payloadProfilesXml.getPayload()) {
            payload.init(configuration);
        }
        this.payloads = new HashSet<>();
        this.payloads.addAll(this.payloadProfilesXml.getPayload());
        for (final PayloadProfile payloadProfile : this.payloadProfilesXml.getPayloadProfile()) {
            payloadProfile.init(configuration);
        }
        this.payloadProfiles = new HashSet<>();
        this.payloadProfiles.addAll(this.payloadProfilesXml.getPayloadProfile());
        for (final ErrorHandling errorHandling : this.errorHandlingsXml.getErrorHandling()) {
            errorHandling.init(configuration);
        }
        this.errorHandlings = new HashSet<>();
        this.errorHandlings.addAll(this.errorHandlingsXml.getErrorHandling());
        for (final Agreement agreement : this.agreementsXml.agreement) {
            agreement.init(configuration);
        }
        this.agreements = new HashSet<>();
        this.agreements.addAll(this.agreementsXml.getAgreement());
        for (final Service service : this.servicesXml.getService()) {
            service.init(configuration);
        }
        this.services = new HashSet<>();
        this.services.addAll(this.servicesXml.getService());
        for (final Action action : this.actionsXml.getAction()) {
            action.init(configuration);
        }
        this.actions = new HashSet<>();
        this.actions.addAll(this.actionsXml.getAction());
        for (final ReceptionAwareness receptionAwareness : this.as4Xml.getReceptionAwareness()) {
            receptionAwareness.init(configuration);
        }
        this.as4ConfigReceptionAwareness = new HashSet<>();
        this.as4ConfigReceptionAwareness.addAll(this.as4Xml.getReceptionAwareness());
        for (final Reliability reliability : this.as4Xml.getReliability()) {
            reliability.init(configuration);
        }
        this.as4Reliability = new HashSet<>();
        this.as4Reliability.addAll(this.as4Xml.getReliability());

        for (final Security security : this.securitiesXml.getSecurity()) {
            security.init(configuration);
        }
        this.securities = new HashSet<>();
        this.securities.addAll(this.securitiesXml.getSecurity());

        if (splittingConfigurationsXml != null) {
            this.splittings = new HashSet<>();
            this.splittings.addAll(this.splittingConfigurationsXml.getSplitting());
        }

        for (final LegConfiguration legConfiguration : this.legConfigurationsXml.getLegConfiguration()) {
            legConfiguration.init(configuration);
        }
        this.legConfigurations = new HashSet<>();
        this.legConfigurations.addAll(this.legConfigurationsXml.getLegConfiguration());

        for (final Process process : this.processes) {
            process.init(configuration);
        }
    }

    public Set<Role> getRoles() {
        return this.roles;
    }

    public void setRoles(final Set<Role> roles) {
        this.roles = roles;
    }

    public List<Party> getParties() {
        return this.parties;
    }

    public void setParties(final List<Party> parties) {
        this.parties = parties;
    }

    public Set<Property> getProperties() {
        return this.properties;
    }

    public void setProperties(final Set<Property> properties) {
        this.properties = properties;
    }

    public Set<PayloadProfile> getPayloadProfiles() {
        return this.payloadProfiles;
    }

    public void setPayloadProfiles(final Set<PayloadProfile> payloadProfiles) {
        this.payloadProfiles = payloadProfiles;
    }

    public Set<ErrorHandling> getErrorHandlings() {
        return this.errorHandlings;
    }

    public void setErrorHandlings(final Set<ErrorHandling> errorHandlings) {
        this.errorHandlings = errorHandlings;
    }

    public Set<Agreement> getAgreements() {
        return this.agreements;
    }

    public void setAgreements(final Set<Agreement> agreements) {
        this.agreements = agreements;
    }

    public Parties getPartiesXml() {
        return this.partiesXml;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof BusinessProcesses)) return false;
        if (!super.equals(o)) return false;

        final BusinessProcesses that = (BusinessProcesses) o;

        if (actions != null ? !actions.equals(that.actions) : that.actions != null) return false;
        if (agreements != null ? !agreements.equals(that.agreements) : that.agreements != null) return false;
        if (as4ConfigReceptionAwareness != null ? !as4ConfigReceptionAwareness.equals(that.as4ConfigReceptionAwareness) : that.as4ConfigReceptionAwareness != null)
            return false;
        if (as4Reliability != null ? !as4Reliability.equals(that.as4Reliability) : that.as4Reliability != null)
            return false;
        if (errorHandlings != null ? !errorHandlings.equals(that.errorHandlings) : that.errorHandlings != null)
            return false;
        if (splittings != null ? !splittings.equals(that.splittings) : that.splittings != null)
            return false;
        if (legConfigurations != null ? !legConfigurations.equals(that.legConfigurations) : that.legConfigurations != null)
            return false;
        if (mepBindings != null ? !mepBindings.equals(that.mepBindings) : that.mepBindings != null) return false;
        if (meps != null ? !meps.equals(that.meps) : that.meps != null) return false;
        if (parties != null ? !parties.equals(that.parties) : that.parties != null) return false;
        if (partyIdTypes != null ? !partyIdTypes.equals(that.partyIdTypes) : that.partyIdTypes != null) return false;
        if (payloadProfiles != null ? !payloadProfiles.equals(that.payloadProfiles) : that.payloadProfiles != null)
            return false;
        if (payloads != null ? !payloads.equals(that.payloads) : that.payloads != null) return false;
        if (processes != null ? !processes.equals(that.processes) : that.processes != null) return false;
        if (properties != null ? !properties.equals(that.properties) : that.properties != null) return false;
        if (propertySets != null ? !propertySets.equals(that.propertySets) : that.propertySets != null) return false;
        if (roles != null ? !roles.equals(that.roles) : that.roles != null) return false;
        if (securities != null ? !securities.equals(that.securities) : that.securities != null) return false;
        if (services != null ? !services.equals(that.services) : that.services != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (processes != null ? processes.hashCode() : 0);
        result = 31 * result + (roles != null ? roles.hashCode() : 0);
        result = 31 * result + (partyIdTypes != null ? partyIdTypes.hashCode() : 0);
        result = 31 * result + (parties != null ? parties.hashCode() : 0);
        result = 31 * result + (mepBindings != null ? mepBindings.hashCode() : 0);
        result = 31 * result + (meps != null ? meps.hashCode() : 0);
        result = 31 * result + (properties != null ? properties.hashCode() : 0);
        result = 31 * result + (propertySets != null ? propertySets.hashCode() : 0);
        result = 31 * result + (payloadProfiles != null ? payloadProfiles.hashCode() : 0);
        result = 31 * result + (payloads != null ? payloads.hashCode() : 0);
        result = 31 * result + (errorHandlings != null ? errorHandlings.hashCode() : 0);
        result = 31 * result + (agreements != null ? agreements.hashCode() : 0);
        result = 31 * result + (services != null ? services.hashCode() : 0);
        result = 31 * result + (actions != null ? actions.hashCode() : 0);
        result = 31 * result + (as4ConfigReceptionAwareness != null ? as4ConfigReceptionAwareness.hashCode() : 0);
        result = 31 * result + (as4Reliability != null ? as4Reliability.hashCode() : 0);
        result = 31 * result + (splittings != null ? splittings.hashCode() : 0);
        result = 31 * result + (legConfigurations != null ? legConfigurations.hashCode() : 0);
        result = 31 * result + (securities != null ? securities.hashCode() : 0);
        return result;
    }

    public Set<Service> getServices() {
        return this.services;
    }

    public void setServices(final Set<Service> services) {
        this.services = services;
    }

    public Set<Action> getActions() {
        return this.actions;
    }

    public void setActions(final Set<Action> actions) {
        this.actions = actions;
    }

    public Set<Splitting> getSplittings() {
        return splittings;
    }

    public void setSplittings(Set<Splitting> splittings) {
        this.splittings = splittings;
    }

    public Set<LegConfiguration> getLegConfigurations() {
        return this.legConfigurations;
    }

    public void setLegConfigurations(final Set<LegConfiguration> legConfigurations) {
        this.legConfigurations = legConfigurations;
    }

    public List<Process> getProcesses() {
        if (this.processes == null) {
            this.processes = new ArrayList<>();
        }
        return this.processes;
    }

}
