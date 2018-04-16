package eu.domibus.configuration;

import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * @Author Flavio Santos
 */
@RunWith(JMockit.class)
public class StorageTest {

    @Tested
    private Storage storage;

    @Injectable
    private Properties domibusProperties;

    @Test
    public void testMissingPayloadFolder() throws IOException {
        String folderName = String.valueOf(System.currentTimeMillis());
        Path tempPath = Paths.get("src", "test", "resources", folderName).toAbsolutePath();
        Assert.assertTrue(Files.notExists(tempPath));

        new Expectations(storage) {{
            domibusProperties.getProperty(Storage.ATTACHMENT_STORAGE_LOCATION);
            result = tempPath.toString();
        }};

        storage.initFileSystemStorage();

        Assert.assertTrue(Files.exists(tempPath));
        File storageDirectory = (File) ReflectionTestUtils.getField(storage, "storageDirectory");
        Assert.assertEquals(tempPath.toAbsolutePath().toString(), storageDirectory.toString());
        Files.delete(tempPath);
    }

    @Test
    public void testWrongPayloadFolder() throws IOException {
        Path tempPath = Paths.get("src", "test", "resources");
        new Expectations(storage) {{
            domibusProperties.getProperty(Storage.ATTACHMENT_STORAGE_LOCATION);
            result = isWindowsOS() ? getWindowsFileSystemIncorrectPath(tempPath.toString()) : getLinuxFileSystemIncorrectPath(tempPath.toString());
        }};

        storage.initFileSystemStorage();

        File storageDirectory = (File) ReflectionTestUtils.getField(storage, "storageDirectory");
        Assert.assertEquals(Paths.get(System.getProperty("java.io.tmpdir")).toString(), storageDirectory.toString());
    }

    private boolean isWindowsOS() {
        String osName = System.getProperty("os.name");
        return (osName.toLowerCase().startsWith("windows"));
    }

    private String getWindowsFileSystemIncorrectPath(String path) {
        //Select a non existent windows file system drive to mock an incorrect path
        char[] possibleDriveLetters = "abcdefghijklmnopqrstuvwxyz".toCharArray();
        List<File> availableDriveLetters = new ArrayList<>(Arrays.asList(File.listRoots()));
        for (int i = 0; i < possibleDriveLetters.length; i++) {
            if (!availableDriveLetters.contains(possibleDriveLetters[i] + ":\\")) {
                return possibleDriveLetters[i] + ":\\" + path;
            }
        }
        return null;
    }

    private String getLinuxFileSystemIncorrectPath(String path) {
        //Select a non existent linux file system path to mock an incorrect path
        return "/wrongpath/" + path;
    }
}
