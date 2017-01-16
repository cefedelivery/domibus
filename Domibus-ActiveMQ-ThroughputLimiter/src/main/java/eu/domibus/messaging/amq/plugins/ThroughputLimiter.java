/*
 * Copyright 2015 e-CODEX Project
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they
 * will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the
 * Licence.
 * You may obtain a copy of the Licence at:
 * http://ec.europa.eu/idabc/eupl5
 * Unless required by applicable law or agreed to in
 * writing, software distributed under the Licence is
 * distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied.
 * See the Licence for the specific language governing
 * permissions and limitations under the Licence.
 */

package eu.domibus.messaging.amq.plugins;

import eu.domibus.messaging.MessageConstants;
import org.apache.activemq.broker.Broker;
import org.apache.activemq.broker.BrokerFilter;
import org.apache.activemq.broker.ConsumerBrokerExchange;
import org.apache.activemq.broker.ProducerBrokerExchange;
import org.apache.activemq.command.ActiveMQDestination;
import org.apache.activemq.command.Message;
import org.apache.activemq.command.MessageAck;
import org.apache.activemq.command.MessageDispatch;
import org.apache.activemq.store.PList;
import org.apache.activemq.store.PListEntry;
import org.apache.activemq.util.ByteSequence;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;

import javax.jms.JMSException;
import java.io.IOException;

/**
 * This class is an ActiveMQ broker filter. This specific filter is responsible for throttling
 * the throughput of the domibus gateway on a per endpoint basis. It is possible to globally define a maximum of
 * parallel connections per endpoint. This prevents for example clustered environments from overloading single
 * deployments. All configuration is done via {@link ThroughputFilter}.
 *
 * @author Christian Koch, Stefan Mueller
 */
public class ThroughputLimiter extends BrokerFilter {

    public static final String MESSAGE_ID_SEPERATOR = "~";
    public static final String DELAY_PLIST_KEY = "DELAYED_MESSAGES";
    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(ThroughputLimiter.class);
    private final ThroughputFilter filter;
    private final String plistName;
    private long resendDelay;


    public ThroughputLimiter(final Broker next, final ThroughputFilter filter, final long resendDelay) {
        super(next);
        this.filter = filter;
        this.resendDelay = resendDelay;
        plistName = "throughputLimiter." + filter.getName();
    }

    @Override
    public void preProcessDispatch(final MessageDispatch messageDispatch) {
        super.preProcessDispatch(messageDispatch);
    }

    @Override
    public void send(final ProducerBrokerExchange producerExchange, final Message messageSend) throws Exception {
         if(messageSend.getDestination().getPhysicalName().equals((filter.getQueue())))
        {
            try {
                PList plist = null;
                LOG.info("send for message {} called on object {}", messageSend.getMessageId().toString(), this);
                synchronized (ThroughputLimiter.class) {
                    PList.PListIterator i = null;
                    boolean processed = false;
                    boolean delay = false;

                    final String jmsMessageId = messageSend.getMessageId().toString();
                    String value = messageSend.getProperty(filter.getPropertyKey()).toString();
                    if (value == null) {
                        return; //this message is not monitored
                    }
                    plist = getTempDataStore().getPList(plistName);
                    i = plist.iterator();
                    int amount = 0;
                    Object locator = null;
                    String idList = "";
                    while (i.hasNext()) {
                        final PListEntry p = i.next();
                        if (p.getId().equals(value)) {
                            idList = new String(p.getByteSequence().getData());
                            final String[] inProgress = idList.split(MESSAGE_ID_SEPERATOR);
                            for (final String inProgres : inProgress) {
                                if (inProgres.equals(jmsMessageId)) {
                                    processed = true; //we have taken over from another broker and the message is resent
                                    break;
                                }
                            }
                            if (!processed) {
                                amount = inProgress.length;
                                if (amount >= filter.getMaxParallel()) {
                                    delay = true;
                                    delayMessage(messageSend);
                                }
                                locator = p.getLocator();
                            }
                            break;
                        }
                    }

                    if (!processed) {
                        if (delay) {
                            value = DELAY_PLIST_KEY;
                            i.release();
                            i = plist.iterator();
                            PListEntry p = null;
                            while (i.hasNext()) {
                                p = i.next();
                                if (p.getId().equals(DELAY_PLIST_KEY)) {
                                    idList = new String(p.getByteSequence().getData());
                                    amount = idList.split(MESSAGE_ID_SEPERATOR).length;
                                    locator = p.getLocator();
                                    break;
                                }
                            }

                        }


                        if (locator != null) {
                            plist.remove(locator);
                        }
                        if (amount > 0) {
                            idList += MESSAGE_ID_SEPERATOR;
                        }
                        idList += jmsMessageId;
                        LOG.info("send adding to plist {}: key={} value={}", plist.hashCode(), value, idList);
                        plist.addFirst(value, new ByteSequence(idList.getBytes()));
                    }
                    if (i != null) {
                        i.release();
                    }
                }

                super.send(producerExchange, messageSend);
            } catch (final Exception e) {
                LOG.error("", e);
                throw e;
            }
        } else {
             super.send(producerExchange,messageSend);
         }
    }

    private void delayMessage(final Message message) {
        try {
            LOG.info("delaying mesage: jmsMessageId={} delay={}", message.getMessageId().toString(), resendDelay);
            message.setProperty(MessageConstants.DELAY, resendDelay);
        } catch (final IOException e) {
            LOG.error("", e);
        }
    }


    @Override
    public void acknowledge(final ConsumerBrokerExchange consumerExchange, final MessageAck ack) throws Exception {
        try {
            if(ack.getDestination().getPhysicalName().equals((filter.getQueue()))) {
                LOG.info("acknowledge for messageId {} called on object {}", ack.getFirstMessageId().toString(), this);
                synchronized (ThroughputLimiter.class) {
                    if (ack.getMessageCount() != 1) {
                        LOG.error("multi-message acknowledgement must not be used in conjunction with this plugin");
                        throw new JMSException("multi-message acknowledgement must not be used in conjunction with this plugin");
                    }
                    final String jmsMessageId = ack.getFirstMessageId().toString();
                    final PList pList = getTempDataStore().getPList(plistName);
                    final PList.PListIterator i = pList.iterator();
                    PListEntry p = null;
                    String[] inProgress = null;
                    int index = -1;
                    while (i.hasNext() && index == -1) {
                        p = i.next();
                        inProgress = new String(p.getByteSequence().getData()).split(MESSAGE_ID_SEPERATOR);
                        for (int j = 0; j < inProgress.length; j++) {
                            if (inProgress[j].equals(jmsMessageId)) {
                                index = j;
                                break;
                            }
                        }

                    }
                    // p can be null
                    if (index == -1 && p != null) {
                        LOG.error("no acknowledgement possible for {} on {}, plist contains: {}", pList.hashCode(), jmsMessageId, new String(p.getByteSequence().getData()));
                        throw new JMSException("no acknowledgement possible for " + jmsMessageId);
                    } else if (p == null || index == -1) {
                        LOG.error("no acknowledgement possible for {} on {}, plist is null", pList.hashCode(), jmsMessageId);
                        throw new JMSException("no acknowledgement possible for " + jmsMessageId);
                    }

                    final Object locator = p.getLocator();
                    LOG.info("removing from plist {}: key={} value={}", pList.hashCode(), p.getId(), new String(p.getByteSequence().getData()));
                    pList.remove(locator);
                    if (inProgress.length > 1) {
                        inProgress[index] = null;
                        final StringBuilder entry = new StringBuilder("");
                        boolean hasPrevious = false;
                        for (final String inProgres : inProgress) {
                            if (inProgres != null) {
                                if (hasPrevious) {
                                    entry.append(MESSAGE_ID_SEPERATOR);
                                }
                                entry.append(inProgres);
                                hasPrevious = true;
                            }
                        }
                        LOG.info("acknowledge adding to plist {}: key={} value={}", pList.hashCode(), p.getId(), entry);
                        pList.addFirst(p.getId(), new ByteSequence(entry.toString().getBytes()));
                    }
                    i.release();
                }
            }
        } catch (final Exception e) {
            LOG.error("", e);
            throw e;
        } finally {
            super.acknowledge(consumerExchange, ack);
        }
    }
}
