package ddsl.dcomponents;

import ddsl.dobjects.DWait;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Catalin Comanici

 * @version 4.1
 */


public class DComponent {

	protected WebDriver driver;
	public DWait wait;
	protected Logger log = LoggerFactory.getLogger(this.getClass());

	public DComponent(WebDriver driver) {
		this.driver = driver;
		this.wait = new DWait(driver);
	}

	public void clickVoidSpace() {
		try {
			wait.forXMillis(500);
			((JavascriptExecutor) driver).executeScript("document.querySelector('[class*=\"overlay-backdrop\"]').click()");
			wait.forXMillis(500);
		} catch (Exception e) {
		}
	}

}
