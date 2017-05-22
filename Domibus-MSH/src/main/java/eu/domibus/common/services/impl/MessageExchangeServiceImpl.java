package eu.domibus.common.services.impl;

import eu.domibus.common.MessageStatus;
import eu.domibus.common.dao.ProcessDao;
import eu.domibus.common.model.configuration.Process;
import eu.domibus.common.services.MessageExchangeService;
import eu.domibus.ebms3.common.context.MessageExchangeContext;
import eu.domibus.plugin.BackendConnector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static eu.domibus.common.MessageStatus.READY_TO_PULL;

/**
 * Created by dussath on 5/19/17.
 * {@inheritDoc}
 */
@Service
public class MessageExchangeServiceImpl implements MessageExchangeService {

    @Autowired
    private ProcessDao processDao;

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public void upgradeMessageExchangeStatus(MessageExchangeContext messageExchangeContext) {
        List<Process> processes = processDao.findProcessForMessageContext(messageExchangeContext);
        messageExchangeContext.updateStatus(MessageStatus.SEND_ENQUEUED);
        for (Process process : processes) {
            boolean pullProcess = BackendConnector.Mode.PULL.getFileMapping().equals(Process.getBindingValue(process));
            boolean oneWay = BackendConnector.Mep.ONE_WAY.getFileMapping().equals(Process.getMepValue(process));
            //@dussath check with cosmin wich kind of exception to throw here.
            if (pullProcess) {
                if (!oneWay) {
                    throw new RuntimeException("We only support oneway/pull at the moment");
                }
                if (processes.size() > 1) {
                    throw new RuntimeException("This configuration is also mapping another process!");
                }
                messageExchangeContext.updateStatus(READY_TO_PULL);
            }
        }
    }
}

