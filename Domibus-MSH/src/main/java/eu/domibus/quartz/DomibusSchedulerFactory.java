package eu.domibus.quartz;

import eu.domibus.api.multitenancy.Domain;
import org.quartz.Scheduler;

/**
 * @author Cosmin Baciu
 * @since 4.0
 */
public interface DomibusSchedulerFactory {

    Scheduler createScheduler(Domain domain);
}
