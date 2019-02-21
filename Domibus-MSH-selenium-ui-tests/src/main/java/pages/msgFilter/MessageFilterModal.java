package pages.msgFilter;

import ddsl.dcomponents.Select;
import ddsl.dcomponents.popups.EditModal;
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


public class MessageFilterModal extends EditModal {
	public MessageFilterModal(WebDriver driver) {
		super(driver);
		PageFactory.initElements(new AjaxElementLocatorFactory(driver, PROPERTIES.TIMEOUT), this);
		log.info("Filter details popup initialized");
	}

	@FindBy(id = "backendfilter_id")
	WebElement pluginSelectContainer;

	@FindBy(id = "from_id")
	public WebElement fromInput;

	@FindBy(id = "to_id")
	public WebElement toInput;

	@FindBy(id = "action_id")
	public WebElement actionInput;

	@FindBy(id = "service_id")
	public WebElement serviceInput;

	@FindBy(css = "md-card > div:nth-child(2) > md-input-container > div > div.mat-input-flex > div > div")
	public WebElement fromErrMess;
	@FindBy(css = "md-card > div:nth-child(3) > md-input-container > div > div.mat-input-flex > div > div")
	public WebElement toErrMess;
	@FindBy(css = "md-card > div:nth-child(4) > md-input-container > div > div.mat-input-flex > div > div")
	public WebElement actionErrMess;
	@FindBy(css = "md-card > div:nth-child(5) > md-input-container > div > div.mat-input-flex > div > div")
	public WebElement serviceErrMess;


	@FindBy(id = "okbutton_id")
	public WebElement okBtn;

	@FindBy(id = "cancelbutton_id")
	public WebElement cancelBtn;

	public Select getPluginSelect() {
		return new Select(driver, pluginSelectContainer);
	}

	public DInput getFromInput() {
		return new DInput(driver, fromInput);
	}

	public DInput getToInput() {
		return new DInput(driver, toInput);
	}

	public DInput getActionInput() {
		return new DInput(driver, actionInput);
	}

	public DInput getServiceInput() {
		return new DInput(driver, serviceInput);
	}

	public DObject getFromErrMess() {
		return new DObject(driver, fromErrMess);
	}

	public DObject getToErrMess() {
		return new DObject(driver, toErrMess);
	}

	public DObject getActionErrMess() {
		return new DObject(driver, actionErrMess);
	}

	public DObject getServiceErrMess() {
		return new DObject(driver, serviceErrMess);
	}

	public void fillForm(String plugin, String from, String to, String action, String service) throws Exception {
		getPluginSelect().selectOptionByText(plugin);
		getFromInput().fill(from);
		getToInput().fill(to);
		getActionInput().fill(action);
		getServiceInput().fill(service);
	}

	public void fillFormPressOK(String plugin, String from, String to, String action, String service) throws Exception {
		fillForm(plugin, from, to, action, service);
		clickOK();
	}

}
