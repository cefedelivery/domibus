package eu.domibus.plugin.fs.worker;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ListFactoryBean;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * Boot class for adding the fsPluginTriggerList triggers to domibusStandardTriggerList
 *
 * @author FERNANDES Henrique, GONCALVES Bruno
 */
@Component
public class FSPluginTriggerListBoot {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(FSPurgeSentService.class);

    @Autowired
    @Qualifier("domibusStandardTriggerList")
    private ListFactoryBean domibusStandardTriggerList;

    @Autowired
    @Qualifier("fsPluginTriggerList")
    private ListFactoryBean fsPluginTriggerList;

    /**
     * Initializer
     */
    @PostConstruct
    public void init() {
        try {
            domibusStandardTriggerList.getObject().addAll(fsPluginTriggerList.getObject());
            LOG.debug("fsPluginTriggerList was successfully added to domibusStandardTriggerList");
        } catch (Exception e) {
            LOG.error("fsPluginTriggerList was not added to domibusStandardTriggerList", e);
        }
    }

}
