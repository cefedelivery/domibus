package eu.domibus.api.party;

import eu.domibus.api.process.Process;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
public class Party {

    private Integer entityId;

    protected Set<Identifier> identifiers; //NOSONAR

    protected List<Process> processesWithPartyAsInitiator =new ArrayList<>();

    protected List<Process> processesWithPartyAsResponder =new ArrayList<>();

    protected String name;

    protected String userName;

    protected String endpoint;

    public Set<Identifier> getIdentifiers() {
        return identifiers;
    }

    public void setIdentifiers(Set<Identifier> identifiers) {
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

    public List<Process> getProcessesWithPartyAsInitiator() {
        return processesWithPartyAsInitiator;
    }
    
    public void setProcessesWithPartyAsInitiator(List<Process> processesWithPartyAsInitiator) {
        this.processesWithPartyAsInitiator = processesWithPartyAsInitiator;
    }
    
    public void addProcessesWithPartyAsInitiator(Process process) {
        this.processesWithPartyAsInitiator.add(process);
    }
    
    public List<Process> getProcessesWithPartyAsResponder() {
        return processesWithPartyAsResponder;
    }

    public void setProcessesWithPartyAsResponder(List<Process> processesWithPartyAsResponder) {
        this.processesWithPartyAsResponder = processesWithPartyAsResponder;
    }

    public void addprocessesWithPartyAsResponder(Process process) {
        this.processesWithPartyAsResponder.add(process);
    }

    @Override
    public String toString() {
        return "Party{" +
                "entityId=" + entityId +
                ", identifiers=" + identifiers +
                ", processesWithPartyAsInitiator=" + processesWithPartyAsInitiator.size() +
                ", processesWithPartyAsResponder=" + processesWithPartyAsResponder.size() +
                ", name='" + name + '\'' +
                ", userName='" + userName + '\'' +
                ", endpoint='" + endpoint + '\'' +
                '}';
    }
}
