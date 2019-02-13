package domibus.ui;

import ddsl.dcomponents.DomibusPage;
import org.apache.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Listeners;
import rest.DomibusRestClient;
import utils.DriverManager;
import utils.PROPERTIES;
import utils.TestDataProvider;
import utils.customReporter.ExcelTestReporter;

@Listeners(ExcelTestReporter.class)
public class BaseTest {

	public static WebDriver driver;
	public static TestDataProvider data = new TestDataProvider();
	public static DomibusRestClient rest = new DomibusRestClient();

	protected Logger log = Logger.getLogger(this.getClass());


	@BeforeSuite(alwaysRun = true)
	/*Starts the browser and navigates to the homepage. This happens once before the test
	suite and the browser window is reused for all tests in suite*/
	public void beforeSuite(){
		log.info("Starting this puppy!!!!");
		driver = DriverManager.getDriver();
		driver.get(PROPERTIES.UI_BASE_URL);
	}


	@AfterSuite(alwaysRun = true)
	/*After the test suite is done we close the browser*/
	public void afterSuite(){
		log.info("Quitting!!!! Buh bye!!!");

		try {
			driver.quit();
		} catch (Exception e) {
			log.warn("Closing the driver failed !!!!");
			e.printStackTrace();
		}
	}

	@AfterMethod(alwaysRun = true)
	protected void logout() throws Exception{
		DomibusPage page = new DomibusPage(driver);
//		refresh will close any remaining opened modals
		page.refreshPage();
		if (page.getSandwichMenu().isLoggedIn()) {
			log.info("Logging out!!!");
			page.getSandwichMenu().logout();
		}
	}
}
