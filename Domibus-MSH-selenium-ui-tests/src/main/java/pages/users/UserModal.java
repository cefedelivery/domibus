package pages.users;

import ddsl.dcomponents.Select;
import ddsl.dcomponents.popups.EditModal;
import ddsl.dobjects.Checkbox;
import ddsl.dobjects.DInput;
import ddsl.dobjects.DObject;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.pagefactory.AjaxElementLocatorFactory;
import utils.PROPERTIES;


public class UserModal extends EditModal {
	public UserModal(WebDriver driver) {
		super(driver);
		PageFactory.initElements( new AjaxElementLocatorFactory(driver, PROPERTIES.TIMEOUT), this);
		wait.forElementToBeVisible(okBtn);
	}
	
	@FindBy(id = "username_id")
	WebElement usernameInput;
	
	@FindBy(id = "email_id")
	WebElement emaiInput;
	
	@FindBy(id = "password_id")
	WebElement passwordInput;
	
	@FindBy(id = "confirmation_id")
	WebElement confirmationInput;
	
//	@FindBy(id = "editbuttonok_id")
//	WebElement okBtn;

	@FindBy(css = "md-card > div:nth-child(4) > md2-select")
	WebElement domainSelectContainer;

//	@FindBy(id = "editbuttoncancel_id")
//	WebElement cancelBtn;
	
	@FindBy(css = "md2-select[placeholder=\"Role\"]")
	WebElement rolesSelectContainer;

	@FindBy(css = "edituser-form > div > form > md-card > div:nth-child(7) input")
	WebElement activeChk;


	@FindBy(css = "edituser-form > div > form > md-card > div:nth-child(1) > md-input-container > div > div.mat-input-flex > div > div")
	private WebElement usernameErrMess;
	@FindBy(css = "edituser-form > div > form > md-card > div:nth-child(2) > md-input-container > div > div.mat-input-flex > div > div")
	private WebElement emailErrMess;
//	@FindBy(css = "edituser-form > div > form > md-card > div:nth-child(3) > div")
//	private WebElement roleErrMess;
	@FindBy(css = "edituser-form > div > form > md-card > div:nth-child(5) > md-input-container > div > div.mat-input-flex > div > div")
	private WebElement passErrMess;
	@FindBy(css = "edituser-form > div > form > md-card > div:nth-child(6) > md-input-container > div > div.mat-input-flex > div > div")
	private WebElement confirmationErrMess;



	public DInput getUserNameInput() {
		return new DInput(driver, usernameInput);
	}

	public DInput getEmailInput() {
		return new DInput(driver, emaiInput);
	}

	public DInput getPasswordInput() {
		return new DInput(driver, passwordInput);
	}

	public DInput getConfirmationInput() {
		return new DInput(driver, confirmationInput);
	}

	public Select getDomainSelect() {
		return new Select(driver, domainSelectContainer);
	}

	public Select getRoleSelect() {
		return new Select(driver, rolesSelectContainer);
	}

	public Checkbox getActiveChk() {
		return new Checkbox(driver, activeChk);
	}

	public void fillData(String user, String email, String role, String password, String confirmation) throws Exception{
		getUserNameInput().fill(user);
		getEmailInput().fill(email);
		getRoleSelect().selectOptionByText(role);
		getPasswordInput().fill(password);
		getConfirmationInput().fill(confirmation);
	}

	public boolean isLoaded() {
		return (getUserNameInput().isPresent()
		&& getPasswordInput().isPresent()
		&& getRoleSelect().isDisplayed()
		&& getPasswordInput().isPresent()
		&& getConfirmationInput().isPresent());
	}

	public boolean isActive() throws Exception{
		return getActiveChk().isChecked();
	}

	public DObject getUsernameErrMess() {
		return new DObject(driver, usernameErrMess);
	}

	public DObject getEmailErrMess() {
		return new DObject(driver, emailErrMess);
	}

	public DObject getPassErrMess() {
		return new DObject(driver, passErrMess);
	}

	public DObject getConfirmationErrMess() {
		return new DObject(driver, confirmationErrMess);
	}
}
