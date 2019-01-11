package eu.domibus.web.rest.ro;

import java.io.Serializable;
import java.util.List;

/**
 * @author Cosmin Baciu, Catalin Enache
 * @since 3.3
 */
public class UserRO implements Serializable {

    private Integer id;
    private String username;
    private List<String> authorities;
    private boolean defaultPasswordUsed;
    private Integer daysTillExpiration;
    private boolean externalAuthProvider;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public List<String> getAuthorities() {
        return authorities;
    }

    public void setAuthorities(List<String> authorities) {
        this.authorities = authorities;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public boolean isDefaultPasswordUsed() {
        return defaultPasswordUsed;
    }

    public void setDefaultPasswordUsed(boolean defaultPasswordUsed) {
        this.defaultPasswordUsed = defaultPasswordUsed;
    }

    public Integer getDaysTillExpiration() {
        return daysTillExpiration;
    }

    public void setDaysTillExpiration(Integer daysTillExpiration) { this.daysTillExpiration = daysTillExpiration; }

    public boolean isExternalAuthProvider() {
        return externalAuthProvider;
    }

    public void setExternalAuthProvider(boolean externalAuthProvider) {
        this.externalAuthProvider = externalAuthProvider;
    }
}
