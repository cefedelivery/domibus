package utility;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import com.relevantcodes.extentreports.ExtentReports;

public class configReport {

	ExtentReports extent;

	public static String screenCapture(WebDriver driver, String ScreenshotName) throws IOException {

		TakesScreenshot ts = (TakesScreenshot) driver;
		File source = ts.getScreenshotAs(OutputType.FILE);
		String dest = System.getProperty("user.dir") + "/Domscreen/" + ScreenshotName + ".png";
		System.out.println("Inside the ConfigReport class");
		File destination = new File(dest);
		FileUtils.copyFile(source, destination);

		return dest;
	}

}