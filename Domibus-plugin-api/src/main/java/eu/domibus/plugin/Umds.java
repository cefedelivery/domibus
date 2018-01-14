package eu.domibus.plugin;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
public class Umds {
    private String domain;
    private String applicationUrl;
    private String user_typeOfIdentifier;
    private String user_identifier;
    private String user_typeOfActor;
    private String delegator_typeOfIdentifier;
    private String delegator_identifier;
    private String delegator_typeOfActor;

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getApplicationUrl() {
        return applicationUrl;
    }

    public void setApplicationUrl(String applicationUrl) {
        this.applicationUrl = applicationUrl;
    }

    public String getUser_typeOfIdentifier() {
        return user_typeOfIdentifier;
    }

    public void setUser_typeOfIdentifier(String user_typeOfIdentifier) {
        this.user_typeOfIdentifier = user_typeOfIdentifier;
    }

    public String getUser_identifier() {
        return user_identifier;
    }

    public void setUser_identifier(String user_identifier) {
        this.user_identifier = user_identifier;
    }

    public String getUser_typeOfActor() {
        return user_typeOfActor;
    }

    public void setUser_typeOfActor(String user_typeOfActor) {
        this.user_typeOfActor = user_typeOfActor;
    }

    public String getDelegator_typeOfIdentifier() {
        return delegator_typeOfIdentifier;
    }

    public void setDelegator_typeOfIdentifier(String delegator_typeOfIdentifier) {
        this.delegator_typeOfIdentifier = delegator_typeOfIdentifier;
    }

    public String getDelegator_identifier() {
        return delegator_identifier;
    }

    public void setDelegator_identifier(String delegator_identifier) {
        this.delegator_identifier = delegator_identifier;
    }

    public String getDelegator_typeOfActor() {
        return delegator_typeOfActor;
    }

    public void setDelegator_typeOfActor(String delegator_typeOfActor) {
        this.delegator_typeOfActor = delegator_typeOfActor;
    }

    @Override
    public String toString() {
        return "Umds{" +
                "domain='" + domain + '\'' +
                ", applicationUrl='" + applicationUrl + '\'' +
                ", user_typeOfIdentifier='" + user_typeOfIdentifier + '\'' +
                ", user_identifier='" + user_identifier + '\'' +
                ", user_typeOfActor='" + user_typeOfActor + '\'' +
                ", delegator_typeOfIdentifier='" + delegator_typeOfIdentifier + '\'' +
                ", delegator_identifier='" + delegator_identifier + '\'' +
                ", delegator_typeOfActor='" + delegator_typeOfActor + '\'' +
                '}';
    }
}
