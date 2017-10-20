package eu.domibus.common.services.impl;

import eu.domibus.common.util.AnnotationsUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
@RunWith(MockitoJUnitRunner.class)
public class AuditServiceImplTest {

    @Spy
    private AnnotationsUtil annotationsUtil;

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

}