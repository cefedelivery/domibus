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
        "initiatorPartiesXml",
        "responderPartiesXml",
        "legsXml"
})
@Entity
@Table(name = "TB_PROCESS")
public class Process extends AbstractBaseEntity {


    @XmlAttribute(name = "name", required = true)
    @Column(name = "NAME")
    protected String name;

    @XmlElement(required = true, name = "initiatorParties")
    @Transient
    protected InitiatorParties initiatorPartiesXml;
    @XmlElement(required = true, name = "responderParties")
    @Transient
    protected ResponderParties responderPartiesXml;
    @XmlElement(required = true, name = "legs")
    @Transient
    protected Legs legsXml;
    @XmlAttribute(name = "initiatorRole", required = true)
    @Transient
    protected String initiatorRoleXml;
    @Transient
    @XmlAttribute(name = "responderRole", required = true)
    protected String responderRoleXml;
    @XmlAttribute(name = "agreement", required = true)
    @Transient
    protected String agreementXml;
    @XmlAttribute(name = "mep", required = true)
    @Transient
    protected String mepXml;
    @XmlAttribute(name = "binding", required = true)
    @Transient
    protected String bindingXml;
    @XmlTransient
    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinTable(name = "TB_JOIN_PROCESS_INIT_PARTY", joinColumns = @JoinColumn(name = "PROCESS_FK"), inverseJoinColumns = @JoinColumn(name = "PARTY_FK"))
    private Set<Party> initiatorParties;
    @XmlTransient
    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinTable(name = "TB_JOIN_PROCESS_RESP_PARTY", joinColumns = @JoinColumn(name = "PROCESS_FK"), inverseJoinColumns = @JoinColumn(name = "PARTY_FK"))
    private Set<Party> responderParties;

    @XmlTransient
    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinTable(name = "TB_JOIN_PROCESS_LEG", joinColumns = @JoinColumn(name = "PROCESS_FK"), inverseJoinColumns = @JoinColumn(name = "LEG_FK"))
    private Set<LegConfiguration> legs;
    @XmlTransient
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "FK_INITIATOR_ROLE")
    private Role initiatorRole;
    @XmlTransient
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "FK_RESPONDER_ROLE")
    private Role responderRole;
    @XmlTransient
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "FK_AGREEMENT")
    private Agreement agreement;
    @XmlTransient
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "FK_MEP")
    private Mep mep;
    @XmlTransient
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "FK_MEP_BINDING")
    private Binding mepBinding;
    @XmlTransient
    @Column(name = "USE_DYNAMIC_RESPONDER")
    private boolean dynamicResponder;
    @XmlTransient
    @Column(name = "USE_DYNAMIC_INITIATOR")
    private boolean dynamicInitiator;


    public void init(final Configuration configuration) {
        this.initiatorParties = new HashSet<>();
        if (initiatorPartiesXml != null) { // empty means dynamic discovery is used
            for (final InitiatorParty ini : this.initiatorPartiesXml.getInitiatorParty()) {
                for (final Party party : configuration.getBusinessProcesses().getParties()) {
                    if (party.getName().equals(ini.getName())) {
                        this.initiatorParties.add(party);
                        break;
                    }
                }
            }
        } else {
            this.dynamicInitiator = true;
        }

        this.responderParties = new HashSet<>();
        if (responderPartiesXml != null) { // empty means dynamic discovery is used
            for (final ResponderParty res : this.responderPartiesXml.getResponderParty()) {
                for (final Party party : configuration.getBusinessProcesses().getParties()) {
                    if (party.getName().equals(res.getName())) {
                        this.responderParties.add(party);
                        break;
                    }
                }
            }
        } else {
            this.dynamicResponder = true;
        }

        this.legs = new HashSet<>();
        for (final Leg leg : this.legsXml.getLeg()) {
            for (final LegConfiguration legConfiguration : configuration.getBusinessProcesses().getLegConfigurations()) {
                if (legConfiguration.getName().equals(leg.getName())) {
                    this.legs.add(legConfiguration);
                    break;
                }
            }
        }

        for (final Role role : configuration.getBusinessProcesses().getRoles()) {
            if (role.getName().equals(this.initiatorRoleXml)) {
                this.initiatorRole = role;
            }
            if (role.getName().equals(this.responderRoleXml)) {
                this.responderRole = role;
            }
        }

        for (final Agreement agreement1 : configuration.getBusinessProcesses().getAgreements()) {
            if (agreement1.getName().equals(this.agreementXml)) {
                this.agreement = agreement1;
                break;
            }
        }
        for (final Mep mep1 : configuration.getBusinessProcesses().getMeps()) {
            if (mep1.getName().equals(this.mepXml)) {
                this.mep = mep1;
                break;
            }
        }
        for (final Binding binding : configuration.getBusinessProcesses().getMepBindings()) {
            if (binding.getName().equals(this.bindingXml)) {
                this.mepBinding = binding;
                break;
            }
        }

    }

    public String getName() {
        return this.name;
    }

    public Set<Party> getInitiatorParties() {
        return this.initiatorParties;
    }

    public Set<Party> getResponderParties() {
        return this.responderParties;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof Process)) return false;
        if (!super.equals(o)) return false;

        final Process process = (Process) o;

        if (!name.equals(process.name)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + name.hashCode();
        return result;
    }

    public Set<LegConfiguration> getLegs() {
        return this.legs;
    }

    public Role getInitiatorRole() {
        return this.initiatorRole;
    }

    public Role getResponderRole() {
        return this.responderRole;
    }

    public Agreement getAgreement() {
        return this.agreement;
    }

    public Mep getMep() {
        return this.mep;
    }

    public Binding getMepBinding() {
        return this.mepBinding;
    }

    public boolean isDynamicResponder() {
        return dynamicResponder;
    }

    public boolean isDynamicInitiator() {
        return dynamicInitiator;
    }
}
