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

package eu.domibus.plugin.routing;

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

    @Cacheable(value = "backendFilterCache")
    public List<BackendFilter> getBackendFilters() {
        final List<BackendFilter> filters = backendFilterDao.findAll();
        final List<NotificationListener> backendsTemp = new ArrayList<>(notificationListeners);
        for (final BackendFilter filter : filters) {
            boolean filterExists = false;
            for (final NotificationListener backend : backendsTemp) {
                if (filter.getBackendName().equals(backend.getBackendName())) {
                    filterExists = true;
                    backendsTemp.remove(backend);
                    break;
                }
            }
            if (!filterExists) {
                filters.remove(filter);
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
    @PreAuthorize("hasRole('ROLE_ADMIN')")
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
