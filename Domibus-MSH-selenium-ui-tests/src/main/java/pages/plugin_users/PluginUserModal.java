package pages.plugin_users;

import ddsl.dcomponents.Select;
import ddsl.dcomponents.popups.EditModal;
import ddsl.dobjects.DButton;
import ddsl.dobjects.DInput;
import ddsl.dobjects.DObject;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.pagefactory.AjaxElementLocatorFactory;
import utils.PROPERTIES;


/**
 * @author Catalin Comanici

 * @version 4.1
 */


public class PluginUserModal extends EditModal {
	public PluginUserModal(WebDriver driver) {
		super(driver);
		PageFactory.initElements(new AjaxElementLocatorFactory(driver, PROPERTIES.TIMEOUT), this);
	}

	@FindBy(css = "#username_id")
	private WebElement userNameInput;

	@FindBy(css = "#originalUser_id")
	private WebElement originalUserInput;

	@FindBy(css = "editbasicpluginuser-form > div > form > md-card > div:nth-child(3) > md2-select")
	private WebElement rolesSelectContainer;

	@FindBy(css = "#password_id")
	private WebElement passwordInput;

	@FindBy(css = "#confirmation_id")
	private WebElement confirmationInput;

	@FindBy(css = "#editbuttonok_id")
	private WebElement okBtn;

	@FindBy(css = "#editbuttoncancel_id")
	private WebElement cancelBtn;

	@FindBy(css = "editbasicpluginuser-form > div > form > md-card > div:nth-child(1) > md-input-container > div > div.mat-input-flex > div > div")
	private WebElement usernameErrMess;

	@FindBy(css = "editbasicpluginuser-form > div > form > md-card > div:nth-child(2) > md-input-container > div > div.mat-input-flex > div > div")
	private WebElement originalUserErrMess;

	@FindBy(css = "editbasicpluginuser-form > div > form > md-card > div:nth-child(4) > md-input-container > div > div.mat-input-flex > div > div")
	private WebElement passErrMess;

	@FindBy(css = "editbasicpluginuser-form > div > form > md-card > div:nth-child(5) > md-input-container > div > div.mat-input-flex > div > div")
	private WebElement confirmationErrMess;

	@FindBy(css = "editbasicpluginuser-form > div > form > md-card > div:nth-child(3) > div")
	private WebElement roleErrMess;


	public void fillData(String user, String role, String password, String confirmation) throws Exception {
		getUserNameInput().fill(user);
		getPasswordInput().fill(password);
		getConfirmationInput().fill(confirmation);

		getRolesSelect().selectOptionByText(role);
	}

	public DInput getUserNameInput() {
		return new DInput(driver, userNameInput);
	}

	public DInput getOriginalUserInput() {
		return new DInput(driver, originalUserInput);
	}

	public Select getRolesSelect() {
		return new Select(driver, rolesSelectContainer);
	}

	public DInput getPasswordInput() {
		return new DInput(driver, passwordInput);
	}

	public DInput getConfirmationInput() {
		return new DInput(driver, confirmationInput);
	}

	@Override
	public DButton getOkBtn() {
		return new DButton(driver, okBtn);
	}

	@Override
	public DButton getCancelBtn() {
		return new DButton(driver, cancelBtn);
	}

	public DObject getUsernameErrMess() {
		return new DObject(driver, usernameErrMess);
	}

	public DObject getPassErrMess() {
		return new DObject(driver, passErrMess);
	}

	public DObject getConfirmationErrMess() {
		return new DObject(driver, confirmationErrMess);
	}

	public DObject getOriginalUserErrMess() {
		return new DObject(driver, originalUserErrMess);
	}

	public DObject getRoleErrMess() {
		return new DObject(driver, roleErrMess);
	}


}
