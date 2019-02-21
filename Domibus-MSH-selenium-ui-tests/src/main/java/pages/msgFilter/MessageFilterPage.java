package pages.msgFilter;

import ddsl.dcomponents.DomibusPage;
import ddsl.dcomponents.grid.DGrid;
import ddsl.dcomponents.popups.Dialog;
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


public class MessageFilterPage extends DomibusPage {
	public MessageFilterPage(WebDriver driver) {
		super(driver);
		PageFactory.initElements(new AjaxElementLocatorFactory(driver, PROPERTIES.TIMEOUT), this);
		log.info("Message filter grid initializing");
	}


	@FindBy(id = "messageFilterTable")
	private WebElement gridContainer;

	@FindBy(id = "moveupbutton_id")
	private WebElement moveUpBtn;

	@FindBy(id = "movedownbutton_id")
	private WebElement moveDownBtn;

	@FindBy(id = "cancelbutton_id")
	private WebElement cancelBtn;

	@FindBy(id = "savebutton_id")
	private WebElement saveBtn;

	@FindBy(id = "newbutton_id")
	private WebElement newBtn;

	@FindBy(id = "editbutton_id")
	private WebElement editBtn;

	@FindBy(id = "deletebutton_id")
	private WebElement deleteBtn;

	public DGrid grid() {
		return new DGrid(driver, gridContainer);
	}

	public DButton getMoveUpBtn() {
		return new DButton(driver, moveUpBtn);
	}

	public DButton getMoveDownBtn() {
		return new DButton(driver, moveDownBtn);
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


	public boolean isLoaded() throws Exception {
		return (grid().getRowsNo() > 0
				&& getMoveUpBtn().isPresent()
				&& getMoveDownBtn().isPresent()
				&& getCancelBtn().isPresent()
				&& getSaveBtn().isPresent()
				&& getEditBtn().isPresent()
				&& getDeleteBtn().isPresent()
				&& getNewBtn().isEnabled()
		);
	}

	public void saveAndConfirmChanges() throws Exception {
		getSaveBtn().click();
		log.info("saving");
		new Dialog(driver).confirm();
		log.info("confirming");
	}

	public void cancelChangesAndConfirm() throws Exception {
		log.info("cancelling");
		getCancelBtn().click();
		new Dialog(driver).confirm();
		log.info("cancel confirmed");
	}

}
