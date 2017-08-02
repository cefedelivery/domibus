package eu.domibus.core.converter;


import eu.domibus.common.dao.PModeDao;
import eu.domibus.ebms3.common.dao.CachingPModeProvider;
import eu.domibus.ebms3.common.dao.DynamicDiscoveryPModeProvider;
import eu.domibus.ebms3.common.dao.PModeProvider;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * @author Cosmin Baciu
 * @since 3.3
 */
public class CXFExecutorFactory {

    @Autowired
    @Qualifier("taskExecutor")
    Executor executor;

    public Executor getObject() throws Exception {
        return executor;
    }

//    public int getnThreads() {
//        return nThreads;
//    }
//
//    public void setnThreads(int nThreads) {
//        this.nThreads = nThreads;
//    }
}
