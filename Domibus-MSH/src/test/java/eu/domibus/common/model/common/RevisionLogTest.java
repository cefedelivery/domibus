package eu.domibus.common.model.common;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
public class RevisionLogTest {
    @Test
    public void addEntityAuditForUser() throws Exception {

        RevisionLog revisionLog = new RevisionLog();
        revisionLog.addEntityAudit("1", "UserEntity", "User", ModificationType.ADD, 1);
        revisionLog.addEntityAudit("1", "UserEntity", "User", ModificationType.MOD, 1);
        revisionLog.addEntityAudit("2", "RoleEntity", "Role", ModificationType.MOD, 2);
        assertEquals(1, revisionLog.getRevisionTypes().size());
        EnversAudit next = revisionLog.getRevisionTypes().iterator().next();
        assertEquals("1", next.getId());
        assertEquals("UserEntity", next.getEntityName());
        assertEquals("User", next.getGroupName());
        assertEquals(ModificationType.ADD, next.getModificationType());

        revisionLog = new RevisionLog();
        revisionLog.addEntityAudit("1", "UserEntity", "User", ModificationType.MOD, 1);
        revisionLog.addEntityAudit("1", "UserEntity", "User", ModificationType.ADD, 1);
        revisionLog.addEntityAudit("2", "RoleEntity", "Role", ModificationType.MOD, 2);

        assertEquals(1, revisionLog.getRevisionTypes().size());
        next = revisionLog.getRevisionTypes().iterator().next();
        assertEquals("1", next.getId());
        assertEquals("UserEntity", next.getEntityName());
        assertEquals("User", next.getGroupName());
        assertEquals(ModificationType.ADD, next.getModificationType());

        revisionLog = new RevisionLog();
        revisionLog.addEntityAudit("1", "UserEntity", "User", ModificationType.MOD, 1);
        revisionLog.addEntityAudit("2", "RoleEntity", "Role", ModificationType.MOD, 2);
        revisionLog.addEntityAudit("1", "UserEntity", "User", ModificationType.ADD, 1);

        assertEquals(1, revisionLog.getRevisionTypes().size());
        next = revisionLog.getRevisionTypes().iterator().next();
        assertEquals("1", next.getId());
        assertEquals("UserEntity", next.getEntityName());
        assertEquals("User", next.getGroupName());
        assertEquals(ModificationType.ADD, next.getModificationType());

        revisionLog = new RevisionLog();
        revisionLog.addEntityAudit("1", "UserEntity", "User", ModificationType.MOD, 1);
        revisionLog.addEntityAudit("2", "UserEntity", "User", ModificationType.MOD, 1);
        revisionLog.addEntityAudit("3", "RoleEntity", "Role", ModificationType.MOD, 2);
        revisionLog.addEntityAudit("1", "UserEntity", "User", ModificationType.ADD, 1);

        assertEquals(2, revisionLog.getRevisionTypes().size());
        boolean match = revisionLog.getRevisionTypes().stream().anyMatch(a ->
                "1".equals(a.getId()) &&
                        "UserEntity".equals(a.getEntityName()) &&
                        "User".equals(a.getGroupName()) &&
                        ModificationType.ADD.equals(a.getModificationType()) &&
                        1 == a.getAuditOrder());
        assertTrue(match);
        match = revisionLog.getRevisionTypes().stream().anyMatch(a ->
                "2".equals(a.getId()) &&
                        "UserEntity".equals(a.getEntityName()) &&
                        "User".equals(a.getGroupName()) &&
                        ModificationType.MOD.equals(a.getModificationType()) &&
                        1 == a.getAuditOrder());
        assertTrue(match);
    }

    @Test
    public void addAuditEntityForConfiguration() {
        RevisionLog revisionLog = new RevisionLog();
        revisionLog.addEntityAudit("107", "eu.domibus.common.model.configuration.Party", "Party", ModificationType.ADD, 1);
        revisionLog.addEntityAudit("109", "eu.domibus.common.model.configuration.Party", "Party", ModificationType.ADD, 1);
        revisionLog.addEntityAudit("111", "eu.domibus.common.model.configuration.PartyIdType", "Party", ModificationType.ADD, 2);
        revisionLog.addEntityAudit("90", "eu.domibus.common.model.configuration.Configuration", "Pmode", ModificationType.ADD, 0);
        revisionLog.addEntityAudit("67", "eu.domibus.common.model.configuration.Party", "Party", ModificationType.MOD, 1);
        revisionLog.addEntityAudit("65", "eu.domibus.common.model.configuration.Party", "Party", ModificationType.MOD, 1);
        revisionLog.addEntityAudit("107", "eu.domibus.common.model.configuration.Party", "Party", ModificationType.MOD, 1);
        revisionLog.addEntityAudit("109", "eu.domibus.common.model.configuration.Party", "Party", ModificationType.MOD, 1);
        revisionLog.addEntityAudit("48", "eu.domibus.common.model.configuration.Configuration", "Pmode", ModificationType.DEL, 0);
        revisionLog.addEntityAudit("65", "eu.domibus.common.model.configuration.Party", "Party", ModificationType.DEL, 1);
        revisionLog.addEntityAudit("67", "eu.domibus.common.model.configuration.Party", "Party", ModificationType.DEL, 1);
        revisionLog.addEntityAudit("69", "eu.domibus.common.model.configuration.PartyIdType", "Party", ModificationType.DEL, 2);
        assertEquals(2, revisionLog.getRevisionTypes().size());
        boolean match = revisionLog.getRevisionTypes().stream().anyMatch(a ->
                "90".equals(a.getId()) &&
                        "eu.domibus.common.model.configuration.Configuration".equals(a.getEntityName()) &&
                        "Pmode".equals(a.getGroupName()) &&
                        ModificationType.ADD.equals(a.getModificationType()) &&
                        0 == a.getAuditOrder());
        assertTrue(match);
        match = revisionLog.getRevisionTypes().stream().anyMatch(a ->
                "48".equals(a.getId()) &&
                        "eu.domibus.common.model.configuration.Configuration".equals(a.getEntityName()) &&
                        "Pmode".equals(a.getGroupName()) &&
                        ModificationType.DEL.equals(a.getModificationType()) &&
                        0 == a.getAuditOrder());
        assertTrue(match);
    }

}