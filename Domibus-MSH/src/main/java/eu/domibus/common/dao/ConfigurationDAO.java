
package eu.domibus.common.dao;


import eu.domibus.common.model.configuration.*;
import eu.domibus.common.model.configuration.Process;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.TypedQuery;

/**
 * @author Christian Koch, Stefan Mueller
 */
@Repository
public class ConfigurationDAO extends BasicDao<Configuration> {

    public ConfigurationDAO() {
        super(Configuration.class);
    }


    public boolean configurationExists() {
        final TypedQuery<Long> query = this.em.createNamedQuery("Configuration.count", Long.class);

        return query.getSingleResult() != 0;
    }

    public Configuration read() {
        final TypedQuery<Configuration> query = this.em.createNamedQuery("Configuration.getConfiguration", Configuration.class);
        return query.getSingleResult();
    }

    @Transactional
    public Configuration readEager() {
        final TypedQuery<Configuration> query = this.em.createNamedQuery("Configuration.getConfiguration", Configuration.class);

        final Configuration configuration = query.getSingleResult();

        for (final Mpc mpc : configuration.getMpcs()) {
            mpc.getName(); //This is just top avoid the compiler optimizing this away
        }
        final BusinessProcesses businessProcesses = configuration.getBusinessProcesses();

        for (final Process process : businessProcesses.getProcesses()) {
            process.getInitiatorParties().size();
            process.getResponderParties().size();
            process.getLegs().size();
        }


        businessProcesses.getRoles().size();

        businessProcesses.getProperties().size();
        businessProcesses.getSecurities().size();
        businessProcesses.getActions().size();
        businessProcesses.getPayloads().size();
        businessProcesses.getProcesses().size();
        businessProcesses.getParties().size();
        for (final Party p : businessProcesses.getParties()) {
            p.getIdentifiers().size();
        }

        businessProcesses.getAgreements().size();
        businessProcesses.getAs4ConfigReceptionAwareness().size();
        businessProcesses.getAs4Reliability().size();
        businessProcesses.getErrorHandlings().size();
        businessProcesses.getLegConfigurations().size();
        businessProcesses.getMepBindings().size();
        businessProcesses.getMeps().size();
        businessProcesses.getPartyIdTypes().size();
        for (final PayloadProfile payloadProfile : businessProcesses.getPayloadProfiles()) {
            payloadProfile.getPayloads().size();
        }
        for (final PropertySet propertySet : businessProcesses.getPropertySets()) {
            propertySet.getProperties().size();
        }
        businessProcesses.getRoles().size();
        businessProcesses.getServices().size();
        return configuration;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    //FIXME: PMode update instead of wipe
    public void updateConfiguration(final Configuration configuration) {
        if (this.configurationExists()) {
            this.delete(this.read());
        }
        this.create(configuration);


    }
}
