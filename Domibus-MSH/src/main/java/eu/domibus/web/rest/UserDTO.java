package eu.domibus.web.rest;

import java.util.List;

/**
 * @author Cosmin Baciu
 * @since 3.3
 */
public class UserDTO {

    private Integer id;

    private String username;


    private List<String> authorities;

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
}
