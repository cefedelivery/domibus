package pageObjects;

import static org.testng.Assert.assertEquals;

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

public class MessagesFilterPage {
	/**
	 * Object declaration...
	 */
	WebDriver driver;
	Log log;
	ExtentReports extent;
	ExtentTest test;
	PropertyFileRead p = new PropertyFileRead();

	// Webelements declaration

	By MessageFilterTab = By.xpath("//*[@id='messagefilter_id']");
	By MessageFilterHead = By.xpath("//*[@id='messagefilterheader_id']");

	By errorMessage = By.xpath("//*[@id='alertmessage_id']");
	By succMessage = By.xpath("//*[@id='alertmessage_id']");

	By moveUp = By.id("moveupbutton_id");
	By moveDown = By.id("movedownbutton_id");
	By cancel = By.id("cancelbutton_id");
	By save = By.xpath("//*[@id='savebutton_id']");
	By sYes = By.xpath("//*[@id='yesbuttondialog_id']");
	By sNo = By.xpath("//*[@id='nobuttondialog_id']");

	By newB = By.id("newbutton_id");
	By edit = By.id("editbutton_id");
	By plugin = By.xpath("//*[@id='backendfilter_id']");
	By from = By.id("from_id");
	By to = By.id("to_id");
	By action = By.id("action_id");
	By service = By.id("service_id");
	By ecancel = By.xpath("//*[@class='cdk-overlay-pane']//*[@id='cancelbutton_id']");
	By eok = By.xpath("//*[@id='okbutton_id']");

	By delete = By.id("deletebutton_id");

	public MessagesFilterPage(WebDriver driver, ExtentReports extent, ExtentTest test) {
		this.driver = driver;
		this.extent = extent;
		this.test = test;
	}


	public void clickMessageFilterTab() throws Exception {

		driver.findElement(MessageFilterTab).click();
		Thread.sleep(1000);
		System.out.println("Inside the click MessageFilterTab function");

		String eMess = "Several filters in the table were not configured yet (Persisted flag is not checked). It is strongly recommended to double check the filters configuration and afterwards save it";

		try {
			if (!driver.findElement(errorMessage).getText().contains(eMess)) {

				System.out.println("we have selected the plugin");

			} else {
				driver.findElement(save).click();
				Thread.sleep(1000);
				driver.findElement(sYes).click();
				Thread.sleep(1000);
				System.out.println("we have already selected the plugin");
			}

		} catch (Exception e) {
			System.out.println("NO error in the page " + e);
		}

	}

	public String getMessageFilterHead() {

		String Header = driver.findElement(MessageFilterHead).getText();
		System.out.println("Inside the get message head function");
		return Header;
	}

	public void dataTableMSGFilter() {

	}

	public void moveUp() {
		driver.findElement(moveUp).click();
	}

	public void moveDown() {
		driver.findElement(moveDown).click();
	}

	public void cancel() {
		driver.findElement(cancel).click();
	}

	public void save() {
		driver.findElement(save).click();
	}

	public void newB() {
		driver.findElement(newB).click();
	}

	public void edit(String pulginEdit, String efrom, String eto, String eAction, String eService) throws Exception {
		try{

		Log.info("Inside the edit plugin function");
		if (pulginEdit.contains("backendWebservice")) {
			driver.findElement(By.xpath("//*[@id='messageFilterTable']//*[contains(text(),'backendWebservice')]"))
					.click();
		} else {
			driver.findElement(By.xpath("//*[@id='messageFilterTable']//*[contains(text(),'Jms')]")).click();

		}

		driver.findElement(edit).click();
		driver.findElement(from).sendKeys(efrom);
		driver.findElement(to).sendKeys(eto);
		driver.findElement(action).sendKeys(eAction);
		driver.findElement(service).sendKeys(eService);
		Thread.sleep(1000);
		driver.findElement(ecancel).click();
		System.out.println("Cancel button clicked");

		Thread.sleep(5000);
		}catch(Exception e){
			System.out.println("error in del"+e);
		}

	}

	public void delete(String del) throws Exception {
		try{
		Log.info("Inside the delete plugin function");

		if (del.contains("backendWebservice")) {
			driver.findElement(By.xpath("//*[@id='messageFilterTable']//*[contains(text(),'backendWebservice')]"))
					.click();
		} else {
			driver.findElement(By.xpath("//*[@id='messageFilterTable']//*[contains(text(),'Jms')]")).click();

		}
		Thread.sleep(1000);
		driver.findElement(delete).click();
		driver.findElement(save).click();
		driver.findElement(sYes).click();
		Thread.sleep(1000);
		}catch(Exception e){
			System.out.println("error in del"+e);
		}
	}

	/**
	 * This method will navigate to message filter page and perform the plugin
	 * edits
	 * 
	 */
	public void messagesFilterPageTest(int i) throws Exception {
		Screenshot ss = new Screenshot(driver, extent, test);
		System.out.println("Inside the MessagesFilter screen page");
		Log.info("Inside the MessagesFilter screen page");

		clickMessageFilterTab();
		Thread.sleep(2000);
		delete(p.Read("Delete"));
		Thread.sleep(2000);
		edit(p.Read("PluginEdit"), p.Read("eFrom"), p.Read("eTo"), p.Read("eAction"), p.Read("eService"));

		ss.screenshot(i, "MessageFilterScreen", true);

		Log.info("MessageFilter Test completed");

	}

}
