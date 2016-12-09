package eu.domibus.common.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.Properties;
import java.util.TimeZone;

/**
 * @author Federico Martini
 *         <p/>
 *         This class is designed to retrieve the main Domibus properties defined in a file and valued using Maven resource filtering.
 *         Spring will take care of the creation of this Singleton object at startup.
 */
@Service(value = "domibusPropertiesService")
public class DomibusPropertiesService {

    private static final Logger LOG = LoggerFactory.getLogger(DomibusPropertiesService.class);

    private static Properties domibusProps = new Properties();

    public DomibusPropertiesService() {
        init();
    }

    public void init() {
        try {
            InputStream is = getClass().getClassLoader().getResourceAsStream("domibus.properties");
            if (is == null) {
                LOG.warn("The 'domibus.properties' has not been found!");
            }
            domibusProps.load(is);
            LOG.info("=========================================================================================================");
            LOG.info("|         " + getDisplayVersion() + "        |");
            LOG.info("=========================================================================================================");
        } catch (Exception ex) {
            LOG.warn("Error loading Domibus properties", ex);
        }
    }

    public String getImplVersion() {
        return domibusProps.getProperty("Artifact-Version");
    }

    public String getArtifactName() {
        return domibusProps.getProperty("Artifact-Name");
    }

    public String getBuiltTime() {
        return domibusProps.getProperty("Build-Time") + "|" + TimeZone.getDefault().getDisplayName();
    }

    public String getDisplayVersion() {
        StringBuilder display = new StringBuilder();
        display.append(getArtifactName());
        display.append(" Version [");
        display.append(getImplVersion());
        display.append("] Build-Time [");
        display.append(getBuiltTime());
        display.append("]");
        return display.toString();
    }

}
