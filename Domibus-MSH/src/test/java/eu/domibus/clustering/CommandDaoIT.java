package eu.domibus.clustering;

import eu.domibus.dao.InMemoryDataBaseConfig;
import eu.domibus.logging.DomibusLoggerFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Date;

/**
 * @author Cosmin Baciu
 * @since 4.0.1
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {InMemoryDataBaseConfig.class, CommandDaoConfig.class})
@ActiveProfiles("IN_MEMORY_DATABASE")
public class CommandDaoIT {

    private final static Logger LOG = DomibusLoggerFactory.getLogger(CommandDaoIT.class);

    @Autowired
    private CommandDao commandDao;


    @Test
    public void createCommand() {
        CommandEntity entity = new CommandEntity();
        entity.setCreationTime(new Date());
        entity.setServerName("ms1");
        entity.setDomain("domain1");
        entity.setCommandName("command1");

        commandDao.create(entity);
    }
}