package ddsl.dcomponents;

import ddsl.dobjects.DButton;
import ddsl.dobjects.DObject;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.pagefactory.AjaxElementLocatorFactory;
import utils.PROPERTIES;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Select extends DComponent{


	public Select(WebDriver driver, WebElement container) {
		super(driver);
		log.info("initialize select");
		PageFactory.initElements( new AjaxElementLocatorFactory(container, PROPERTIES.TIMEOUT), this);

		this.selectContainer = container;
		extractOptionIDs();
	}

	protected List<String> optionIDs = new ArrayList<String>();
	protected WebElement selectContainer;


	@FindBy(css = "span[class*=\"select-arrow\"]")
	protected WebElement expandBtn;

	@FindBy(css = "span.md2-select-value")
	protected WebElement selectedOptionValue;

	private void expand(){
		try{
			new DButton(driver, expandBtn).click();
		}catch (Exception e){}
	}

	private void extractOptionIDs(){
		wait.forElementToBeVisible(selectContainer);
		wait.forAttributeNotEmpty(selectContainer, "aria-owns");
		String[] idsAttrib = selectContainer.getAttribute("aria-owns").trim().split(" ");
		optionIDs.addAll(Arrays.asList(idsAttrib));
		log.info("option ids identified");
	}

	public boolean isDisplayed(){
		return (expandBtn.isDisplayed());
	}



	protected List<WebElement> getOptionElements(){

		log.info("searching options for select");
		expand();
		List<WebElement> options = new ArrayList<WebElement>();

		for (int i = 0; i < optionIDs.size(); i++) {
			options.add(driver.findElement(By.id(optionIDs.get(i))));
		}
		return options;
	}

	public boolean selectOptionByText(String text) throws Exception {

		log.info("selecting option with text");
		List<String> texts = getOptionsTexts();
		List<WebElement> options = getOptionElements();

		int index = texts.indexOf(text);
		if(index == -1){return false;}
		options.get(index).click();
		return true;
	}

	public boolean selectOptionByIndex(int index) throws Exception{

		log.info("selecting option by index");
		List<WebElement> options = getOptionElements();

		if(index>=options.size()){
			return false;
		}

		options.get(index).click();
		return true;
	}

	public String getSelectedValue() throws Exception {
		return new DObject(driver, selectedOptionValue).getText();
	}

	public List<String> getOptionsTexts() throws Exception{
		List<WebElement> options = getOptionElements();
		List<String> texts = new ArrayList<>();
		for (WebElement option : options) {
			texts.add(option.getText().trim());
		}
		return texts;
	}
}
