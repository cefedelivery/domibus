package domibus.ui;

import ddsl.dcomponents.DomibusPage;
import ddsl.enums.DOMIBUS_PAGES;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import pages.TestServicePage;
import pages.login.LoginPage;

import java.util.HashMap;
import java.util.List;

public class TestServicePgTest extends BaseTest {

	protected void login(HashMap<String, String> user) throws Exception {
		LoginPage loginPage = new LoginPage(driver);
		loginPage.login(user);
		new DomibusPage(driver).getSidebar().getPageLnk(DOMIBUS_PAGES.TEST_SERVICE).click();
	}

	@Test(description = "TS-1", groups = {"multiTenancy", "singleTenancy"})
	public void openWindow() throws Exception{
		SoftAssert soft = new SoftAssert();
		login(data.getAdminUser());
		TestServicePage page = new TestServicePage(driver);

		soft.assertTrue(page.isLoaded(), "Page shows all desired elements");

		if(!rest.isPmodeUploaded(null)){
			soft.assertTrue(page.invalidConfigurationState(), "Page shows invalid configuration state");
		}

		rest.uploadPMode("pmode-invalid_process.xml", null);
		page.refreshPage();
		soft.assertTrue(page.invalidConfigurationState(), "Page shows invalid configuration state (2)");

		soft.assertAll();
	}

	@Test(description = "TS-2", groups = {"multiTenancy", "singleTenancy"})
	public void availableParties() throws Exception{
		SoftAssert soft = new SoftAssert();
		rest.uploadPMode("pmode-blue.xml", null);

		login(data.getAdminUser());
		TestServicePage page = new TestServicePage(driver);

		soft.assertTrue(page.isLoaded(), "Page shows all desired elements");

		List<String> options = page.getPartySelector().getOptionsTexts();

		soft.assertTrue(options.contains("domibus-blue") && options.contains("domibus-red"), "Party selector shows the correct parties");

		soft.assertAll();
	}

	@Test(description = "TS-3", groups = {"multiTenancy", "singleTenancy"})
	public void testBlueParty() throws Exception{
		SoftAssert soft = new SoftAssert();
		rest.uploadPMode("pmode-blue.xml", null);

		login(data.getAdminUser());
		TestServicePage page = new TestServicePage(driver);

		page.getPartySelector().selectOptionByText("domibus-blue");

		soft.assertTrue(page.getTestBtn().isEnabled(), "Test button is enabled after picking a party to test");

//		Bug already posted
//		soft.assertTrue(!page.getUpdateBtn().isEnabled(), "Update button is disabled until test button is clicked");

		page.getTestBtn().click();
		soft.assertTrue(page.getUpdateBtn().isEnabled(), "Update button is enabled after test button is clicked");

		page.wait.forXMillis(500);

		soft.assertTrue(page.getToParty().getText().equalsIgnoreCase("domibus-blue"), "Correct party is listed");
		soft.assertTrue(!page.getToAccessPoint().getText().isEmpty(), "To acces point contains data");
		soft.assertTrue(!page.getTimeSent().getText().isEmpty(), "Time sent contains data");
		soft.assertTrue(!page.getToMessage().getText().isEmpty(), "To Message id contains data");

		soft.assertAll();
	}



}
