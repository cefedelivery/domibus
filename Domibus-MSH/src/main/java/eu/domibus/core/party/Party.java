package eu.domibus.core.party;

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

    protected List<Process> processesWithMeAsInitiator=new ArrayList<>();
    protected List<Process> processesWithMeAsResponder=new ArrayList<>();

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

    public List<Process> getProcessesWithMeAsInitiator() {
        return processesWithMeAsInitiator;
    }

    public void addProcessesWithMeAsInitiator(Process process) {
        this.processesWithMeAsInitiator.add(process);
    }

    public List<Process> getProcessesWithMeAsResponder() {
        return processesWithMeAsResponder;
    }

    public void addProcessesWithMeAsResponder(Process process) {
        this.processesWithMeAsResponder.add(process);
    }
}
