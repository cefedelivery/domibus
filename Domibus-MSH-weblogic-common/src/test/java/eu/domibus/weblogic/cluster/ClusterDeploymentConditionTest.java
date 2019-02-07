package eu.domibus.weblogic.cluster;

import eu.domibus.api.configuration.DomibusConfigurationService;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import static org.junit.Assert.assertTrue;

/**
 * @author Cosmin Baciu
 * @since 4.0.1
 */
@RunWith(JMockit.class)
public class ClusterDeploymentConditionTest {

    @Tested
    ClusterDeploymentCondition clusterDeploymentCondition;


    @Test
    public void testClusterDeploymentFalse(@Injectable ConditionContext context, @Injectable AnnotatedTypeMetadata metadata) {
        new Expectations() {{
            context.getEnvironment().getProperty(DomibusConfigurationService.CLUSTER_DEPLOYMENT);
            result = false;
        }};

        Assert.assertFalse(clusterDeploymentCondition.matches(context, metadata));
    }

    @Test
    public void testClusterDeploymentTrue(@Injectable ConditionContext context, @Injectable AnnotatedTypeMetadata metadata) {
        new Expectations() {{
            context.getEnvironment().getProperty(DomibusConfigurationService.CLUSTER_DEPLOYMENT);
            result = true;
        }};

        assertTrue(clusterDeploymentCondition.matches(context, metadata));
    }
}
