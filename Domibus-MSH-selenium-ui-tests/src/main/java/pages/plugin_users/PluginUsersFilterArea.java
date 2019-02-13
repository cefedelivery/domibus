package pages.plugin_users;

import ddsl.dcomponents.DComponent;
import ddsl.dcomponents.Select;
import ddsl.dobjects.DButton;
import ddsl.dobjects.DInput;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.pagefactory.AjaxElementLocatorFactory;
import utils.PROPERTIES;

public class PluginUsersFilterArea extends DComponent {

	public PluginUsersFilterArea(WebDriver driver) {
		super(driver);
		PageFactory.initElements( new AjaxElementLocatorFactory(driver, PROPERTIES.TIMEOUT), this);
	}

	@FindBy(css = "#authType_id")
	private WebElement authTypeSelectContainer;

	@FindBy(css = "#endPoint_id")
	private WebElement userRoleSelectContainer;


	@FindBy(css = "#process_id")
	private WebElement originalUserInput;

	@FindBy(css = "#partyID_id")
	private WebElement usernameInput;

	@FindBy(css = "#searchbutton_id")
	private WebElement searchButton;

	public boolean isLoaded() throws Exception{

		if(!getSearchButton().isEnabled()){return false;}
		if(!getAuthTypeSelect().isDisplayed()){return false;}
		if(!getUserRoleSelect().isDisplayed()){return false;}
		if(!getOriginalUserInput().isEnabled()){return false;}
		if(!getUsernameInput().isPresent()){return false;}
		return true;
	}

	public void search(String authType, String role, String origUser, String username) throws Exception{
		if(null != authType) getAuthTypeSelect().selectOptionByText(authType);
		if(null != role) getUserRoleSelect().selectOptionByText(role);
		if(null != origUser) getOriginalUserInput().fill(origUser);
		if(null != username) getUsernameInput().fill(username);
		getSearchButton().click();
	}


	public Select getAuthTypeSelect() {
		return new Select(driver, authTypeSelectContainer);
	}

	public Select getUserRoleSelect() {
		return new Select(driver, userRoleSelectContainer);
	}

	public DInput getOriginalUserInput() {
		return new DInput(driver, originalUserInput);
	}

	public DInput getUsernameInput() {
		return new DInput(driver, usernameInput);
	}

	public DButton getSearchButton() {
		return new DButton(driver, searchButton);
	}
}