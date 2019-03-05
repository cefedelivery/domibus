package utils;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;


/**
 * @author Catalin Comanici

 * @version 4.1
 */


public class DriverManager {

	public static WebDriver getDriver() {
		return new FirefoxDriver();
	}


}
