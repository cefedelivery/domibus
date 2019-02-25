package pages;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.pagefactory.AjaxElementLocatorFactory;
import ddsl.dcomponents.DomibusPage;
import utils.PROPERTIES;


/**
 * @author Catalin Comanici

 * @version 4.1
 */


public class PModePage extends DomibusPage {
	public PModePage(WebDriver driver) {
		super(driver);
		PageFactory.initElements(new AjaxElementLocatorFactory(driver, PROPERTIES.TIMEOUT), this);
	}


}
