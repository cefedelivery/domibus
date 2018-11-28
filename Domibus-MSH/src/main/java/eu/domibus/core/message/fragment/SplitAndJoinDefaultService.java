package eu.domibus.core.message.fragment;

import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.common.model.configuration.Splitting;
import org.springframework.stereotype.Service;

/**
 * @author Cosmin Baciu
 * @since 4.1
 */
@Service
public class SplitAndJoinDefaultService implements SplitAndJoinService {

    @Override
    public boolean mayUseSplitAndJoin(LegConfiguration legConfiguration) {
        final Splitting splitting = legConfiguration.getSplitting();
        if(splitting == null) {
            return false;
        }
        return true;
    }

    @Override
    public boolean useSplitAndJoin(LegConfiguration legConfiguration, long payloadSize) {
        final boolean mayUseSplitAndJoin = mayUseSplitAndJoin(legConfiguration);
        if(mayUseSplitAndJoin && payloadSize > legConfiguration.getSplitting().getFragmentSize()) {
            return true;
        }
        return false;
    }
}
