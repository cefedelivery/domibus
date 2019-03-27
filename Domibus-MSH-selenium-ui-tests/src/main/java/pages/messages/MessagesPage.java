package pages.messages;

import ddsl.dcomponents.DomibusPage;
import ddsl.dcomponents.grid.DGrid;
import ddsl.dobjects.DButton;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.pagefactory.AjaxElementLocatorFactory;
import utils.PROPERTIES;

/**
 * @author Catalin Comanici
 * @description:
 * @since 4.1
 */
public class MessagesPage extends DomibusPage {


	public MessagesPage(WebDriver driver) {
		super(driver);
		log.info("Messages page init");
		PageFactory.initElements( new AjaxElementLocatorFactory(driver, PROPERTIES.TIMEOUT), this);

	}

	@FindBy(id = "messageLogTable")
	private WebElement gridContainer;

	@FindBy(id = "downloadbutton_id")
	WebElement downloadButton;

	@FindBy(id = "resendbutton_id")
	WebElement resendButton;


	public DGrid grid() {
		return new DGrid(driver, gridContainer);
	}

	public DButton getDownloadButton() {
		return new DButton(driver, downloadButton);
	}

	public DButton getResendButton() {
		return new DButton(driver, resendButton);
	}

	public SearchFilters getFilters(){ return new SearchFilters(driver);}

	public boolean isLoaded(){
		return (getDownloadButton().isPresent()
		&& getResendButton().isPresent()
		&& null != grid()
		&& null != getFilters());
	}



}
