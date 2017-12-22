package eu.domibus.core.certificate;

import eu.domibus.common.model.certificate.Certificate;

import java.util.List;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
public interface CertificateDao {

    void saveOrUpdate(Certificate certificate);

    List<Certificate> getUnNotifiedSoonRevoked();

    List<Certificate> getUnNotifiedRevoked();

    void updateRevocation(Certificate certificate);
}
