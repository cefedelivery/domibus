package ddsl.dobjects;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class DInput extends DObject {


	public DInput(WebDriver driver, WebElement element) {
		super(driver, element);
	}

	public void fill(String value) throws Exception {
		if(isEnabled()){
			if(null == value){return;}
			element.clear();
			element.sendKeys(value);
		}else {
			throw new Exception("Cannot type disabled field");
		}
	}

	public void clear() throws Exception {
		if(isEnabled()){
			element.clear();
		}else {
			throw new Exception("Cannot type disabled field");
		}
	}

	@Override
	public String getText(){
		if(isPresent()){
			return element.getAttribute("value").trim();
		}
		return null;
	}


}
