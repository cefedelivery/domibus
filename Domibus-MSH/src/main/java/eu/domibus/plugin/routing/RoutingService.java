package eu.domibus.plugin.routing;

import eu.domibus.plugin.NotificationListener;
import eu.domibus.plugin.routing.dao.BackendFilterDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Iterator;
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

    /**
     * Returns the configured backend filters present in the classpath
     *
     * @return The configured backend filters
     */
    @Cacheable(value = "backendFilterCache")
    public List<BackendFilter> getBackendFilters() {
        final List<BackendFilter> filters = new ArrayList<>(backendFilterDao.findAll());
        final List<NotificationListener> backendsTemp = new ArrayList<>(notificationListeners);

        final Iterator<BackendFilter> backendFilterIterator = filters.iterator();
        while (backendFilterIterator.hasNext()) {
            BackendFilter filter = backendFilterIterator.next();

            boolean filterExists = false;
            for (final NotificationListener backend : backendsTemp) {
                if (filter.getBackendName().equals(backend.getBackendName())) {
                    filterExists = true;
                    backendsTemp.remove(backend);
                    break;
                }
            }
            if (!filterExists) {
                backendFilterIterator.remove();
            }
        }

        for (final NotificationListener backend : backendsTemp) {
            final BackendFilter filter = new BackendFilter();
            filter.setBackendName(backend.getBackendName());
            filters.add(filter);
        }
        return filters;
    }

    @CacheEvict(value = "backendFilterCache", allEntries = true)
    public void updateBackendFilters(final List<BackendFilter> filters) {
        backendFilterDao.update(filters);
    }

/*    public BackendConnector findResponsibleBackend(UserMessage message){
        for(BackendFilter filter:getBackendFilters()){
            for (RoutingCriteria routingCriteria: filter.getRoutingCriterias()) {
                if (routingCriteria.matches(message, )){
                    for (BackendConnector backend:backends){
                        if (backend.getName().equals(filter.getBackendName())){
                            return backend;
                        }
                    }
                }
            }
        }
        return null;
    }*/

}
