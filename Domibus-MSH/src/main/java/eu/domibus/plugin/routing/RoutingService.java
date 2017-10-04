package eu.domibus.plugin.routing;

import eu.domibus.api.routing.BackendFilter;
import eu.domibus.core.converter.DomainCoreConverter;
import eu.domibus.plugin.NotificationListener;
import eu.domibus.plugin.routing.dao.BackendFilterDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Christian Walczac
 */
@Service
public class RoutingService {

    @Autowired
    private BackendFilterDao backendFilterDao;

    @Autowired
    private List<NotificationListener> notificationListeners;

    @Autowired
    private DomainCoreConverter coreConverter;

    /**
     * Returns the configured backend filters present in the classpath
     *
     * @return The configured backend filters
     */
    @Cacheable(value = "backendFilterCache")
    public List<BackendFilter> getBackendFilters() {
        return getBackendFiltersUncached();
    }

    public List<BackendFilter> getBackendFiltersUncached() {
        final List<BackendFilterEntity> filters = new ArrayList<>(backendFilterDao.findAll());
        final List<NotificationListener> backendsTemp = new ArrayList<>(notificationListeners);

        for (BackendFilterEntity filter : filters) {
            for (final NotificationListener backend : backendsTemp) {
                if (filter.getBackendName().equals(backend.getBackendName())) {
                    backendsTemp.remove(backend);
                    break;
                }
            }
        }

        for (final NotificationListener backend : backendsTemp) {
            final BackendFilterEntity filter = new BackendFilterEntity();
            filter.setBackendName(backend.getBackendName());
            filters.add(filter);
        }
        return coreConverter.convert(filters, BackendFilter.class);
    }

    @CacheEvict(value = "backendFilterCache", allEntries = true)
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public void updateBackendFilters(final List<BackendFilter> filters) {
        List<BackendFilterEntity> backendFilterEntities = coreConverter.convert(filters, BackendFilterEntity.class);
        List<BackendFilterEntity> allBackendFilterEntities = backendFilterDao.findAll();
        List<BackendFilterEntity> backendFilterEntityListToDelete = backendFiltersToDelete(allBackendFilterEntities, backendFilterEntities);
        backendFilterDao.deleteAll(backendFilterEntityListToDelete);
        backendFilterDao.update(backendFilterEntities);
    }

    private List<BackendFilterEntity> backendFiltersToDelete(final List<BackendFilterEntity> masterData, final List<BackendFilterEntity> newData) {
        List<BackendFilterEntity> result = new ArrayList<>(masterData);
        result.removeAll(newData);
        return result;
    }
}
