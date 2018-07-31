/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.domibus.common.services;

import java.util.List;

/**
 *
 * @author Pion
 */
public interface UserPersistenceService {
    void updateUsers(List<eu.domibus.api.user.User> users);
}
