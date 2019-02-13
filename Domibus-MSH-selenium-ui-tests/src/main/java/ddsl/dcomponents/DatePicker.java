package ddsl.dcomponents;

import ddsl.dobjects.DInput;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.pagefactory.AjaxElementLocatorFactory;
import utils.PROPERTIES;

public class DatePicker extends DComponent {
	public DatePicker(WebDriver driver, WebElement container) {
		super(driver);
		PageFactory.initElements(new AjaxElementLocatorFactory(container, PROPERTIES.TIMEOUT), this);
	}
	
	private final String dateformat = "dd/MM/yyyy HH:mm";
	
	
	@FindBy(className = "md2-datepicker-value")
	private WebElement input;
	
	public void selectDate(String date) throws Exception {
		log.info("inputing date... " + date);

		DInput pickerInput = new DInput(driver, input);
		pickerInput.fill(date);
	}
	
	
}
