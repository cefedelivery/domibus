package ddsl.dobjects;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;


/**
 * @author Catalin Comanici

 * @version 4.1
 */


public class Checkbox extends DObject {

	public Checkbox(WebDriver driver, WebElement element) {
		super(driver, element);
	}

	public boolean isChecked() throws Exception {
		if (isPresent()) {
			return (null != element.getAttribute("checked"));
		}
		throw new Exception("Element not present");
	}

	public void check() throws Exception {
		if (isChecked()) return;
		if (isEnabled()) {
			element.click();
			return;
		}
		throw new Exception("Checkbox is not enabled");
	}

	public void uncheck() throws Exception {
		if (!isChecked()) return;
		if (isEnabled()) {
			element.click();
			return;
		}
		throw new Exception("Checkbox is not enabled");
	}


}
