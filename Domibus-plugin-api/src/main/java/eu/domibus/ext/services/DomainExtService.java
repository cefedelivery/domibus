package eu.domibus.ext.services;

import eu.domibus.ext.domain.DomainDTO;

/**
 * Service used to operations related with domains.
 *
 * <p>Operations available in the {@link DomainExtService} : </p>
 * <ul>
 *     <li>Gets domain for scheduler ({@link #getDomainForScheduler(String)})</li>
 *     <li>Gets domain defined with code ({@link #getDomain(String)}</li>
 * </ul>
 *
 * @author Tiago Miguel
 * @since 4.0
 */
public interface DomainExtService {

    DomainDTO getDomainForScheduler(String schedulerName);

    DomainDTO getDomain(String code);
}
