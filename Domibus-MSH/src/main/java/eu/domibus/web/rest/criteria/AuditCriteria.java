package eu.domibus.web.rest.criteria;

import java.util.Date;
import java.util.Set;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
public class AuditCriteria {

    private Set<String> auditTargetName;

    private Set<String> action;

    private Set<String> user;

    private Date from;

    private Date to;

    private int start, max;

    public Set<String> getAuditTargetName() {
        return auditTargetName;
    }

    public void setAuditTargetName(Set<String> auditTargetName) {
        this.auditTargetName = auditTargetName;
    }

    public Set<String> getAction() {
        return action;
    }

    public void setAction(Set<String> action) {
        this.action = action;
    }

    public Set<String> getUser() {
        return user;
    }

    public void setUser(Set<String> user) {
        this.user = user;
    }

    public Date getFrom() {
        return from;
    }

    public void setFrom(Date from) {
        this.from = from;
    }

    public Date getTo() {
        return to;
    }

    public void setTo(Date to) {
        this.to = to;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getMax() {
        return max;
    }

    public void setMax(int max) {
        this.max = max;
    }

    @Override
    public String toString() {
        return "AuditCriteria{" +
                "auditTargetName=" + auditTargetName +
                ", action=" + action +
                ", user=" + user +
                ", from=" + from +
                ", to=" + to +
                ", start=" + start +
                ", max=" + max +
                '}';
    }
}
