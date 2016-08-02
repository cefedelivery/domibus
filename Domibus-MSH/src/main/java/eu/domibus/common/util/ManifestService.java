package eu.domibus.common.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.TimeZone;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

/**
 * @author Federico Martini
 *         <p/>
 *         Spring will take care of the creation of this Singleton object at startup.
 */
@Service(value = "manifestService")
@Scope(value = "singleton")
public class ManifestService {

    private static final Log LOG = LogFactory.getLog(ManifestService.class);

    private static Attributes attributes;

    private String path = System.getProperty("user.dir") + "/../webapps/domibus/META-INF/";

    // For Spring
    private ManifestService() {
        init();
    }

    // For testing purposes
    ManifestService(String pathToManifest) {
        path = pathToManifest;
        init();
    }

    private void init() {
        try {
            InputStream is = new FileInputStream(path + "MANIFEST.MF");
            Manifest manifest = new Manifest(is);
            attributes = manifest.getMainAttributes();
            LOG.info("==============================================================================================================");
            LOG.info("|         " + getDisplayVersion() + "         |");
            LOG.info("==============================================================================================================");
        } catch (Exception ex) {
            LOG.warn("The manifest has not been found!", ex);
        }
    }

    public String getImplVersion() {
        if (attributes == null) return "";
        return attributes.getValue("Specification-Version");
    }

    public String getSpecTitle() {
        if (attributes == null) return "";
        return attributes.getValue("Specification-Title");
    }

    public String getBuiltTime() {
        if (attributes == null) return "";
        return attributes.getValue("Build-Time") + "|" + TimeZone.getDefault().getDisplayName();
    }

    public String getBuiltBy() {
        if (attributes == null) return "";
        return attributes.getValue("Built-By");
    }

    public String getDisplayVersion() {
        StringBuilder display = new StringBuilder();
        display.append(getSpecTitle());
        display.append(" Version [");
        display.append(getImplVersion());
        display.append("] Build-Time [");
        display.append(getBuiltTime());
        display.append("]");
        return display.toString();
    }

}
