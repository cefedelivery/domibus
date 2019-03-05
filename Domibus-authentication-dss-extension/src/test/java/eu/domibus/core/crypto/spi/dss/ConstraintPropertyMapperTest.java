package eu.domibus.core.crypto.spi.dss;

import mockit.Expectations;
import mockit.Mocked;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.core.env.Environment;

import java.util.List;

import static eu.domibus.core.crypto.spi.dss.ConstraintPropertyMapper.DOMIBUS_DSS_DEFAULT_CONSTRAINT_NAME;
import static eu.domibus.core.crypto.spi.dss.ConstraintPropertyMapper.DOMIBUS_DSS_DEFAULT_CONSTRAINT_STATUS;
import static eu.europa.esig.dss.validation.process.MessageTag.ADEST_IRTPTBST;
import static eu.europa.esig.dss.validation.process.MessageTag.QUAL_FOR_SIGN_AT_CC;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
@RunWith(JMockit.class)
public class ConstraintPropertyMapperTest {

    @Test
    public void map(@Mocked Environment environment) {
        new Expectations() {{
            environment.containsProperty("domibus.dss.default.constraint.name[0]");
            result = true;
            environment.getProperty("domibus.dss.default.constraint.name[0]");
            result = ADEST_IRTPTBST.name();
            environment.getProperty("domibus.dss.default.constraint.status[0]");
            result = "OK";
            environment.containsProperty("domibus.dss.default.constraint.name[1]");
            result = true;
            environment.getProperty("domibus.dss.default.constraint.name[1]");
            result = QUAL_FOR_SIGN_AT_CC.name();
            environment.getProperty("domibus.dss.default.constraint.status[1]");
            result = "WARNING";
        }};
        ConstraintPropertyMapper constraintPropertyMapper = new ConstraintPropertyMapper(environment);
        final List<ConstraintInternal> constraints = constraintPropertyMapper.map(DOMIBUS_DSS_DEFAULT_CONSTRAINT_NAME, DOMIBUS_DSS_DEFAULT_CONSTRAINT_STATUS);
        Assert.assertEquals(2, constraints.size());
        Assert.assertTrue(constraints.stream().
                anyMatch(constraintInternal -> constraintInternal.getName().equals(ADEST_IRTPTBST.name()) && constraintInternal.getStatus().equals("OK")));
        Assert.assertTrue(constraints.stream().
                anyMatch(constraintInternal -> constraintInternal.getName().equals(QUAL_FOR_SIGN_AT_CC.name()) && constraintInternal.getStatus().equals("WARNING")));

    }
}