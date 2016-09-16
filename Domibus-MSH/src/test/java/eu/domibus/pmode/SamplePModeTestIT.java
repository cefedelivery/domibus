package eu.domibus.pmode;

import eu.domibus.common.model.configuration.Configuration;
import eu.domibus.common.model.configuration.Mpc;
import eu.domibus.common.model.configuration.Mpcs;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import java.io.File;
import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by Cosmin Baciu on 16-Sep-16.
 */
public class SamplePModeTestIT {

    @Test
    public void testRetentionValuesForBluePmode() throws Exception {
        testRetentionUndownloadedIsBiggerThanZero("src/main/conf/pmodes/domibus-gw-sample-pmode-blue.xml");
    }

    @Test
    public void testRetentionValuesForRedPmode() throws Exception {
        testRetentionUndownloadedIsBiggerThanZero("src/main/conf/pmodes/domibus-gw-sample-pmode-red.xml");
    }

    protected void testRetentionUndownloadedIsBiggerThanZero(String location) throws Exception {
        Configuration bluePmode = readPMode(location);
        assertNotNull(bluePmode);
        Mpcs mpcsXml = bluePmode.getMpcsXml();
        assertNotNull(mpcsXml);
        List<Mpc> mpcList = mpcsXml.getMpc();
        assertNotNull(mpcList);
        for (Mpc mpc : mpcList) {
            assertTrue(mpc.getRetentionUndownloaded() > 0);
            assertTrue(mpc.getRetentionDownloaded() == 0);
        }
    }

    protected Configuration readPMode(String location) throws Exception {
        File pmodeFile = new File(location);
        String pmodeContent = FileUtils.readFileToString(pmodeFile);
        pmodeContent = StringUtils.replaceEach(pmodeContent, new String[]{"<red_hostname>", "<blue_hostname>"}, new String[]{"red_hostname", "blue_hostname"});

        JAXBContext jaxbContext = JAXBContext.newInstance("eu.domibus.common.model.configuration");
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        SAXParserFactory spf = SAXParserFactory.newInstance();
        spf.setNamespaceAware(true);

        XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        XMLEventReader eventReader = inputFactory.createXMLEventReader(IOUtils.toInputStream(pmodeContent));
        return (Configuration) unmarshaller.unmarshal(eventReader);
    }
}
