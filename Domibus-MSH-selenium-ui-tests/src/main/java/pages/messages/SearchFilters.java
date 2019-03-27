package pages.messages;

import ddsl.dcomponents.DComponent;
import ddsl.dcomponents.Select;
import ddsl.dobjects.DButton;
import ddsl.dobjects.DInput;
import ddsl.dobjects.DLink;
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
public class SearchFilters extends DComponent {
	public SearchFilters(WebDriver driver) {
		super(driver);
		PageFactory.initElements( new AjaxElementLocatorFactory(driver, PROPERTIES.TIMEOUT), this);
	}


	@FindBy(id = "advancedlink_id")
	WebElement advancedSearchExpandLnk;

	@FindBy(id = "searchbutton_id")
	WebElement searchButton;

	@FindBy(id = "messageid_id")
	WebElement messageIDInput;

	@FindBy(id = "frompartyid_id")
	WebElement fromPartyInput;

	@FindBy(id = "topartyid_id")
	WebElement toPartyInput;

	@FindBy(id = "messagestatus_id")
	WebElement messageStatusContainer;

	@FindBy(id = "basiclink_id")
	WebElement basicSearchLnk;


	@FindBy(id = "conversationid_id")
	WebElement conversationIDInput;

	@FindBy(id = "referencemessageid_id")
	WebElement referenceMessageIDInput;

	@FindBy(id = "originalsender_id")
	WebElement originalSenderInput;

	@FindBy(id = "finalrecipient_id")
	WebElement finalRecipientInput;

	@FindBy(id = "aprole_id")
	WebElement apRoleContainer;

	@FindBy(id = "messagetype_id")
	WebElement messageTypeContainer;

	@FindBy(id = "notificationstatus_id")
	WebElement notificationStatusContainer;


	public DLink getAdvancedSearchExpandLnk() {
		return new DLink(driver, advancedSearchExpandLnk);
	}

	public DButton getSearchButton() {
		return new DButton(driver, searchButton);
	}

	public DInput getMessageIDInput() {
		return new DInput(driver, messageIDInput);
	}

	public DInput getFromPartyInput() {
		return new DInput(driver, fromPartyInput);
	}

	public DInput getToPartyInput() {
		return new DInput(driver, toPartyInput);
	}

	public Select getMessageStatus() {
		return new Select(driver, messageStatusContainer);
	}

	public DLink getBasicSearchLnk() {
		return new DLink(driver, basicSearchLnk);
	}

	public DInput getConversationIDInput() {
		return new DInput(driver, conversationIDInput);
	}

	public DInput getReferenceMessageIDInput() {
		return new DInput(driver, referenceMessageIDInput);
	}

	public DInput getOriginalSenderInput() {
		return new DInput(driver, originalSenderInput);
	}

	public DInput getFinalRecipientInput() {
		return new DInput(driver, finalRecipientInput);
	}

	public Select getApRoleSelect() {
		return new Select(driver, apRoleContainer);
	}

	public Select getMessageTypeSelect() {
		return new Select(driver, messageTypeContainer);
	}

	public Select getNotificationStatus() {
		return new Select(driver, notificationStatusContainer);
	}

	public boolean basicFiltersLoaded()throws Exception{
		return (getMessageIDInput().isEnabled()
		&& getMessageStatus().isDisplayed()
		&& getFromPartyInput().isEnabled()
		&& getToPartyInput().isEnabled()
		&& getToPartyInput().isEnabled()
		);
	}

	public boolean advancedFiltersLoaded()throws Exception{
		return (getMessageIDInput().isEnabled()
		&& getMessageStatus().isDisplayed()
		&& getFromPartyInput().isEnabled()
		&& getToPartyInput().isEnabled()
		&& getToPartyInput().isEnabled()

		&& getConversationIDInput().isPresent()
		&& getApRoleSelect().isDisplayed()
		&& getMessageTypeSelect().isDisplayed()
		&& getNotificationStatus().isDisplayed()
		&& getReferenceMessageIDInput().isPresent()
		&& getOriginalSenderInput().isPresent()
		&& getFinalRecipientInput().isPresent()
		);
	}

}
