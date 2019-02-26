package domibus.ui;

import ddsl.dcomponents.DomibusPage;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Listeners;
import rest.DomibusRestClient;
import utils.DriverManager;
import utils.PROPERTIES;
import utils.TestDataProvider;
import utils.customReporter.ExcelTestReporter;

/**
 * @author Catalin Comanici
 * @version 4.1
*/

@Listeners(ExcelTestReporter.class)
public class BaseTest {

	public static WebDriver driver;
	public static TestDataProvider data = new TestDataProvider();
	public static DomibusRestClient rest = new DomibusRestClient();

	@SuppressWarnings("WeakerAccess")
	protected final Logger log = LoggerFactory.getLogger(this.getClass().getName());


	/**
	 * Starts the browser and navigates to the homepage. This happens once before the test
	 * suite and the browser window is reused for all tests in suite
	 */
	@BeforeSuite(alwaysRun = true)
	public void beforeSuite(){
		log.info("-------- Starting -------");
		driver = DriverManager.getDriver();
		driver.get(PROPERTIES.UI_BASE_URL);
	}


	/**After the test suite is done we close the browser*/
	@AfterSuite(alwaysRun = true)
	public void afterSuite(){
		log.info("-------- Quitting -------");

		try {
			driver.quit();
		} catch (Exception e) {
			log.warn("Closing the driver failed");
			e.printStackTrace();
		}
	}

	/**After each test method page is refreshed and logout is attempted*/
	@AfterMethod(alwaysRun = true)
	protected void logout() throws Exception{
		DomibusPage page = new DomibusPage(driver);

		/*refresh will close any remaining opened modals*/
		page.refreshPage();
		if (page.getSandwichMenu().isLoggedIn()) {
			log.info("Logging out");
			page.getSandwichMenu().logout();
		}
	}
}
