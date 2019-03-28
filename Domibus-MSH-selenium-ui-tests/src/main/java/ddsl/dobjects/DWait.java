package ddsl.dobjects;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import utils.PROPERTIES;


/**
 * @author Catalin Comanici
 * @version 4.1
 */


public class DWait {

	public WebDriverWait webDriverWait;


	public DWait(WebDriver driver) {
		this.webDriverWait = new WebDriverWait(driver, PROPERTIES.TIMEOUT);
	}

	public void forXMillis(Integer millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public WebElement forElementToBeClickable(WebElement element) {
		return webDriverWait.until(ExpectedConditions.elementToBeClickable(element));
	}

	public WebElement forElementToBeVisible(WebElement element) {
		return webDriverWait.until(ExpectedConditions.visibilityOf(element));
	}

	public void forElementToBeEnabled(WebElement element) {
		int maxTimeout = PROPERTIES.TIMEOUT * 1000;
		int waitedSoFar = 0;
		while ((null != element.getAttribute("disabled")) && (waitedSoFar < maxTimeout)) {
			waitedSoFar += 300;
			forXMillis(300);
		}
	}

	public void forAttributeNotEmpty(WebElement element, String attributeName) {
		webDriverWait.until(ExpectedConditions.attributeToBeNotEmpty(element, attributeName));
	}

	public void forElementToBeGone(WebElement element) {
		try {
			webDriverWait.until(ExpectedConditions.not(ExpectedConditions.visibilityOf(element)));
		} catch (Exception e) {
		}
	}

	public void forElementToBe(WebElement element) {
		int secs = 0;
		while (secs < PROPERTIES.TIMEOUT * 10) {
			try { if (null != element.getText()) {	break;	}	} catch (Exception e) { }
			try {Thread.sleep(100);} catch (InterruptedException e) {}
			secs++;
		}
	}


}
