package eu.domibus.web.rest;

import eu.domibus.common.exception.EbMS3Exception;
import eu.domibus.ebms3.common.dao.PModeProvider;
import eu.domibus.ebms3.common.model.Ebms3Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author Tiago Miguel
 * @since 4.0
 */

@RestController
@RequestMapping(value = "/rest/testservice")
public class TestServiceResource {

    @Autowired
    private PModeProvider pModeProvider;

    @RequestMapping(value = "parties", method = RequestMethod.GET)
    public List<String> getTestParties() throws EbMS3Exception {
        return pModeProvider.findPartyNamesByServiceAndAction(Ebms3Constants.TEST_SERVICE, Ebms3Constants.TEST_ACTION);
    }
}
