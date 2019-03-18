package eu.domibus.core.crypto.spi.dss;

import eu.domibus.ext.domain.DomainDTO;
import eu.domibus.ext.services.DomainContextExtService;
import eu.domibus.ext.services.DomibusPropertyExtService;
import mockit.Expectations;
import mockit.Mocked;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.core.env.Environment;

import java.util.List;

import static eu.europa.esig.dss.validation.process.MessageTag.ADEST_IRTPTBST;
import static eu.europa.esig.dss.validation.process.MessageTag.QUAL_FOR_SIGN_AT_CC;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
@RunWith(JMockit.class)
public class ConstraintPropertyMapperTest {

    @Test
    public void map(
            @Mocked DomibusPropertyExtService domibusPropertyExtService,
            @Mocked DomainContextExtService domainContextExtService,
            @Mocked Environment environment) {
        new Expectations() {{
            final DomainDTO result = new DomainDTO("DE", "DEFAULT");
            domainContextExtService.getCurrentDomain();
            this.result = result;
            domibusPropertyExtService.getDomainProperty(result, "domibus.dss.default.constraint.name[0]");
            this.result = ADEST_IRTPTBST.name();
            domibusPropertyExtService.getDomainProperty(result, "domibus.dss.default.constraint.status[0]");
            this.result = "OK";
            domibusPropertyExtService.getDomainProperty(result, "domibus.dss.default.constraint.name[1]");
            this.result = QUAL_FOR_SIGN_AT_CC.name();
            domibusPropertyExtService.getDomainProperty(result, "domibus.dss.default.constraint.status[1]");
            this.result = "WARNING";
        }};
        ValidationConstraintPropertyMapper constraintPropertyMapper =
                new ValidationConstraintPropertyMapper(
                        domibusPropertyExtService,
                        domainContextExtService,
                        environment);

        final List<ConstraintInternal> constraints = constraintPropertyMapper.map();
        Assert.assertEquals(2, constraints.size());
        Assert.assertTrue(constraints.stream().
                anyMatch(constraintInternal -> constraintInternal.getName().equals(ADEST_IRTPTBST.name()) && constraintInternal.getStatus().equals("OK")));
        Assert.assertTrue(constraints.stream().
                anyMatch(constraintInternal -> constraintInternal.getName().equals(QUAL_FOR_SIGN_AT_CC.name()) && constraintInternal.getStatus().equals("WARNING")));

    }
}