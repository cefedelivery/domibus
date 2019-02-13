package ddsl.dobjects;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class Checkbox extends DObject{

	public Checkbox(WebDriver driver, WebElement element) {
		super(driver, element);
	}

	public boolean isChecked() throws Exception{
		if(isPresent()){
			boolean checked = (null != element.getAttribute("checked"));
			return checked;
		}
		throw new Exception("Element not present");
	}

	public void check() throws Exception{
		if(isChecked()) return;
		if(isEnabled()){
			element.click();
			return;
		}
		throw new Exception("Checkbox is not enabled");
	}

	public void uncheck() throws Exception{
		if(!isChecked()) return;
		if(isEnabled()){
			element.click();
			return;
		}
		throw new Exception("Checkbox is not enabled");
	}


}
