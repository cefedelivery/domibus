package eu.domibus.web.rest;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.party.PartyService;
import eu.domibus.api.security.TrustStoreEntry;
import eu.domibus.common.services.impl.CsvServiceImpl;
import eu.domibus.core.converter.DomainCoreConverter;
import eu.domibus.core.crypto.api.MultiDomainCryptoService;
import eu.domibus.core.party.CertificateContentRo;
import eu.domibus.core.party.IdentifierRo;
import eu.domibus.core.party.PartyResponseRo;
import eu.domibus.core.party.ProcessRo;
import eu.domibus.pki.CertificateService;
import eu.domibus.web.rest.ro.TrustStoreRO;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.Verifications;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
@RunWith(JMockit.class)
public class PartyResourceTest {

    @Injectable
    private DomainCoreConverter domainConverter;

    @Injectable
    private PartyService partyService;

    @Tested
    private PartyResource partyResource;

    @Injectable
    private CsvServiceImpl csvServiceImpl;

    @Injectable
    protected MultiDomainCryptoService multiDomainCertificateProvider;

    @Injectable
    protected DomainContextProvider domainProvider;

    @Injectable
    private CertificateService certificateService;

    @Test
    public void listParties() throws Exception {
        final String name = "name";
        final String endPoint = "endPoint";
        final String partyId = "partyId";
        final String processName = "processName";
        final int pageStart = 0;
        final int pageSize = 1;

        final PartyResponseRo partyResponseRo = new PartyResponseRo();
        partyResponseRo.setIdentifiers(Sets.newHashSet(new IdentifierRo()));
        partyResponseRo.setProcessesWithPartyAsInitiator(Lists.newArrayList(new ProcessRo()));
        partyResponseRo.setProcessesWithPartyAsResponder(Lists.newArrayList(new ProcessRo()));

        final List<PartyResponseRo> partyResponseRos = Lists.newArrayList(partyResponseRo);
        new Expectations(partyResource) {{

            domainConverter.convert(withAny(new ArrayList<>()), PartyResponseRo.class);
            result = partyResponseRos;
            times = 1;

            partyResource.flattenIdentifiers(withAny(new ArrayList<>()));
            partyResource.flattenProcesses(withAny(new ArrayList<>()));

        }};

        partyResource.listParties(name, endPoint, partyId, processName, pageStart, pageSize);

        new Verifications() {{

            partyService.getParties(
                    name,
                    endPoint,
                    partyId,
                    processName,
                    pageStart,
                    pageSize);
            times = 1;

            partyResource.flattenIdentifiers(partyResponseRos);
            times = 1;

            partyResource.flattenProcesses(partyResponseRos);
            times = 1;

        }};
    }

    @Test
    public void flattenIdentifiers() throws Exception {
        PartyResponseRo partyResponseRo = new PartyResponseRo();

        IdentifierRo firstId = new IdentifierRo();
        firstId.setPartyId("blue");

        IdentifierRo secondId = new IdentifierRo();
        secondId.setPartyId("pale blue");

        partyResponseRo.setIdentifiers(Sets.newHashSet(firstId, secondId));

        partyResource.flattenIdentifiers(Lists.newArrayList(partyResponseRo));
        assertEquals("blue, pale blue", partyResponseRo.getJoinedIdentifiers());
    }

    @Test
    public void flattenProcesses() throws Exception {

        PartyResponseRo partyResponseRo = new PartyResponseRo();

        ProcessRo firstProcess = new ProcessRo();
        firstProcess.setName("tc1");

        ProcessRo secondProcess = new ProcessRo();
        secondProcess.setName("tc2");

        ProcessRo thirdProcess = new ProcessRo();
        thirdProcess.setName("tc3");

        partyResponseRo.setProcessesWithPartyAsInitiator(Lists.newArrayList(firstProcess, thirdProcess));
        partyResponseRo.setProcessesWithPartyAsResponder(Lists.newArrayList(secondProcess, thirdProcess));

        partyResource.flattenProcesses(Lists.newArrayList(partyResponseRo));
        assertEquals("tc1(I), tc2(R), tc3(IR)", partyResponseRo.getJoinedProcesses());

    }

    @Test
    public void listProcesses() {
        final ProcessRo proc1 = new ProcessRo();
        proc1.setName("process 1");
        final ProcessRo proc2 = new ProcessRo();
        proc1.setName("process 2");
        final List<ProcessRo> procs = Lists.newArrayList(proc1, proc2);

        new Expectations(partyResource) {{
            domainConverter.convert(withAny(new ArrayList<>()), ProcessRo.class);
            result = procs;
            times = 1;
        }};

        partyResource.listProcesses();

        new Verifications() {{
            partyService.getAllProcesses();
            times = 1;
        }};
    }

    @Test
    public void getCertificateForParty() throws Exception {
        final Date date = new Date();
        final TrustStoreEntry tre = new TrustStoreEntry("name", "subject", "issuer", date, date);
        final TrustStoreRO tr = new TrustStoreRO(); tr.setName("name"); tr.setSubject("subject"); tr.setIssuer("issuer"); tr.setValidFrom(date); tr.setValidUntil(date);
        final String partyName = "party1";

        new Expectations(partyResource) {{
            domainConverter.convert(withAny(tre), TrustStoreRO.class);
            result = tr;
            times = 1;
        }};

        partyResource.getCertificateForParty(partyName);

        new Verifications() {{
            certificateService.getPartyCertificateFromTruststore(partyName);
            times = 1;
        }};
    }

    @Test
    public void convertCertificateContent() throws Exception {
        final Date date = new Date();
        final TrustStoreEntry tre = new TrustStoreEntry("name", "subject", "issuer", date, date);
        final TrustStoreRO tr = new TrustStoreRO(); tr.setName("name"); tr.setSubject("subject"); tr.setIssuer("issuer"); tr.setValidFrom(date); tr.setValidUntil(date);
        final String partyName = "party1";
        final String certContent = "content";

        CertificateContentRo cert = new CertificateContentRo();
        cert.setContent(certContent);

        new Expectations(partyResource) {{
            domainConverter.convert(withAny(tre), TrustStoreRO.class);
            result = tr;
            times = 1;

            certificateService.convertCertificateContent(withAny(certContent));
            result = tre;
            times = 1;
        }};

        TrustStoreRO res = partyResource.convertCertificateContent(partyName, cert);

        assertEquals(res, tr);

//        new Verifications() {{
//            certificateService.convertCertificateContent(certContent);
//            times = 1;
//        }};
    }
}