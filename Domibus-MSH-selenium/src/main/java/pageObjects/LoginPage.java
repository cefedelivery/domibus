package pageObjects;

import java.io.File;
import java.util.logging.Logger;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import com.relevantcodes.extentreports.ExtentReports;
import com.relevantcodes.extentreports.ExtentTest;
import com.relevantcodes.extentreports.LogStatus;

import utility.Log;
import utility.PropertyFileRead;
import utility.configReport;

/*This call will store the locators and methods of the login page*/

public class LoginPage {

	/**
	 * Objects Declaration
	 */
	WebDriver driver;
	Log log;
	ExtentReports extent;
	private ExtentTest test;

	/* WebElements declaration */
	By username = By.name("username");
	By password = By.name("password");
	By loginButton = By.xpath("//button[@class='mat-raised-button mat-primary']");
	By loginHeader = By.xpath("//*[@class='domibusTextHeading']");
	By popupHeader = By.xpath("//h1[@class='mat-dialog-title']");
	By popupClickOk = By.xpath("//*[@id='defaultpassbutton_id']");

	public LoginPage(WebDriver driver, ExtentReports extent, ExtentTest test) {
		this.driver = driver;
		this.extent = extent;
		this.test = test;
	}

	public void typeUserName(String uid) {
		Log.info("Enter the username to login");
		driver.findElement(username).sendKeys(uid);
	}

	public void typePassword(String pass) {
		Log.info("Enter the password to login");
		driver.findElement(password).sendKeys(pass);
	}

	public void clickOnLogin() throws Exception {
		Log.info("Click login button to login");
		if (driver.findElement(loginButton).isEnabled()) {
			driver.findElement(loginButton).click();
			Thread.sleep(1000);
		} else {
			System.out.println("login button not enabled please enter user details");
		}
	}

	public String LoginPage_Header() {
		String mess;
		if (driver.findElement(loginHeader).isDisplayed()) {
			mess = driver.findElement(loginHeader).getText();
			System.out.println("Header Value is" + mess);
		} else
			mess = "NO";
		return mess;
	}

	public String popup_header() throws Exception {
		Log.info("Header on popup when we use default password");
		String header=null;
		try {
		if(driver.findElement(popupHeader).isDisplayed()) 
		{	
		header = driver.findElement(popupHeader).getText();
		System.out.println("Value in the popup " + header);
		}
		
		}catch(Exception e) {
			Log.info("Popup header is not visible "+e);
		}
		return header;
	}

	public void popupClick() throws Exception {
		Log.info("click Ok button in the popup");
		Thread.sleep(1000);
		try {
		if (driver.findElement(popupClickOk).isEnabled()) {
			driver.findElement(popupClickOk).click();
			System.out.println("click event happended ");
		} else
			System.out.println("Not used default password");
		}catch(Exception e) {
		Log.info("OK button in the popup is not enabled"+e);
		}
	}

	/**
	 * @throws Exception
	 *             This method will pass login details of user to login to
	 *             application
	 */
	public boolean userLoginPage(int i, String user, String pass) throws Exception {
		Log.info("Inside the userLoginPage function");
		MessagesPage mp = new MessagesPage(driver, extent, test);
		String head = "";
		System.out.println("Inside login page");
		test.setDescription("Verifying the Title Page");
		Log.info("Verifying the Title Page");
		String Title = driver.getTitle();

		if (Title.contains("Domibus")) {
			test.log(LogStatus.PASS, "Title passed");
			Log.info("Title Passed");
			Thread.sleep(3000);
			typeUserName(user);
			typePassword(pass);
			clickOnLogin();
			Thread.sleep(5000);
			try {
				head = mp.getMessageHead();
				if (pass.equalsIgnoreCase("123456")) {
					// header popup verify
					popup_header();
					popupClick();
				} else
					System.out.println("Password has changed");
				if (head.contains("Messages")) {
					mp.ReadMessageTable();
					Thread.sleep(3000);
					System.out.println("Inside Message page");
					test.log(LogStatus.PASS, "Verifying the Login");
					test.log(LogStatus.PASS, "Login Success TC_00" + i);
					String screenshotPath = configReport.screenCapture(driver, "MyAccountPage" + i);
					test.log(LogStatus.PASS, "Screenshot Below: " + test.addScreenCapture(screenshotPath));
					Log.info("Login successfull");
				} else {
					System.out.println("login not success");
					Log.info("Login Unsuccessfull");
					return false;
				}
			} catch (Exception e) {
				System.out.println("Error in Header of the page " + e.getMessage());
				Log.info("Error in the Header of the page" + e.getMessage());
				LogErrorPage ep = new LogErrorPage(driver, extent, test);
				ep.errorPageTest(i, e);
				return false;
			}
		}
		return true;
	}

}
