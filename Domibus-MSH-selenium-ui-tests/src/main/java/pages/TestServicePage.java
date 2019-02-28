package pages;

import ddsl.dcomponents.DomibusPage;
import ddsl.dcomponents.Select;
import ddsl.dobjects.DButton;
import ddsl.dobjects.DInput;
import ddsl.enums.DMessages;
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


public class TestServicePage extends DomibusPage {
	public TestServicePage(WebDriver driver) {
		super(driver);
		PageFactory.initElements(new AjaxElementLocatorFactory(driver, PROPERTIES.TIMEOUT), this);
	}

	@FindBy(id = "receiverPartyId_id")
	WebElement partySelector;

	@FindBy(id = "updatebutton_id")
	WebElement updateBtn;

	@FindBy(id = "testbutton_id")
	WebElement testBtn;
	//------------------------------------------------
	@FindBy(id = "toPartyId_id")
	WebElement toParty;

	@FindBy(id = "toAccessPoint_id")
	WebElement toAccessPoint;

	@FindBy(id = "timeSent_id")
	WebElement timeSent;

	@FindBy(id = "toMessageId_id")
	WebElement toMessage;

	//------------------------------------------------
	@FindBy(id = "fromPartyId_id")
	WebElement fromParty;

	@FindBy(id = "fromAccessPoint_id")
	WebElement fromAccessPoint;

	@FindBy(id = "timeReceived_id")
	WebElement timeReceived;

	@FindBy(id = "fromMessageId_id")
	WebElement fromMessage;


	public Select getPartySelector() {
		return new Select(driver, partySelector);
	}

	public DButton getUpdateBtn() {
		return new DButton(driver, updateBtn);
	}

	public DButton getTestBtn() {
		return new DButton(driver, testBtn);
	}

	public DInput getToParty() {
		return new DInput(driver, toParty);
	}

	public DInput getToAccessPoint() {
		return new DInput(driver, toAccessPoint);
	}

	public DInput getTimeSent() {
		return new DInput(driver, timeSent);
	}

	public DInput getToMessage() {
		return new DInput(driver, toMessage);
	}

	public DInput getFromParty() {
		return new DInput(driver, fromParty);
	}

	public DInput getFromAccessPoint() {
		return new DInput(driver, fromAccessPoint);
	}

	public DInput getTimeReceived() {
		return new DInput(driver, timeReceived);
	}

	public DInput getFromMessage() {
		return new DInput(driver, fromMessage);
	}

	public boolean invalidConfigurationState() throws Exception {
		return (getAlertArea().isError()
				&& getAlertArea().getAlertMessage().equalsIgnoreCase(DMessages.TESTSERVICE_NOTCONFIGURED)
				&& !getTestBtn().isEnabled()
				&& !getUpdateBtn().isEnabled()
				&& getPartySelector().getOptionsTexts().size() == 1
		);
	}


	public boolean isLoaded() {
		return (getPartySelector().isDisplayed()
				&& getTestBtn().isPresent()
				&& getUpdateBtn().isPresent()
				&& getToParty().isPresent()
				&& getToAccessPoint().isPresent()
				&& getTimeSent().isPresent()
				&& getToMessage().isPresent()
				&& getFromParty().isPresent()
				&& getFromAccessPoint().isPresent()
				&& getTimeReceived().isPresent()
				&& getFromMessage().isPresent()
		);
	}
}
