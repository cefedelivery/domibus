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


public class InfoModal extends DComponent {

	public InfoModal(WebDriver driver) {
		super(driver);
		PageFactory.initElements(new AjaxElementLocatorFactory(driver, PROPERTIES.TIMEOUT), this);
		wait.forElementToBeVisible(closeBtn);
	}

	@FindBy(css = "md-dialog-actions > button")
	WebElement closeBtn;

	@FindBy(css = "md-dialog-container h2")
	WebElement title;

	public DButton getCloseBtn() {
		return new DButton(driver, closeBtn);
	}

	public String getTitle() throws Exception {
		return new DObject(driver, title).getText();
	}

	public void closeModal() throws Exception {
		getCloseBtn().click();
		wait.forElementToBeGone(closeBtn);
	}


}
