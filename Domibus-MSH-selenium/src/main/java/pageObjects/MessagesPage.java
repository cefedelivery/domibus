package pageObjects;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.relevantcodes.extentreports.ExtentReports;
import com.relevantcodes.extentreports.ExtentTest;
import com.relevantcodes.extentreports.LogStatus;

import utility.Log;
import utility.PropertyFileRead;
import utility.Screenshot;
import utility.configReport;

public class MessagesPage {

	/**
	 * Object declaration...
	 */
	WebDriver driver;
	Log log;
	ExtentReports extent;
	ExtentTest test;
	PropertyFileRead p = new PropertyFileRead();
	Actions action;

	// Webelements declaration
	By MessagesTab = By.xpath("//*[@id='messages_id']");
	By MessageHead = By.xpath("//*[@id='messagesheader_id']");
	By MessageID = By.xpath("//*[@id='messageid_id']");/* Normal search */
	By ConverstionID = By.id("conversationid_id");
	By MSHRole = By.name("mshRole");
	By mshRoleSending = By.xpath("//*[@class='mat-option'][2]");
	By mshRoleReceiving = By.xpath("//*[@class='mat-option'][3]");
	By MessageType = By.name("messageType");
	By msgTypeUser = By.xpath("//*[@class='mat-option mat-selected']");
	By msgTypeSignal = By.xpath("//*[@class='mat-option'][2]");
	By MessageStatus = By.id("messagestatus_id"); /* Normal search */
	By NotificationStatus = By.name("notificationStatus");
	By FromPartyID = By.name("fromPartyId"); /* Normal search */
	By ToPartyID = By.name("toPartyId"); /* Normal search */
	By RefMessID = By.name("refToMessageId");
	By OriginalSender = By.name("originalSender");
	By FinalRecepient = By.name("finalRecipient");
	By FromTimeStamp = By.xpath("//*[@name='receivedFrom']//*[@class='md2-datepicker-button']");
	By FromTimeStamptxt = By.xpath("//*[@name='receivedFrom']//*[@class='md2-datepicker-value']");
	By ToTimeStamp = By.xpath("//*[@name='receivedTo']//*[@class='md2-datepicker-button']");
	By ToTimeStamptxt = By.xpath("//*[@name='receivedTo']//*[@class='md2-datepicker-value']");
	By searchButton = By.xpath("//*[@id='searchbutton_id']");
	By buttonDownload = By.xpath("//*[@id='downloadbutton_id']");
	By buttonResent = By.xpath("//*[@id='resendbutton_id']");
	By messageDialogResendButton = By.xpath("//*[@id='messageDialogResendButton']");
	By messageDialogCancelButton = By.xpath("//*[@id='messageDialogCancelButton']");
	By statusMessage = By.xpath("//*[@id='alertmessage_id']");
	By advanced = By.xpath("//*[@id='advancedlink_id']");
	By basic = By.xpath("//*[@id='basiclink_id']");
	By columnsoff = By.xpath("//*[@id='columnslinkoff_id']");
	By columnson = By.xpath("//*[@id='columnslinkon_id']");

	/*
	 * checkbox items By boxMessageId=By.id("Message Id"); By
	 * boxFromPartyId=By.id("From Party Id"); By boxToPartyId=By.id("To Party Id");
	 * By boxMessageStatus=By.id("Message Status"); By
	 * boxNotificationStatus=By.id("Notification Status"); By
	 * boxReceived=By.id("Received"); By boxAPRole=By.id("AP Role"); By
	 * boxSendAttempts=By.id("Send Attempts"); By
	 * boxSendAttemptsMax=By.id("Send Attempts Max"); By
	 * boxConversationId=By.id("Conversation Id"); By
	 * boxMessageType=By.id("Message Type"); By boxDeleted=By.id("Deleted"); By
	 * boxOriginalSender=By.id("Original Sender"); By
	 * boxRefToMessageId=By.id("Ref To Message Id"); By boxFailed=By.id("Failed");
	 * By boxRestored=By.id("Restored");
	 */
	By all = By.id("all_button_id");
	By none = By.id("none_button_id");
	String MessagelogTable = "//*[contains(@class,'datatable-body') and contains(@class,'datatable-body-row')][1]//span[contains(text(),'domibus.eu')]";
	String MessagelogTable1 = "//*[contains(@class,'datatable-body') and contains(@class,'datatable-body-row')]";

	public MessagesPage(WebDriver driver, ExtentReports extent, ExtentTest test) {
		this.driver = driver;
		this.extent = extent;
		this.test = test;
	}

	public void clickMessagesTab() throws Exception {
		Log.info("Click on the message Tab");
		driver.findElement(MessagesTab).click();
		System.out.println("Inside the click message tab function");
		Thread.sleep(1000);
	}

	public String getMessageHead() {
		String Header=null;
		Log.info("Get the message page header");
		try {
		
		if(driver.findElement(MessageHead).isDisplayed()) {
			Header= driver.findElement(MessageHead).getText();
		System.out.println("Inside the get message head function");
		}
		}catch(Exception e) {
			Log.info("Header is not visible"+e);
		}
		return Header;
	}

	public void putMessageId(String Mid) {
		Log.info("Enter the messageId");
		// Mid="NO";
		if ((Mid).equals("NO")) {
			System.out.println("We have not given Mid");
		} else {
			driver.findElement(MessageID).sendKeys(Mid);
			System.out.println("Inside the putMessageId function");
		}
	}

	public void putConversionId(String Cid) {
		Log.info("Click on the conversionID Tab");
		if ((Cid).equals("NO")) {
			System.out.println("We have not given SMid");
		} else {
			driver.findElement(ConverstionID).sendKeys(Cid);
		}
	}

	public void selectMSHRole(String mshValue) throws Exception {
		Log.info("Select the MSHROLE");
		if ((mshValue).equals("NO")) {
			System.out.println("We have not given mshValue");
		} else {
			driver.findElement(MSHRole).click();
			Thread.sleep(1000);
			if (mshValue.contains("SENDING"))
				driver.findElement(mshRoleSending).click();
			if (mshValue.contains("RECEIVING"))
				driver.findElement(mshRoleReceiving).click();
		}
		Thread.sleep(1000);
	}

	public void selectMessageType(String msgTyp) throws Exception {
		Log.info("Select the messageType");
		if ((msgTyp).equals("NO")) {
			System.out.println("We have not given msgTyp");
		} else {
			System.out.println("We are in  msgTyp");
			if (msgTyp.equalsIgnoreCase("USER_MESSAGE")) {
				driver.findElement(MessageType).click();
				driver.findElement(msgTypeUser).click();
			} else {
				driver.findElement(MessageType).click();
				driver.findElement(msgTypeSignal).click();
			}
		}
		Thread.sleep(1000);
	}

	public void selectMessageStatus(String msgSelect) throws Exception {
		Log.info("Select the messageStatus");
		if ((msgSelect).equals("NO")) {
			System.out.println("We have not given mshValue");
		} else {
			driver.findElement(MessageStatus).click();
			ArrayList<WebElement> errorValue = (ArrayList<WebElement>) driver
					.findElements(By.xpath("//*[@class='mat-option']"));
			for (int j = 0; j < errorValue.size(); j++) {
				String MessageStatus = errorValue.get(j).getText().trim();
				if (MessageStatus.equals(msgSelect)) {
					errorValue.get(j).click();
					break;
				} else {
					System.out.println("In Else part of message status values are not correct");
				}
			}
		}
		Thread.sleep(1000);
	}

	public void selectNotificationStatus(String notifySelect) throws Exception {
		Log.info("Select the NotificationStatus");
		if ((notifySelect).equals("NO")) {
			System.out.println("We have not given mshValue");
		} else {
			driver.findElement(NotificationStatus).click();
			Thread.sleep(1000);
			ArrayList<WebElement> errorValue = (ArrayList<WebElement>) driver
					.findElements(By.xpath("//*[@class='mat-option']"));
			for (int j = 0; j < errorValue.size(); j++) {
				if (errorValue.get(j).getText().equals(notifySelect)) {
					errorValue.get(j).click();
					break;
				} else {
					System.out.println("In Else part values are not correct");
				}
			}
		}
		Thread.sleep(1000);
	}

	public void fromPartyID(String fPID) {
		Log.info("Enter From PartyID");
		if ((fPID).equals("NO")) {
			System.out.println("We have not given ErrDet");
		} else {
			driver.findElement(FromPartyID).sendKeys(fPID);
		}
	}

	public void toPartyID(String tPID) {
		Log.info("Enter To PartyID");
		if ((tPID).equals("NO")) {
			System.out.println("We have not given ErrDet");
		} else {
			driver.findElement(ToPartyID).sendKeys(tPID);
		}
	}

	public void refMessID(String RMID) {
		Log.info("Enter refMessID");
		if ((RMID).equals("NO")) {
			System.out.println("We have not given ErrDet");
		} else {
			driver.findElement(RefMessID).sendKeys(RMID);
		}
	}

	public void originalSender(String OS) {
		Log.info("Enter originalSender");
		if ((OS).equals("NO")) {
			System.out.println("We have not given ErrDet");
		} else {
			driver.findElement(OriginalSender).sendKeys(OS);
		}
	}

	public void finalRecepient(String FR) {
		Log.info("Enter final Recepient");
		if ((FR).equals("NO")) {
			System.out.println("We have not given ErrDet");
		} else {
			driver.findElement(FinalRecepient).sendKeys(FR);
		}
	}

	// From TIME STAMP
	public void selectFromTime(String FromTime) throws Exception {
		Log.info("Enter FromTimestamp");
		if ((FromTime).equals("NO")) {
			System.out.println("We have not given FromTime");
		} else {
			Thread.sleep(1000);
			// driver.findElement(FromTimeStamp).click();
			// timeGet(FromTime);
			driver.findElement(FromTimeStamptxt).sendKeys(FromTime);
		}
	}

	// To TIME STAMP
	public void selectToTime(String ToTime) throws Exception {
		Log.info("Enter To Timestamp");
		if ((ToTime).equals("NO")) {
			System.out.println("We have not given in ToTime");
		} else {
			// driver.findElement(ToTimeStamp).click();
			// timeGet(ToTime);
			driver.findElement(ToTimeStamptxt).sendKeys(ToTime);
		}
	}

	public void clickSearch() throws Exception {
		Log.info("Click the search button");
		driver.findElement(searchButton).click();
		Thread.sleep(5000);
	}

	public void buttonDownload(String dStat) throws Exception {
		Log.info("Click the download button for download the message");
		if ((dStat).equals("NO")) {
			System.out.println("We are not executing the download");
		} else {
			System.out.println("we are in download function");
			Thread.sleep(3000);
			driver.findElement(By.xpath(
					"//*[@id='messageLogTable']/div/datatable-body/datatable-selection/datatable-scroller/datatable-row-wrapper/datatable-body-row/div[2]/datatable-body-cell[1]"))
					.click();
			if (!driver.findElement(buttonDownload).isEnabled()) {
				System.out.println("Download button is not visible");
			} else {
				Thread.sleep(3000);
				driver.findElement(buttonDownload).click();
				Thread.sleep(5000);
				System.out.println("----------------------------------------------");
			}
		}
	}

	public boolean buttonResent(String Mid, String sStat) throws Exception {
		Log.info("Click the resent button for resent the message");
		boolean flag = false;
		if ((sStat).equals("NO")) {
			System.out.println("We are not executing the Resent");
		} else {
			System.out.println("we are in resent function");
			System.out.println("----------------------------------------------");
			driver.findElement(By.xpath(
					"//*[@id='messageLogTable']/div/datatable-body/datatable-selection/datatable-scroller/datatable-row-wrapper/datatable-body-row/div[2]/datatable-body-cell[1]"))
					.click();
			if (!driver.findElement(buttonResent).isEnabled()) {
				System.out.println("Resent button is not visible");
				flag = false;
			} else {
				driver.findElement(buttonResent).click();
				driver.findElement(messageDialogResendButton).click();
				Thread.sleep(3000);
				flag = true;
				String statusMess = driver.findElement(statusMessage).getText();
				System.out.println("Status message is" + statusMess);
			}
			System.out.println("----------------------------------------------");
		}
		return flag;
	}

	public boolean fileVerify(String Mid) throws Exception {
		Log.info("Verifying the file that downloaded");
		Path p = Paths.get("c:\\mydownloads\\" + Mid + ".zip");
		System.out.println("file exists  " + Files.exists(p));
		return Files.exists(p);
	}

	public void clickAdvanced() throws Exception {
		Log.info("Click the advance link to get more fields");
		Thread.sleep(1000);
		try {
			if (driver.findElement(advanced).isDisplayed())
				driver.findElement(advanced).click();
			else
				System.out.println("Advance button is not displayed");
			Thread.sleep(1000);
		} catch (Exception e) {
			Log.info("Exception in Advance button " + e);
		}
	}

	public void clickColumns() throws Exception {
		Log.info("Click the column link to select more columns");
		Thread.sleep(1000);
		driver.findElement(columnsoff).click();

	}

	public void selectColumns() throws Exception {
		Log.info("Select the required columns by click the check box");
		String col = "OriginalSender,FinalRecipient";
		int count = col.split(",").length;
		String column[] = col.split(",");

		ArrayList<WebElement> errorValue = (ArrayList<WebElement>) driver
				.findElements(By.xpath("//input[@type='checkbox']"));
		for (int j = 0; j < errorValue.size(); j++) {
			for (int i = 0; i < count; i++) {
				String s = errorValue.get(j).getAttribute("id").replaceAll("\\s", "");
				if (s.contains(column[i])) {
					if (!errorValue.get(j).isSelected()) {
						errorValue.get(j).click();
					} else
						System.out.println("ids in the checkbox  " + s);
				} else
					System.out.println("text in the check box is  " + s);
			}
		}
	}

	public void MessagesLogtable() throws Exception {
		Log.info("Reading the message log table to display the selected messages");
		System.out.println("we are in reading the MessageLogTable");
		Thread.sleep(1000);
		try {
		ArrayList<WebElement> datarow = (ArrayList<WebElement>) driver.findElements(By.xpath(MessagelogTable));
		System.out.println("----------------------------------------------");
		for (int i = 0; i < datarow.size(); i++) {
			System.out.println("values are " + datarow.get(i).getText());
		}
		System.out.println("----------------------------------------------");
		}catch(Exception e) {
			Log.info("Exception in MessageLogTable"+e);
		}
	}

	public void ReadMessageTable() throws Exception {
		Log.info("Reading the message log table to display the messages");
		List<String> mesId = new ArrayList<String>();
		System.out.println("we are in reading the MessageLogTable for MessageID");
		Thread.sleep(1000);
		ArrayList<WebElement> datarow = (ArrayList<WebElement>) driver.findElements(By.xpath(MessagelogTable));
		System.out.println("----------------------------------------------");
		for (int i = 0; i < datarow.size(); i++) {
			mesId.add(datarow.get(i).getText());
		}
		p.write(mesId);
		for (String elem : mesId) {
			System.out.println("values are [" + elem + "]");
		}
	}

	public void doubleClickMessage(String sStat) throws Exception {
		action = new Actions(driver);

		Log.info("DoubleClick event on the message");
		boolean flag = false;
		if ((sStat).equals("NO")) {
			System.out.println("We are not executing the doubleclick");
		} else {
			System.out.println("we are in doubleClick function");
			System.out.println("----------------------------------------------");
			WebElement x = driver.findElement(By.xpath(
					"//*[@id='messageLogTable']/div/datatable-body/datatable-selection/datatable-scroller/datatable-row-wrapper/datatable-body-row/div[2]/datatable-body-cell[1]"));
			action.doubleClick(x).perform();
		}
		System.out.println("----------------------------------------------");
	}

	// Date and Time selector function
	private void timeGet(String DandT) throws Exception {
		String DAT[] = DandT.split("/");
		List<WebElement> allDates = driver
				.findElements(By.cssSelector(".md2-calendar-body.ng-trigger.ng-trigger-slideCalendar"));
		List<WebElement> allHours = driver.findElements(By.cssSelector(".md2-clock-hours"));
		List<WebElement> allMin = driver.findElements(By.cssSelector(".md2-clock-minute"));
		for (WebElement dat : allDates) {
			String date = dat.getText();
			if (date.equalsIgnoreCase(DAT[1])) {
				dat.click();
				Thread.sleep(500);
				for (WebElement hou : allHours) {
					String hour = hou.getText();
					if (hour.equalsIgnoreCase("12")) {
						hou.click();
						Thread.sleep(500);
						for (WebElement min : allMin) {
							String minu = min.getText();
							if (minu.equalsIgnoreCase("00")) {
								min.click();
								Thread.sleep(500);
								break;
							}
						}
						break;
					}
				}
				break;
			}
		}
	}

	/**
	 * This method will perform the Resent of given messageID
	 * 
	 * @param i
	 * @param messageID
	 * @throws Exception
	 */
	public void messagesPageResentTest(int i, String messageID) throws Exception {
		Screenshot ss = new Screenshot(driver, extent, test);
		boolean Flag;
		int j = 0;
		System.out.println("Inside the Messages screen page");
		test.setDescription("Inside the MessagePage for Resent the message");
		Log.info("Inside the Messages screen page");
		try {
		clickMessagesTab();
		Thread.sleep(2000);
		clickAdvanced();
		Thread.sleep(1000);
		putMessageId(messageID);
		clickColumns();
		selectColumns();
		clickSearch();
		MessagesLogtable();
		Flag = buttonResent(messageID, messageID);
		Thread.sleep(3000);
		if (Flag) {
			ss.screenshot(i, "MessageLogScreenResent", true);
			Log.info("MessageLogScreen Resend Test completed");
		} else {
			ss.screenshot(i, "MessageLogScreenResent", false);
			Log.info("MessageLogScreen Resend Test completed");
		}
		}catch(Exception e) {
			Log.info("Error in the Messagepage for Resent messages"+e);
		}

	}

	/**
	 * This method will perform the download function for given messageID
	 * 
	 * @param i
	 * @param messageID
	 * @throws Exception
	 */
	public void messagesPageDownloadTest(int i, String messageID) throws Exception {
		Screenshot ss = new Screenshot(driver, extent, test);
		boolean Flag;
		int j = 0;
		System.out.println("Inside the Messages screen page");
		test.setDescription("Inside the MessagePage for Download the message");
		Log.info("Inside the Messages screen page for Download the message");
		try {
		clickMessagesTab();
		Thread.sleep(1000);
		// clickAdvanced();
		Thread.sleep(1000);
		putMessageId(messageID);
		// clickColumns();
		selectColumns();
		clickSearch();
		MessagesLogtable();
		buttonDownload(p.ReadW("MessageID1"));
		Thread.sleep(3000);
		Flag = fileVerify(p.ReadW("MessageID1"));
		if (Flag) {
			ss.screenshot(i, "MessageLogScreenDownload", true);
			Log.info("MessageLogScreen download Test completed");
		} else {
			ss.screenshot(i, "MessageLogScreenDownload", false);
			Log.info("MessageLogScreen download Test completed");
		}
		}catch(Exception e) {
			Log.info("Exception in the Messagespage for download "+e);
		}
		
	}

	/**
	 * This method will perform the doubleClick for given messageID
	 * 
	 * @param i
	 * @param messageID
	 * @param Status
	 * @throws Exception
	 */
	public void messagesDoubleClickTest(int i, String messageID, String Status) throws Exception {
		Screenshot ss = new Screenshot(driver, extent, test);
		boolean Flag;
		int j = 0;
		System.out.println("Inside the Messages screen page");
		test.setDescription("Inside the MessagePage for Download the message");
		Log.info("Inside the Messages screen page for Download the message");
		clickMessagesTab();
		Thread.sleep(1000);
		// clickAdvanced();
		Thread.sleep(1000);
		putMessageId(messageID);
		clickSearch();
		MessagesLogtable();
		doubleClickMessage(Status);
		Thread.sleep(3000);
		ss.screenshot(i, "DoubleClickedOnMID", true);
		Log.info("DoubleClickedOnMID Test completed");

	}

}
