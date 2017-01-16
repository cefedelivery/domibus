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

package eu.domibus.pki;

import eu.domibus.api.configuration.DomibusConfigurationService;
import eu.domibus.common.exception.ConfigurationException;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.cxf.BusFactory;
import org.apache.cxf.ws.policy.PolicyBuilder;
import org.apache.neethi.Policy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * @author Arun Raj
 * @since 3.3
 */
@Service
public class PolicyServiceImpl implements PolicyService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(PolicyServiceImpl.class);

    @Autowired
    DomibusConfigurationService domibusConfigurationService;


    /**
     * To retrieve the domibus security policy xml from the specified location and create the Security Policy object.
     *
     * @param location
     * @return
     * @throws ConfigurationException
     */
    @Override
    @Cacheable("policyCache")
    public Policy parsePolicy(final String location) throws ConfigurationException {
        final PolicyBuilder pb = BusFactory.getDefaultBus().getExtension(PolicyBuilder.class);
        try {
            return pb.getPolicy(new FileInputStream(new File(domibusConfigurationService.getConfigLocation(), location)));
        } catch (IOException | ParserConfigurationException | SAXException e) {
            throw new ConfigurationException(e);
        }
    }

    /**
     * Checks whether the security policy specified is a No Signature - No security policy.
     * If null is provided, a no security policy is assumed.
     * A no security policy would be used to avoid certificate validation.
     *
     * @param policy
     * @return boolean
     */
    @Override
    public boolean isNoSecurityPolicy(Policy policy) {

        if (null == policy) {
            LOG.warn("Security policy provided is null! Assuming no security policy - no signature is specified!");
            return true;
        } else if (policy.isEmpty()) {
            LOG.debug("Policy components are empty! No security policy specified!");
            return true;
        } else {
            return false;
        }
    }
}
