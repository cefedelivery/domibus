package com.DomibusGUI;

import org.testng.annotations.Test;
import org.testng.annotations.Test;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.xml.DOMConfigurator;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.interactions.Actions;
import org.testng.ITestResult;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.relevantcodes.extentreports.ExtentReports;
import com.relevantcodes.extentreports.ExtentTest;
import com.relevantcodes.extentreports.LogStatus;

import pageObjects.ErrorLogPage;
import pageObjects.JMSMonitoringPage;
import pageObjects.LogErrorPage;
import pageObjects.LoginPage;
import pageObjects.LogoutPage;
import pageObjects.MessagesFilterPage;
import pageObjects.MessagesPage;
import pageObjects.PModePage;
import pageObjects.TruststorePage;
import pageObjects.UserManagementPage;
import utility.BrowserSelect;
import utility.Log;
import utility.PropertyFileRead;
import utility.Screenshot;
import utility.TestDataRead;
import utility.configReport;

public class TestDomibus {

	ExtentReports extent;
	ExtentTest test;
	WebDriver driver;
	BrowserSelect bw = new BrowserSelect();
	Screenshot ss = new Screenshot(driver, extent, test);
	PropertyFileRead p = new PropertyFileRead();
	int i = 0, j = 1;
	Boolean LSucc = false;

	// HTML Report configuration

	@BeforeClass
	public void reportConfigure() throws Exception {
		
		extent = new ExtentReports(System.getProperty("user.dir") + "/test-output/DomTestRep.html", true);
		extent.addSystemInfo("HostName", "Domibus").addSystemInfo("Environment", "QA").addSystemInfo("User Name",
				"Ram");
		extent.loadConfig(new File(System.getProperty("user.dir") + "/extent.config.xml"));
				
	}

	// <<<<<<<<<<<<< Test case 001 start here>>>>>>>>>>>>>>>>>>>>>>

	@Test(priority = 1) // Starting of test
	public void tc_001() throws Throwable {
		DOMConfigurator.configure("log4j.xml");
		try {
			i = 01;
			j = 1;
			test = extent.startTest("Verifying the TestCase TC_00" + i);
			System.out.println("After test initilaize with testcase");
			driver = bw.startBrowser(p.Read("browser"), p.Read("url"));
			System.out.println("After browser select");

			/**
			 * object declaration for PageObject classes
			 */
			LoginPage lp = new LoginPage(driver, extent, test);
	
			Log.startTestCase("Testcase TC_" + i);
			Log.info("Login Screen Test starts");

			if (lp.userLoginPage(i, p.Read("Username"), p.Read("Password"))) {
				Log.info("Login success");
				Log.info("UserManagement page Test starts");

			} else {
				System.out.println("Login failed not executing and continue");
				Log.info("Login credentials are not correct");
				driver.quit();
			}
		} catch (Exception e) {
			System.out.println("Exception in the test case is " + e.getMessage());
			Log.info("Exception in the test case is " + e.getMessage());
			test.log(LogStatus.ERROR, e);
		}
		try {
			LogoutPage lo = new LogoutPage(driver);
			lo.logOutTest();
			Log.endTestCase("Testcase End");
		} catch (Exception e) {
			Log.info("Exception in logout " + e);
		}
		driver.quit();
	}

	
	
		
		
	@AfterMethod
	public void getResult(ITestResult result) throws IOException {
		if (result.getStatus() == ITestResult.FAILURE) {
			test.log(LogStatus.FAIL, result.getThrowable());
		}
		extent.endTest(test);
	}

	@AfterClass
	public void endTest() throws Exception {
		extent.flush();
	}

}
