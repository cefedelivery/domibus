package eu.domibus.taskexecutor.weblogic;

import org.springframework.scheduling.commonj.DelegatingWork;

/**
 * @author Cosmin Baciu
 * @since 3.3
 */
public class DomibusDelegatingWork extends DelegatingWork {

    public DomibusDelegatingWork(Runnable delegate) {
        super(delegate);
    }

    @Override
    public void run() {
        super.run();
    }

}
