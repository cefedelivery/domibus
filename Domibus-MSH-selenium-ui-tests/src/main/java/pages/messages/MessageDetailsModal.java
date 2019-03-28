package pages.messages;

import ddsl.dcomponents.popups.InfoModal;
import ddsl.dobjects.DInput;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.FindBys;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.pagefactory.AjaxElementLocatorFactory;
import utils.PROPERTIES;

import java.util.List;
import java.util.Properties;

/**
 * @author Catalin Comanici
 * @description:
 * @since 4.1
 */
public class MessageDetailsModal extends InfoModal {
	public MessageDetailsModal(WebDriver driver) {
		super(driver);
		PageFactory.initElements(new AjaxElementLocatorFactory(driver, PROPERTIES.TIMEOUT), this);
	}

	@FindBy(css = "app-messagelog-details > md-dialog-content input")
	List<WebElement> inputs;

	public String getValue(String fieldName){
		for (WebElement input : inputs) {
			String curentFieldName = input.getAttribute("placeholder").trim();
			if(curentFieldName.equalsIgnoreCase(fieldName)){
				return new DInput(driver, input).getText();
			}
		}
		return null;
	}

}
