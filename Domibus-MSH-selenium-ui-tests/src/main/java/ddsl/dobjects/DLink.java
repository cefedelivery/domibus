package ddsl.dobjects;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;


/**
 * @author Catalin Comanici

 * @version 4.1
 */


public class DLink extends DObject {


	public DLink(WebDriver driver, WebElement element) {
		super(driver, element);
	}

	public String getLinkText() throws Exception {
		if (isPresent()) {
			return super.getText();
		}
		throw new Exception("Element not present");
	}

	public String getLinkTarget() throws Exception {
		if (isPresent()) {
			return element.getAttribute("href").trim();
		}
		throw new Exception("Element not present");
	}


}
