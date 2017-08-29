package pageObjects;

import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

import com.relevantcodes.extentreports.ExtentReports;
import com.relevantcodes.extentreports.ExtentTest;

import utility.Log;
import utility.PropertyFileRead;
import utility.Screenshot;

public class ErrorLogPage {

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
	By ErrorLogTab = By.xpath("//*[@id='errorlog_id']");
	By ErrorLogHeader = By.xpath("//*[@id='errorlogheader_id']");
	By signalMessageID = By.name("errorSignalMessageId");
	By MessageID = By.name("messageInErrorId");
	By ErrorDetails = By.name("ErrorDetail");
	By ErrorCode = By.name("errorCode");
	By MSHRole = By.name("mshRole");
	By mshRoleSending = By.xpath("//*[@class='mat-option'][2]");
	By mshRoleReceiving = By.xpath("//*[@class='mat-option'][3]");
	By FromTimeStamp = By.xpath("//*[@name='fromTimestmap']//*[@class='md2-datepicker-button']");
	By ToTimeStamp = By.xpath("//*[@name='toTimestmap']//*[@class='md2-datepicker-button']");
	By NotifiedFrom = By.xpath("//*[@name='notifiedFrom']//*[@class='md2-datepicker-button']");
	By NotifiedTo = By.xpath("//*[@name='notifiedTo']//*[@class='md2-datepicker-button']");
	By FromTimeStamptxt = By.xpath("//*[@name='fromTimestmap']//*[@class='md2-datepicker-value']");
	By ToTimeStamptxt = By.xpath("//*[@name='toTimestmap']//*[@class='md2-datepicker-value']");
	By NotifiedFromtxt = By.xpath("//*[@name='notifiedFrom']//*[@class='md2-datepicker-value']");
	By NotifiedTotxt = By.xpath("//*[@name='notifiedTo']//*[@class='md2-datepicker-value']");
	By advanced = By.xpath("//*[@id='advancedlink_id']");
	By basic = By.xpath("//*[@id='basiclink_id']");
	By columnoff = By.xpath("//*[@id='columnslinkoff_id']");
	By columnon = By.xpath("//*[@id='columnslinkon_id']");
	String ErrorlogTable = "//*[contains(@class,'datatable-body') and contains(@class,'datatable-body-row')][1]//*[contains(@class,'text-select')]";
	By searchButton = By.cssSelector(".mat-primary.mat-raised-button");

	public ErrorLogPage(WebDriver driver, ExtentReports extent, ExtentTest test) {
		this.driver = driver;
		this.extent = extent;
		this.test = test;
	}

	public void clickErrorTab() throws Exception {
		Log.info("click on the errorlogTab");
		driver.findElement(ErrorLogTab).click();
		Thread.sleep(2000);
		driver.findElement(advanced).click();
		Thread.sleep(2000);
	}

	public void putSMId(String SMid) {
		Log.info("Send messageID to the SMID");
		if ((SMid).equals("NO")) {
			System.out.println("We have not given SMid");
		} else {
			driver.findElement(signalMessageID).sendKeys(SMid);
		}
	}

	public void putMId(String Mid) {
		Log.info("Send messageID to the MID");
		if ((Mid).equals("NO")) {
			System.out.println("We have not given Mid");
		} else {
			driver.findElement(MessageID).sendKeys(Mid);
		}
	}

	public void selectMSHRole(String mshValue) throws Exception {
		Log.info("Select MSHROLE");
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

	public void selectErrorCode(String errorCode) throws Exception {
		Log.info("Select Errorcode");
		driver.findElement(ErrorCode).click();
		Thread.sleep(1000);
		ArrayList<WebElement> errorValue = (ArrayList<WebElement>) driver
				.findElements(By.xpath("//*[@class='mat-option']"));
		for (int j = 0; j < errorValue.size(); j++) {
			if (errorValue.get(j).getText().equals(errorCode)) {
				errorValue.get(j).click();
				break;
			} else {
				System.out.println("In Else part values are not correct");
			}
		}
		Thread.sleep(1000);
	}

	// From TIME STAMP
	public void selectFromTime(String FromTime) throws Exception {
		Log.info("Send the FROM Time");
		if ((FromTime).equals("NO")) {
			System.out.println("We have not given FromTime");
		} else {
			// driver.findElement(FromTimeStamp).click();
			// timeGet(FromTime);
			driver.findElement(FromTimeStamptxt).sendKeys(FromTime);
		}
	}

	// To TIME STAMP
	public void selectToTime(String ToTime) throws Exception {
		Log.info("Send the To Time");
		if ((ToTime).equals("NO")) {
			System.out.println("We have not given in ToTime");
		} else {
			// driver.findElement(ToTimeStamp).click();
			// timeGet(ToTime);
			driver.findElement(ToTimeStamptxt).sendKeys(ToTime);
		}
	}

	// Notify From
	public void notifyFrom(String NotiFrom) throws Exception {
		Log.info("Send the Notify Time");
		if ((NotiFrom).equals("NO")) {
			System.out.println("We have not given NotiFrom");
		} else {
			// driver.findElement(NotifiedFrom).click();
			// timeGet(NotiFrom);
			driver.findElement(NotifiedFromtxt).sendKeys(NotiFrom);
		}
	}

	// Notify To
	public void notifyTo(String NotiTo) throws Exception {
		Log.info("Send the To Time");
		if ((NotiTo).equals("NO")) {
			System.out.println("We have not given NotiTo");
		} else {
			// driver.findElement(NotifiedTo).click();
			// timeGet(NotiTo);
			driver.findElement(NotifiedTotxt).sendKeys(NotiTo);
		}
	}

	public void putErrDet(String ErrDet) {
		Log.info("Send the ErrorDescription");
		if ((ErrDet).equals("NO")) {
			System.out.println("We have not given ErrDet");
		} else {
			driver.findElement(ErrorDetails).sendKeys(ErrDet);
		}
	}

	public void clickSearch() throws Exception {
		Log.info("Click on the search button");
		driver.findElement(searchButton).click();
		Thread.sleep(2000);
	}

	public void clickColumns() throws Exception {
		Log.info("Click on column button to select the specific columns");
		driver.findElement(columnoff).click();
		Thread.sleep(1000);
	}

	public void selectColumns() throws Exception {
		Log.info("Select the checkboxes for the required columns");
		String col = "SignalMessageId,APRole";
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
						System.out.println("ids in the checkbox           " + s);
				} else
					System.out.println("text in the check box is  " + s);
			}
		}
	}

	public void ErrorLogtable() throws Exception {
		Log.info("Displays the error log table after the search button");
		System.out.println("we are in reading the errorlog table");
		Thread.sleep(1000);
		ArrayList<WebElement> datarow = (ArrayList<WebElement>) driver.findElements(By.xpath(ErrorlogTable));
		System.out.println("----------------------------------------------");
		for (int i = 0; i < datarow.size(); i++) {
			System.out.println("values are " + datarow.get(i).getText());
		}
		System.out.println("----------------------------------------------");
	}

	// Date and Time selector function
	private void timeGet(String DandT) throws Exception {
		String DAT[] = DandT.split("/");
		List<WebElement> allDates = driver.findElements(By.cssSelector(".md2-calendar-day.curr-month"));
		List<WebElement> allHours = driver.findElements(By.cssSelector(".md2-clock-hour"));
		List<WebElement> allMin = driver.findElements(By.cssSelector(".md2-clock-minute"));

		for (WebElement dat : allDates) {

			String date = dat.getText();

			if (date.equalsIgnoreCase(DAT[1])) {
				dat.click();
				Thread.sleep(2000);
				for (WebElement hou : allHours) {
					String hour = hou.getText();

					if (hour.equalsIgnoreCase("12")) {
						hou.click();
						Thread.sleep(2000);
						for (WebElement min : allMin) {
							String minu = min.getText();

							if (minu.equalsIgnoreCase("00")) {
								min.click();
								Thread.sleep(2000);
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
	 * This method will navigate to error log page and display the provided
	 * error message
	 */
	public void errorLogPageTest(int i) throws Exception {
		Screenshot ss = new Screenshot(driver,extent,test);
		Log.info("Inside the ErrorLog screen page");
		System.out.println("Inside the ErrorLog screen page");
		clickErrorTab();
		putSMId(p.Read("SMID"));
		putMId(p.Read("MID"));
		putErrDet(p.Read("EDES"));
		selectMSHRole(p.Read("MSHValue"));
		selectErrorCode(p.Read("ErrorCode"));
		clickColumns();
		selectColumns();
		clickSearch();
		ErrorLogtable();
		ss.screenshot(i, "ErrorLogScreen", true);
		Log.info("ErrorLogPage Test completed");

	}

}
