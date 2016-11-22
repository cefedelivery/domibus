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

package eu.domibus.common.services.impl;

import eu.domibus.common.dao.ConfigurationDAO;
import eu.domibus.ebms3.security.util.AuthUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;


/**
 * @author Christian Koch, Stefan Mueller
 */
@DisallowConcurrentExecution
public class RetentionWorker extends QuartzJobBean {


    private static final Log LOG = LogFactory.getLog(RetentionWorker.class);


    @Autowired
    private MessageRetentionService messageRetentionService;

    @Autowired
    private ConfigurationDAO configurationDAO;

    @Autowired
    AuthUtils authUtils;

    @Override
    protected void executeInternal(final JobExecutionContext context) throws JobExecutionException {

        RetentionWorker.LOG.debug("RetentionWorker executed");
        if(!authUtils.isUnsecureLoginAllowed()) {
            authUtils.setAuthenticationToSecurityContext("retention_user", "retention_password");
        }

        if (configurationDAO.configurationExists()) {
            messageRetentionService.deleteExpiredMessages();
        }
    }
}
