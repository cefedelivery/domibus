package eu.domibus.ebms3.common.dao;

import eu.domibus.api.xml.UnmarshallerResult;
import eu.domibus.api.xml.XMLUtil;
import eu.domibus.common.dao.ConfigurationDAO;
import eu.domibus.common.dao.PModeDao;
import eu.domibus.common.model.configuration.Configuration;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.xml.XMLUtilImpl;
import org.apache.commons.io.IOUtils;
import org.dozer.DozerBeanMapper;
import org.dozer.Mapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.jms.core.JmsOperations;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import javax.persistence.EntityManagerFactory;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class)
public class DozerMappingTest {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DozerMappingTest.class);


    @org.springframework.context.annotation.Configuration
    static class ContextConfiguration {

        @Bean
        public ConfigurationDAO configurationDAO() {
            return Mockito.mock(ConfigurationDAO.class);
        }

        @Bean
        public EntityManagerFactory entityManagerFactory() {
            return Mockito.mock(EntityManagerFactory.class);
        }

        @Bean
        public JAXBContext jaxbContextConfig() throws JAXBException {
            return JAXBContext.newInstance("eu.domibus.common.model.configuration");
        }

        @Bean
        @Qualifier("jmsTemplateCommand")
        public JmsOperations jmsOperations() throws JAXBException {
            return Mockito.mock(JmsOperations.class);
        }

        @Bean
        public XMLUtil xmlUtil() {
            return new XMLUtilImpl();
        }

        @Bean
        public PModeDao pModeDao() {
            return new PModeDao();
        }

        @Bean
        public Mapper mapper() {
            final DozerBeanMapper mapper = new DozerBeanMapper();
            mapper.setMappingFiles(Arrays.asList("dozer/dozer-mapping.xml"));
            return mapper;
        }
    }

    @Autowired
    PModeDao pModeDao;

    @Autowired
    XMLUtil xmlUtil;

    @Autowired
    ConfigurationDAO configurationDAO;

    @Autowired
    JAXBContext jaxbContext;

    @Autowired
    Mapper mapper;

    @Before
    public void resetMocks() {
        Mockito.reset(configurationDAO);
    }

    @Test
    public void testMarshalling() throws Exception {
        InputStream xmlStream = getClass().getClassLoader().getResourceAsStream("SamplePModes/domibus-configuration-with-whitespaces.xml");
        byte[] pModeBytes = IOUtils.toByteArray(xmlStream);
        UnmarshallerResult unmarshallerResult = xmlUtil.unmarshal(true, jaxbContext, new ByteArrayInputStream(pModeBytes), null);

        final Configuration result = unmarshallerResult.getResult();

        result.preparePersist();

        final Configuration map = mapper.map(result, Configuration.class);

        System.out.println("Plm");

        final ByteArrayOutputStream xml = marshall(result);

        System.out.println("Plm");

    }

    public ByteArrayOutputStream marshall(Configuration configuration) throws JAXBException {
        final Marshaller marshaller = jaxbContext.createMarshaller();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        marshaller.marshal(configuration, baos);
        return baos;
    }


}
