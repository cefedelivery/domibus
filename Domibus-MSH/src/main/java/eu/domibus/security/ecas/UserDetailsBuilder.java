/*
 * ECAS Software
 * Copyright (c) 2015 European Commission
 * Licensed under the EUPL
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * http://ec.europa.eu/idabc/eupl
 *
 * This product includes the CAS software developed by Yale University,
 * Copyright (c) 2000-2004 Yale University. All rights reserved.
 * THE CAS SOFTWARE IS PROVIDED "AS IS," AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, ARE EXPRESSLY
 * DISCLAIMED. IN NO EVENT SHALL YALE UNIVERSITY OR ITS EMPLOYEES BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED, THE COSTS OF
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED IN ADVANCE OF THE POSSIBILITY OF SUCH
 * DAMAGE.
 */

package eu.domibus.security.ecas;

import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Catalin Enache
 * @since 4.1
 */
public class UserDetailsBuilder {

    private String username;

    private List<GrantedAuthority> authorities = new ArrayList<>();



    public UserDetailsBuilder withAuthorities(List<GrantedAuthority> authorities) {
        if (authorities != null) {
            this.authorities.addAll(authorities);
        }
        return this;
    }

    public UserDetailsBuilder withUsername(String username) {
        this.username = username;
        return this;
    }


    public User build() {

        return new User(username, StringUtils.EMPTY, authorities);
    }
}
