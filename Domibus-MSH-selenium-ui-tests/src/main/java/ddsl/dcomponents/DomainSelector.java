package ddsl.dcomponents;

import ddsl.dobjects.DObject;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.pagefactory.AjaxElementLocatorFactory;
import utils.PROPERTIES;

public class DomainSelector extends Select {

	public DomainSelector(WebDriver driver, WebElement container) {
		super(driver, container);
		PageFactory.initElements( new AjaxElementLocatorFactory(container, PROPERTIES.TIMEOUT), this);
	}

	@FindBy(css = "span.mat-select-value")
	protected WebElement selectedOptionValue;

	public String getSelectedValue() throws Exception{
		return new DObject(driver, this.selectedOptionValue).getText();
	}

}
