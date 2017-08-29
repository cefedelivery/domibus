package pageObjects;

import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

import com.relevantcodes.extentreports.ExtentReports;
import com.relevantcodes.extentreports.ExtentTest;
import com.relevantcodes.extentreports.LogStatus;

import utility.Log;
import utility.PropertyFileRead;
import utility.Screenshot;

public class JMSMonitoringPage {

	/**
	 * Object declaration...
	 */
	WebDriver driver;
	Log log;
	ExtentReports extent;
	ExtentTest test;
	PropertyFileRead p = new PropertyFileRead();
	Actions action;
	String mess;

	// Webelements declaration
	By JMSMonitoringTab = By.xpath("//*[@id='jmsmonitoring_id']");
	By JMSHeader = By.xpath("//*[@class='domibusTextHeading']");

	By Source = By.xpath("//*[@id='jmsQueueSelector']");

	By FromDate = By.xpath("//*[@id='jmsFromDatePicker']");
	By FromDateTxt = By.xpath("//*[@id='jmsFromDatePicker']//*[@class='md2-datepicker-value']");
	By ToDate = By.xpath("//*[@id='jmsToDatePicker']");
	By ToDateTxt = By.xpath("//*[@id='jmsToDatePicker']//*[@class='md2-datepicker-value']");

	By Selector = By.xpath("//*[@id='jmsSelector']");
	By JMSType = By.xpath("//*[@id='jmsTypeInput']");

	By searchButton = By.xpath("//*[@id='jmsSearchButton']");

	By cancelButton = By.xpath("//*[@id='jmsCancelButton']");
	By saveButton = By.xpath("//*[@id='jmsSaveButton']");
	By moveButton = By.xpath("//*[@id='jmsMoveButton']");
	// By newQue = By.xpath("//*[@id='jmsQueueSelector']");
	By okButton = By.xpath("//*[@id='messageDialogResendButton']");
	By cancelBut = By.xpath("//*[@id='messageDialogCancelButton']");

	By deleteButton = By.xpath("//*[@id='jmsDeleteButton']");

	By statusMessage = By.xpath("//*[@id='alertmessage_id']");

	public JMSMonitoringPage(WebDriver driver, ExtentReports extent, ExtentTest test) {
		this.driver = driver;
		this.extent = extent;
		this.test = test;
	}
	
	public void clickJMSMonitoringTab() throws Exception {

		driver.findElement(JMSMonitoringTab).click();
		System.out.println("Inside the click JMSMonitoring function");

	}

	public String getMessageHead() {

		String Header = driver.findElement(JMSHeader).getText();
		System.out.println("Inside the get JMSMonitoring head function");
		return Header;
	}

	public void selectSource(String sSource) throws Exception {
		Actions action = new Actions(driver);
		if ((sSource).equals("NO")) {
			System.out.println("We have not given Source queue");
		} else {

			driver.findElement(Source).click();
			System.out.println("We have Source queue");
			ArrayList<WebElement> errorValue = (ArrayList<WebElement>) driver
					.findElements(By.xpath("//*[@class='mat-option']"));
			sSource = "[internal] domibus.notification.webservice";
			for (int j = 0; j < errorValue.size(); j++) {

				String MessageStatus = errorValue.get(j).getText().trim();
				if (MessageStatus.contains(sSource)) {

					Thread.sleep(4000);
					action.click(errorValue.get(j)).perform();
					// errorValue.get(j).click();
					break;
				} else
					System.out.println("In Else part of selectsource values");

			}
		}

	}

	public void fromDate() {

	}

	public void toDate() {

	}

	public void fromDatetxt() {

		driver.findElement(FromDateTxt).clear();

	}

	public void toDatetxt() {

		driver.findElement(ToDateTxt).clear();

	}

	public void putSelector() {

	}

	public void putJMSType() {

	}

	public void clickSearch() {

		driver.findElement(searchButton).click();

	}

	public void clickCancel() {

	}

	public void clickSave() {

	}

	// public void clickMove() throws Exception{
	//
	//// if((que).equals("NO")){
	//// System.out.println("We are not executing the download");
	//// }else{
	// System.out.println("we are in move queue function");
	//
	// Thread.sleep(3000);
	// driver.findElement(By.xpath("//*[@id='errorLogTable']//*[@class='datatable-body']//*[@class='datatable-row-wrapper'][1]")).click();
	// if(!driver.findElement(moveButton).isEnabled()){
	// System.out.println("Move button is not visible");
	// }
	// else
	// {
	// Thread.sleep(3000);
	// driver.findElement(moveButton).click();
	//
	// Thread.sleep(2000);
	//
	// newQue("[internal]");
	//
	//
	//
	//
	//
	// System.out.println("----------------------------------------------");
	// }
	// //}
	//
	// }
	//
	public String newQue(String nque) throws Exception {

		if ((nque).equals("NO")) {
			System.out.println("We are not executing the move to new queue");
			mess = "Pass";
		} else {
			System.out.println("we are in move queue function");

			driver.findElement(By.xpath("//*[@id='errorLogTable']//*[@class='datatable-body-cell sort-active'][1]"))
					.click();
			Thread.sleep(3000);
			if (!driver.findElement(moveButton).isEnabled()) {
				System.out.println("Move button is not visible");
			} else {
				Thread.sleep(3000);
				driver.findElement(moveButton).click();

				Thread.sleep(2000);

				if (driver.findElement(By.xpath("//*[@id='jmsqueuedestination_id']")).isEnabled()) {
					driver.findElement(By.xpath("//*[@id='jmsqueuedestination_id']")).click();

					ArrayList<WebElement> errorValue = (ArrayList<WebElement>) driver
							.findElements(By.xpath("//*[@class='mat-option']"));

					for (int j = 0; j < errorValue.size(); j++) {
						System.out.println("New Queue list is " + errorValue.get(j).getText());

						String MessageStatus = errorValue.get(j).getText().trim();
						if (MessageStatus.contains(nque)) {
							errorValue.get(j).click();

							Thread.sleep(1000);
							driver.findElement(okButton).click();
							Thread.sleep(2000);
							String statusMess = driver.findElement(statusMessage).getText();
							if (statusMess.contains("The operation 'move messages' completed successfully")) {
								mess = "Pass";
								System.out.println("Move to new queue successfull");
							} else {
								System.out.println("Move to New queue not successfull");
								mess = "Fail";
							}

							break;

						} else {
							System.out.println("In Else part new queue values are not correct");

						}

					}
				} else {
					Thread.sleep(1000);
					driver.findElement(okButton).click();
					Thread.sleep(2000);
					String statusMess = driver.findElement(statusMessage).getText();
					if (statusMess.contains("The operation 'move messages' completed successfully")) {
						mess = "Pass";
						System.out.println("Move to new queue successfull");
					} else {
						System.out.println("Move to New queue not successfull");
						mess = "Fail";
					}

				}
			}
		}

		return mess;

	}

	public void clickDelete() {

	}

	/**
	 * This method perform message moves from one queue to another queue
	 */
	public void JMSMonitoringPageTest(int i) throws Exception {
		Screenshot ss = new Screenshot(driver,extent,test);
		String flag;
		System.out.println("Inside the JMSMonitor screen page");
		Log.info("Inside the JMSMonitor screen page");
		clickJMSMonitoringTab();
		test.setDescription("Navigated to JMSMonitor Page");
		selectSource(p.Read("Source"));
		Thread.sleep(5000);
		// jm.clickSearch();
		test.log(LogStatus.PASS, "Navigation to JMS monitor page is successfull");
		ss.screenshot(i,"JMSMonitoringTab", true);
		// jm.fromDatetxt();
		// jm.toDatetxt();
		flag = newQue(p.Read("NewQueue"));
		System.out.println("Flag value is " + flag);
		Thread.sleep(3000);
		if (flag.equals("Pass")) {
			ss.screenshot(i, "JMSMonitoringPage", true);
			Log.info("JMSMonitoringPage Test completed");
		} else {
			ss.screenshot(i, "JMSMonitoringPage", false);
			Log.info("JMSMonitoringPage Test completed but failed");
		}
		Thread.sleep(1000);
	}
}
