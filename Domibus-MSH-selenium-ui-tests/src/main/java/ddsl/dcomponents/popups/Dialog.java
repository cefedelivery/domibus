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


public class Dialog extends DComponent {


	public Dialog(WebDriver driver) {
		super(driver);
		PageFactory.initElements(new AjaxElementLocatorFactory(driver, PROPERTIES.TIMEOUT), this);
	}

	@FindBy(id = "yesbuttondialog_id")
	private WebElement yesBtn;

	@FindBy(id = "nobuttondialog_id")
	private WebElement noBtn;

	@FindBy(css = "md-dialog-container h1")
	private WebElement dialogMessage;

	public void confirm() throws Exception {
		log.info("dialog .. confirm");
		new DButton(driver, yesBtn).click();
		wait.forElementToBeGone(yesBtn);
	}

	public void cancel() throws Exception {
		log.info("dialog .. cancel");
		new DButton(driver, noBtn).click();
		wait.forElementToBeGone(noBtn);
	}

	public String getMessage() throws Exception {
		return new DObject(driver, dialogMessage).getText();
	}


}
