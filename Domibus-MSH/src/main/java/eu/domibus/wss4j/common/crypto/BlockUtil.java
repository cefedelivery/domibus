package eu.domibus.wss4j.common.crypto;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BlockUtil {

    private final static Logger LOG = LoggerFactory.getLogger(BlockUtil.class);

    private static int initiated=0;

    public static boolean getInitiated() {
        return initiated>0;
    }

    public static void increase(){
        initiated++;
    }
}
