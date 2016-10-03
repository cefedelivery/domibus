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

package eu.domibus.plugin.routing.dao;

import eu.domibus.common.dao.BasicDao;
import eu.domibus.plugin.routing.BackendFilter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Christian Koch, Stefan Mueller
 */
@Service
@Transactional
public class BackendFilterDao extends BasicDao<BackendFilter> {

    private static final Log LOG = LogFactory.getLog(BackendFilterDao.class);

    public BackendFilterDao() {
        super(BackendFilter.class);
    }

    public void create(final List<BackendFilter> filters) {
        for (int i = 0; i < filters.size(); i++) {
            final BackendFilter f = filters.get(i);
            f.setIndex(i);
            super.create(f);
        }
    }

    public void update(final List<BackendFilter> filters) {
        for (int i = 0; i < filters.size(); i++) {
            final BackendFilter f = filters.get(i);
            f.setIndex(i);
            super.update(f);
        }
    }

    public List<BackendFilter> findAll() {
        final TypedQuery<BackendFilter> query = em.createNamedQuery("BackendFilter.findEntries", BackendFilter.class);
        try {
            final List result = query.getResultList();
            Collections.sort(result);
            return result;
        } catch (final NoResultException nrEx) {
            LOG.debug("Query BackendFilter.findEntries did not find any result", nrEx);
            return new ArrayList<>();
        }
    }

}
