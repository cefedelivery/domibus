package eu.domibus.common.services.impl;

import java.util.Map;

import static eu.domibus.common.services.impl.PullContext.MPC;
import static eu.domibus.common.services.impl.PullContext.NOTIFY_BUSINNES_ON_ERROR;

/**
 * @author Thomas Dussart
 * @since 3.3
 */

public class TestResult {
    private final String mpc;
    private final String pMode;
    private final String notifyBusiness;
    private TestResult next;

    public TestResult(String mpc, String pMode, String notifyBusiness) {
        this.mpc = mpc;
        this.pMode = pMode;
        this.notifyBusiness = notifyBusiness;
    }

    public TestResult chain(TestResult result) {
        next = result;
        return next;
    }

    public boolean testSucced(Map allValue) {
        boolean success = mpc.equals(allValue.get(MPC)) &&
                pMode.equals(allValue.get(PullContext.PMODE_KEY)) &&
                notifyBusiness.equals(allValue.get(NOTIFY_BUSINNES_ON_ERROR));
        if (!success) {
            if(next==null){
                return false;
            }
            return next.testSucced(allValue);
        }
        return success;
    }
}
