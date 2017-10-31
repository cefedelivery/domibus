package eu.domibus.common.util;

import eu.domibus.common.model.audit.JmsMessageAudit;
import eu.domibus.common.model.audit.MessageAudit;
import eu.domibus.common.model.common.RevisionLogicalName;
import eu.domibus.common.model.configuration.BusinessProcesses;
import eu.domibus.common.model.configuration.Configuration;
import eu.domibus.common.model.configuration.Party;
import eu.domibus.common.model.security.User;
import eu.domibus.plugin.routing.BackendFilterEntity;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
public class AnnotationsUtilTest {
    @Test
    public void getDefaultValue() throws Exception {
        AnnotationsUtil annotationsUtil = new AnnotationsUtil();
        assertEquals("Pmode", annotationsUtil.getValue(Configuration.class, RevisionLogicalName.class).get());
        assertEquals("Message", annotationsUtil.getValue(MessageAudit.class, RevisionLogicalName.class).get());
        assertEquals("Party", annotationsUtil.getValue(Party.class, RevisionLogicalName.class).get());
        assertEquals("User", annotationsUtil.getValue(User.class, RevisionLogicalName.class).get());
        assertEquals("Message filter", annotationsUtil.getValue(BackendFilterEntity.class, RevisionLogicalName.class).get());
        assertEquals("Jms message", annotationsUtil.getValue(JmsMessageAudit.class, RevisionLogicalName.class).get());
        assertEquals(false, annotationsUtil.getValue(BusinessProcesses.class, RevisionLogicalName.class).isPresent());
    }

}