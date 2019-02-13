package pages;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.pagefactory.AjaxElementLocatorFactory;
import ddsl.dcomponents.DomibusPage;
import utils.PROPERTIES;

public class TrustStorePage extends DomibusPage {
	public TrustStorePage(WebDriver driver) {
		super(driver);
		PageFactory.initElements( new AjaxElementLocatorFactory(driver, PROPERTIES.TIMEOUT), this);
	}



	
}
