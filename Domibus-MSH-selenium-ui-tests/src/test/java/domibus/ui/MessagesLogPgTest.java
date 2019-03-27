package domibus.ui;

import ddsl.enums.DOMIBUS_PAGES;
import ddsl.enums.DRoles;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import pages.messages.MessagesPage;
import utils.Generator;

/**
 * @author Catalin Comanici
 * @description:
 * @since 4.1
 */
public class MessagesLogPgTest extends BaseTest {

	@Test(description = "MSG-1", groups = {"multiTenancy", "singleTenancy"})
	public void openPage() throws Exception{
		SoftAssert soft = new SoftAssert();
		login(data.getAdminUser()).getSidebar().gGoToPage(DOMIBUS_PAGES.MESSAGES);
		MessagesPage page = new MessagesPage(driver);

		soft.assertTrue(page.isLoaded(), "Page elements are loaded");
		soft.assertTrue(page.getFilters().basicFiltersLoaded(), "Basic filters are present");
		soft.assertTrue(!page.getFilters().advancedFiltersLoaded(), "Advanced filters are NOT present");
		soft.assertTrue(!page.getDownloadButton().isEnabled(), "Download button is not enabled");
		soft.assertTrue(!page.getResendButton().isEnabled(), "Resend button is not enabled");

		soft.assertAll();
	}

	@Test(description = "MSG-1", groups = {"multiTenancy", "singleTenancy"})
	public void rowSelect() throws Exception{
		SoftAssert soft = new SoftAssert();

		String user = Generator.randomAlphaNumeric(10);
		rest.createPluginUser(user, DRoles.ADMIN, data.getDefaultTestPass(),null);
		String messID = messageSender.sendMessage(user, data.getDefaultTestPass()).getMessageID().get(0);

		login(data.getAdminUser()).getSidebar().gGoToPage(DOMIBUS_PAGES.MESSAGES);
		MessagesPage page = new MessagesPage(driver);

//		page.wait.forXMillis(5000);

		page.refreshPage();
		page.grid().scrollToAndSelect("Message Id", messID);

		soft.assertTrue(page.getDownloadButton().isEnabled(), "After a row is selected the Download button");

		soft.assertAll();
	}



}
