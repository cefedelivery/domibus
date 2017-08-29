package utility;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.WebDriverWait;

public class BrowserSelect {

	WebDriver driver;
	

	public WebDriver startBrowser(String browserName, String url) throws Exception {

		if (browserName.equalsIgnoreCase("firefox")) {
			//FirefoxProfile profile = new FirefoxProfile();

			//profile.setPreference("browser.download.folderList", 2);
			//profile.setPreference("browser.download.manager.showWhenStarting", false);
			//profile.setPreference("browser.download.dir", "c:\\mydownloads");
			//profile.setPreference("browser.helperApps.neverAsk.saveToDisk", "application/zip");

			System.setProperty("webdriver.gecko.driver",
					System.getProperty("user.dir") + "/webDrivers/geckodriver");
			System.out.println("After the setproperty");
			Thread.sleep(5000);
			driver = new FirefoxDriver();
			//driver.manage().window().maximize();

		} else if (browserName.equalsIgnoreCase("chrome")) {
			System.setProperty("webdriver.chrome.driver",
					System.getProperty("user.dir") + "/webDrivers/chromedriver");
			//ChromeOptions chromeOptions = new ChromeOptions();
			//chromeOptions.addArguments("--start-maximized");
			//driver = new ChromeDriver(chromeOptions);
			driver = new ChromeDriver();


		} else if (browserName.equalsIgnoreCase("ie")) {
			
			DesiredCapabilities capabilities = DesiredCapabilities.internetExplorer();
			capabilities.setCapability(InternetExplorerDriver.INTRODUCE_FLAKINESS_BY_IGNORING_SECURITY_DOMAINS, true);
			//capabilities.setCapability(InternetExplorerDriver.IGNORE_ZOOM_SETTING, true);
			System.setProperty("webdriver.ie.driver",
					System.getProperty("user.dir") + "/webDrivers/IEDriverServer.exe");
			driver = new InternetExplorerDriver(capabilities);
			driver.manage().window().maximize();		
		}

		Thread.sleep(5000);
		driver.get(url);
		return driver;
	}
	

}
