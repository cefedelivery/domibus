package eu.domibus.core.party;

import eu.domibus.common.dao.PartyDao;
import eu.domibus.core.converter.DomainCoreConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
@Service
public class PartyServiceImpl implements PartyService {

    @Autowired
    private PartyDao partyDao;

    @Autowired
    private DomainCoreConverter domainCoreConverter;

    @Override
    public List<Party> listParties(String name,
                                   String endPoint,
                                   String partyId,
                                   String process,
                                   int pargeStart,
                                   int pageSize) {
        List<eu.domibus.common.model.configuration.Party> sourceList = partyDao.listParties(
                name,
                endPoint,
                partyId,
                process,
                pargeStart,
                pageSize);
        return domainCoreConverter.convert(sourceList, Party.class);

    }
}
