package eu.domibus.common.services.impl;

import eu.domibus.common.model.configuration.Configuration;
import org.dozer.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by musatmi on 24/04/2017.
 */
@Service
public class DozerMappingService {

    @Autowired
    private Mapper mapper;

    public Configuration fromDb(Configuration dbConf) {

        return null;
    }

    public Configuration fromXml(Configuration xmlConf) {
        return null;
    }

}
