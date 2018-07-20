package eu.domibus.common.model.certificate;

import eu.domibus.ebms3.common.model.AbstractBaseEntity;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.Objects;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
@Entity
@Table(name = "TB_CERTIFICATE")
@NamedQueries({
        @NamedQuery(name = "Certificate.findExpiredToNotifyCertificate", query = "FROM Certificate c where (c.alertExpiredNotificationDate is null OR c.alertExpiredNotificationDate<=:NEXT_NOTIFICATION) AND c.certificateStatus='REVOKED' AND c.notAfter>=:END_NOTIFICATION"),
        @NamedQuery(name = "Certificate.findImminentExpirationToNotifyCertificate", query = "FROM Certificate c where (c.alertImminentNotificationDate is null OR c.alertImminentNotificationDate<=:NEXT_NOTIFICATION) AND c.certificateStatus!='REVOKED' AND c.notAfter<=:OFFSET_DATE"),
        @NamedQuery(name = "Certificate.findByAlias", query = "FROM Certificate c where c.alias=:ALIAS"),
        @NamedQuery(name = "Certificate.findByAliasAndType", query = "FROM Certificate c where c.alias=:ALIAS AND c.certificateType=:CERTIFICATE_TYPE"),
        @NamedQuery(name = "Certificate.findByStatusAndNotificationDate", query = "FROM Certificate c where c.certificateStatus=:CERTIFICATE_STATUS AND (c.lastNotification is null OR c.lastNotification<:CURRENT_DATE)")
})
public class Certificate extends AbstractBaseEntity {

    @Column(name = "CERTIFICATE_ALIAS")
    @NotNull
    private String alias;

    @Column(name = "NOT_VALID_BEFORE_DATE")
    @NotNull
    @Temporal(TemporalType.TIMESTAMP)
    private Date notBefore;

    @Column(name = "NOT_VALID_AFTER_DATE")
    @NotNull
    @Temporal(TemporalType.TIMESTAMP)
    private Date notAfter;

    @Column(name = "REVOKE_NOTIFICATION_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastNotification;

    @Column(name = "ALERT_IMM_NOTIFICATION_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    private Date alertImminentNotificationDate;

    @Column(name = "ALERT_EXP_NOTIFICATION_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    private Date alertExpiredNotificationDate;

    @Column(name = "CERTIFICATE_TYPE")
    @Enumerated(EnumType.STRING)
    @NotNull
    private CertificateType certificateType;

    @Column(name = "CERTIFICATE_STATUS")
    @Enumerated(EnumType.STRING)
    @NotNull
    private CertificateStatus certificateStatus;

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public Date getNotBefore() {
        return notBefore;
    }

    public void setNotBefore(Date notBefore) {
        this.notBefore = notBefore;
    }

    public Date getNotAfter() {
        return notAfter;
    }

    public void setNotAfter(Date notAfter) {
        this.notAfter = notAfter;
    }

    public Date getLastNotification() {
        return lastNotification;
    }

    public void setLastNotification(Date lastNotification) {
        this.lastNotification = lastNotification;
    }

    public CertificateType getCertificateType() {
        return certificateType;
    }

    public CertificateStatus getCertificateStatus() {
        return certificateStatus;
    }

    public void setCertificateStatus(CertificateStatus certificateStatus) {
        this.certificateStatus = certificateStatus;
    }

    public Date getAlertImminentNotificationDate() {
        return alertImminentNotificationDate;
    }

    public void setAlertImminentNotificationDate(Date alertNotification) {
        this.alertImminentNotificationDate = alertNotification;
    }

    public Date getAlertExpiredNotificationDate() {
        return alertExpiredNotificationDate;
    }

    public void setAlertExpiredNotificationDate(Date alertExpiredNotificationDate) {
        this.alertExpiredNotificationDate = alertExpiredNotificationDate;
    }

    public void setCertificateType(CertificateType certificateType) {
        this.certificateType = certificateType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Certificate that = (Certificate) o;
        return Objects.equals(alias, that.alias) &&
                Objects.equals(notBefore, that.notBefore) &&
                Objects.equals(notAfter, that.notAfter) &&
                Objects.equals(lastNotification, that.lastNotification) &&
                certificateType == that.certificateType &&
                certificateStatus == that.certificateStatus;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), alias, notBefore, notAfter, lastNotification, certificateType, certificateStatus);
    }

    @Override
    public String toString() {
        return "Certificate{" +
                "alias='" + alias + '\'' +
                ", notBefore=" + notBefore +
                ", notAfter=" + notAfter +
                ", lastNotification=" + lastNotification +
                ", certificateType=" + certificateType +
                ", certificateStatus=" + certificateStatus +
                '}';
    }
}
