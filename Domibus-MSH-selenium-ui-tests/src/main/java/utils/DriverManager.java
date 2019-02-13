package utils;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

public class DriverManager {

	public static WebDriver getDriver(){
		WebDriver driver = new ChromeDriver();
		return driver;
    }




}
