package eu.domibus.ext.delegate.converter;

import eu.domibus.api.message.acknowledge.MessageAcknowledgement;
import eu.domibus.api.message.attempt.MessageAttempt;
import eu.domibus.ext.domain.MessageAcknowledgementDTO;
import eu.domibus.ext.domain.MessageAttemptDTO;
import eu.europa.ec.digit.commons.test.api.ObjectService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import java.util.ArrayList;
import java.util.List;

/**
 * @author migueti, Cosmin Baciu
 * @since 3.3
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class)
public class DomainExtDefaultConverterTest {

    @Configuration
    @ComponentScan(basePackageClasses = {MessageAcknowledgement.class, MessageAcknowledgementDTO.class, DomainExtDefaultConverter.class})
    @ImportResource({
            "classpath:config/ext-services-delegate-domibusContext.xml",
            "classpath:config/commonsTestContext.xml"
    })
    static class ContextConfiguration {

    }

    @Autowired
    DomainExtConverter domibusDomainConverter;

    @Autowired
    ObjectService objectService;

    @Test
    public void testConvertMessageAcknowledge() throws Exception {
        MessageAcknowledgement toConvert = (MessageAcknowledgement) objectService.createInstance(MessageAcknowledgement.class);
        final MessageAcknowledgementDTO converted = domibusDomainConverter.convert(toConvert, MessageAcknowledgementDTO.class);
        objectService.assertObjects(converted, toConvert);
    }

    @Test
    public void testConvertMessageAcknowledgeList() throws Exception {
        MessageAcknowledgement toConvert1 = (MessageAcknowledgement) objectService.createInstance(MessageAcknowledgement.class);
        MessageAcknowledgement toConvert2 = (MessageAcknowledgement) objectService.createInstance(MessageAcknowledgement.class);

        List<MessageAcknowledgement> toConvertList = new ArrayList<>();
        toConvertList.add(toConvert1);
        toConvertList.add(toConvert2);


        final List<MessageAcknowledgementDTO> convertedList = domibusDomainConverter.convert(toConvertList, MessageAcknowledgementDTO.class);
        objectService.assertObjects(convertedList, toConvertList);
    }

    @Test
    public void testConvertMessageAttempt() throws Exception {
        MessageAttempt toConvert = (MessageAttempt) objectService.createInstance(MessageAttempt.class);
        final MessageAttemptDTO converted = domibusDomainConverter.convert(toConvert, MessageAttemptDTO.class);
        objectService.assertObjects(converted, toConvert);
    }

    @Test
    public void testtConvertMessageAttemptList() throws Exception {
        MessageAttempt toConvert1 = (MessageAttempt) objectService.createInstance(MessageAttempt.class);
        MessageAttempt toConvert2 = (MessageAttempt) objectService.createInstance(MessageAttempt.class);

        List<MessageAttempt> toConvertList = new ArrayList<>();
        toConvertList.add(toConvert1);
        toConvertList.add(toConvert2);


        final List<MessageAttemptDTO> convertedList = domibusDomainConverter.convert(toConvertList, MessageAttemptDTO.class);
        objectService.assertObjects(convertedList, toConvertList);
    }
}
