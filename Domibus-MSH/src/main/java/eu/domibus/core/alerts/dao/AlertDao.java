package eu.domibus.core.alerts.dao;

import com.google.common.collect.Lists;
import eu.domibus.common.dao.BasicDao;
import eu.domibus.core.alerts.model.persist.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Repository
public class AlertDao extends BasicDao<Alert> {

    private final static Logger LOG = LoggerFactory.getLogger(AlertDao.class);

    public AlertDao() {
        super(Alert.class);
    }

    public List<Alert> findRetryAlerts() {
        final TypedQuery<Alert> namedQuery = em.createNamedQuery("Alert.findRetry", Alert.class);
        return namedQuery.getResultList();
    }

    public List<Alert> filterAlerts(AlertCriteria alertCriteria) {

        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<Alert> criteria = builder.createQuery(Alert.class);

        //create root entity specifying that we want to eager fetch. (Avoid the N+1 hibernate problem)
        final Root<Alert> root = criteria.from(Alert.class);
        root.fetch(Alert_.events).fetch(Event_.properties);

        final Subquery<Integer> subQuery = criteria.subquery(Integer.class);
        Root<Alert> subRoot=subQuery.from(Alert.class);

        //Do first a subQuery to retrieve the filtered alerts id based on criteria.
        subQuery.select( subRoot.get( Alert_.entityId) );

        List<Predicate> predicates = new ArrayList<>(getAlertPredicates(alertCriteria, builder, subRoot));
        final SetJoin<Alert, Event> eventJoin = subRoot.join(Alert_.events);
        final Map<String, String> parameters = alertCriteria.getParameters();
        parameters.forEach((key, value) -> {
            //because event properties are key value, we need to create a join on each parameters.
            final MapJoin<Event, String, EventProperty> join = eventJoin.join(Event_.properties);
            final Predicate parameterPredicate = builder.and(
                    builder.equal(join.get(EventProperty_.key), key),
                    builder.equal(join.get(EventProperty_.value), value));
            predicates.add(parameterPredicate);

        });
        //add predicates to the sub query.
        subQuery.where(predicates.toArray(new Predicate[predicates.size()])).distinct(true);

        //create main query by retrieving alerts where ides are in the sub query selection.
        criteria.where(root.get(Alert_.entityId).in(subQuery)).distinct(true);
        return em.createQuery(criteria).getResultList();
    }

    private List<Predicate> getAlertPredicates(AlertCriteria alertCriteria, CriteriaBuilder cb, Root<Alert> alertRoot) {
        List<Predicate> predicates= Lists.newArrayList();
        if (alertCriteria.isProcessed() != null) {
            predicates.add(cb.equal(alertRoot.get(Alert_.processed), alertCriteria.isProcessed()));
        }

        if(alertCriteria.getAlertType()!=null){
            predicates.add(cb.equal(alertRoot.get(Alert_.alertType), alertCriteria.getAlertType()));
        }

        if(alertCriteria.getAlertID()!=null){
            predicates.add(cb.equal(alertRoot.get(Alert_.entityId), alertCriteria.getAlertID()));
        }

        if (alertCriteria.getCreationFrom() != null) {
            predicates.add(cb.greaterThanOrEqualTo(alertRoot.get(Alert_.creationTime), alertCriteria.getCreationFrom()));
        }
        if (alertCriteria.getCreationTo() != null) {
            predicates.add(cb.lessThanOrEqualTo(alertRoot.get(Alert_.creationTime), alertCriteria.getCreationTo()));
        }

        if (alertCriteria.getReportingFrom() != null) {
            predicates.add(cb.greaterThanOrEqualTo(alertRoot.get(Alert_.reportingTime), alertCriteria.getReportingFrom()));
        }
        if (alertCriteria.getReportingTo() != null) {
            predicates.add(cb.lessThanOrEqualTo(alertRoot.get(Alert_.reportingTime), alertCriteria.getReportingTo()));
        }
        return predicates;

    }


}
