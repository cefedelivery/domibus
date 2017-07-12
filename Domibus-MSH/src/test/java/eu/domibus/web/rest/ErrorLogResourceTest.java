package eu.domibus.web.rest;

import eu.domibus.api.util.DateUtil;
import eu.domibus.common.dao.ErrorLogDao;
import mockit.Injectable;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Created by migueti on 12/07/2017.
 */
@RunWith(JMockit.class)
public class ErrorLogResourceTest {

    @Tested
    ErrorLogResource errorLogResource;

    @Injectable
    ErrorLogDao errorLogDao;

    @Injectable
    DateUtil dateUtil;


    @Test
    public void testGetErrorLog() {
        //errorLogResource.getErrorLog()
    }
}
