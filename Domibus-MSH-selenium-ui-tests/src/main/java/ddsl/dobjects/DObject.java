package ddsl.dobjects;

import org.apache.log4j.Logger;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class DObject {

	protected WebDriver driver;
	protected DWait wait;

	protected Logger log = Logger.getLogger(this.getClass());
	protected WebElement element;

	public DObject(WebDriver driver, WebElement element) {
		wait = new DWait(driver);
		this.driver = driver;
		this.element = element;
	}

	public void clickVoidSpace(){
		try {
			wait.forXMillis(300);
			((JavascriptExecutor)driver).executeScript("document.querySelector('[class*=\"overlay-backdrop\"]').click()");
			wait.forXMillis(300);
		} catch (Exception e) {	}
		wait.forXMillis(300);
	}

	public boolean isPresent(){
		try{
			return wait.forElementToBeVisible(element).isDisplayed();
		}catch (Exception e){}
		return false;
	}

	public boolean isEnabled() throws Exception{
		if(isPresent()){
			wait.forElementToBeEnabled(element);
			return element.isEnabled();}
		throw new Exception("Element not present");
	}

	public String getText() throws Exception{
		if(isPresent()){
			return element.getText().trim();}
		throw new Exception("Element not present");
	}

	public void click() throws Exception{
		if(isEnabled()){
			wait.forElementToBeClickable(element).click();
			wait.forXMillis(100);
		}else {
			throw new Exception("Not enabled");
		}
	}


	public String getAttribute(String attributeName) throws Exception{
		if(isPresent()){
			return element.getAttribute(attributeName).trim();}
		throw new Exception("Element not present");
	}
}
