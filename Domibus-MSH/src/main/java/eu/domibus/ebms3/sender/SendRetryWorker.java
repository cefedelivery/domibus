/*
 * Copyright 2015 e-CODEX Project
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they
 * will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the
 * Licence.
 * You may obtain a copy of the Licence at:
 * http://ec.europa.eu/idabc/eupl5
 * Unless required by applicable law or agreed to in
 * writing, software distributed under the Licence is
 * distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied.
 * See the Licence for the specific language governing
 * permissions and limitations under the Licence.
 */

package eu.domibus.ebms3.sender;

import eu.domibus.common.AuthRole;
import eu.domibus.ebms3.security.util.AuthUtils;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;

/**
 * Quartz based worker responsible for the periodical execution of {@link eu.domibus.ebms3.sender.MessageSender#sendUserMessage(String)}
 *
 * @author Christian Koch, Stefan Mueller
 * @since 3.0
 */

@DisallowConcurrentExecution //Only one SenderWorker runs at any time
public class SendRetryWorker extends QuartzJobBean {

    @Autowired
    private RetryService retryService;

    @Autowired
    AuthUtils authUtils;

    @Override
    protected void executeInternal(final JobExecutionContext context) throws JobExecutionException {

        if(!authUtils.isUnsecureLoginAllowed()) {
            authUtils.setAuthenticationToSecurityContext("retry_user", "retry_password");
        }

        retryService.enqueueMessages();
    }


}
