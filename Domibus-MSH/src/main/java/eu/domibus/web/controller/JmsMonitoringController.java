package eu.domibus.web.controller;

import eu.domibus.api.jms.JMSDestination;
import eu.domibus.api.jms.JMSManager;
import eu.domibus.api.jms.JmsMessage;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

/**
 * Created by Cosmin Baciu on 17-Aug-16.
 */
@Controller
public class JmsMonitoringController {

    private final static Log LOG = LogFactory.getLog(JmsMonitoringController.class);

    @Autowired
    JMSManager jmsManager;

    protected SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");


    @RequestMapping(value = {"/home/jmsmonitoring"}, method = {GET, POST})
    public ModelAndView jmsMonitoringPage(
            @RequestParam(value = "source", required = false) final String source,
            @RequestParam(value = "jmsType", required = false) final String jmsType,
            @RequestParam(value = "from", required = false) final String fromDate,
            @RequestParam(value = "to", required = false) final String toDate,
            @RequestParam(value = "selector", required = false) final String selector
    ) {

        final ModelAndView model = new ModelAndView();
        Map<String, JMSDestination> jmsDestinations = jmsManager.getDestinations();
        model.addObject("destinationMap", jmsDestinations);
        model.addObject("source", source);
        model.addObject("jmsType", jmsType);
        model.addObject("selector", selector);

        Date from = getFromDate(fromDate);
        Date to = getToDate(toDate);
        model.addObject("fromDate", df.format(from));
        model.addObject("toDate", df.format(to));

        List<JmsMessage> messages = jmsManager.getMessages(source, jmsType, from, to, selector);

        model.addObject("messages", messages);
        model.setViewName("jmsmonitoring");
        return model;

    }

    protected Date getFromDate(String fromDate) {
        if (StringUtils.isNotEmpty(fromDate)) {
            try {
                return df.parse(fromDate);
            } catch (ParseException e) {
                LOG.error("Error parsing from date [" + fromDate + "]", e);
            }
        }
        return new DateTime().minusDays(30).toDate();
    }

    protected Date getToDate(String toDate) {
        if (StringUtils.isNotEmpty(toDate)) {
            try {
                return df.parse(toDate);
            } catch (ParseException e) {
                LOG.error("Error parsing to date [" + toDate + "]", e);
            }
        }
        return new Date();
    }
}
