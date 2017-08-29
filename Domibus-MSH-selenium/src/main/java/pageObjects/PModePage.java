package pageObjects;

import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyEvent;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.relevantcodes.extentreports.ExtentReports;
import com.relevantcodes.extentreports.ExtentTest;

import utility.BrowserSelect;
import utility.Log;
import utility.PropertyFileRead;
import utility.Screenshot;

public class PModePage {

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
	String mess;

	By PmodeTab = By.xpath("//*[@id='pmode_id']");
	By PmodeHead = By.xpath("//*[@id='pmodeheader_id']");

	By Upload = By.xpath("//*[@id='uploadbutton_id']");
	By Download = By.xpath("//*[@id='downloadbutton_id']");

	By browse = By.id("pmode");
	By ok = By.id("okbuttonupload_id");
	By cancel = By.id("cancelbuttonupload_id");

	By statusMessage = By.xpath("//*[@id='alertmessage_id']");

	public PModePage(WebDriver driver, ExtentReports extent, ExtentTest test) {
		this.driver = driver;
		this.extent = extent;
		this.test = test;
	}

	public void clickPmodeTab() throws Exception {

		driver.findElement(PmodeTab).click();
		Thread.sleep(2000);

	}

	public String clickUpload(String pPath) throws Exception {

		if ((pPath).equals("NO")) {
			System.out.println("We have not given pPath");
		} else {

			driver.findElement(Upload).click();

			Thread.sleep(1000);

			driver.findElement(browse).click();
			
			Thread.sleep(3000);
			StringSelection ss = new StringSelection(pPath);
			Toolkit.getDefaultToolkit().getSystemClipboard().setContents(ss, null);
			Thread.sleep(1000);

			Robot robot = new Robot();

			robot.keyPress(KeyEvent.VK_CONTROL);
			robot.keyPress(KeyEvent.VK_V);
			robot.keyRelease(KeyEvent.VK_V);
			robot.keyRelease(KeyEvent.VK_CONTROL);
			Thread.sleep(3000);
			robot.keyPress(KeyEvent.VK_ENTER);
			robot.keyRelease(KeyEvent.VK_ENTER);

			driver.findElement(ok).click();
			Thread.sleep(7000);

			String statusMess = driver.findElement(statusMessage).getText();
			if (statusMess.contains("PMode file has been successfully uploaded")) {
				mess = "Pass";
				System.out.println("PMODE successfully uploaded");
			} else {
				System.out.println("NO uploaded PMODE");
				mess = "Fail";
			}
		}

		return mess;
	}

	public void clickDownload() throws Exception {

		String dStat = "NO";
		// driver.findElement(Download).click();

		if ((dStat).equals("NO")) {
			System.out.println("We are not executing the download");
		} else {
			System.out.println("we are in download function");

			Thread.sleep(3000);
			if (!driver.findElement(Download).isEnabled()) {
				System.out.println("Download button is not visible");
			} else {
				Thread.sleep(3000);
				driver.findElement(Download).click();

				Thread.sleep(5000);

				System.out.println("----------------------------------------------");
			}

		}

	}

	public void expliciteWait(WebElement element, int timeToWaitInSec) {
		WebDriverWait wait = new WebDriverWait(driver, timeToWaitInSec);
		wait.until(ExpectedConditions.visibilityOf(element));
	}

	/**
	 * This method will perform PMODE upload
	 * 
	 * @param i
	 * @param j
	 * @param driver
	 * @throws Exception
	 */
	public void pModePageTest(int i) throws Exception {
		String flag;
		Screenshot ss = new Screenshot(driver, extent, test);
		System.out.println("Inside the PMODE screen page");
		Log.info("Inside the PMODE screen page");

		clickPmodeTab();

		clickDownload();

		String path = System.getProperty("user.dir") + "\\TestFiles\\domibus-gw-sample-pmode-blue.xml";

		flag = clickUpload(path);

		if (flag.equals("Pass")) {
			ss.screenshot(i, "PModeScreen", true);
			Log.info("PModeScreen Test completed");
		} else {

			ss.screenshot(i, "PModeScreen", false);
			Log.info("PModeScreen Test completed but failed");

		}

	}

}
