package eu.domibus.ext.services;

/**
 * Service used to operations related with Domibus Configuration.
 *
 * <p>Operations available in the {@link DomibusConfigurationExtService} : </p>
 * <ul>
 *     <li>Checks if is multi tenant aware ({@link #isMultiTenantAware()})</li>
 * </ul>
 *
 * @author Tiago Miguel
 * @since 1.2
 */
public interface DomibusConfigurationExtService {

    boolean isMultiTenantAware();
}
