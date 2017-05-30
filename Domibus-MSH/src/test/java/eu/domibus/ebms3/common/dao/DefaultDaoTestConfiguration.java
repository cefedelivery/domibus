package eu.domibus.ebms3.common.dao;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.PropertySource;

/**
 * Created by dussath on 5/18/17.
 *
 * Default configuration class for dao testing. Allows to load limited configuratin in order
 * to reduce test load time.
 */
@Configuration
@ImportResource("classpath:/spring-dao-context.xml")
@PropertySource("classpath:domibustest.properties")
//@thom I do not have a relative path in the domibutest.properties. Fix this before commit.
public class DefaultDaoTestConfiguration {

}
