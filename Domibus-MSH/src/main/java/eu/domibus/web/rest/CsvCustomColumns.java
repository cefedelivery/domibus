package eu.domibus.web.rest;

import java.util.HashMap;
import java.util.Map;



public enum CsvCustomColumns {
    AUDIT_RESOURCE,
    ERRORLOG_RESOURCE,
    JMS_RESOURCE,
    MESSAGE_RESOURCE,
    PARTY_RESOURCE,
    TRUSTSTORE_RESOURCE,
    USER_RESOURCE,
    ALERT_RESOURCE,
    ;

    private Map<String, String> customFields;

    private static final Map<String, String> auditMap;
    static {
        auditMap = new HashMap<>();
        auditMap.put("AuditTargetName".toUpperCase(), "Table");
    }

    private static final Map<String,String> errorlogMap;
    static {
        errorlogMap = new HashMap<>();
        errorlogMap.put("ErrorSignalMessageId".toUpperCase(), "Signal Message Id");
        errorlogMap.put("MshRole".toUpperCase(), "AP Role");
        errorlogMap.put("MessageInErrorId".toUpperCase(), "Message Id");
    }

    private static final Map<String, String> jmsMap;
    static {
        jmsMap = new HashMap<>();
        jmsMap.put("type".toUpperCase(), "JMS Type");
        jmsMap.put("Timestamp".toUpperCase(), "Time");
        jmsMap.put("CustomProperties".toUpperCase(), "Custom prop");
        jmsMap.put("Properties".toUpperCase(), "JMS prop");
    }

    private static final Map<String, String> messageMap;
    static {
        messageMap = new HashMap<>();
        messageMap.put("mshRole".toUpperCase(), "AP Role");
    }

    private static final Map<String, String> partyMap;
    static {
        partyMap = new HashMap<>();
        partyMap.put("EndPoint".toUpperCase(), "End point");
        partyMap.put("JoinedIdentifiers".toUpperCase(), "Party id");
        partyMap.put("JoinedProcesses".toUpperCase(), "Process");
    }

    private static final Map<String, String> userMap;
    static {
        userMap = new HashMap<>();
        userMap.put("UserName".toUpperCase(), "Username");
        userMap.put("Roles".toUpperCase(), "Role");
    }

    private static final Map<String, String> truststoreMap;
    static {
        truststoreMap = new HashMap<>();
        truststoreMap.put("ValidFrom".toUpperCase(), "Valid from");
        truststoreMap.put("ValidUntil".toUpperCase(), "Valid until");
    }

    private static final Map<String, String> alertMap;
    static {
        alertMap = new HashMap<>();
        alertMap.put("entityId".toUpperCase(), "Alert Id");
    }


    static {
        AUDIT_RESOURCE.customFields = auditMap;
        ERRORLOG_RESOURCE.customFields = errorlogMap;
        JMS_RESOURCE.customFields = jmsMap;
        MESSAGE_RESOURCE.customFields = messageMap;
        PARTY_RESOURCE.customFields = partyMap;
        TRUSTSTORE_RESOURCE.customFields = truststoreMap;
        USER_RESOURCE.customFields = userMap;
        ALERT_RESOURCE.customFields=alertMap;
    }

    public Map<String, String> getCustomColumns() {
        return this.customFields;
    }
}
