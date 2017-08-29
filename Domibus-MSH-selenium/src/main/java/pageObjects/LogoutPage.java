package pageObjects;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import utility.Log;

public class LogoutPage {

	/**
	 *  Objects declaration....
	 */
	WebDriver driver;
	Log log;
	
	//Webelements declaration
	By logoutmenu = By.xpath("//*[@id='settingsmenu_id']");
	By userdetails = By.xpath("//*[@id='currentuser_id']");
	By logoutbutton = By.xpath("//*[@id='logout_id']");

	public LogoutPage(WebDriver driver) {
		this.driver = driver;
	}

	public void clickOnLogoutmenu() throws Exception {
		Log.info("Clicked on the logout menu");
		System.out.println("Came to click the logoutMenu");
		driver.findElement(logoutmenu).click();
		Thread.sleep(1000);
	}

	public void clickOnLogoutButton() throws Exception {
		Log.info("Clicked on the logout button");
		try{
		if(driver.findElement(logoutbutton).isEnabled()){
			System.out.println("Logout button is displayed");
			driver.findElement(logoutbutton).click();
			Thread.sleep(1000);
		}
		else
		System.out.println("logoutbutton is not visible");
		} 
		catch(Exception e){
			System.out.println("Logout exception "+e);
		}
	}
	
	public void userDetails() throws Exception {
		Log.info("identify the userdetails");
		String s=driver.findElement(userdetails).getText();
		System.out.println("User details "+s);
	}
	
	//<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<logoutest steps here>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
	public void logOutTest() throws Exception{
		Log.info("Inside the logout test");
		clickOnLogoutmenu();
		userDetails();
		clickOnLogoutButton();
	}

}
