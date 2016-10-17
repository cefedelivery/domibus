package eu.domibus.taskexecutor.weblogic;

import org.springframework.scheduling.commonj.DelegatingWork;

public class DomibusDelegatingWork extends DelegatingWork {

    public DomibusDelegatingWork(Runnable delegate) {
        super(delegate);
    }

    @Override
    public void run() {
        super.run();
    }

}
