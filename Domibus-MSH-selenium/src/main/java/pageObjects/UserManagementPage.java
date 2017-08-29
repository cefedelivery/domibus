package pageObjects;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.interactions.Actions;

import com.relevantcodes.extentreports.ExtentReports;
import com.relevantcodes.extentreports.ExtentTest;
import com.relevantcodes.extentreports.LogStatus;

import utility.Log;
import utility.PropertyFileRead;
import utility.Screenshot;

public class UserManagementPage {

	/**
	 * Object declaration...
	 */
	WebDriver driver;
	Log log;
	ExtentReports extent;
	ExtentTest test;
	PropertyFileRead p = new PropertyFileRead();
	Actions action;

	// Webelements declaration
	By UserManagementTab = By.xpath("//*[@id='user_id']");
	By UserManagementHead = By.xpath("//*[@id='usersheader_id']");

	By admin = By.xpath("//*[@id='userTable']//span[contains(text(),'admin')]");
	By user = By.xpath("//*[@id='userTable']//span[contains(text(),'user')]");

	By buttonNew = By.xpath("//*[@id='userNewButton']");
	By buttonEdit = By.xpath("//*[@id='userEditButton']");
	By username = By.xpath("//*[@id='username_id']");
	By email = By.xpath("//*[@id='email_id']");
	By roles = By.xpath("//*[@id='roles_id']");
	By Role_A = By.xpath("//md2-option[contains(text(),'ROLE_ADMIN')]");
	By Role_U = By.xpath("//md2-option[contains(text(),'ROLE_USER')]");
	By password = By.xpath("//*[@id='password_id']");
	By conPassword = By.xpath("//*[@id='confirmation_id']");
	By eOK = By.xpath("//*[@id='editbuttonok_id']");
	By eCancel = By.xpath("//*[@id='editbuttoncancel_id']");
	By activecheck = By.tagName("input");

	By buttonDelete = By.xpath("//*[@id='userDeleteButton']");
	By buttonSave = By.xpath("//*[@id='userSaveButton']");
	By sYes = By.xpath("//*[@id='yesbuttondialog_id']");
	By sNo = By.xpath("//*[@id='nobuttondialog_id']");

	By buttonCancel = By.xpath("//*[@id='userCancelButton']");

	By messageAlert = By.xpath("//*[@id='alertmessage_id']");

	public UserManagementPage(WebDriver driver, ExtentReports extent, ExtentTest test) {
		this.driver = driver;
		this.extent = extent;
		this.test = test;
	}

	public void clickUserManagementTab() throws Exception {

		driver.findElement(UserManagementTab).click();
		System.out.println("Inside the click UserManagement tab function");

	}

	public void userEdit(String userN, String emailid, String pass, String cPass) throws Exception {

		String mess = "The operation 'update users' completed successfully";

		By duser = By.xpath("//*[@id='userTable']//span[contains(text(),'" + userN + "')]");
		driver.findElement(user).click();

		System.out.println("edit values from property file are " + userN + emailid + pass + cPass);

		if (!driver.findElement(buttonEdit).isEnabled()) {

			System.out.println("Not able to edit");
		} else {
			driver.findElement(buttonEdit).click();
			Thread.sleep(1000);
			driver.findElement(email).clear();
			driver.findElement(email).sendKeys(emailid);
			driver.findElement(password).sendKeys(pass);
			driver.findElement(conPassword).sendKeys(cPass);
			driver.findElement(eOK).click();
			Thread.sleep(1000);
			driver.findElement(buttonSave).click();
			driver.findElement(sYes).click();

			Thread.sleep(1000);
			if (driver.findElement(messageAlert).getText().contains(mess)) {
				System.out.println("Edit successful " + mess);
			} else
				System.out.println("edit failed ");

		}
	}

	public void userNew(String userN, String emailid, String roleU, String pass, String cPass) throws Exception {

		String mess = "The operation 'update users' completed successfully";
		Actions action = new Actions(driver);

		System.out.println("New user values from property file are " + userN + emailid + pass + cPass);

		if (!driver.findElement(buttonNew).isEnabled()) {

			System.out.println("Not able to edit");
		} else {
			driver.findElement(buttonNew).click();
			Thread.sleep(1000);
			driver.findElement(username).sendKeys(userN);
			driver.findElement(email).clear();
			driver.findElement(email).sendKeys(emailid);

			driver.findElement(roles).click();
			// Role selection
			if (roleU.contains("ROLE_USER")) {
				driver.findElement(Role_U).click();
			}
			if (roleU.contains("ROLE_ADMIN,ROLE_USER")) {
				driver.findElement(Role_A).click();
				driver.findElement(Role_U).click();
				Thread.sleep(2000);
			}

			Thread.sleep(2000);
			action.sendKeys(Keys.TAB).sendKeys(Keys.RETURN);

			driver.findElement(password).sendKeys(pass);

			action.click(driver.findElement(conPassword)).build().perform();

			driver.findElement(conPassword).clear();
			driver.findElement(conPassword).sendKeys(cPass);
			driver.findElement(password).clear();
			driver.findElement(password).sendKeys(pass);
			driver.findElement(conPassword).clear();
			driver.findElement(conPassword).sendKeys(cPass);

			Thread.sleep(5000);
			if (driver.findElement(eOK).isEnabled()) {
				driver.findElement(eOK).click();
			} else {
				driver.findElement(eCancel).click();

			}
			Thread.sleep(1000);
			driver.findElement(buttonSave).click();
			driver.findElement(sYes).click();

			Thread.sleep(1000);
			if (driver.findElement(messageAlert).getText().contains(mess)) {
				System.out.println("Edit successful " + mess);
			} else
				System.out.println("edit failed ");

		}
	}

	public void deleteUser(String dUser) throws Exception {

		String mess = "The operation 'update users' completed successfully";
		By duser = By.xpath("//*[@id='userTable']//span[contains(text(),'" + dUser + "')]");

		driver.findElement(duser).click();

		if (driver.findElement(buttonDelete).isEnabled()) {
			driver.findElement(buttonDelete).click();
			Thread.sleep(2000);
			driver.findElement(buttonSave).click();
			driver.findElement(sYes).click();

		} else {
			driver.findElement(eCancel).click();

		}
		Thread.sleep(1000);
		if (driver.findElement(messageAlert).getText().contains(mess)) {
			System.out.println("Edit successful " + mess);
		} else
			System.out.println("edit failed ");

	}

	/**
	 * This method will perform User Creation, deletion and update.
	 * 
	 * @param i
	 * @param j
	 * @param driver
	 * @throws Exception
	 */
	public void userManagementPageTest(int i) throws Exception {
		Screenshot ss = new Screenshot(driver, extent, test);
		String flag;
		System.out.println("Inside the UserManagement screen page");
		Log.info("Inside the UserManagement screen page");

		clickUserManagementTab();
		test.setDescription("Navigated to UserManagement Page");

		userNew(p.Read("User"), p.Read("email"), p.Read("role"), p.Read("pass"), p.Read("cPass"));
		test.log(LogStatus.PASS, "Navigation to UserManagementNewUser is successfull");
		ss.screenshot(i, "UserManagementNewUser", true);

		Thread.sleep(8000);
		userEdit(p.Read("User"), p.Read("email"), p.Read("pass"), p.Read("cPass"));
		test.log(LogStatus.PASS, "Navigation to UserManagementEdit page is successfull");
		ss.screenshot(i, "UserManagementEdit", true);

		Thread.sleep(7000);
		deleteUser(p.Read("User"));
		test.log(LogStatus.PASS, "Navigation to UserManagementDelete page is successfull");
		ss.screenshot(i, "UserManagementDelete", true);

		// ss.screenshot1(driver, test, i,j, "UserManagementScreen",true);
		Log.info("UserManagementScreen Test completed");

	}

}
