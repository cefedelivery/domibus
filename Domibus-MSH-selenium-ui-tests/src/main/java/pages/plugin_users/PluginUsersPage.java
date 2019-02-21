package pages.plugin_users;

import ddsl.dcomponents.DomibusPage;
import ddsl.dcomponents.grid.DGrid;
import ddsl.dobjects.DButton;
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


public class PluginUsersPage extends DomibusPage {
	public PluginUsersPage(WebDriver driver) {
		super(driver);
		PageFactory.initElements(new AjaxElementLocatorFactory(driver, PROPERTIES.TIMEOUT), this);
	}

	public PluginUsersFilterArea filters = new PluginUsersFilterArea(driver);

	@FindBy(css = "#userTable")
	private WebElement userGridContainer;

	@FindBy(css = "#userCancelButton")
	private WebElement cancelBtn;

	@FindBy(css = "#userSaveButton")
	private WebElement saveBtn;

	@FindBy(css = "#userNewButton")
	private WebElement newBtn;

	@FindBy(css = "table > tbody > tr > td > button:nth-child(4)")
	private WebElement editBtn;

	@FindBy(css = "#userDeleteButton")
	private WebElement deleteBtn;

	public PluginUsersFilterArea getFilters() {
		return new PluginUsersFilterArea(driver);
	}

	public DButton getCancelBtn() {
		return new DButton(driver, cancelBtn);
	}

	public DButton getSaveBtn() {
		return new DButton(driver, saveBtn);
	}

	public DButton getNewBtn() {
		return new DButton(driver, newBtn);
	}

	public DButton getEditBtn() {
		return new DButton(driver, editBtn);
	}

	public DButton getDeleteBtn() {
		return new DButton(driver, deleteBtn);
	}

	public DGrid grid() {
		return new DGrid(driver, userGridContainer);
	}

	public void newUser(String user, String role, String password, String confirmation) throws Exception {
		getNewBtn().click();

		PluginUserModal popup = new PluginUserModal(driver);
		popup.fillData(user, role, password, confirmation);
		popup.clickOK();
	}


	public boolean isLoaded() throws Exception {

		if (!getCancelBtn().isPresent()) {
			return false;
		}
		if (!getSaveBtn().isPresent()) {
			return false;
		}

		if (!getNewBtn().isEnabled()) {
			return false;
		}
		if (!getEditBtn().isPresent()) {
			return false;
		}
		if (!getDeleteBtn().isPresent()) {
			return false;
		}

		if (!userGridContainer.isDisplayed()) {
			return false;
		}
		if (!filters.isLoaded()) {
			return false;
		}

		return true;
	}
}
