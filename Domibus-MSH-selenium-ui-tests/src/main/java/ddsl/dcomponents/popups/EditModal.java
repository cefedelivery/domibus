package ddsl.dcomponents.popups;

import ddsl.dcomponents.DComponent;
import ddsl.dobjects.DButton;
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


public class EditModal extends DComponent {
	public EditModal(WebDriver driver) {
		super(driver);
		PageFactory.initElements(new AjaxElementLocatorFactory(driver, PROPERTIES.TIMEOUT), this);
		wait.forXMillis(300);
	}


	@FindBy(css = "md-dialog-container button:nth-of-type(1)")
	protected WebElement okBtn;

	@FindBy(css = "md-dialog-container button:nth-of-type(2)")
	protected WebElement cancelBtn;

	@FindBy(css = "md-dialog-container p")
	protected WebElement title;

	public DButton getOkBtn() {
		return new DButton(driver, okBtn);
	}

	public DButton getCancelBtn() {
		return new DButton(driver, cancelBtn);
	}

	public String getTitle() throws Exception {
		return new DObject(driver, title).getText();
	}

	public void clickOK() throws Exception {
		getOkBtn().click();
		wait.forElementToBeGone(okBtn);
	}

	public void clickCancel() throws Exception {
		getCancelBtn().click();
		wait.forElementToBeGone(cancelBtn);
	}

	public boolean isOKBtnEnabled() throws Exception {
		return getOkBtn().isEnabled();
	}


}
