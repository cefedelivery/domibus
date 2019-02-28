package pages.login;

import ddsl.dobjects.DInput;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.pagefactory.AjaxElementLocatorFactory;
import ddsl.dcomponents.DomibusPage;
import utils.PROPERTIES;
import utils.TestDataProvider;

import java.util.HashMap;


/**
 * @author Catalin Comanici

 * @version 4.1
 */


public class LoginPage extends DomibusPage {

	public LoginPage(WebDriver driver) {
		super(driver);

		log.info(".... init");

		PageFactory.initElements(new AjaxElementLocatorFactory(driver, PROPERTIES.TIMEOUT), this);
	}

	@FindBy(id = "username_id")
	private WebElement username;

	@FindBy(id = "password_id")
	private WebElement password;

	@FindBy(id = "loginbutton_id")
	private WebElement loginBtn;

	public boolean isLoaded() {

		log.info("check if is loaded");
		wait.forElementToBeVisible(username);
		if (!username.isEnabled()) {
			log.error("Could not find username input");
			return false;
		}
		if (!password.isEnabled()) {
			log.error("Could not find password input");
			return false;
		}
		if (!loginBtn.isDisplayed()) {
			log.error("Could not find login button");
			return false;
		}
		log.info("Login page controls loaded");
		return true;
	}

	public <T extends DomibusPage> T login(String user, String pass, Class<T> expect) {
		log.info("Login started");
		username.clear();
		username.sendKeys(user);
		password.clear();
		password.sendKeys(pass);
		loginBtn.click();
		wait.forElementToBeVisible(helpLnk);
		log.info("Login action done");

		return PageFactory.initElements(driver, expect);
	}

	public void login(String user, String pass) {
		log.info("Login started");
		username.clear();
		username.sendKeys(user);
		password.clear();
		password.sendKeys(pass);
		loginBtn.click();
		wait.forElementToBeVisible(helpLnk);
		log.info("Login action done");
	}

	public void login(String userRole) throws Exception {
		HashMap<String, String> user = new TestDataProvider().getUser(userRole);
		log.info("Login started");
		new DInput(driver, username).fill(user.get("username"));
		new DInput(driver, password).fill(user.get("pass"));
		loginBtn.click();
		wait.forElementToBeVisible(helpLnk);
		log.info("Login action done");
	}

	public void login(HashMap<String, String> user) throws Exception {

		log.info("Login started");
		new DInput(driver, username).fill(user.get("username"));
		new DInput(driver, password).fill(user.get("pass"));
		loginBtn.click();
		wait.forElementToBeVisible(helpLnk);
		log.info("Login action done");
	}


}
