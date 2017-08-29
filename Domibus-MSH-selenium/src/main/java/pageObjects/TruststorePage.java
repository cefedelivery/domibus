package pageObjects;

import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyEvent;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.interactions.Actions;

import com.relevantcodes.extentreports.ExtentReports;
import com.relevantcodes.extentreports.ExtentTest;

import utility.Log;
import utility.PropertyFileRead;
import utility.Screenshot;

public class TruststorePage {

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

	By TruststoreTab = By.xpath("//*[@id='truststore_id']");
	By TruststoreHead = By.xpath("//*[@id='truststoreheader_id']");

	By TUpload = By.xpath("//*[@id='uploadbutton_id']");
	By TBrowse = By.xpath("//*[@id='trustsore']");
	By TPassword = By.xpath("//*[@id='password_id']");
	By tOk = By.xpath("//*[@id='okbuttonupload_id']");
	By tCancel = By.xpath("//*[@id='cancelbuttonupload_id']");

	By statusMessage = By.xpath("//*[@id='alertmessage_id']");

	public TruststorePage(WebDriver driver, ExtentReports extent, ExtentTest test) {
		this.driver = driver;
		this.extent = extent;
		this.test = test;
	}

	public void clickTruststoreTab() {

		driver.findElement(TruststoreTab).click();

	}

	public void upload() throws Exception {

		driver.findElement(TUpload).click();
		Thread.sleep(1000);
	}

	public void clickTBrowse(String tPath) throws Exception {

		if ((tPath).equals("NO")) {
			System.out.println("We have not given tPath");
		} else {

			driver.findElement(TBrowse).click();
			Thread.sleep(3000);
			StringSelection ss = new StringSelection(tPath);
			Toolkit.getDefaultToolkit().getSystemClipboard().setContents(ss, null);
			Thread.sleep(3000);

			Robot robot = new Robot();

			robot.keyPress(KeyEvent.VK_CONTROL);
			robot.keyPress(KeyEvent.VK_V);
			robot.keyRelease(KeyEvent.VK_V);
			robot.keyRelease(KeyEvent.VK_CONTROL);
			Thread.sleep(3000);
			robot.keyPress(KeyEvent.VK_ENTER);
			robot.keyRelease(KeyEvent.VK_ENTER);
			Thread.sleep(3000);
		}
	}

	public void clickTPassword(String tPassword) {

		if ((tPassword).equals("NO")) {
			System.out.println("We have not given tPassword");
		} else {

			driver.findElement(TPassword).sendKeys(tPassword);
		}
	}

	public String clicktOk() throws Exception {
		Thread.sleep(1000);
		driver.findElement(tOk).click();
		Thread.sleep(3000);
		String statusMess = driver.findElement(statusMessage).getText();
		if (statusMess.contains("Truststore file has been successfully replaced")) {
			mess = "Pass";
			System.out.println("Truststore successfully uploaded");
		} else {
			System.out.println("uploaded Truststore failed");
			mess = "Fail";
		}

		return mess;

	}

	/**
	 * This method will perform the upload of truststore
	 * 
	 * @param i
	 * @param j
	 * @param driver
	 * @throws Exception
	 */
	public void trustStorePageTest(int i) throws Exception {
		String flag;
		Screenshot ss = new Screenshot(driver, extent, test);
		System.out.println("Inside the Truststore screen page");
		Log.info("Inside the Truststore screen page");

		clickTruststoreTab();
		Thread.sleep(1000);

		upload();
		String path = System.getProperty("user.dir") + "\\TestFiles\\gateway_truststore.jks";
		clickTBrowse(path);
		clickTPassword(p.Read("TrustPassword"));

		flag = clicktOk();

		if (flag.equals("Pass")) {

			ss.screenshot(i, "TruststoreScreen", true);
			Log.info("TruststoreScreen Test completed");

		} else {

			ss.screenshot(i, "TruststoreScreen", false);
			Log.info("TruststoreScreen Test completed but failed");

		}

	}

}
