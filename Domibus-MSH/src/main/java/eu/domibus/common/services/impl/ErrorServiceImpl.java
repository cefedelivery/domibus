package eu.domibus.common.services.impl;

import eu.domibus.common.dao.ErrorLogDao;
import eu.domibus.common.model.logging.ErrorLogEntry;
import eu.domibus.common.services.ErrorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Thomas Dussart
 * @since 3.3
 * <p>
 * Service in charge or persisting errors.
 */
@Service
public class ErrorServiceImpl implements ErrorService {

    @Autowired
    private ErrorLogDao errorLogDao;

    /**
     * {@inheritDoc}
     *
     */
    //@TODO change the ErrorLogEntry to an ErrorLogEntry from the api. Not possible right now because of MSHRole enumeration not accessible from Domibus-MSH-api
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void createErrorLog(ErrorLogEntry errorLogEntry) {
        this.errorLogDao.create(errorLogEntry);
    }
}
