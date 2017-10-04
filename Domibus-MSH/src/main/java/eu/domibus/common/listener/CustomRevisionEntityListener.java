package eu.domibus.common.listener;

import eu.domibus.common.model.common.RevisionAction;
import eu.domibus.common.model.common.RevisionLog;
import org.hibernate.envers.RevisionListener;

import java.util.Date;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
public class CustomRevisionEntityListener implements RevisionListener {
    @Override
    public void newRevision(Object o) {
        RevisionLog revisionLog= (RevisionLog) o;
        revisionLog.setRevisionDate(new Date(System.currentTimeMillis()));
        revisionLog.setUserName("Thomas");
    }
}
