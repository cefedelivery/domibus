package ddsl.dcomponents;

import ddsl.dobjects.DObject;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.pagefactory.AjaxElementLocatorFactory;
import utils.PROPERTIES;


/**
 * @author Catalin Comanici

 * @version 4.1
 */


public class DomibusPage extends DComponent {

	public DomibusPage(WebDriver driver) {
		super(driver);
		PageFactory.initElements(new AjaxElementLocatorFactory(driver, PROPERTIES.TIMEOUT), this);
	}

//	@FindBy(css = "#routerHolder h1")
//	private WebElement pageTitle;


	@FindBy(css = "page-header > h1")
	protected WebElement pageTitle;

	@FindBy(css = ".helpMenu")
	protected WebElement helpLnk;


	public AlertArea getAlertArea() {
		return new AlertArea(driver);
	}

	public SideNavigation getSidebar() {
		return new SideNavigation(driver);
	}

	public SandwichMenu getSandwichMenu() {
		return new SandwichMenu(driver);
	}

	public void refreshPage() {
		driver.navigate().refresh();
	}

	public String getTitle() throws Exception {
		DObject pgTitleObj = new DObject(driver, pageTitle);
		String rawTitle = pgTitleObj.getText();

		if (rawTitle.contains(":")) {
//			removing listed domain from title
			return rawTitle.split(":")[1].trim();
		}
		return rawTitle;
	}

	public String getDomainFromTitle() throws Exception {
		DObject pgTitleObj = new DObject(driver, pageTitle);
		String rawTitle = pgTitleObj.getText();

		if (rawTitle.contains(":")) {
//			removing listed title
			return rawTitle.split(":")[0].trim();
		}
		return null;
	}

	public DomainSelector getDomainSelector() throws Exception {
		By domainSelectSelector = By.cssSelector("#sandwichMenuHolder > domain-selector > md-select");
		WebElement element = driver.findElement(domainSelectSelector);
		return new DomainSelector(driver, element);
	}


}
