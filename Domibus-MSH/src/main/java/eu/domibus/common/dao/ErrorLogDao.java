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

package eu.domibus.common.dao;

import eu.domibus.common.model.logging.ErrorLogEntry;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.sql.Timestamp;
import java.util.*;


@Repository
@Transactional
/**
 * @author Christian Koch, Stefan Mueller
 */
public class ErrorLogDao extends BasicDao<ErrorLogEntry> {

    public ErrorLogDao() {
        super(ErrorLogEntry.class);
    }

    public List<ErrorLogEntry> getErrorsForMessage(final String messageId) {
        final TypedQuery<ErrorLogEntry> query = this.em.createNamedQuery("ErrorLogEntry.findErrorsByMessageId", ErrorLogEntry.class);
        query.setParameter("MESSAGE_ID", messageId);
        return query.getResultList();
    }


    public List<ErrorLogEntry> getUnnotifiedErrorsForMessage(final String messageId) {
        final TypedQuery<ErrorLogEntry> query = this.em.createNamedQuery("ErrorLogEntry.findUnnotifiedErrorsByMessageId", ErrorLogEntry.class);
        query.setParameter("MESSAGE_ID", messageId);
        return query.getResultList();
    }

    public long countEntries(HashMap<String, Object> filters) {
        CriteriaBuilder cb = this.em.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<ErrorLogEntry> mle = cq.from(ErrorLogEntry.class);
        cq.select(cb.count(mle));
        List<Predicate> predicates = getPredicates(filters, cb, mle);
        cq.where(cb.and(predicates.toArray(new Predicate[predicates.size()])));
        TypedQuery<Long> query = em.createQuery(cq);
        return query.getSingleResult();

    }

    public List<ErrorLogEntry> findPaged(final int from, final int max, final String column, final boolean asc, final HashMap<String, Object> filters) {
        final CriteriaBuilder cb = this.em.getCriteriaBuilder();
        final CriteriaQuery<ErrorLogEntry> cq = cb.createQuery(ErrorLogEntry.class);
        final Root<ErrorLogEntry> ele = cq.from(ErrorLogEntry.class);
        cq.select(ele);
        List<Predicate> predicates = getPredicates(filters, cb, ele);
        cq.where(cb.and(predicates.toArray(new Predicate[predicates.size()])));
        if (column != null) {
            if (asc) {
                cq.orderBy(cb.asc(ele.get(column)));
            } else {
                cq.orderBy(cb.desc(ele.get(column)));
            }

        }
        final TypedQuery<ErrorLogEntry> query = this.em.createQuery(cq);
        query.setFirstResult(from);
        query.setMaxResults(max);
        return query.getResultList();
    }

    protected List<Predicate> getPredicates(HashMap<String, Object> filters, CriteriaBuilder cb, Root<ErrorLogEntry> ele) {
        List<Predicate> predicates = new ArrayList<Predicate>();
        for (final Map.Entry<String, Object> filter : filters.entrySet()) {
            if (filter.getValue() != null) {
                if (filter.getValue() instanceof String) {
                    if (!filter.getValue().toString().isEmpty()) {
                        switch (filter.getKey().toString()) {
                            case "":
                                break;
                            default:
                                predicates.add(cb.like(ele.<String>get(filter.getKey()), (String) filter.getValue()));
                                break;
                        }
                    }
                } else if (filter.getValue() instanceof Date) {
                    if (!filter.getValue().toString().isEmpty()) {
                        switch (filter.getKey().toString()) {
                            case "":
                                break;
                            case "timestampFrom":
                                predicates.add(cb.greaterThanOrEqualTo(ele.<Date>get("timestamp"), (Timestamp) filter.getValue()));
                                break;
                            case "timestampTo":
                                predicates.add(cb.lessThanOrEqualTo(ele.<Date>get("timestamp"), (Timestamp) filter.getValue()));
                                break;
                            case "notifiedFrom":
                                predicates.add(cb.greaterThanOrEqualTo(ele.<Date>get("notified"), (Timestamp) filter.getValue()));
                                break;
                            case "notifiedTo":
                                predicates.add(cb.lessThanOrEqualTo(ele.<Date>get("notified"), (Timestamp) filter.getValue()));
                                break;
                            default:
                                predicates.add(cb.like(ele.<String>get(filter.getKey()), (String) filter.getValue()));
                                break;
                        }
                    }
                } else {
                    predicates.add(cb.equal(ele.<String>get(filter.getKey()), filter.getValue()));
                }
            }
        }
        return predicates;
    }

    public List<ErrorLogEntry> findAll() {
        final TypedQuery<ErrorLogEntry> query = this.em.createNamedQuery("ErrorLogEntry.findEntries", ErrorLogEntry.class);
        return query.getResultList();
    }

    public long countEntries() {
        final TypedQuery<Long> query = this.em.createNamedQuery("ErrorLogEntry.countEntries", Long.class);
        return query.getSingleResult();
    }


}
