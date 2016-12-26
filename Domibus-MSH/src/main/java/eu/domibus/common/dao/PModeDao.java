package eu.domibus.common.dao;

import eu.domibus.common.ErrorCode;
import eu.domibus.common.exception.EbMS3Exception;
import eu.domibus.common.model.configuration.*;
import eu.domibus.ebms3.common.dao.PModeProvider;
import eu.domibus.ebms3.common.model.AgreementRef;
import eu.domibus.ebms3.common.model.PartyId;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.logging.DomibusMessageCode;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

/**
 * @author Christian Koch, Stefan Mueller
 */
@Transactional
public class PModeDao extends PModeProvider {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(PModeDao.class);


    @Override
    public Party getSenderParty(final String pModeKey) {
        final TypedQuery<Party> query = this.entityManager.createNamedQuery("Party.findByName", Party.class);
        query.setParameter("NAME", this.getSenderPartyNameFromPModeKey(pModeKey));
        return query.getSingleResult();
    }

    @Override
    public Party getReceiverParty(final String pModeKey) {
        final TypedQuery<Party> query = this.entityManager.createNamedQuery("Party.findByName", Party.class);
        query.setParameter("NAME", this.getReceiverPartyNameFromPModeKey(pModeKey));
        return query.getSingleResult();
    }

    @Override
    public Service getService(final String pModeKey) {
        final TypedQuery<Service> query = this.entityManager.createNamedQuery("Service.findByName", Service.class);
        query.setParameter("NAME", this.getServiceNameFromPModeKey(pModeKey)); //FIXME enable multiple ServiceTypes with the same name
        return query.getSingleResult();
    }

    @Override
    public Action getAction(final String pModeKey) {
        final TypedQuery<Action> query = this.entityManager.createNamedQuery("Action.findByName", Action.class);
        query.setParameter("NAME", this.getActionNameFromPModeKey(pModeKey));
        return query.getSingleResult();
    }

    @Override
    public Agreement getAgreement(final String pModeKey) {
        final TypedQuery<Agreement> query = this.entityManager.createNamedQuery("Agreement.findByName", Agreement.class);
        query.setParameter("NAME", this.getAgreementRefNameFromPModeKey(pModeKey));
        return query.getSingleResult();
    }

    @Override
    public LegConfiguration getLegConfiguration(final String pModeKey) {
        final TypedQuery<LegConfiguration> query = this.entityManager.createNamedQuery("LegConfiguration.findByName", LegConfiguration.class);
        query.setParameter("NAME", this.getLegConfigurationNameFromPModeKey(pModeKey));
        return query.getSingleResult();
    }


    @Override //nothing to init here
    public void init() {

    }

    protected String findLegName(final String agreementName, final String senderParty, final String receiverParty, final String service, final String action) throws EbMS3Exception {
        LOG.debug("Finding leg name using agreement [{}], senderParty [{}], receiverParty [{}], service [{}] and action [{}]",
                agreementName, senderParty, receiverParty, service, action);
        String namedQuery;
        if (agreementName.equals(OPTIONAL_AND_EMPTY)) {
            namedQuery = "LegConfiguration.findForPartiesAndAgreementsOAE";
        } else {
            namedQuery = "LegConfiguration.findForPartiesAndAgreements";
        }
        LOG.debug("Using named query [{}]", namedQuery);

        Query candidatesQuery = this.entityManager.createNamedQuery(namedQuery);
        if (!agreementName.equals(OPTIONAL_AND_EMPTY)) {
            LOG.debug("Setting agreement [{}]", OPTIONAL_AND_EMPTY);
            candidatesQuery.setParameter("AGREEMENT", agreementName);
        }
        candidatesQuery.setParameter("SENDER_PARTY", senderParty);
        candidatesQuery.setParameter("RECEIVER_PARTY", receiverParty);

        List<LegConfiguration> candidates = candidatesQuery.getResultList();
        if (candidates == null || candidates.isEmpty()) {
            // To be removed when the backward compatibility will be finally broken!
            namedQuery = "LegConfiguration.findForPartiesAndAgreementEmpty";
            LOG.debug("No candidates found, using namedQuery to find candidates [{}]", namedQuery);
            candidatesQuery = this.entityManager.createNamedQuery(namedQuery);
            candidatesQuery.setParameter("SENDER_PARTY", senderParty);
            candidatesQuery.setParameter("RECEIVER_PARTY", receiverParty);
            candidates = candidatesQuery.getResultList();
            if (candidates == null || candidates.isEmpty()) {
                LOG.businessError(DomibusMessageCode.BUS_LEG_NAME_NOT_FOUND, agreementName, senderParty, receiverParty, service, action);
                throw new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0001, "No Candidates for Legs found", null, null);
            }
        }
        final TypedQuery<String> query = this.entityManager.createNamedQuery("LegConfiguration.findForPMode", String.class);
        query.setParameter("SERVICE", service);
        query.setParameter("ACTION", action);
        final Collection<String> candidateIds = new HashSet<>();
        for (final LegConfiguration candidate : candidates) {
            candidateIds.add(candidate.getName());
        }
        query.setParameter("CANDIDATES", candidateIds);
        try {
            return query.getSingleResult();
        } catch (final NoResultException e) {
            LOG.businessError(DomibusMessageCode.BUS_LEG_NAME_NOT_FOUND, e, agreementName, senderParty, receiverParty, service, action);
            throw new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0001, "No matching leg found", null, null);
        }
    }

    protected String findAgreement(final AgreementRef agreementRef) throws EbMS3Exception {
        if (agreementRef == null || agreementRef.getValue() == null || agreementRef.getValue().isEmpty()) {
            return OPTIONAL_AND_EMPTY;
        }
        final String value = agreementRef.getValue();
        final String type = agreementRef.getType();
        final TypedQuery<String> query = this.entityManager.createNamedQuery("Agreement.findByValueAndType", String.class);
        query.setParameter("VALUE", value);
        query.setParameter("TYPE", (type == null) ? "" : type);
        try {
            return query.getSingleResult();
        } catch (final NoResultException e) {
            LOG.businessError(DomibusMessageCode.BUS_MESSAGE_AGREEMENT_NOT_FOUND, e, agreementRef);
            throw new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0001, "No matching agreement found", null, null);
        }
    }

    protected String findActionName(final String action) throws EbMS3Exception {
        if (action == null || action.isEmpty()) {
            throw new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0004, "Action parameter must not be null or empty", null, null);
        }

        final TypedQuery<String> query = this.entityManager.createNamedQuery("Action.findByAction", String.class);
        query.setParameter("ACTION", action);
        try {
            return query.getSingleResult();
        } catch (final NoResultException e) {
            LOG.businessError(DomibusMessageCode.BUS_MESSAGE_ACTION_NOT_FOUND, e, action);
            throw new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0001, "No matching action found", null, null);
        }
    }

    protected String findServiceName(final eu.domibus.ebms3.common.model.Service service) throws EbMS3Exception {
        final String type = service.getType();
        final String value = service.getValue();
        final TypedQuery<String> query;
        if (type == null || type.isEmpty()) {
            try {
                URI.create(value); //if not an URI an IllegalArgumentException will be thrown
                query = entityManager.createNamedQuery("Service.findWithoutType", String.class);
                query.setParameter("SERVICE", value);
            } catch (final IllegalArgumentException e) {
                final EbMS3Exception ex = new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0003, "Service " + value + " is not a valid URI [CORE] 5.2.2.8", null, e);
                throw ex;
            }
        } else {
            query = this.entityManager.createNamedQuery("Service.findByServiceAndType", String.class);
            query.setParameter("SERVICE", value);
            query.setParameter("TYPE", type);
        }
        try {
            return query.getSingleResult();
        } catch (final NoResultException e) {
            LOG.businessError(DomibusMessageCode.BUS_MESSAGE_SERVICE_FOUND, e);
            throw new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0001, "No matching service found", null, null);
        }
    }

    protected String findPartyName(final Collection<PartyId> partyIds) throws EbMS3Exception {
        Identifier identifier;
        for (final PartyId partyId : partyIds) {
            LOG.debug("Trying to find party [" + partyId + "]");
            try {
                String type = partyId.getType();
                if (type == null || type.isEmpty()) { //PartyId must be an URI
                    try {
                        //noinspection ResultOfMethodCallIgnored
                        URI.create(partyId.getValue()); //if not an URI an IllegalArgumentException will be thrown
                        type = "";
                    } catch (final IllegalArgumentException e) {
                        final EbMS3Exception ex = new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0003, "PartyId " + partyId.getValue() + " is not a valid URI [CORE] 5.2.2.3", null, e);
                        throw ex;
                    }
                }
                final TypedQuery<Identifier> identifierQuery = this.entityManager.createNamedQuery("Identifier.findByTypeAndPartyId", Identifier.class);
                identifierQuery.setParameter("PARTY_ID", partyId.getValue());
                identifierQuery.setParameter("PARTY_ID_TYPE", type);
                identifier = identifierQuery.getSingleResult();
                LOG.debug("Found identifier [" + identifier + "]");
                final TypedQuery<String> query = this.entityManager.createNamedQuery("Party.findPartyByIdentifier", String.class);
                query.setParameter("PARTY_IDENTIFIER", identifier);

                return query.getSingleResult();
            } catch (final NoResultException e) {
                LOG.debug("", e); // Its ok to not know all identifiers, we just have to know one
            }
        }
        LOG.businessError(DomibusMessageCode.BUS_PARTY_ID_NOT_FOUND, partyIds);
        throw new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0003, "No matching party found", null, null);
    }

    @Override
    public boolean isMpcExistant(final String mpc) {
        final TypedQuery<Integer> query = this.entityManager.createNamedQuery("Mpc.countForQualifiedName", Integer.class);
        return query.getSingleResult() > 0;
    }

    @Override
    public int getRetentionDownloadedByMpcName(final String mpcName) {

        final TypedQuery<Mpc> query = entityManager.createNamedQuery("Mpc.findByName", Mpc.class);
        query.setParameter("NAME", mpcName);

        final Mpc result = query.getSingleResult();

        if (result == null) {
            LOG.error("No MPC with name: " + mpcName + " found. Assuming message retention of 0 for downloaded messages.");
            return 0;
        }

        return result.getRetentionDownloaded();
    }

    @Override
    public int getRetentionDownloadedByMpcURI(final String mpcURI) {

        final TypedQuery<Mpc> query = entityManager.createNamedQuery("Mpc.findByQualifiedName", Mpc.class);
        query.setParameter("QUALIFIED_NAME", mpcURI);

        final Mpc result = query.getSingleResult();

        if (result == null) {
            LOG.error("No MPC with name: " + mpcURI + " found. Assuming message retention of 0 for downloaded messages.");
            return 0;
        }

        return result.getRetentionDownloaded();
    }

    @Override
    public int getRetentionUndownloadedByMpcName(final String mpcName) {

        final TypedQuery<Mpc> query = this.entityManager.createNamedQuery("Mpc.findByName", Mpc.class);
        query.setParameter("NAME", mpcName);

        final Mpc result = query.getSingleResult();

        if (result == null) {
            LOG.error("No MPC with name: " + mpcName + " found. Assuming message retention of -1 for undownloaded messages.");
            return 0;
        }

        return result.getRetentionUndownloaded();
    }

    @Override
    public int getRetentionUndownloadedByMpcURI(final String mpcURI) {

        final TypedQuery<Mpc> query = entityManager.createNamedQuery("Mpc.findByQualifiedName", Mpc.class);
        query.setParameter("QUALIFIED_NAME", mpcURI);

        final Mpc result = query.getSingleResult();

        if (result == null) {
            LOG.error("No MPC with name: " + mpcURI + " found. Assuming message retention of -1 for undownloaded messages.");
            return 0;
        }

        return result.getRetentionUndownloaded();
    }

    @Override
    public List<String> getMpcList() {
        final TypedQuery<String> query = entityManager.createNamedQuery("Mpc.getAllNames", String.class);
        return query.getResultList();
    }

    @Override
    public List<String> getMpcURIList() {
        final TypedQuery<String> query = entityManager.createNamedQuery("Mpc.getAllURIs", String.class);
        return query.getResultList();
    }

    @Override
    public void refresh() {
        //as we always query the DB pmodes never are stale, thus no refresh needed
    }
}
