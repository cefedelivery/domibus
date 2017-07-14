package eu.domibus.common.services.impl;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.common.model.configuration.Party;
import eu.domibus.common.model.configuration.Process;
import org.apache.commons.lang3.Validate;

import java.util.Collection;

/**
 * @author Thomas Dussart
 * @since 3.3
 * <p>
 * Contextual class for a pull.
 */
public class PullContext {

    private Process process;
    private Party initiator;
    private String mpcQualifiedName;
    public static final String MPC = "mpc";
    public static final String PMODE_KEY = "pmodKey";
    public static final String NOTIFY_BUSINNES_ON_ERROR = "NOTIFY_BUSINNES_ON_ERROR";

    public PullContext(final Process process, final Party initiator, final String mpcQualifiedName) {
        Validate.notNull(process);
        Validate.notNull(initiator);
        Validate.notNull(mpcQualifiedName);
        Validate.isTrue(process.getResponderParties().size() == 1);
        this.process = process;
        this.mpcQualifiedName = mpcQualifiedName;
        this.initiator = initiator;
    }

    public String getAgreement() {
        if (process.getAgreement() != null) {
            return process.getAgreement().getName();
        }
        return "";
    }

    public Process getProcess() {
        return process;
    }

    public Party getResponder() {
        return process.getResponderParties().iterator().next();
    }

    public String getMpcQualifiedName() {
        return mpcQualifiedName;
    }

    public Party getInitiator() {
        return initiator;
    }

    public LegConfiguration filterLegOnMpc() {
        if (mpcQualifiedName != null) {
            Collection<LegConfiguration> filter = Collections2.filter(process.getLegs(), new Predicate<LegConfiguration>() {
                @Override
                public boolean apply(LegConfiguration legConfiguration) {
                    return mpcQualifiedName.equals(legConfiguration.getDefaultMpc().getQualifiedName());
                }
            });
            return filter.iterator().next();
        } else throw new IllegalArgumentException("Method should be called after correct context setup.");
    }



}
