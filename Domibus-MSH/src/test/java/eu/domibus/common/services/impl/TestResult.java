package eu.domibus.common.services.impl;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;

import java.util.Map;

import static eu.domibus.common.services.impl.PullContext.*;

/**
 * @author Thomas Dussart
 * @since 3.3
 */

class TestResult {
    private final String mpc;
    private final String pMode;
    private final String notifyBusiness;
    private TestResult next;
    private final static DomibusLogger LOG = DomibusLoggerFactory.getLogger(TestResult.class);

    TestResult(String mpc, String pMode, String notifyBusiness) {
        this.mpc = mpc;
        this.pMode = pMode;
        this.notifyBusiness = notifyBusiness;
    }

    void chain(TestResult result) {
        next = result;
    }

    boolean testSucced(Map allValue) {
        Object compareMpc = allValue.get(MPC);
        Object compareNotify = allValue.get(NOTIFY_BUSINNES_ON_ERROR);
        Object comparePmode = allValue.get(PMODE_KEY);
        boolean success = mpc.equals(compareMpc) &&
                pMode.equals(comparePmode) &&
                notifyBusiness.equals(compareNotify);
        LOG.info("Comparing " + compareMpc + " " + mpc + " " + comparePmode + " " + pMode + " " + compareNotify + " " + notifyBusiness);
        LOG.info("succes " + success);

        if (!success) {
            if(next==null){
                return false;
            }
            return next.testSucced(allValue);
        }
        return success;
    }
}
