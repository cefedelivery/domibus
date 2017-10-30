package eu.domibus.common.services.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import eu.domibus.api.audit.AuditLog;
import eu.domibus.common.dao.AuditDao;
import eu.domibus.common.model.audit.Audit;
import eu.domibus.common.model.audit.JmsMessageAudit;
import eu.domibus.common.model.audit.MessageAudit;
import eu.domibus.common.model.common.ModificationType;
import eu.domibus.common.services.UserService;
import eu.domibus.common.util.AnnotationsUtil;
import eu.domibus.core.converter.DomainCoreConverter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
@RunWith(MockitoJUnitRunner.class)
public class AuditServiceImplTest {

    @Spy
    private AnnotationsUtil annotationsUtil;

    @Mock
    private DomainCoreConverter domainCoreConverter;

    @Mock
    private AuditDao auditDao;

    @Mock
    private UserService userService;

    @InjectMocks
    private AuditServiceImpl auditService;

    @Test
    public void listAuditTarget() throws Exception {
        List<String> targets = auditService.listAuditTarget();
        targets.stream().forEach(System.out::println);
        assertEquals(6, targets.size());
        assertTrue(targets.contains("User"));
        assertTrue(targets.contains("Party"));
        assertTrue(targets.contains("Pmode"));
        assertTrue(targets.contains("Message"));
        assertTrue(targets.contains("Message filter"));
        assertTrue(targets.contains("Jms message"));
    }

    @Test
    public void listAudit() {

        Audit audit = Mockito.mock(Audit.class);
        List<Audit> audits = Lists.newArrayList(audit);
        Date from = new Date();
        when(auditDao.listAudit(
                Sets.newHashSet("User", "Pmode"),
                Sets.newHashSet("ADD"),
                Sets.newHashSet("Admin"),
                from,
                from,
                0,
                10))
                .thenReturn(audits);
        auditService.listAudit(
                Sets.newHashSet("User", "Pmode"),
                Sets.newHashSet("ADD"),
                Sets.newHashSet("Admin"),
                from,
                from,
                0,
                10);
        verify(domainCoreConverter, times(1)).convert(eq(audits), eq(AuditLog.class));

    }

    @Test
    public void countAudit() {
        Date from = new Date();
        auditService.countAudit(
                Sets.newHashSet("User", "Pmode"),
                Sets.newHashSet("ADD"),
                Sets.newHashSet("Admin"),
                from,
                from);
        verify(auditDao, times(1)).countAudit(
                Sets.newHashSet("User", "Pmode"),
                Sets.newHashSet("ADD"),
                Sets.newHashSet("Admin"),
                from,
                from
        );
    }

    @Test
    public void addMessageResentAudit() {
        when(userService.getLoggedUserNamed()).thenReturn("thomas");
        auditService.addMessageResentAudit("resendMessageId");
        ArgumentCaptor<MessageAudit> messageAuditCaptor = ArgumentCaptor.forClass(MessageAudit.class);
        verify(auditDao, times(1)).saveMessageAudit(messageAuditCaptor.capture());
        MessageAudit value = messageAuditCaptor.getValue();
        assertEquals("resendMessageId", value.getId());
        assertEquals("thomas", value.getUserName());
        assertEquals(ModificationType.RESENT, value.getModificationType());
        assertNotNull(value.getRevisionDate());
    }

    @Test
    public void addMessageDownloadedAudit() {
        when(userService.getLoggedUserNamed()).thenReturn("thomas");
        auditService.addMessageDownloadedAudit("resendMessageId");
        ArgumentCaptor<MessageAudit> messageAuditCaptor = ArgumentCaptor.forClass(MessageAudit.class);
        verify(auditDao, times(1)).saveMessageAudit(messageAuditCaptor.capture());
        MessageAudit value = messageAuditCaptor.getValue();
        assertEquals("resendMessageId", value.getId());
        assertEquals("thomas", value.getUserName());
        assertEquals(ModificationType.DOWNLOADED, value.getModificationType());
        assertNotNull(value.getRevisionDate());
    }

    @Test
    public void addJmsMessageDeletedAudit() {
        when(userService.getLoggedUserNamed()).thenReturn("thomas");
        auditService.addJmsMessageDeletedAudit("resendMessageId", "fromQueue");
        ArgumentCaptor<JmsMessageAudit> jmsMessageAuditCaptor = ArgumentCaptor.forClass(JmsMessageAudit.class);
        verify(auditDao, times(1)).saveJmsMessageAudit(jmsMessageAuditCaptor.capture());
        JmsMessageAudit value = jmsMessageAuditCaptor.getValue();
        assertEquals("resendMessageId", value.getId());
        assertEquals("thomas", value.getUserName());
        assertEquals(ModificationType.DEL, value.getModificationType());
        assertEquals("fromQueue", value.getFromQueue());
        assertNull(value.getToQueue());
    }

    @Test
    public void addJmsMessageMovedAudit() {
        when(userService.getLoggedUserNamed()).thenReturn("thomas");
        auditService.addJmsMessageMovedAudit("resendMessageId", "fromQueue", "toQueue");
        ArgumentCaptor<JmsMessageAudit> jmsMessageAuditCaptor = ArgumentCaptor.forClass(JmsMessageAudit.class);
        verify(auditDao, times(1)).saveJmsMessageAudit(jmsMessageAuditCaptor.capture());
        JmsMessageAudit value = jmsMessageAuditCaptor.getValue();
        assertEquals("resendMessageId", value.getId());
        assertEquals("thomas", value.getUserName());
        assertEquals(ModificationType.DEL, value.getModificationType());
        assertEquals("fromQueue", value.getFromQueue());
        assertEquals("toQueue", value.getToQueue());
    }

}