package eu.domibus.common.services;

import eu.domibus.api.audit.AuditLog;

import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * @author Thomas Dussart
 * @since 4.0
 * <p>
 * Service in charge of retrieving the audit logs.
 * The audit component will track historical changes on a set of tables.
 */
public interface AuditService {
    /**
     * Retrieve the list of audit for the given criterias.
     *
     * @param auditTargetName the type of audit to retrive (Message, User, etc...).
     * @param action          the type of action to retrieve (DEL/ADD/MODD).
     * @param user            the users that did the modifications.
     * @param from            the lower bound of the modification date.
     * @param to              the higher bound of the modification date.
     * @param start           the pagination start at.
     * @param max             the page number of reccords.
     * @return a list of audit.
     */
    List<AuditLog> listAudit(Set<String> auditTargetName,
                             Set<String> action,
                             Set<String> user,
                             Date from,
                             Date to,
                             int start,
                             int max);

    /**
     * Count the number of audit for the given criterias.
     *
     * @param auditTargetName the type of audit to retrive (Message, User, etc...).
     * @param action          the type of action to retrieve (DEL/ADD/MODD).
     * @param user            the users that did the modifications.
     * @param from            the lower bound of the modification date.
     * @param to              the higher bound of the modification date.
     * @return the audit count.
     */
    Long countAudit(Set<String> auditTargetName,
                    Set<String> action,
                    Set<String> user,
                    Date from,
                    Date to);

    /**
     * Entities targeted by the auditing system have a logical name.{@link eu.domibus.common.model.common.RevisionLogicalName}
     *
     * @return the logical names.
     */
    List<String> listAuditTarget();

    /**
     * Add message download audit for a message.
     *
     * @param messageId the id of the message.
     */
    void addMessageDownloadedAudit(String messageId);

    /**
     * Add message resent audit for a message.
     * @param messageId the id of the message.
     */
    void addMessageResentAudit(String messageId);

    /**
     * Add message deleted audit for a jms message.
     * @param messageId the id of the message.
     */
    void addJmsMessageDeletedAudit(
            String messageId,
            String fromQueue);

    /**
     * Add message moved audit for a message.
     * @param messageId the id of the message.
     */
    void addJmsMessageMovedAudit(
            String messageId,
            String fromQueue, String toQueue);
}

