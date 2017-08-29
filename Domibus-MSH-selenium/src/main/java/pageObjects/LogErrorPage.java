package pageObjects;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import com.relevantcodes.extentreports.ExtentReports;
import com.relevantcodes.extentreports.ExtentTest;
import com.relevantcodes.extentreports.LogStatus;

import utility.Log;
import utility.Screenshot;
import utility.configReport;

public class LogErrorPage {

	/**
	 * Objects declaration
	 */
	WebDriver driver;
	ExtentReports extent;
	ExtentTest test;
	
	// Webelements declaration....
	By errorMess = By.cssSelector(".alert.alert-error");

	public LogErrorPage(WebDriver driver, ExtentReports extent, ExtentTest test) {
		this.driver = driver;
		this.extent = extent;
		this.test = test;
	}

	public String LoginPage_ErrorMess() {
		String mess = driver.findElement(errorMess).getText();
		// Removing spaces and the line break.....
		mess = mess.replaceAll("(\\r|\\n)", "");
		return mess;
	}

	/**
	 * @throws Exception
	 *             This method will show the error message if login fails in
	 *             application
	 */
	public void errorPageTest(int i, Exception e) throws Exception {
		int j = 0;
		Screenshot ss = new Screenshot(driver,extent,test);
		Log.info("Inside the ErrorpageTest");
		test.setDescription("Log Failed");
		String error_mess = LoginPage_ErrorMess();
		String exp = "The username/password combination you provided are not valid. Please try again or contact your administrator.";
		if (error_mess.contains(exp)) {
			Thread.sleep(3000);
			ss.screenshot(i, "LoginFailedErrorTrue", true);
			Log.info("Error message displayed correct");
		} else {
			Thread.sleep(3000);
			ss.screenshot(i, "LoginFailedErrorFalse", true);
		}
	}
}
