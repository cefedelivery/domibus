package eu.domibus.web.controller;

import eu.domibus.api.jms.JMSDestination;
import eu.domibus.api.jms.JMSManager;
import eu.domibus.api.jms.JmsMessage;
import eu.domibus.util.JsonUtil;
import org.apache.commons.lang.StringUtils;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
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

    private final static DomibusLogger LOG = DomibusLoggerFactory.getLogger(JmsMonitoringController.class);

    @Autowired
    JMSManager jmsManager;

    @Autowired
    JsonUtil jsonUtil;

    protected SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");


    @RequestMapping(value = {"/home/jmsmonitoring"}, method = {GET, POST})
    public ModelAndView jmsMonitoringPage(
            HttpServletRequest request,
            @RequestParam(value = "source", required = false) final String source,
            @RequestParam(value = "jmsType", required = false) final String jmsType,
            @RequestParam(value = "fromDate", required = false) final String fromDate,
            @RequestParam(value = "toDate", required = false) final String toDate,
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

    protected void addFilters(ModelAndView model, HttpServletRequest request) {
        model.addObject("source", request.getParameter("source"));
        model.addObject("jmsType", request.getParameter("jmsType"));
        model.addObject("selector", request.getParameter("selector"));

        Date from = getFromDate(request.getParameter("fromDate"));
        Date to = getToDate(request.getParameter("toDate"));
        model.addObject("fromDate", df.format(from));
        model.addObject("toDate", df.format(to));
    }

    @RequestMapping(value = {"/home/jmsmessage"}, method = {GET, POST})
    public ModelAndView jmsMessagePage(
            HttpServletRequest request,
            @RequestParam(value = "source", required = false) final String source,
            @RequestParam(value = "selectedMessages", required = false) final List<String> messageIds,
            @RequestParam(value = "action", required = false) final String action
    ) {

        final ModelAndView model = new ModelAndView();
        addFilters(model, request);

        JmsMessage jmsMessage = new JmsMessage();
        if(messageIds != null) {
            model.addObject("multiMessage", messageIds.size() > 1 ? true : false);

            if(messageIds.size() == 1) {
                jmsMessage = jmsManager.getMessage(source, messageIds.iterator().next());
            }
        }

        Map<String, JMSDestination> jmsDestinations = jmsManager.getDestinations();
        model.addObject("destinationMap", jmsDestinations);

        model.addObject("action", action);
        model.addObject("message", jmsMessage);
        if(jmsMessage.getProperties() != null) {
            String originalQueue = jmsMessage.getProperty(JmsMessage.PROPERTY_ORIGINAL_QUEUE);
            model.addObject("originalQueue", originalQueue);
        }

        model.addObject("selectedMessages", messageIds);

        model.setViewName("jmsmessage");
        return model;
    }

    @RequestMapping(value = {"/home/jmsmessage/action"}, method = {GET, POST})
    public ModelAndView jmsMessageActionPage(
            HttpServletRequest request,
            @RequestParam(value = "source", required = false) final String source,
            @RequestParam(value = "type", required = false) final String type,
            @RequestParam(value = "content", required = false) final String content,
            @RequestParam(value = "destinationKey", required = false) final String destination,
            @RequestParam(value = "action", required = false) final String action
    ) {

        final ModelAndView model = new ModelAndView();
        addFilters(model, request);

        StringBuffer messageOutcome = new StringBuffer();

        String[] messageIds = request.getParameterValues("selectedMessages");

        if ("send".equals(action)) {
            if(messageIds != null && messageIds.length > 0) {
                for (String messageId : messageIds) {
                    JmsMessage message = jmsManager.getMessage(source, messageId);
                    boolean success = jmsManager.sendMessageToQueue(message, destination);
                    if(!success) {
                        messageOutcome.append("Failed to send message [" + message.getId() + "]");
                    }

                }
                if(messageOutcome.length() == 0) {
                    messageOutcome.append("Messages sent.");
                }
            } else { //new message
                JmsMessage message = new JmsMessage();
                message.setContent(content);
                message.setType(type);
                message.setProperties(jsonUtil.jsonToMap(request.getParameter("customProperties")));
                boolean success = jmsManager.sendMessageToQueue(message, destination);
                messageOutcome.append(success ? "Message sent." : "Failed to send message.");
            }
        } else if ("move".equals(action)) {
            boolean success = jmsManager.moveMessages(source, destination, messageIds);
            if(!success) {
                messageOutcome.append("Failed to move messages");
            }

            if(messageOutcome.length() == 0) {
                messageOutcome.append("Messages moved.");
            }
        } else if ("remove".equals(action)) {
            boolean success = jmsManager.deleteMessages(source, messageIds);
            if(!success) {
                messageOutcome.append("Failed to delete messages");
            }

            if(messageOutcome.length() == 0) {
                messageOutcome.append("Messages deleted.");
            }
        }
        model.addObject("messageResult", messageOutcome.toString());

        model.setViewName("jmsmessageaction");
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
        return new DateTime().plusDays(1).toDate();
    }
}
