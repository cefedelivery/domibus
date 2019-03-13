package eu.domibus.core.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.domibus.core.crypto.spi.AuthorizationServiceSpi;
import eu.domibus.core.crypto.spi.model.PullRequest;
import eu.domibus.core.crypto.spi.model.PullRequestMapping;
import eu.domibus.core.crypto.spi.model.UserMessage;
import eu.domibus.core.crypto.spi.model.UserMessageMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.cert.X509Certificate;
import java.util.Iterator;
import java.util.Map;

import static eu.domibus.core.crypto.spi.model.UserMessageMapping.*;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
@Component
@PropertySource(value = "classpath:uumds.properties")
@PropertySource(ignoreResourceNotFound = true, value = "file:${domibus.config.location}/extensions/config/uumds.properties")
public class UUMDSAuthorizationServiceSpiImpl implements AuthorizationServiceSpi {

    private static final Logger LOG = LoggerFactory.getLogger(UUMDSAuthorizationServiceSpiImpl.class);

    protected static final String UUMDS_AUTHORIZATION_SPI = "UUMDS_AUTHORIZATION_SPI";

    @Value("${uumds.config.json.file}")
    private String jsonConfigFilePath;

    private volatile Boolean configurationLoaded = false;

    private final Object lock = new Object();

    private JsonNode rootNodeConfig;

    @PostConstruct
    public void init() {

    }

    private void loadConfiguration() {
        if (!configurationLoaded) {
            synchronized (lock) {
                if (!configurationLoaded) {
                    final byte[] jsonData;
                    try {
                        jsonData = Files.readAllBytes(Paths.get(jsonConfigFilePath));
                        ObjectMapper objectMapper = new ObjectMapper();
                        rootNodeConfig = objectMapper.readTree(jsonData);
                        JsonNode servicesNode = rootNodeConfig.path("services");
                        final String name = servicesNode.get("name").asText();
                        LOG.debug("Service name:[{}]", name);
                        configurationLoaded = true;
                    } catch (IOException e) {
                        throw new IllegalStateException(String.format("Impossibe to load UUM&DS configuration at path:[%s]", jsonConfigFilePath), e);
                    }
                }
            }
        }

    }

    @Override
    public boolean authorize(X509Certificate[] certs, UserMessage userMessage, Map<UserMessageMapping, String> messageMapping) {
        final String serviceValue = userMessage.getCollaborationInfo().getService().getValue();
        final String serviceType = userMessage.getCollaborationInfo().getService().getType();
        final String serviceName = messageMapping.get(SERVICE_NAME);
        final String actionValue = userMessage.getCollaborationInfo().getAction();
        final String actionName = messageMapping.get(ACTION_NAME);
        final String fromPartyName = messageMapping.get(FROM_PARTY_NAME);
        if (LOG.isDebugEnabled()) {
            logIncomingMessage(
                    serviceValue,
                    serviceType,
                    serviceName,
                    actionValue,
                    actionName,
                    fromPartyName);
        }

        return true;
    }

    @Override
    public boolean authorize(X509Certificate[] certs, PullRequest pullRequest, Map<PullRequestMapping, String> pullRequestMapping) {
        return false;
    }

    @Override
    public String getIdentifier() {
        return UUMDS_AUTHORIZATION_SPI;
    }

    private String logIncomingMessage(
            final String service,
            final String serviceType,
            final String serviceName,
            final String action,
            final String actionName,
            final String fromPartyName) {
        return "\nNew UserMessage to get authorization from UUMDS:" +
                "\n\t" +
                String.format("Service:Value[%s], Type[%s], Name[%s]", service, serviceType, serviceName) +
                "\n\t" +
                String.format("Action:Value[%s], Value[%s]", action, actionName) +
                "\n\t" +
                String.format("From party name:[%s]", fromPartyName);
    }

    private String logMessageStaticMetaData(
            final String domain,
            final String subDomain,
            final String application,
            final String typeOfActor) {
        return "\nUserMessage metadata to pass to UUMDS:" +
                "\n\t" +
                String.format("Domain:[%s], subdomain[%s]", domain, subDomain) +
                "\n\t" +
                String.format("Application[%s]", application) +
                "\n\t" +
                String.format("typeOfActor:[%s]", typeOfActor);
    }


}
