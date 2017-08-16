/*
 * Copyright 2015 e-CODEX Project
 * Licensed under the EUPL, Version 1.1 or – as soon they
 * will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the
 * Licence.
 * You may obtain a copy of the Licence at:
 * http://ec.europa.eu/idabc/eupl.html
 * Unless required by applicable law or agreed to in
 * writing, software distributed under the Licence is
 * distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied.
 * See the Licence for the specific language governing
 * permissions and limitations under the Licence.
 */

package eu.domibus.plugin.fs.worker;

import mockit.Injectable;
import mockit.Tested;
import mockit.VerificationsInOrder;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.quartz.JobExecutionContext;

/**
 * @author FERNANDES Henrique (hfernandes@linkare.com)
 */
@RunWith(JMockit.class)
public class FSPurgeSentWorkerTest {

    @Injectable
    private FSPurgeSentService purgeSentService;

    @Tested
    private FSPurgeSentWorker purgeSentWorker;

    @Test
    public void testExecuteInternal(@Injectable final JobExecutionContext context) throws Exception {
        purgeSentWorker.executeInternal(context);

        new VerificationsInOrder(1){{
            purgeSentService.purgeSentFSMessages();
        }};
    }

}