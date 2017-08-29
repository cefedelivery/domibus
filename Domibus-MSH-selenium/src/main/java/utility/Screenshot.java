package utility;

import java.io.IOException;

import org.openqa.selenium.WebDriver;

import com.relevantcodes.extentreports.ExtentReports;
import com.relevantcodes.extentreports.ExtentTest;
import com.relevantcodes.extentreports.LogStatus;

public class Screenshot {
	
	/**
	 * Objects Declaration
	 */
	WebDriver driver;
	Log log;
	ExtentReports extent;
	ExtentTest test;
	
	public Screenshot(WebDriver driver, ExtentReports extent, ExtentTest test) {
		this.driver = driver;
		this.extent = extent;
		this.test = test;
	}

	
	public void screenshot(int i, String screenName, boolean cond)
			throws Exception {
		if (cond) {
			test.log(LogStatus.PASS, screenName + " screen " + i);
			String screenshotPath = configReport.screenCapture(driver, screenName + i);
			test.log(LogStatus.PASS, "Screenshot Below: " + test.addScreenCapture(screenshotPath));
		} else {
			test.log(LogStatus.FAIL, screenName + " screen " + i);
			String screenshotPath = configReport.screenCapture(driver, screenName + i);
			test.log(LogStatus.FAIL, "Screenshot Below: " + test.addScreenCapture(screenshotPath));
		}
	}
}
