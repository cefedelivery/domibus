package eu.domibus.cxf;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.concurrent.Executor;

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
}
