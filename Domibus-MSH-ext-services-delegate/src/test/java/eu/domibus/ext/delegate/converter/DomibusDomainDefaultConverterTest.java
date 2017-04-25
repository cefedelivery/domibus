package eu.domibus.ext.delegate.converter;

import eu.domibus.api.message.acknowledge.MessageAcknowledgement;
import eu.domibus.ext.domain.MessageAcknowledgementDTO;
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
public class DomibusDomainDefaultConverterTest {

    @Configuration
    @ComponentScan(basePackageClasses = {MessageAcknowledgement.class, MessageAcknowledgementDTO.class, DomibusDomainDefaultConverter.class})
    @ImportResource({
            "classpath:config/ext-services-delegate-domibusContext.xml",
            "classpath:config/commonsTestContext.xml"
    })
    static class ContextConfiguration {

    }

    @Autowired
    DomibusDomainConverter domibusDomainConverter;

    @Autowired
    ObjectService objectService;

    @Test
    public void testConvert() throws Exception {
        MessageAcknowledgement toConvert = (MessageAcknowledgement) objectService.createInstance(MessageAcknowledgement.class);
        final MessageAcknowledgementDTO converted = domibusDomainConverter.convert(toConvert, MessageAcknowledgementDTO.class);
        objectService.assertObjects(converted, toConvert);
    }

    @Test
    public void testConvertList() throws Exception {
        MessageAcknowledgement toConvert1 = (MessageAcknowledgement) objectService.createInstance(MessageAcknowledgement.class);
        MessageAcknowledgement toConvert2 = (MessageAcknowledgement) objectService.createInstance(MessageAcknowledgement.class);

        List<MessageAcknowledgement> toConvertList = new ArrayList<>();
        toConvertList.add(toConvert1);
        toConvertList.add(toConvert2);


        final List<MessageAcknowledgementDTO> convertedList = domibusDomainConverter.convert(toConvertList, MessageAcknowledgementDTO.class);
        objectService.assertObjects(convertedList, toConvertList);
    }
}
