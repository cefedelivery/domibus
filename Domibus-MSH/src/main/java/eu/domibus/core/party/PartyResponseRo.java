package eu.domibus.core.party;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
public class PartyResponseRo {

    private Integer entityId;

    protected Set<IdentifierRo> identifiers; //NOSONAR

    protected String name;

    protected String userName;

    protected String endpoint;

    private String joinedIdentifiers;

    private String joinedProcesses;

    private List<ProcessRo> processesWithPartyAsInitiator =new ArrayList<>();

    private List<ProcessRo> processesWithPartyAsResponder =new ArrayList<>();

    public Set<IdentifierRo> getIdentifiers() {
        return identifiers;
    }

    public void setIdentifiers(Set<IdentifierRo> identifiers) {
        this.identifiers = identifiers;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public Integer getEntityId() {
        return entityId;
    }

    public void setEntityId(Integer entityId) {
        this.entityId = entityId;
    }

    public String getJoinedIdentifiers() {
        return joinedIdentifiers;
    }

    public void setJoinedIdentifiers(String joinedIdentifiers) {
        this.joinedIdentifiers = joinedIdentifiers;
    }

    public String getJoinedProcesses() {
        return joinedProcesses;
    }

    public void setJoinedProcesses(String joinedProcesses) {
        this.joinedProcesses = joinedProcesses;
    }

    public List<ProcessRo> getProcessesWithPartyAsInitiator() {
        return processesWithPartyAsInitiator;
    }

    public void setProcessesWithPartyAsInitiator(List<ProcessRo> processesWithPartyAsInitiator) {
        this.processesWithPartyAsInitiator = processesWithPartyAsInitiator;
    }

    public List<ProcessRo> getProcessesWithPartyAsResponder() {
        return processesWithPartyAsResponder;
    }

    public void setProcessesWithPartyAsResponder(List<ProcessRo> processesWithPartyAsResponder) {
        this.processesWithPartyAsResponder = processesWithPartyAsResponder;
    }

    protected String certificateContent;
    public String getCertificateContent() {
        return certificateContent;
    }
    public void setCertificateContent(String certificateContent) {
        this.certificateContent = certificateContent;
    }
}
