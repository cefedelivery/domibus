package utils;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;


/**
 * @author Catalin Comanici

 * @version 4.1
 */


public class DriverManager {

	public static WebDriver getDriver() {
//		System.setProperty(FirefoxDriver.SystemProperty.DRIVER_USE_MARIONETTE,"true");
//		System.setProperty(FirefoxDriver.SystemProperty.BROWSER_LOGFILE,"/dev/null");
//		WebDriver driver = new FirefoxDriver();
//		driver.manage().window().maximize();
//		return driver;

		WebDriver driver = new ChromeDriver();
		((JavascriptExecutor)driver).executeScript("document.body.style.zoom = '10%';");
		driver.manage().window().maximize();

		return driver;
	}


}
